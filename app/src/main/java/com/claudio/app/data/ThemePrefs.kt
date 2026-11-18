package com.claudio.app.data

/** Accent used across buttons, highlights, the mascot and active states. */
enum class AccentTone(val key: String, val label: String) {
    Ember("ember", "Ember"),
    Amber("amber", "Amber"),
    Sage("sage", "Sage"),
    Ocean("ocean", "Ocean"),
    Iris("iris", "Iris"),
    Rose("rose", "Rose"),
    Mono("mono", "Mono");

    companion object {
        fun from(value: String?): AccentTone = entries.firstOrNull { it.key == value } ?: Ember
    }
}

/** Warm-black background family. */
enum class SurfaceTone(val key: String, val label: String) {
    Espresso("espresso", "Espresso"),
    Graphite("graphite", "Graphite"),
    Midnight("midnight", "Midnight"),
    Amoled("amoled", "AMOLED");

    companion object {
        fun from(value: String?): SurfaceTone = entries.firstOrNull { it.key == value } ?: Espresso
    }
}

/** How the user's own messages are drawn. */
enum class BubbleStyle(val key: String, val label: String) {
    Soft("soft", "Soft"),
    Tint("tint", "Tint"),
    Outline("outline", "Outline");

    companion object {
        fun from(value: String?): BubbleStyle = entries.firstOrNull { it.key == value } ?: Soft
    }
}

data class ThemePrefs(
    val accent: AccentTone = AccentTone.Ember,
    val tone: SurfaceTone = SurfaceTone.Espresso,
    val bubble: BubbleStyle = BubbleStyle.Soft,
    val fontScale: Float = 1f,
    val reduceMotion: Boolean = false,
    val name: String = "saint"
)
