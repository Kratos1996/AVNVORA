# AYNVORA Event Catalog

Version: 1.0 (Phase 9.0)

All events use stable dot-notated identifiers: `<feature>.<action>.<interaction>`

## 1. Dashboard & Core Navigation Events

| Event ID                            | Description                                     | Source Component         | Typed Payload                                 |
|-------------------------------------|-------------------------------------------------|--------------------------|-----------------------------------------------|
| `dashboard.theme.toggle_clicked`    | User toggled between Dark/Light mode            | `theme_toggle`           | `ThemeTogglePayload`                          |
| `dashboard.language.open_clicked`   | User opened language selector bottom sheet      | `language_selector`      | `Empty`                                       |
| `dashboard.language.change_clicked` | User selected a supported locale                | `language_row`           | `LanguageChangePayload(localeId)`             |
| `dashboard.tarot.open_clicked`      | User tapped Tarot feature card                  | `feature_card_tarot`     | `FeatureOpenPayload(CoreFeatureId.TAROT)`     |
| `dashboard.palmistry.open_clicked`  | User tapped Palmistry feature card              | `feature_card_palmistry` | `FeatureOpenPayload(CoreFeatureId.PALMISTRY)` |
| `dashboard.feature.open_clicked`    | User tapped any core feature card               | `feature_card_*`         | `FeatureOpenPayload(featureId)`               |
| `dashboard.ai.download_clicked`     | User initiated On-Device AI package download    | `ai_setup_download`      | `AiDownloadPayload(modelId)`                  |
| `dashboard.ai.cancel_clicked`       | User cancelled On-Device AI download            | `ai_setup_cancel`        | `AiCancelPayload(modelId)`                    |
| `dashboard.ai.delete_clicked`       | User deleted On-Device AI model from local disk | `ai_setup_delete`        | `AiDeletePayload(modelId)`                    |

## 2. Tarot Events

| Event ID                        | Description                                 | Source Component       | Typed Payload                        |
|---------------------------------|---------------------------------------------|------------------------|--------------------------------------|
| `tarot.disclaimer.viewed`       | User acknowledged non-predictive disclaimer | `disclaimer_screen`    | `Empty`                              |
| `tarot.spread.selected`         | User selected a spread for reading          | `spread_card`          | `TarotSelectSpreadPayload(spreadId)` |
| `tarot.deck.selected`           | User chose an authentic Tarot deck          | `deck_selector`        | `TarotSelectDeckPayload(deckId)`     |
| `tarot.card.draw_clicked`       | User tapped to draw cards deterministically | `draw_button`          | `TarotDrawCardPayload(position)`     |
| `tarot.card.reveal_clicked`     | User revealed a card in the reading spread  | `card_flipper`         | `Empty`                              |
| `tarot.question.submit_clicked` | User submitted follow-up question           | `question_input`       | `TarotQuestionPayload(questionText)` |
| `tarot.clarification.requested` | User asked for a clarification card         | `clarification_btn`    | `TarotClarificationPayload`          |
| `tarot.clarification.accepted`  | User confirmed drawing clarification card   | `clarification_dialog` | `Empty`                              |
| `tarot.feedback.submitted`      | User submitted 1-5 star reading feedback    | `rating_bar`           | `TarotFeedbackPayload(starRating)`   |
| `tarot.history.opened`          | User opened historical readings log         | `history_nav`          | `Empty`                              |
| `tarot.close_clicked`           | User tapped to exit Tarot experience        | `close_btn`            | `Empty`                              |
| `tarot.back_clicked`            | User tapped back navigation within Tarot    | `back_btn`             | `Empty`                              |

## 3. Palmistry Events

| Event ID                       | Description                                   | Source Component     | Typed Payload                        |
|--------------------------------|-----------------------------------------------|----------------------|--------------------------------------|
| `palmistry.disclaimer.viewed`  | User acknowledged palmistry ethical framing   | `disclaimer_screen`  | `Empty`                              |
| `palmistry.hand.selected`      | User selected Left or Right hand              | `hand_toggle`        | `PalmSelectHandPayload(hand)`        |
| `palmistry.image.selected`     | User selected or captured palm image          | `camera_gallery_btn` | `PalmImageSourcePayload(sourceType)` |
| `palmistry.analysis.started`   | Local on-device palm feature extraction began | `analyze_btn`        | `PalmSelectHandPayload(hand)`        |
| `palmistry.analysis.completed` | Palm analysis completed successfully          | `engine`             | `PalmSelectHandPayload(hand)`        |
| `palmistry.question.submitted` | User submitted question on palm features      | `question_box`       | `PalmQuestionPayload(questionText)`  |
| `palmistry.feedback.submitted` | User submitted palmistry session feedback     | `rating_dialog`      | `PalmFeedbackPayload(starRating)`    |
| `palmistry.close_clicked`      | User exited Palmistry flow                    | `close_btn`          | `Empty`                              |

## 4. Report & Sharing Events

| Event ID                      | Description                               | Source Component | Typed Payload                         |
|-------------------------------|-------------------------------------------|------------------|---------------------------------------|
| `report.generate_clicked`     | User triggered structured report document | `report_btn`     | `ReportGeneratePayload(reportTypeId)` |
| `report.pdf.download_clicked` | User triggered local PDF rendering        | `pdf_export_btn` | `ReportPdfPayload(reportTypeId)`      |
| `report.share.clicked`        | User triggered system share dialog        | `share_btn`      | `ReportSharePayload(reportTypeId)`    |
