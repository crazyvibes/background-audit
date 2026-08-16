package io.github.crazyvibes.backgroundaudit.internal

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import io.github.crazyvibes.backgroundaudit.Finding
import io.github.crazyvibes.backgroundaudit.Remediation
import io.github.crazyvibes.backgroundaudit.Severity

internal object PowerChecks {

    fun run(context: Context): List<Finding> {
        val power = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            ?: return emptyList()

        return buildList {
            dozeFinding(power)?.let(::add)
            batteryOptimisationFinding(context, power)?.let(::add)
            powerSaveFinding(power)?.let(::add)
        }
    }

    /**
     * Doze is transient, so this is reported as INFO rather than a problem: seeing it
     * true simply tells you the deferral window is open right now.
     */
    private fun dozeFinding(power: PowerManager): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        if (!power.isDeviceIdleMode) return null

        return Finding(
            id = Finding.Id.DOZE_ACTIVE,
            severity = Severity.INFO,
            title = "Device is in Doze right now",
            detail = "Network access and non-exempt alarms are deferred until the next " +
                "maintenance window. This is normal and transient.",
        )
    }

    /**
     * The one that actually matters for long-running collection. Without the
     * exemption, Doze and App Standby apply in full.
     */
    private fun batteryOptimisationFinding(context: Context, power: PowerManager): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return null
        if (power.isIgnoringBatteryOptimizations(context.packageName)) return null

        return Finding(
            id = Finding.Id.BATTERY_OPTIMISED,
            severity = Severity.RESTRICTED,
            title = "App is subject to battery optimisation",
            detail = "The app is not exempt from Doze and App Standby, so background " +
                "network access, alarms and job execution will be deferred while the " +
                "screen is off.",
            remediation = Remediation(
                userAction = "Allow the app to run in the background without battery restrictions.",
                intentProvider = { ctx ->
                    Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        .takeIf { it.resolvesOn(ctx) }
                        ?: Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", ctx.packageName, null),
                        ).takeIf { it.resolvesOn(ctx) }
                },
            ),
        )
    }

    private fun powerSaveFinding(power: PowerManager): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return null
        if (!power.isPowerSaveMode) return null

        return Finding(
            id = Finding.Id.POWER_SAVE_MODE_ACTIVE,
            severity = Severity.DEGRADED,
            title = "Battery saver is on",
            detail = "Background work, location updates and network access are throttled " +
                "system-wide while battery saver is active.",
        )
    }
}
