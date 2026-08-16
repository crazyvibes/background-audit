package io.github.crazyvibes.backgroundaudit

/**
 * One thing the audit discovered about this device's willingness to run your
 * background work.
 */
public class Finding internal constructor(

    /** Stable identifier. Safe to branch on; the enum is append-only. */
    public val id: Id,

    /** How badly this affects background work. */
    public val severity: Severity,

    /** Short human-readable title, in English. */
    public val title: String,

    /** One or two sentences of detail, including the observed value where relevant. */
    public val detail: String,

    /** What the user can do about it, when there is anything they can do. */
    public val remediation: Remediation? = null,
) {

    /**
     * Identifiers for everything the audit can report.
     *
     * New constants may be added in any minor release, so treat unknown values as
     * informational rather than exhaustively matching on this enum.
     */
    public enum class Id {
        DOZE_ACTIVE,
        BATTERY_OPTIMISED,
        STANDBY_BUCKET,
        BACKGROUND_RESTRICTED,
        DATA_SAVER_ACTIVE,
        LOCATION_SERVICES_OFF,
        LOCATION_PERMISSION_MISSING,
        LOCATION_PERMISSION_COARSE_ONLY,
        BACKGROUND_LOCATION_MISSING,
        NOTIFICATION_PERMISSION_MISSING,
        EXACT_ALARMS_DENIED,
        VENDOR_RESTRICTIONS_LIKELY,
        POWER_SAVE_MODE_ACTIVE,
    }

    override fun toString(): String = "[$severity] $title — $detail"
}
