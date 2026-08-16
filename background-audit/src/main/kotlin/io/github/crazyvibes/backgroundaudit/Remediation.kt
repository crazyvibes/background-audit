package io.github.crazyvibes.backgroundaudit

import android.content.Context
import android.content.Intent

/**
 * What the user has to do to clear a [Finding], and where to send them to do it.
 *
 * Vendor settings screens are not part of the Android SDK and are not guaranteed to
 * exist. [settingsIntent] resolves the target before returning it, so a non-null result
 * is safe to start; a null result means send the user to the app's own settings page
 * and explain the step in your own words.
 */
public class Remediation internal constructor(

    /** One sentence, addressed to the user, describing the change they need to make. */
    public val userAction: String,

    private val intentProvider: (Context) -> Intent?,
) {

    /**
     * An intent that opens the relevant settings screen, or null when no resolvable
     * screen exists on this device.
     *
     * Always null-check. Vendors rename and remove these activities between OS versions
     * without notice.
     */
    public fun settingsIntent(context: Context): Intent? = try {
        intentProvider(context)
    } catch (e: RuntimeException) {
        null
    }

    override fun toString(): String = userAction
}
