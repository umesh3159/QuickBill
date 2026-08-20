package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.AppThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8), // Light Vibrant Slate/Sky Navy Accent for dark headers
    onPrimary = DeepNavy,
    primaryContainer = Color(0xFF1E293B), // Deep Slate Container
    onPrimaryContainer = Color(0xFFF8FAFC),
    secondary = MintGreenBright, // Vibrant Mint Green Accent (#34D399)
    onSecondary = Color(0xFF064E3B),
    secondaryContainer = Color(0xFF065F46),
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = MintGreen, // Vibrant Mint Green (#10B981) for actions/paid
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF047857),
    onTertiaryContainer = Color(0xFFD1FAE5),
    background = MidnightNavy, // Deep Midnight Canvas (#0B0F19)
    onBackground = OffWhite, // Crisp Off-White Text
    surface = SlateDarkCard, // Elevated Deep Slate Card (#111827)
    onSurface = OffWhite,
    surfaceVariant = Color(0xFF1E293B), // Deep Navy/Slate Container
    onSurfaceVariant = Color(0xFF94A3B8), // Slate Grey Secondary Labels
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepNavy, // High-Contrast Deep Navy Headers (#0F172A)
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2E8F0), // Soft Slate Navy Container
    onPrimaryContainer = DeepNavy,
    secondary = MintGreen, // Vibrant Mint Green Accent (#10B981)
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5), // Mint Green Soft Container
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = MintGreen, // Vibrant Mint Green Action Button Accent (#10B981)
    onTertiary = Color.White,
    tertiaryContainer = MintGreenLight, // Mint Green Tint Container (#ECFDF5)
    onTertiaryContainer = Color(0xFF047857),
    background = CanvasSlate, // Clean Financial Slate Canvas (#F1F5F9)
    onBackground = DeepNavy, // High-Contrast Navy Typography
    surface = OffWhite, // Off-White Card Backgrounds (#F8FAFC)
    onSurface = DeepNavy, // High-Contrast Deep Navy Text on Cards
    surfaceVariant = Color(0xFFF1F5F9), // Subtle Slate Container
    onSurfaceVariant = SlateGrey, // Slate Grey for Secondary Labels (#64748B)
    outline = SlateCardBorder, // Subtle Slate Border (#E2E8F0)
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun JMDDigiSignTheme(
    appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (appThemeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

