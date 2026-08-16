package io.github.crazyvibes.backgroundaudit

import android.content.Context
import io.github.crazyvibes.backgroundaudit.internal.LocationChecks
import io.github.crazyvibes.backgroundaudit.internal.PowerChecks
import io.github.crazyvibes.backgroundaudit.internal.SchedulingChecks
import io.github.crazyvibes.backgroundaudit.internal.StandbyChecks
import io.github.crazyvibes.backgroundaudit.internal.VendorChecks
import io.github.crazyvibes.backgroundaudit.internal.readVendor

/**
 * Reports why background work is likely to be killed on this device.
 *
 * Android gives you a dozen separate switches that each independently stop background
 * work — Doze, App Standby buckets, per-app background restriction, battery
 * optimisation, Data Saver, power-save mode, runtime permissions, exact-alarm policy —
 * and then several vendors add undocumented ones on top. Reading them individually is
 * tedious and easy to get wrong across API levels. This reads all of them and hands
 * back one [BackgroundReport].
 *
 * ```kotlin
 * val report = BackgroundAudit.inspect(context)
 * if (!report.isHealthy) {
 *     Log.w("bg", report.toReportString())
 * }
 * ```
 *
 * Nothing here requests permissions, starts activities, or touches the network. Every
 * check is a read, and every check degrades to "not reported" rather than throwing when
 * the platform does not expose it at this API level.
 */
public object BackgroundAudit {

    /** The library's own version, matching the published artifact. */
    public const val VERSION: String = "0.1.0"

    /**
     * Inspects [context] and returns a snapshot of everything currently working
     * against your background execution.
     *
     * Safe to call from any thread. Typical cost is under a millisecond; it performs no
     * blocking I/O.
     *
     * @param context any context. The application context is retrieved internally, so
     *   passing an Activity does not leak it.
     */
    @JvmStatic
    public fun inspect(context: Context): BackgroundReport {
        val app = context.applicationContext
        val device = DeviceProfile(
            manufacturer = android.os.Build.MANUFACTURER.orEmpty(),
            model = android.os.Build.MODEL.orEmpty(),
            sdkInt = android.os.Build.VERSION.SDK_INT,
            vendor = readVendor(android.os.Build.MANUFACTURER.orEmpty()),
        )

        val findings = buildList {
            addAll(PowerChecks.run(app))
            addAll(StandbyChecks.run(app))
            addAll(LocationChecks.run(app))
            addAll(SchedulingChecks.run(app))
            addAll(VendorChecks.run(app, device))
        }.sortedByDescending { it.severity }

        return BackgroundReport(device, findings)
    }
}
