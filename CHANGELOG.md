# Changelog

All notable changes to this project are documented here.

The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this
project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed
- CI workflow updated off deprecated/Node-20 GitHub Actions (`checkout`, `setup-java`,
  `setup-gradle`, `upload-artifact`).
- Build failed outright without `android.useAndroidX=true`; added `gradle.properties`.
- Regenerated the stale public API dump so `apiCheck` passes.
- Pinned the Gradle daemon to JDK 17 (`gradle/gradle-daemon-jvm.properties`) — AGP 8.5.2
  and Robolectric 4.13 both break under newer JDKs picked up from `JAVA_HOME`.
- Added `jitpack.yml` pinning `openjdk17` for JitPack builds.

## [0.1.0] — 2026-08-16

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
- 

[Unreleased]: https://github.com/crazyvibes/background-audit/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/crazyvibes/background-audit/releases/tag/v0.1.0
