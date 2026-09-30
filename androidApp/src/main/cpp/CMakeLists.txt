cmake_minimum_required(VERSION 3.22.1)
project(aynvora_native C CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Android debug APKs are used for on-device native runtime verification. Keep
# symbols for diagnostics, but optimize llama.cpp kernels so model prefill and
# token generation remain representative and complete in a useful time.
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    set(CMAKE_C_FLAGS_DEBUG "-O3 -g")
    set(CMAKE_CXX_FLAGS_DEBUG "-O3 -g")
endif()

set(LLAMA_SOURCE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../native/llama.cpp)

set(BUILD_SHARED_LIBS ON CACHE BOOL "Build shared libs" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "Do not build examples" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "Do not build tests" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "Do not build server" FORCE)

add_subdirectory(${LLAMA_SOURCE_DIR} llama_build)

target_sources(llama PRIVATE ${CMAKE_CURRENT_SOURCE_DIR}/aynvora_llama_jni.cpp)
target_include_directories(llama PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(llama PRIVATE log)
