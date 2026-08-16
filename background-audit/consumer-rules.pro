# BackgroundAudit keeps no reflection-based entry points, so consumers need no rules.
# Enum names are used in Finding.Id.toString() output; keep them readable in reports.
-keepclassmembers enum io.github.crazyvibes.backgroundaudit.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
