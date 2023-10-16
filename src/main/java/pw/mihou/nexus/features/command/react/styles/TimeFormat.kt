package pw.mihou.nexus.features.command.react.styles

enum class TimeFormat(val suffix: String) {
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    LONG_DATE_WITH_SHORT_TIME("f"),
    LONG_DATE_WITH_DAY_OF_WEEK_AND_SHORT_TIME("F"),
    RELATIVE("R");
}