package io.github.crazyvibes.backgroundaudit.internal

import android.Manifest
import android.content.Context
import android.location.LocationManager
import android.os.Build
import io.github.crazyvibes.backgroundaudit.Finding
import io.github.crazyvibes.backgroundaudit.Severity

internal object LocationChecks {

    fun run(context: Context): List<Finding> = buildList {
        locationServicesFinding(context)?.let(::add)
        permissionFindings(context).forEach(::add)
        notificationPermissionFinding(context)?.let(::add)
    }

    private fun locationServicesFinding(context: Context): Finding? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val enabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lm.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        if (enabled) return null

        return Finding(
            id = Finding.Id.LOCATION_SERVICES_OFF,
            severity = Severity.BLOCKED,
            title = "Location services are off",
            detail = "No provider will return a fix regardless of permissions.",
            remediation = locationSettingsRemediation(),
        )
    }

    private fun permissionFindings(context: Context): List<Finding> {
        val fine = context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!fine && !coarse) {
            return listOf(
                Finding(
                    id = Finding.Id.LOCATION_PERMISSION_MISSING,
                    severity = Severity.BLOCKED,
                    title = "No location permission granted",
                    detail = "Neither fine nor coarse location is granted to the app.",
                    remediation = appSettingsRemediation("Grant location access to the app."),
                ),
            )
        }

        return buildList {
            if (!fine) {
                add(
                    Finding(
                        id = Finding.Id.LOCATION_PERMISSION_COARSE_ONLY,
                        severity = Severity.DEGRADED,
                        title = "Only approximate location is granted",
                        detail = "Fixes will be accurate to roughly a city block. Anything " +
                            "depending on precise position will misbehave rather than fail.",
                        remediation = appSettingsRemediation(
                            "Change location access from Approximate to Precise.",
                        ),
                    ),
                )
            }

            // Background location became a separate, separately-granted permission in
            // API 29, and from API 30 it can only be granted from the settings screen.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                !context.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            ) {
                add(
                    Finding(
                        id = Finding.Id.BACKGROUND_LOCATION_MISSING,
                        severity = Severity.RESTRICTED,
                        title = "Background location is not granted",
                        detail = "Location is only delivered while the app is visible or a " +
                            "location-typed foreground service is running. Continuous " +
                            "background collection will stop when the app is backgrounded.",
                        remediation = appSettingsRemediation(
                            "Set location access to \"Allow all the time\".",
                        ),
                    ),
                )
            }
        }
    }

    /**
     * Not a location check strictly, but a foreground service is the usual way to keep
     * collection alive, and from API 33 a denied notification permission makes its
     * notification invisible — which users report as the app "doing something secretly".
     */
    private fun notificationPermissionFinding(context: Context): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        if (context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) return null

        return Finding(
            id = Finding.Id.NOTIFICATION_PERMISSION_MISSING,
            severity = Severity.DEGRADED,
            title = "Notification permission is denied",
            detail = "Foreground services still run, but their notification is hidden, " +
                "which tends to be reported by users as unexplained battery use.",
            remediation = appSettingsRemediation("Allow notifications for the app."),
        )
    }
}
