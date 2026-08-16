package io.github.crazyvibes.backgroundaudit.internal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import io.github.crazyvibes.backgroundaudit.Remediation

internal fun Context.hasPermission(permission: String): Boolean =
    checkPermission(permission, android.os.Process.myPid(), android.os.Process.myUid()) ==
        PackageManager.PERMISSION_GRANTED

/**
 * Resolving before returning is the whole point: every settings screen this library
 * points at is optional, and starting an unresolvable intent crashes the host app.
 */
internal fun Intent.resolvesOn(context: Context): Boolean =
    context.packageManager.resolveActivity(this, PackageManager.MATCH_DEFAULT_ONLY) != null

internal fun appSettingsIntent(context: Context): Intent? =
    Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    ).takeIf { it.resolvesOn(context) }

internal fun appSettingsRemediation(userAction: String): Remediation =
    Remediation(userAction) { ctx -> appSettingsIntent(ctx) }

internal fun locationSettingsRemediation(): Remediation =
    Remediation("Turn on location services.") { ctx ->
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).takeIf { it.resolvesOn(ctx) }
    }
