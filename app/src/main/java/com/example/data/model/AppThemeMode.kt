package com.example.data.model

enum class AppThemeMode(
    val id: String,
    val titleEn: String,
    val description: String
) {
    SYSTEM(
        id = "SYSTEM",
        titleEn = "System Default",
        description = "Follows device system light/dark theme"
    ),
    LIGHT(
        id = "LIGHT",
        titleEn = "Light Mode",
        description = "Deep Navy headers & vibrant Mint Green on Off-White cards"
    ),
    DARK(
        id = "DARK",
        titleEn = "Dark Mode",
        description = "Deep Midnight Slate & vibrant Mint Green contrast"
    );

    companion object {
        fun fromId(id: String?): AppThemeMode {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: SYSTEM
        }
    }
}
