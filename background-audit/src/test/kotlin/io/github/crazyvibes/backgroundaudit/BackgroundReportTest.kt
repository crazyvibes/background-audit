package io.github.crazyvibes.backgroundaudit

import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
class BackgroundReportTest {

    @Test
    fun `report severity is the worst finding`() {
        val report = report(
            finding(Finding.Id.DOZE_ACTIVE, Severity.INFO),
            finding(Finding.Id.BATTERY_OPTIMISED, Severity.RESTRICTED),
            finding(Finding.Id.POWER_SAVE_MODE_ACTIVE, Severity.DEGRADED),
        )

        assertEquals(Severity.RESTRICTED, report.severity)
    }

    @Test
    fun `empty report is healthy`() {
        val report = report()

        assertTrue(report.isHealthy)
        assertEquals(Severity.INFO, report.severity)
    }

    @Test
    fun `atLeast filters by severity`() {
        val report = report(
            finding(Finding.Id.DOZE_ACTIVE, Severity.INFO),
            finding(Finding.Id.STANDBY_BUCKET, Severity.BLOCKED),
        )

        assertEquals(1, report.atLeast(Severity.RESTRICTED).size)
        assertEquals(2, report.atLeast(Severity.INFO).size)
    }

    @Test
    fun `get returns finding by id or null`() {
        val report = report(finding(Finding.Id.DOZE_ACTIVE, Severity.INFO))

        assertNotNull(report[Finding.Id.DOZE_ACTIVE])
        assertNull(report[Finding.Id.DATA_SAVER_ACTIVE])
    }

    @Test
    @Config(sdk = [33])
    fun `xiaomi devices report vendor restrictions`() {
        ShadowBuild.setManufacturer("Xiaomi")

        val report = BackgroundAudit.inspect(ApplicationProvider.getApplicationContext())

        assertEquals(DeviceProfile.Vendor.XIAOMI, report.device.vendor)
        assertNotNull(report[Finding.Id.VENDOR_RESTRICTIONS_LIKELY])
    }

    @Test
    @Config(sdk = [33])
    fun `stock android reports no vendor restrictions`() {
        ShadowBuild.setManufacturer("Google")

        val report = BackgroundAudit.inspect(ApplicationProvider.getApplicationContext())

        assertEquals(DeviceProfile.Vendor.AOSP_OR_UNKNOWN, report.device.vendor)
        assertNull(report[Finding.Id.VENDOR_RESTRICTIONS_LIKELY])
    }

    @Test
    fun `inspect never throws on any supported api level`() {
        // The contract is that a failed read degrades to "not reported".
        val report = BackgroundAudit.inspect(ApplicationProvider.getApplicationContext())

        assertNotNull(report.toReportString())
    }

    private fun report(vararg findings: Finding) = BackgroundReport(
        device = DeviceProfile("Test", "Model", 33, DeviceProfile.Vendor.AOSP_OR_UNKNOWN),
        findings = findings.toList(),
    )

    private fun finding(id: Finding.Id, severity: Severity) =
        Finding(id, severity, "title", "detail")
}
