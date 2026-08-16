package io.github.crazyvibes.backgroundaudit

/**
 * The result of one [BackgroundAudit.inspect] call.
 *
 * A report is a snapshot. Doze state, standby bucket and power-save mode all change
 * while the app runs, so re-run the audit rather than caching the result.
 */
public class BackgroundReport internal constructor(

    /** The device the audit ran on. */
    public val device: DeviceProfile,

    /** Everything the audit found, in descending order of severity. */
    public val findings: List<Finding>,
) {

    /** The worst severity present, or [Severity.INFO] when nothing was found. */
    public val severity: Severity
        get() = findings.maxOfOrNull { it.severity } ?: Severity.INFO

    /** True when nothing worse than [Severity.INFO] was found. */
    public val isHealthy: Boolean
        get() = severity == Severity.INFO

    /** Findings at or above [minimum], preserving report order. */
    public fun atLeast(minimum: Severity): List<Finding> =
        findings.filter { it.severity >= minimum }

    /** The finding with the given [id], or null when the audit did not report it. */
    public operator fun get(id: Finding.Id): Finding? = findings.firstOrNull { it.id == id }

    /**
     * A multi-line plain-text summary suitable for a bug report or a log line.
     *
     * The format is for humans and may change between releases. Do not parse it; read
     * [findings] instead.
     */
    public fun toReportString(): String = buildString {
        appendLine("BackgroundAudit — $device")
        appendLine("Overall: $severity")
        if (findings.isEmpty()) {
            appendLine("No restrictions detected.")
        } else {
            findings.forEach { appendLine("  $it") }
        }
    }

    override fun toString(): String = "BackgroundReport(severity=$severity, findings=${findings.size})"
}
