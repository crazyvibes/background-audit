package io.github.crazyvibes.backgroundaudit.internal

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import io.github.crazyvibes.backgroundaudit.Finding
import io.github.crazyvibes.backgroundaudit.Severity

internal object SchedulingChecks {

    fun run(context: Context): List<Finding> = buildList {
        exactAlarmFinding(context)?.let(::add)
    }

    private fun exactAlarmFinding(context: Context): Finding? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return null
        if (am.canScheduleExactAlarms()) return null

        return Finding(
            id = Finding.Id.EXACT_ALARMS_DENIED,
            severity = Severity.DEGRADED,
            title = "Exact alarms are not permitted",
            detail = "setExact and setAlarmClock will be downgraded or throw. Inexact " +
                "alarms still fire, but with no timing guarantee.",
            remediation = appSettingsRemediation("Allow alarms and reminders for the app."),
        )
    }
}
