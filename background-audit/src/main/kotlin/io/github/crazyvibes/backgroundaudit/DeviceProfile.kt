package io.github.crazyvibes.backgroundaudit

import android.os.Build

/** Identifying details of the device the audit ran on. */
public class DeviceProfile internal constructor(

    /** [Build.MANUFACTURER], verbatim. */
    public val manufacturer: String,

    /** [Build.MODEL], verbatim. */
    public val model: String,

    /** [Build.VERSION.SDK_INT] of the running device. */
    public val sdkInt: Int,

    /** The vendor's background-restriction family, if recognised. */
    public val vendor: Vendor,
) {

    /**
     * Vendors known to add background restrictions on top of AOSP.
     *
     * Recognition is by [Build.MANUFACTURER] only. It tells you which vendor's rules
     * are likely to apply; it does not tell you whether this particular app is
     * currently restricted, because none of these vendors expose that as a public API.
     */
    public enum class Vendor {
        XIAOMI,
        OPPO,
        VIVO,
        HUAWEI,
        HONOR,
        SAMSUNG,
        ONEPLUS,
        REALME,
        MEIZU,
        ASUS,
        TECNO,

        /** Stock Android, or a vendor with no known extra restrictions. */
        AOSP_OR_UNKNOWN,
        ;

        /** True when this vendor is known to kill background work beyond AOSP rules. */
        public val addsRestrictions: Boolean
            get() = this != AOSP_OR_UNKNOWN && this != SAMSUNG
    }

    override fun toString(): String = "$manufacturer $model (API $sdkInt, $vendor)"
}
