# BackgroundAudit

**Tells you why Android is about to kill your background work — on this device, right now.**

[![CI](https://github.com/crazyvibes/background-audit/actions/workflows/ci.yml/badge.svg)](https://github.com/crazyvibes/background-audit/actions/workflows/ci.yml)
[![JitPack](https://jitpack.io/v/crazyvibes/background-audit.svg)](https://jitpack.io/#crazyvibes/background-audit)
[![API 21+](https://img.shields.io/badge/API-21%2B-brightgreen)](https://developer.android.com)

Android has at least eight independent switches that each silently stop background work:
Doze, App Standby buckets, per-app background restriction, battery optimisation, Data
Saver, battery saver, runtime permissions, and exact-alarm policy. Several vendors add
undocumented ones on top. Each has a different API, arrived in a different API level,
and fails differently when absent.

The result is the most common bug report in mobile: *"it works on my Pixel and stops
after four hours on the customer's Redmi."*

BackgroundAudit reads all of them and hands you one report.

```kotlin
val report = BackgroundAudit.inspect(context)

if (!report.isHealthy) {
    Log.w("collection", report.toReportString())
}
```

```
BackgroundAudit — Xiaomi M2101K6G (API 33, XIAOMI)
Overall: BLOCKED
  [BLOCKED] Background execution is restricted for this app — The user has set this app
    to "Restricted" background usage. Jobs, alarms and services will not run while the
    app is in the background, and foreground services cannot be started from the background.
  [RESTRICTED] Xiaomi applies its own background restrictions — This device has a vendor
    autostart or background manager on top of AOSP. Its state cannot be read
    programmatically, so treat background work as unreliable here until the user
    allowlists the app.
  [RESTRICTED] Background location is not granted — Location is only delivered while the
    app is visible or a location-typed foreground service is running.
  [DEGRADED] Standby bucket: FREQUENT — Jobs are deferred by roughly eight hours.
  [INFO] Device is in Doze right now — Network access and non-exempt alarms are deferred
    until the next maintenance window.
```

Not a mockup — actual output from `BackgroundAudit.inspect()` on real hardware:

<table>
<tr>
<td align="center"><b>Samsung (One UI)</b></td>
<td align="center"><b>OPPO (ColorOS)</b></td>
</tr>
<tr>
<td><img src="docs/screenshots/samsung-audit.png" width="360" alt="BackgroundAudit output on a Samsung device"></td>
<td><img src="docs/screenshots/oppo-audit.png" width="360" alt="BackgroundAudit output on an OPPO device"></td>
</tr>
</table>

## Install

Add JitPack as a repository, then the dependency. In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

In your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.crazyvibes:background-audit:v0.1.0")
}
```

JitPack always uses `com.github.<owner>:<repo>:<tag>` as the coordinate, regardless of
the library's own Maven `group` — use the tag name (e.g. `v0.1.0`), not the bare version.

No transitive dependencies. No Play Services. `minSdk 21`.

## Why you'd use it

**In an SDK.** If you ship a library that collects in the background, half your support
load is integrators reporting that collection stopped, on devices you don't have. Attach
a report to your diagnostic payload and the answer arrives with the ticket.

**In an app.** Drive an onboarding prompt from real state rather than guessing. Ask the
user for the battery exemption only when they don't have it, and send them to the right
screen — including the vendor screens Android doesn't know about.

**In a crash or analytics pipeline.** Tag events with `report.severity` and the
"background collection is unreliable" cohort separates itself from your actual bugs.

## Reporting from user devices

`report.severity` and `report.toReportString()` are meant to leave the device. Two
different patterns, depending on what question you're answering:

**Enrich a crash report (reactive).** Firebase Crashlytics only surfaces custom keys and
log lines on the *next* crash or non-fatal from that session — it isn't a standing
telemetry pipe. Good for "was this crash caused by a background restriction," useless for
population-wide numbers:

```kotlin
val report = BackgroundAudit.inspect(context)
FirebaseCrashlytics.getInstance().apply {
    setCustomKey("bg_audit_severity", report.severity.name)
    log(report.toReportString())
}
```

**Track it across your install base (proactive).** Crashlytics can't answer "what
fraction of my users are BLOCKED, broken down by vendor" — for that you want Firebase
Analytics instead, logged as a regular event so it's queryable via the BigQuery export
regardless of whether anyone ever crashes:

```kotlin
firebaseAnalytics.logEvent("bg_audit") {
    param("severity", report.severity.name)
    param("vendor", report.device.vendor.name)
}
```

Use the first to explain individual crashes, the second to answer "why is retention worse
on Xiaomi."

## Acting on a finding

Every finding that a user can fix carries a `Remediation` with a resolved intent:

```kotlin
report.atLeast(Severity.RESTRICTED).forEach { finding ->
    val intent = finding.remediation?.settingsIntent(context)
    if (intent != null) {
        // finding.remediation.userAction is a one-line instruction for the user
        showPrompt(finding.title, finding.remediation.userAction) { startActivity(intent) }
    }
}
```

`settingsIntent` resolves the target before returning it, so a non-null result is safe to
start. A null result means no such screen exists on this device — send the user to the
app's own settings page and explain the step in your own words.

## What it can and cannot tell you

Honest limits, because a diagnostic tool that overstates its confidence is worse than none:

| | |
|---|---|
| **Can read** | Doze, battery-optimisation exemption, battery saver, App Standby bucket, per-app background restriction, Data Saver status, location services, location permission tier, background location, notification permission, exact-alarm policy |
| **Cannot read** | Whether a vendor autostart manager has allowlisted you. No vendor exposes this. The library reports *"this device has a restriction layer we cannot inspect"* and offers a route to the settings screen — it never claims you are restricted when it doesn't know |
| **Won't do** | Request permissions, start activities, touch the network, or hold a reference to your Activity |

A report is a snapshot. Doze state, standby bucket and power-save mode all change while
the app runs. Re-run the audit; don't cache the result.

## Compatibility

This library is meant to be embedded in other people's SDKs, so its API surface is treated
as a contract:

- **Semantic versioning**, strictly. No breaking change without a major bump.
- **Binary compatibility is enforced in CI.** The public API is dumped to
  [`background-audit/api/background-audit.api`](background-audit/api/background-audit.api)
  and `apiCheck` fails the build on any undeclared change, so an accidental signature
  change cannot ship.
- `explicitApi()` is on. Nothing reaches the public surface without someone deciding it should.
- `Finding.Id` is **append-only**. New constants may appear in a minor release, so treat
  unknown values as informational rather than matching exhaustively.
- `toReportString()` is for humans and its format may change. Read `findings` instead.
- Deprecations get one full minor cycle with a `ReplaceWith` before removal.

## Contributing

The most valuable contribution is a vendor component name that resolves on a device I
don't own. Open an issue with the manufacturer, model, Android version, and the output of:

```
adb shell dumpsys package | grep -i -A2 "autostart\|startupmgr\|chainlaunch"
```

## License

Apache 2.0
