# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this
project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Nothing yet.

## [0.1.0] — 2026-08-17

Initial release. Public API is dumped to `background-audit/api/background-audit.api` and
enforced by `apiCheck` from this version onward.

### Added
- `BackgroundAudit.inspect(Context)` returning a `BackgroundReport`.
- Power checks: Doze state, battery-optimisation exemption, battery saver.
- Standby checks: App Standby bucket, per-app background restriction, Data Saver status.
- Location checks: location services, fine/coarse permission tier, background location,
  notification permission.
- Scheduling checks: exact-alarm policy.
- Vendor detection for Xiaomi, Oppo, Vivo, Huawei, Honor, OnePlus, Realme, Meizu, Asus
  and Tecno families, with resolvable settings intents where one exists.
- `Remediation.settingsIntent(Context)`, which resolves the target before returning it.

### Notes
- `minSdk` is 21. Checks that require a higher API level are omitted rather than
  reported as false negatives.
- No transitive dependencies.

[Unreleased]: https://github.com/crazyvibes/background-audit/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/crazyvibes/background-audit/releases/tag/v0.1.0
