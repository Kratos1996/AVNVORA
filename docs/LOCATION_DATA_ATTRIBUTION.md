# Offline birth location catalog

The bundled country, region, and city catalog is derived from
[`dr5hn/countries-states-cities-database`](https://github.com/dr5hn/countries-states-cities-database).

- Source revision: `54ab470dae7d13d3af505c9e0217d5d1e3f1cce0`
- Source export date: 2026-09-28
- Source file: `json/countries+states+cities.json`
- Packaged representation: `design-system/src/commonMain/composeResources/files/locations.tsv`
- Fields retained: city id/name, state code/name, country code/name, latitude, longitude, timezone
- License: Open Database License (ODbL) 1.0; see [`docs/licenses/ODbL-1.0.txt`](licenses/ODbL-1.0.txt)

The catalog is packaged with the application and read locally. The birth location picker does not
call a public API. Source rows with unusable coordinate values are omitted by the compacting step;
rows with an empty timezone remain identifiable and are rejected for calculation by input validation.

This attribution covers the source database and does not claim additional licensing terms for
individual records.
