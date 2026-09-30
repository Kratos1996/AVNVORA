#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <atomic>
#include <cstring>
#include <memory>
#include <sstream>
#include <unistd.h>
#include <chrono>
#include <mutex>

#include "llama.h"

#define TAG "AynvoraLlamaJni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)

struct AynvoraNativeContext {
    llama_model* model = nullptr;
    llama_context* ctx = nullptr;
    std::atomic<bool> cancelled{false};
    std::string modelPath;
    int nCtx = 2048;
    int nThreads = 4;
    int generatedTokenCount = 0;
};

static std::atomic<bool> s_backend_initialized{false};
static std::mutex s_generation_mutex;

static bool llama_abort_requested(void* data) {
    auto* nativeCtx = static_cast<AynvoraNativeContext*>(data);
    return nativeCtx == nullptr || nativeCtx->cancelled.load();
}

static void ensure_backend_init() {
    bool expected = false;
    if (s_backend_initialized.compare_exchange_strong(expected, true)) {
        LOGI("Initializing llama_backend_init()");
        llama_backend_init();
    }
}

static inline void batch_add(
        struct llama_batch& batch,
        llama_token id,
        llama_pos pos,
        const std::vector<llama_seq_id>& seq_ids,
        bool logits) {
    batch.token[batch.n_tokens] = id;
    batch.pos[batch.n_tokens] = pos;
    batch.n_seq_id[batch.n_tokens] = seq_ids.size();
    for (size_t i = 0; i < seq_ids.size(); ++i) {
        batch.seq_id[batch.n_tokens][i] = seq_ids[i];
    }
    batch.logits[batch.n_tokens] = logits ? 1 : 0;
    batch.n_tokens++;
}

static inline void batch_clear(struct llama_batch& batch) {
    batch.n_tokens = 0;
}

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_aynvora_core_ai_RealLlamaJniBridge_nativeLoad(
        JNIEnv* env,
        jobject /* thiz */,
        jstring jModelPath,
        jint jContextLength,
        jint jThreads) {

    if (!jModelPath) {
        LOGE("nativeLoad: modelPath is null");
        return -1L;
    }

    ensure_backend_init();

    const char* pathStr = env->GetStringUTFChars(jModelPath, nullptr);
    if (!pathStr) {
        LOGE("nativeLoad: failed to get UTF chars");
        return -1L;
    }

    std::string modelPath(pathStr);
    env->ReleaseStringUTFChars(jModelPath, pathStr);

    if (access(modelPath.c_str(), F_OK) != 0) {
        std::string appInternal = "/data/data/com.aynvora.app/files/" + modelPath;
        if (access(appInternal.c_str(), F_OK) == 0) {
            modelPath = appInternal;
        } else {
            size_t slash = modelPath.find_last_of('/');
            std::string filename = (slash != std::string::npos) ? modelPath.substr(slash + 1) : modelPath;
            std::string appDirect = "/data/data/com.aynvora.app/files/models/ondevice/" + filename;
            if (access(appDirect.c_str(), F_OK) == 0) {
                modelPath = appDirect;
            } else {
                std::string tmpPath = "/data/local/tmp/" + filename;
                if (access(tmpPath.c_str(), F_OK) == 0) {
                    modelPath = tmpPath;
                }
            }
        }
    }

    LOGI("nativeLoad: resolved path '%s' (ctx=%d, threads=%d)",
         modelPath.c_str(), (int)jContextLength, (int)jThreads);

    llama_model_params mparams = llama_model_default_params();
    mparams.use_mmap = true;
    mparams.use_mlock = false;

    llama_model* model = llama_load_model_from_file(modelPath.c_str(), mparams);
    if (!model) {
        LOGE("nativeLoad: failed to load model from '%s'", modelPath.c_str());
        return -1L;
    }

    int nCtx = jContextLength > 0 ? (int)jContextLength : 2048;
    int nThreads = jThreads > 0 ? (int)jThreads : 4;

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx = nCtx;
    cparams.n_threads = nThreads;
    cparams.n_threads_batch = nThreads;

    llama_context* ctx = llama_new_context_with_model(model, cparams);
    if (!ctx) {
        LOGE("nativeLoad: failed to create llama_context");
        llama_free_model(model);
        return -1L;
    }

    auto* nativeCtx = new AynvoraNativeContext();
    nativeCtx->model = model;
    nativeCtx->ctx = ctx;
    nativeCtx->modelPath = modelPath;
    nativeCtx->nCtx = nCtx;
    nativeCtx->nThreads = nThreads;
    nativeCtx->cancelled.store(false);
    llama_set_abort_callback(ctx, llama_abort_requested, nativeCtx);

    LOGI("nativeLoad: successfully loaded model and created context. Handle: %p", nativeCtx);
    return reinterpret_cast<jlong>(nativeCtx);
}

JNIEXPORT jstring JNICALL
Java_com_aynvora_core_ai_RealLlamaJniBridge_nativeGenerate(
        JNIEnv* env,
        jobject /* thiz */,
        jlong jHandle,
        jstring jPrompt,
        jint jMaxTokens,
        jfloat jTemperature) {

    if (jHandle == 0 || jHandle == -1) {
        LOGE("nativeGenerate: invalid handle %lld", (long long)jHandle);
        return env->NewStringUTF("");
    }

    auto* nativeCtx = reinterpret_cast<AynvoraNativeContext*>(jHandle);
    if (!nativeCtx || !nativeCtx->model || !nativeCtx->ctx) {
        LOGE("nativeGenerate: context is null or corrupted");
        return env->NewStringUTF("");
    }

    std::unique_lock<std::mutex> generationLock(s_generation_mutex, std::try_to_lock);
    if (!generationLock.owns_lock()) {
        LOGW("nativeGenerate: rejected overlapping inference request");
        return env->NewStringUTF("\x01AYNVORA_BUSY");
    }

    nativeCtx->cancelled.store(false);
    const char* promptCStr = env->GetStringUTFChars(jPrompt, nullptr);
    if (!promptCStr) {
        return env->NewStringUTF("");
    }
    std::string prompt(promptCStr);
    env->ReleaseStringUTFChars(jPrompt, promptCStr);

    const auto inferenceStart = std::chrono::steady_clock::now();
    auto firstTokenTime = inferenceStart;
    bool hasFirstToken = false;
    nativeCtx->generatedTokenCount = 0;

    LOGI("nativeGenerate: prompt length = %zu, maxTokens = %d, temp = %.2f",
         prompt.length(), (int)jMaxTokens, (float)jTemperature);

    // 1. Tokenize prompt
    const int maxPromptTokens = nativeCtx->nCtx;
    std::vector<llama_token> promptTokens(maxPromptTokens);
    int nTokens = llama_tokenize(
            nativeCtx->model,
            prompt.c_str(),
            (int)prompt.length(),
            promptTokens.data(),
            maxPromptTokens,
            true,  // add_special
            true   // parse_special
    );

    if (nTokens < 0) {
        LOGE("nativeGenerate: tokenization buffer too small, required %d", -nTokens);
        promptTokens.resize(-nTokens);
        nTokens = llama_tokenize(
                nativeCtx->model,
                prompt.c_str(),
                (int)prompt.length(),
                promptTokens.data(),
                (int)promptTokens.size(),
                true,
                true
        );
    }

    if (nTokens <= 0) {
        LOGE("nativeGenerate: failed to tokenize prompt");
        return env->NewStringUTF("");
    }

    promptTokens.resize(nTokens);
    LOGI("nativeGenerate: tokenized into %d tokens", nTokens);

    // 2. Clear KV cache for fresh generation
    llama_kv_cache_clear(nativeCtx->ctx);

    // 3. Batch evaluation of prompt
    int batchSize = 64;
    llama_batch batch = llama_batch_init(batchSize, 0, 1);
    const auto promptDecodeStart = std::chrono::steady_clock::now();
    LOGI("nativeGenerate: starting prompt decode tokens=%d batch_size=%d", nTokens, batchSize);

    for (int i = 0; i < nTokens; i++) {
        if (nativeCtx->cancelled.load()) {
            LOGI("nativeGenerate: cancelled during prompt prefill at token=%d/%d", i, nTokens);
            llama_batch_free(batch);
            return env->NewStringUTF("");
        }
        batch_add(batch, promptTokens[i], i, {0}, false);
        if (batch.n_tokens == batchSize || i == nTokens - 1) {
            if (i == nTokens - 1) {
                batch.logits[batch.n_tokens - 1] = true;
            }
            if (llama_decode(nativeCtx->ctx, batch) != 0) {
                if (nativeCtx->cancelled.load()) {
                    LOGI("nativeGenerate: llama_decode aborted during prompt prefill");
                    llama_batch_free(batch);
                    return env->NewStringUTF("");
                }
                LOGE("nativeGenerate: llama_decode failed on prompt evaluation");
                llama_batch_free(batch);
                return env->NewStringUTF("");
            }
            batch_clear(batch);
        }
    }
    if (nativeCtx->cancelled.load()) {
        llama_batch_free(batch);
        return env->NewStringUTF("");
    }
    const auto promptDecodeEnd = std::chrono::steady_clock::now();
    LOGI("nativeGenerate: prompt decode completed ms=%lld",
         (long long) std::chrono::duration_cast<std::chrono::milliseconds>(promptDecodeEnd - promptDecodeStart).count());

    // 4. Token generation loop
    std::ostringstream responseStream;
    int maxGenerate = jMaxTokens > 0 ? (int)jMaxTokens : 256;
    int nVocab = llama_n_vocab(nativeCtx->model);
    int curPos = nTokens;

    std::vector<llama_token_data> candidates;
    candidates.reserve(nVocab);
    LOGI("nativeGenerate: entering token generation max_tokens=%d vocab=%d", maxGenerate, nVocab);

    for (int genCount = 0; genCount < maxGenerate; ++genCount) {
        if (nativeCtx->cancelled.load()) {
            LOGI("nativeGenerate: cancelled by user");
            break;
        }

        float* logits = llama_get_logits_ith(nativeCtx->ctx, -1);
        if (!logits) {
            LOGE("nativeGenerate: logits is null");
            break;
        }

        candidates.clear();
        for (llama_token tokenId = 0; tokenId < nVocab; ++tokenId) {
            candidates.emplace_back(llama_token_data{tokenId, logits[tokenId], 0.0f});
        }
        llama_token_data_array candidatesArr = {candidates.data(), candidates.size(), false};

        llama_token nextToken;
        if (jTemperature > 0.05f) {
            llama_sample_top_k(nativeCtx->ctx, &candidatesArr, 40, 1);
            llama_sample_top_p(nativeCtx->ctx, &candidatesArr, 0.95f, 1);
            llama_sample_temp(nativeCtx->ctx, &candidatesArr, jTemperature);
            nextToken = llama_sample_token(nativeCtx->ctx, &candidatesArr);
        } else {
            nextToken = llama_sample_token_greedy(nativeCtx->ctx, &candidatesArr);
        }

        if (llama_token_is_eog(nativeCtx->model, nextToken)) {
            LOGI("nativeGenerate: reached EOG token (%d)", nextToken);
            break;
        }

        if (genCount == 0) {
            LOGI("nativeGenerate: first output token sampled id=%d", nextToken);
        }

        if (!hasFirstToken) {
            firstTokenTime = std::chrono::steady_clock::now();
            hasFirstToken = true;
        }
        nativeCtx->generatedTokenCount++;

        char pieceBuf[256];
        int pieceLen = llama_token_to_piece(nativeCtx->model, nextToken, pieceBuf, sizeof(pieceBuf), 0, false);
        if (pieceLen > 0) {
            responseStream.write(pieceBuf, pieceLen);
        }

        // Prepare next single-token batch
        batch_clear(batch);
        batch_add(batch, nextToken, curPos++, {0}, true);

        if (llama_decode(nativeCtx->ctx, batch) != 0) {
            LOGE("nativeGenerate: failed to decode generated token %d", nextToken);
            break;
        }
        if (genCount == 0) {
            LOGI("nativeGenerate: first output token decoded");
        }
    }

    llama_batch_free(batch);

    std::string resultStr = responseStream.str();
    const auto inferenceEnd = std::chrono::steady_clock::now();
    const auto totalMs = std::chrono::duration_cast<std::chrono::milliseconds>(inferenceEnd - inferenceStart).count();
    const auto firstTokenMs = hasFirstToken
            ? std::chrono::duration_cast<std::chrono::milliseconds>(firstTokenTime - inferenceStart).count()
            : -1;
    const double tokensPerSecond = totalMs > 0
            ? (nativeCtx->generatedTokenCount * 1000.0 / totalMs)
            : 0.0;
    LOGI("nativeGenerate: completed chars=%zu tokens=%d first_token_ms=%lld total_ms=%lld tokens_per_sec=%.2f",
         resultStr.length(), nativeCtx->generatedTokenCount,
         (long long) firstTokenMs, (long long) totalMs, tokensPerSecond);
    return env->NewStringUTF(resultStr.c_str());
}

JNIEXPORT jint JNICALL
Java_com_aynvora_core_ai_RealLlamaJniBridge_nativeGeneratedTokenCount(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jlong jHandle) {
    if (jHandle == 0 || jHandle == -1) return 0;
    auto* nativeCtx = reinterpret_cast<AynvoraNativeContext*>(jHandle);
    return nativeCtx ? nativeCtx->generatedTokenCount : 0;
}

JNIEXPORT void JNICALL
Java_com_aynvora_core_ai_RealLlamaJniBridge_nativeCancel(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jlong jHandle) {
    if (jHandle != 0 && jHandle != -1) {
        auto* nativeCtx = reinterpret_cast<AynvoraNativeContext*>(jHandle);
        if (nativeCtx) {
            LOGI("nativeCancel: setting cancelled=true");
            nativeCtx->cancelled.store(true);
        }
    }
}

JNIEXPORT void JNICALL
Java_com_aynvora_core_ai_RealLlamaJniBridge_nativeRelease(
        JNIEnv* /* env */,
        jobject /* thiz */,
        jlong jHandle) {
    if (jHandle != 0 && jHandle != -1) {
        auto* nativeCtx = reinterpret_cast<AynvoraNativeContext*>(jHandle);
        if (nativeCtx) {
            LOGI("nativeRelease: releasing context %p", nativeCtx);
            if (nativeCtx->ctx) {
                llama_free(nativeCtx->ctx);
                nativeCtx->ctx = nullptr;
            }
            if (nativeCtx->model) {
                llama_free_model(nativeCtx->model);
                nativeCtx->model = nullptr;
            }
            delete nativeCtx;
        }
    }
}

} // extern "C"
