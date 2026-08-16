package io.github.crazyvibes.backgroundaudit.internal

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import io.github.crazyvibes.backgroundaudit.DeviceProfile
import io.github.crazyvibes.backgroundaudit.DeviceProfile.Vendor
import io.github.crazyvibes.backgroundaudit.Finding
import io.github.crazyvibes.backgroundaudit.Remediation
import io.github.crazyvibes.backgroundaudit.Severity

/**
 * Vendor autostart and background-kill managers.
 *
 * None of these are part of Android. No vendor exposes an API to read whether the app
 * is currently allowlisted, so the honest answer is "this device has a restriction
 * layer we cannot inspect" — reported once, with a route to the settings screen when
 * one resolves.
 *
 * Component names are collected from the community and verified only by resolving them
 * at runtime. They change without notice between OS versions.
 */
internal object VendorChecks {

    fun run(context: Context, device: DeviceProfile): List<Finding> {
        if (!device.vendor.addsRestrictions) return emptyList()

        return listOf(
            Finding(
                id = Finding.Id.VENDOR_RESTRICTIONS_LIKELY,
                severity = Severity.RESTRICTED,
                title = "${device.manufacturer} applies its own background restrictions",
                detail = "This device has a vendor autostart or background manager on top " +
                    "of AOSP. Its state cannot be read programmatically, so treat " +
                    "background work as unreliable here until the user allowlists the app.",
                remediation = Remediation(
                    userAction = vendorInstruction(device.vendor),
                    intentProvider = { ctx -> vendorIntent(device.vendor, ctx) },
                ),
            ),
        )
    }

    private fun vendorInstruction(vendor: Vendor): String = when (vendor) {
        Vendor.XIAOMI -> "In Security, enable Autostart for the app and set battery saver to No restrictions."
        Vendor.OPPO, Vendor.REALME -> "In Phone Manager, enable Auto-start and allow background running."
        Vendor.VIVO -> "In i Manager, enable background high power consumption and autostart."
        Vendor.HUAWEI, Vendor.HONOR -> "In Phone Manager, set the app to Manage manually and enable all three switches."
        Vendor.ONEPLUS -> "Disable Advanced optimisation and enable Auto-launch for the app."
        Vendor.MEIZU, Vendor.ASUS, Vendor.TECNO -> "Allow the app to auto-start and run in the background."
        else -> "Allow the app to run in the background."
    }

    private fun vendorIntent(vendor: Vendor, context: Context): Intent? {
        val candidates = when (vendor) {
            Vendor.XIAOMI -> listOf(
                "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
            )
            Vendor.OPPO, Vendor.REALME -> listOf(
                "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
                "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
                "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
            )
            Vendor.VIVO -> listOf(
                "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
                "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
            )
            Vendor.HUAWEI, Vendor.HONOR -> listOf(
                "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
                "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
            )
            Vendor.ONEPLUS -> listOf(
                "com.oneplus.security" to "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity",
            )
            Vendor.ASUS -> listOf(
                "com.asus.mobilemanager" to "com.asus.mobilemanager.autostart.AutoStartActivity",
            )
            Vendor.MEIZU -> listOf(
                "com.meizu.safe" to "com.meizu.safe.security.SHOW_APPSEC",
            )
            else -> emptyList()
        }

        candidates.forEach { (pkg, cls) ->
            val intent = Intent().setComponent(ComponentName(pkg, cls))
            if (intent.resolvesOn(context)) return intent
        }
        // Nothing vendor-specific resolved; fall back to the app's own settings page.
        return appSettingsIntent(context)
    }
}

internal fun readVendor(manufacturer: String): Vendor = when (manufacturer.lowercase()) {
    "xiaomi", "redmi", "poco" -> Vendor.XIAOMI
    "oppo" -> Vendor.OPPO
    "realme" -> Vendor.REALME
    "vivo", "iqoo" -> Vendor.VIVO
    "huawei" -> Vendor.HUAWEI
    "honor" -> Vendor.HONOR
    "samsung" -> Vendor.SAMSUNG
    "oneplus" -> Vendor.ONEPLUS
    "meizu" -> Vendor.MEIZU
    "asus" -> Vendor.ASUS
    "tecno", "infinix", "itel" -> Vendor.TECNO
    else -> Vendor.AOSP_OR_UNKNOWN
}
