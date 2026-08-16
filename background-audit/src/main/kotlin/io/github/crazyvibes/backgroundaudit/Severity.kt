package io.github.crazyvibes.backgroundaudit

/**
 * How badly a [Finding] threatens background work.
 *
 * Ordered from least to most severe, so `maxOrNull()` over a set of findings gives the
 * overall state of the device.
 */
public enum class Severity {

    /** Informational. Nothing is wrong; the value is reported for context. */
    INFO,

    /** Background work will run, but less often or less reliably than requested. */
    DEGRADED,

    /** Background work is being actively suppressed and will miss updates. */
    RESTRICTED,

    /** Background work cannot run at all until the user changes something. */
    BLOCKED,
}
