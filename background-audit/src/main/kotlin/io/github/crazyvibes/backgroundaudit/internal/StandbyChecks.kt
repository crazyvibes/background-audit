package io.github.crazyvibes.backgroundaudit.internal

import android.app.ActivityManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import io.github.crazyvibes.backgroundaudit.Finding
import io.github.crazyvibes.backgroundaudit.Severity

internal object StandbyChecks {

    fun run(context: Context): List<Finding> = buildList {
        standbyBucketFinding(context)?.let(::add)
        backgroundRestrictedFinding(context)?.let(::add)
        dataSaverFinding(context)?.let(::add)
    }

    /**
     * Bucket determines how often deferred work is allowed to run. RESTRICTED is the
     * one that silently destroys a collection pipeline: roughly one job per day.
     */
    private fun standbyBucketFinding(context: Context): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        val usage = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null

        val bucket = usage.appStandbyBucket
        val (severity, name, consequence) = when {
            bucket <= BUCKET_ACTIVE -> return Finding(
                id = Finding.Id.STANDBY_BUCKET,
                severity = Severity.INFO,
                title = "Standby bucket: ACTIVE",
                detail = "No deferral is being applied from App Standby.",
            )
            bucket <= BUCKET_WORKING_SET -> Triple(
                Severity.INFO, "WORKING_SET", "Jobs are deferred by roughly two hours.",
            )
            bucket <= BUCKET_FREQUENT -> Triple(
                Severity.DEGRADED, "FREQUENT", "Jobs are deferred by roughly eight hours.",
            )
            bucket <= BUCKET_RARE -> Triple(
                Severity.RESTRICTED, "RARE", "Jobs are deferred by roughly twenty-four hours.",
            )
            else -> Triple(
                Severity.BLOCKED,
                "RESTRICTED",
                "The app is in the most aggressive bucket: roughly one job per day, and " +
                    "network access only during that window.",
            )
        }

        return Finding(
            id = Finding.Id.STANDBY_BUCKET,
            severity = severity,
            title = "Standby bucket: $name",
            detail = "$consequence Buckets are assigned from usage patterns and change " +
                "over time; open the app regularly to move it up.",
        )
    }

    private fun backgroundRestrictedFinding(context: Context): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            ?: return null
        if (!am.isBackgroundRestricted) return null

        return Finding(
            id = Finding.Id.BACKGROUND_RESTRICTED,
            severity = Severity.BLOCKED,
            title = "Background execution is restricted for this app",
            detail = "The user has set this app to \"Restricted\" background usage. " +
                "Jobs, alarms and services will not run while the app is in the " +
                "background, and foreground services cannot be started from the background.",
            remediation = appSettingsRemediation(
                "Set the app's background battery usage to Unrestricted.",
            ),
        )
    }

    private fun dataSaverFinding(context: Context): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return null
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return null

        return when (cm.restrictBackgroundStatus) {
            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED -> Finding(
                id = Finding.Id.DATA_SAVER_ACTIVE,
                severity = Severity.RESTRICTED,
                title = "Data Saver is blocking background network access",
                detail = "The app has no background network access on metered connections. " +
                    "Uploads will queue until the device is on unmetered Wi-Fi or the app " +
                    "is allowlisted.",
                remediation = appSettingsRemediation(
                    "Allow unrestricted data usage for this app.",
                ),
            )

            ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED -> Finding(
                id = Finding.Id.DATA_SAVER_ACTIVE,
                severity = Severity.INFO,
                title = "Data Saver is on, app is allowlisted",
                detail = "Background network access is permitted for this app.",
            )

            else -> null
        }
    }

    // UsageStatsManager bucket constants are only defined from API 28, and RESTRICTED
    // only from API 30. Inlining them keeps the comparisons readable and avoids a
    // NoSuchFieldError on older devices.
    private const val BUCKET_ACTIVE = 10
    private const val BUCKET_WORKING_SET = 20
    private const val BUCKET_FREQUENT = 30
    private const val BUCKET_RARE = 40
}
