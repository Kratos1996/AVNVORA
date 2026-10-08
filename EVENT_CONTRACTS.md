# AYNVORA Canonical Event Contracts

## Canonical Event Structure

All communication across SDK boundaries, Core routing, and Feature Engines uses `AynvoraEvent` and `AynvoraEventResponse`.

### AynvoraEvent
```json
{
  "eventId": "evt_1774000000000_astrology",
  "featureId": "ASTROLOGY",
  "eventType": "CALCULATE_CHART",
  "version": "1.0.0",
  "schemaVersion": "1.0",
  "timestampEpochMs": 1774000000000,
  "requestId": "req_8472",
  "payloadJson": "{\"name\":\"Aditi\",\"dateOfBirth\":\"1992-04-12\",\"timeOfBirth\":\"10:15:00\",\"location\":{\"latitude\":28.6139,\"longitude\":77.209,\"timezone\":5.5}}",
  "metadata": {
    "requestId": "req_8472",
    "callerId": "mobile_app",
    "platform": "android"
  }
}
```

### AynvoraEventResponse (Success)
```json
{
  "eventId": "evt_1774000000000_astrology",
  "requestId": "req_8472",
  "featureId": "ASTROLOGY",
  "status": "SUCCESS",
  "resultJson": "{\"julianDay\":2448724.6979166665,\"lagnaLongitude\":68.42,\"lagnaSign\":\"GEMINI\",\"planets\":[{\"name\":\"SUN\",\"longitude\":358.5,\"sign\":\"PISCES\",\"nakshatra\":\"REVATI\",\"pada\":4}]}",
  "error": null,
  "metadata": {
    "requestId": "req_8472",
    "timestampEpochMs": 1774000000050,
    "schemaVersion": "1.0",
    "durationMs": 48,
    "provenance": {
      "source": "astro-engine",
      "engineId": "com.aynvora.astro",
      "engineVersion": "1.0.0",
      "calculationVersion": "1.0.0",
      "generatedAtEpochMs": 1774000000049,
      "isDeterministic": true
    }
  }
}
```

### AynvoraEventResponse (Error / Gate Blocked)
```json
{
  "eventId": "evt_1774000000000_palmistry",
  "requestId": "req_9912",
  "featureId": "PALMISTRY",
  "status": "FEATURE_DISABLED",
  "resultJson": "{}",
  "error": {
    "code": "FEATURE_DISABLED",
    "messageKey": "error.access.feature_disabled",
    "details": "Feature 'PALMISTRY' is explicitly disabled in settings."
  },
  "metadata": {
    "requestId": "req_9912",
    "timestampEpochMs": 1774000000002,
    "durationMs": 1
  }
}
```
