package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class InvoiceTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val description: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val accentColorHex: String,
    val bgTintHex: String,
    val borderHex: String
) {
    CLASSIC_CORPORATE(
        id = "classic_corporate",
        title = "Classic Corporate",
        subtitle = "Corporate Standard",
        description = "Executive Deep Navy & Cerulean with dual-box address cards & clean grid",
        primaryColorHex = "#0F3854",
        secondaryColorHex = "#0284C7",
        accentColorHex = "#B45309",
        bgTintHex = "#F8FAFC",
        borderHex = "#CBD5E1"
    ),
    MODERN_MINIMAL(
        id = "modern_minimal",
        title = "Modern Minimal",
        subtitle = "Clean & Sleek",
        description = "Tech Indigo & Slate with sleek underlines, airy spacing & modern pill badges",
        primaryColorHex = "#4338CA",
        secondaryColorHex = "#6366F1",
        accentColorHex = "#059669",
        bgTintHex = "#EEF2FF",
        borderHex = "#C7D2FE"
    ),
    ELEGANT_EMERALD(
        id = "elegant_emerald",
        title = "Emerald Prestige",
        subtitle = "Boutique & Retail",
        description = "Deep Forest Emerald & Warm Gold with luxury verified stamps & boutique styling",
        primaryColorHex = "#065F46",
        secondaryColorHex = "#059669",
        accentColorHex = "#D97706",
        bgTintHex = "#ECFDF5",
        borderHex = "#A7F3D0"
    ),
    BOLD_COMPACT(
        id = "bold_compact",
        title = "Bold High-Contrast",
        subtitle = "High Visibility",
        description = "Charcoal Slate & Crimson Header with high-visibility totals & prominent QR callout",
        primaryColorHex = "#1E293B",
        secondaryColorHex = "#BE123C",
        accentColorHex = "#EA580C",
        bgTintHex = "#F1F5F9",
        borderHex = "#94A3B8"
    );

    val composePrimaryColor: Color
        get() = Color(android.graphics.Color.parseColor(primaryColorHex))

    val composeSecondaryColor: Color
        get() = Color(android.graphics.Color.parseColor(secondaryColorHex))

    val composeAccentColor: Color
        get() = Color(android.graphics.Color.parseColor(accentColorHex))

    val composeBgTint: Color
        get() = Color(android.graphics.Color.parseColor(bgTintHex))

    val composeBorder: Color
        get() = Color(android.graphics.Color.parseColor(borderHex))

    companion object {
        fun fromId(id: String?): InvoiceTemplate {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: CLASSIC_CORPORATE
        }
    }
}
