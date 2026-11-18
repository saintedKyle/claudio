package com.claudio.app.ui.components

private val SUPER = mapOf(
    '0' to "⁰", '1' to "¹", '2' to "²", '3' to "³", '4' to "⁴", '5' to "⁵", '6' to "⁶", '7' to "⁷", '8' to "⁸", '9' to "⁹",
    '+' to "⁺", '-' to "⁻", '=' to "⁼", '(' to "⁽", ')' to "⁾", 'n' to "ⁿ", 'i' to "ⁱ", 'x' to "ˣ", 'y' to "ʸ",
    'a' to "ᵃ", 'b' to "ᵇ", 'c' to "ᶜ", 'd' to "ᵈ", 'e' to "ᵉ", 'f' to "ᶠ", 'g' to "ᵍ", 'h' to "ʰ", 'j' to "ʲ",
    'k' to "ᵏ", 'l' to "ˡ", 'm' to "ᵐ", 'o' to "ᵒ", 'p' to "ᵖ", 'r' to "ʳ", 's' to "ˢ", 't' to "ᵗ", 'u' to "ᵘ",
    'v' to "ᵛ", 'w' to "ʷ", 'z' to "ᶻ", 'T' to "ᵀ", '°' to "°", '′' to "′"
)
private val SUB = mapOf(
    '0' to "₀", '1' to "₁", '2' to "₂", '3' to "₃", '4' to "₄", '5' to "₅", '6' to "₆", '7' to "₇", '8' to "₈", '9' to "₉",
    '+' to "₊", '-' to "₋", '=' to "₌", '(' to "₍", ')' to "₎", 'a' to "ₐ", 'e' to "ₑ", 'h' to "ₕ", 'i' to "ᵢ",
    'j' to "ⱼ", 'k' to "ₖ", 'l' to "ₗ", 'm' to "ₘ", 'n' to "ₙ", 'o' to "ₒ", 'p' to "ₚ", 'r' to "ᵣ", 's' to "ₛ",
    't' to "ₜ", 'u' to "ᵤ", 'v' to "ᵥ", 'x' to "ₓ"
)

private val SYMBOLS = listOf(
    "varnothing" to "∅", "emptyset" to "∅", "subseteq" to "⊆", "supseteq" to "⊇", "rightarrow" to "→", "leftarrow" to "←",
    "Rightarrow" to "⇒", "Leftarrow" to "⇐", "leftrightarrow" to "↔", "Leftrightarrow" to "⇔", "longrightarrow" to "⟶",
    "longleftarrow" to "⟵", "mapsto" to "↦", "uparrow" to "↑", "downarrow" to "↓",
    "triangle" to "△", "square" to "□", "angle" to "∠", "measuredangle" to "∡", "perp" to "⊥", "parallel" to "∥",
    "infty" to "∞", "partial" to "∂", "nabla" to "∇", "forall" to "∀", "exists" to "∃", "nexists" to "∄",
    "approx" to "≈", "equiv" to "≡", "simeq" to "≃", "sim" to "∼", "propto" to "∝", "cong" to "≅",
    "neq" to "≠", "ne" to "≠", "leq" to "≤", "le" to "≤", "geq" to "≥", "ge" to "≥", "ll" to "≪", "gg" to "≫",
    "times" to "×", "cdot" to "·", "div" to "÷", "pm" to "±", "mp" to "∓", "ast" to "∗", "star" to "⋆", "bullet" to "•",
    "circ" to "°", "degree" to "°", "prime" to "′", "ldots" to "…", "dots" to "…", "cdots" to "⋯", "vdots" to "⋮",
    "langle" to "⟨", "rangle" to "⟩", "lfloor" to "⌊", "rfloor" to "⌋", "lceil" to "⌈", "rceil" to "⌉",
    "vert" to "|", "Vert" to "‖", "mid" to "|", "backslash" to "\\", "setminus" to "∖",
    "cup" to "∪", "cap" to "∩", "subset" to "⊂", "supset" to "⊃", "notin" to "∉", "in" to "∈", "ni" to "∋",
    "sum" to "∑", "prod" to "∏", "int" to "∫", "iint" to "∬", "oint" to "∮", "oplus" to "⊕", "otimes" to "⊗",
    "checkmark" to "✓", "dagger" to "†", "S" to "§", "P" to "¶", "copyright" to "©", "pounds" to "£",
    "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ", "epsilon" to "ε", "varepsilon" to "ε", "zeta" to "ζ",
    "eta" to "η", "theta" to "θ", "vartheta" to "ϑ", "iota" to "ι", "kappa" to "κ", "lambda" to "λ", "mu" to "μ",
    "nu" to "ν", "xi" to "ξ", "pi" to "π", "rho" to "ρ", "varrho" to "ϱ", "sigma" to "σ", "varsigma" to "ς",
    "tau" to "τ", "upsilon" to "υ", "phi" to "φ", "varphi" to "φ", "chi" to "χ", "psi" to "ψ", "omega" to "ω",
    "Gamma" to "Γ", "Delta" to "Δ", "Theta" to "Θ", "Lambda" to "Λ", "Xi" to "Ξ", "Pi" to "Π", "Sigma" to "Σ",
    "Upsilon" to "Υ", "Phi" to "Φ", "Psi" to "Ψ", "Omega" to "Ω",
    "arcsin" to "arcsin", "arccos" to "arccos", "arctan" to "arctan", "sinh" to "sinh", "cosh" to "cosh", "tanh" to "tanh",
    "sin" to "sin", "cos" to "cos", "tan" to "tan", "cot" to "cot", "sec" to "sec", "csc" to "csc",
    "log" to "log", "ln" to "ln", "exp" to "exp", "lim" to "lim", "max" to "max", "min" to "min", "deg" to "deg"
).sortedByDescending { it.first.length }

private fun readGroup(s: String, start: Int): Pair<String, Int>? {
    if (start >= s.length || s[start] != '{') return null
    var depth = 0
    var i = start
    while (i < s.length) {
        if (s[i] == '{') depth++
        else if (s[i] == '}') {
            depth--
            if (depth == 0) return s.substring(start + 1, i) to i + 1
        }
        i++
    }
    return null
}

private fun toScript(g: String, table: Map<Char, String>, fallback: String): String {
    if (g.isEmpty()) return ""
    val mapped = g.map { table[it] }
    if (mapped.all { it != null }) return mapped.joinToString("") { it!! }
    return if (g.length == 1) fallback + g else "$fallback($g)"
}

private val SIMPLE = Regex("^[A-Za-z0-9°.′α-ωΑ-Ω]+$")

private fun expandFractions(input: String): String {
    var s = input.replace("\\dfrac", "\\frac").replace("\\tfrac", "\\frac")
    s = s.replace(Regex("\\\\frac\\s*(\\d)\\s*(\\d)"), "$1⁄$2")
    var guard = 0
    while (guard++ < 32) {
        val idx = s.indexOf("\\frac")
        if (idx < 0) break
        val g1 = readGroup(s, idx + 5) ?: break
        val g2 = readGroup(s, g1.second) ?: break
        val a = g1.first.trim()
        val b = g2.first.trim()
        val rep = if (SIMPLE.matches(a) && SIMPLE.matches(b) && a.length <= 8 && b.length <= 8) "$a⁄$b" else "($a)/($b)"
        s = s.substring(0, idx) + rep + s.substring(g2.second)
    }
    return s
}

private fun expandSqrt(input: String): String {
    var s = input.replace(Regex("\\\\sqrt\\[([^\\]]*)\\]\\{([^{}]*)\\}")) { m ->
        toScript(m.groupValues[1], SUPER, "^") + "√" + wrapRoot(m.groupValues[2])
    }
    s = s.replace(Regex("\\\\sqrt\\{([^{}]*)\\}")) { m -> "√" + wrapRoot(m.groupValues[1]) }
    s = s.replace(Regex("\\\\sqrt\\s*([A-Za-z0-9])"), "√$1")
    return s
}

private fun wrapRoot(x: String): String {
    val t = x.trim()
    return if (SIMPLE.matches(t)) t else "($t)"
}

fun latexToUnicode(src: String): String {
    var s = src
    s = s.replace(Regex("\\\\begin\\{[^}]*\\}"), "").replace(Regex("\\\\end\\{[^}]*\\}"), "")
    s = s.replace("\\\\", "\n").replace("&", " ")
    listOf("\\left", "\\right", "\\displaystyle", "\\textstyle", "\\limits", "\\nolimits", "\\!", "\\,", "\\;", "\\:", "\\quad", "\\qquad")
        .forEach { s = s.replace(it, " ") }
    repeat(3) {
        s = s.replace(Regex("\\\\(?:text|mathrm|mathbf|mathit|textrm|operatorname|boxed)\\{([^{}]*)\\}"), "$1")
    }
    s = s.replace(Regex("\\\\(overline|bar|hat|vec|overrightarrow|tilde)\\{([^{}]*)\\}")) { m ->
        val mark = when (m.groupValues[1]) {
            "hat" -> "̂"
            "vec", "overrightarrow" -> "⃗"
            "tilde" -> "̃"
            else -> "̄"
        }
        m.groupValues[2].map { "$it$mark" }.joinToString("")
    }
    SYMBOLS.forEach { (cmd, uni) -> s = s.replace("\\$cmd", uni) }
    s = expandFractions(s)
    s = expandSqrt(s)
    s = s.replace(Regex("\\^\\{([^{}]*)\\}")) { toScript(it.groupValues[1], SUPER, "^") }
    s = s.replace(Regex("\\^([A-Za-z0-9°′])")) { toScript(it.groupValues[1], SUPER, "^") }
    s = s.replace(Regex("_\\{([^{}]*)\\}")) { toScript(it.groupValues[1], SUB, "_") }
    s = s.replace(Regex("_([A-Za-z0-9])")) { toScript(it.groupValues[1], SUB, "_") }
    listOf("\\$" to "$", "\\{" to "{", "\\}" to "}", "\\_" to "_", "\\%" to "%", "\\&" to "&", "\\#" to "#")
        .forEach { (a, b) -> s = s.replace(a, b) }
    s = s.replace(Regex("\\\\([a-zA-Z]+)"), "")
    s = s.replace(Regex("(?m)(^|[=\\s(])-(\\d)"), "$1−$2")
    s = s.replace(Regex("(?<![A-Za-z0-9])-(?![A-Za-z0-9])"), "−")
    s = s.replace(Regex("[ \t]{2,}"), " ")
    s = s.lines().joinToString("\n") { it.trim() }
    s = s.replace(Regex("\n{3,}"), "\n\n")
    return s.trim()
}

private fun processProse(segment: String): String {
    val open = segment.indexOf("<svg", ignoreCase = true)
    if (open < 0) return processMath(segment)
    val head = processMath(segment.substring(0, open))
    val close = segment.indexOf("</svg>", open, ignoreCase = true)
    if (close < 0) return head + "\n```svg-pending\n" + segment.substring(open) + "\n```\n"
    val end = close + 6
    return head + "\n```svg\n" + segment.substring(open, end) + "\n```\n" + processProse(segment.substring(end))
}

private fun processMath(segment: String): String {
    var prose = segment.replace(Regex("\\$\\$([\\s\\S]*?)\\$\\$")) { m ->
        "\n```math\n${m.groupValues[1].trim()}\n```\n"
    }
    val openMath = prose.indexOf("$$")
    if (openMath >= 0) {
        prose = prose.substring(0, openMath) + "\n```math\n" + prose.substring(openMath + 2) + "\n```\n"
    }
    return prose
}

fun normalizeRichText(raw: String): String {
    val text = raw
        .replace(Regex("\\\\\\[([\\s\\S]*?)\\\\\\]")) { m -> "\n$$\n${m.groupValues[1].trim()}\n$$\n" }
        .replace(Regex("\\\\\\(([\\s\\S]*?)\\\\\\)")) { m -> "$${m.groupValues[1].trim()}$" }
    val out = StringBuilder()
    var i = 0
    while (i < text.length) {
        val fenceStart = text.indexOf("```", i)
        if (fenceStart < 0) {
            out.append(processProse(text.substring(i)))
            break
        }
        out.append(processProse(text.substring(i, fenceStart)))
        val lineEnd = text.indexOf('\n', fenceStart)
        val contentStart = if (lineEnd < 0) text.length else lineEnd + 1
        val fenceEnd = text.indexOf("```", contentStart)
        val content = if (fenceEnd < 0) text.substring(contentStart) else text.substring(contentStart, fenceEnd)
        val closed = fenceEnd >= 0
        if (content.contains("<svg", ignoreCase = true)) {
            val lang = if (closed && content.contains("</svg>", ignoreCase = true)) "svg" else "svg-pending"
            out.append("\n```$lang\n").append(content.trim()).append("\n```\n")
        } else {
            out.append(text.substring(fenceStart, if (closed) fenceEnd + 3 else text.length))
        }
        i = if (closed) fenceEnd + 3 else text.length
    }
    return out.toString()
}

fun previewText(raw: String): String {
    val noSvg = raw.replace(Regex("<svg[\\s\\S]*?</svg>", RegexOption.IGNORE_CASE), " [drawing] ")
    val noFence = noSvg.replace(Regex("```[\\s\\S]*?```"), " ")
    return latexToUnicode(noFence)
        .replace(Regex("[#*`>$]"), "")
        .replace(Regex("\\s+"), " ")
        .trim()
        .take(90)
}