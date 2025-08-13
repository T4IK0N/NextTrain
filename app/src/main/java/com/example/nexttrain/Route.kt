package com.example.nexttrain

data class Route(
    val start: String,
    val end: String,
    val timestamp: String,
    val date: String,
    val time: String,
    val direct: Boolean
) {
    override fun toString(): String {
        return "start: $start\n" +
                "end: $end\n" +
                "timestamp: $timestamp\n" +
                "date: $date\n" +
                "time: $time\n" +
                "direct: $direct"
    }

    fun toStringBeautiful(): String {
        return "\n$start -> $end\n$timestamp\n${
            if (direct) {
                "Only direct"
            } else {
                "Direct and indirect"
            }
        }"
    }
}