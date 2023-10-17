package pw.mihou.nexus.features.react.styles

enum class TimeFormat(val suffix: String) {
    /**
     * Shows the time without the seconds (e.g. 10:07 PM)
     */
    SHORT_TIME("t"),

    /**
     * Shows the time with the seconds (e.g. 10:07:00 PM)
     */
    LONG_TIME("T"),

    /**
     * Shows the date with the month as a number and the year shortened (e.g. 10/17/23)
     */
    SHORT_DATE("d"),

    /**
     * Shows the date with the full month name and the full year (e.g. October 17, 2023)
     */
    LONG_DATE("D"),

    /**
     * Shows the date with a short time (e.g. October 17, 2023 at 10:07 PM).
     */
    LONG_DATE_WITH_SHORT_TIME("f"),

    /**
     * Shows the date with the short time and the day of the week (e.g. Tuesday, October 17, 2023 at 10:07 PM)
     */
    LONG_DATE_WITH_DAY_OF_WEEK_AND_SHORT_TIME("F"),

    /**
     * Shows a relative time (e.g. 30 seconds ago).
     */
    RELATIVE("R");
}