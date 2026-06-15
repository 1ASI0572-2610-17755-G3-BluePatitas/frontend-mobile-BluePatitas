package com.bluepatitas.mobile.domain.model

enum class AppLanguage(val tag: String) {
    ENGLISH("en"),
    SPANISH_LATAM("es-419");

    companion object {
        fun fromTag(tag: String): AppLanguage {
            val normalizedTag = tag.lowercase()
            return when {
                normalizedTag == SPANISH_LATAM.tag -> SPANISH_LATAM
                normalizedTag.startsWith("es-419") -> SPANISH_LATAM
                normalizedTag == "es" -> SPANISH_LATAM
                else -> entries.firstOrNull { it.tag == normalizedTag } ?: ENGLISH
            }
        }
    }
}
