package de.hbch.traewelling.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import de.hbch.traewelling.R

val RalewayFont = Font(R.font.raleway)
val TwindexxFont = Font(R.font.ae_matrix16_twindexx_standard)
val Raleway = FontFamily(RalewayFont)
val Twindexx = FontFamily(TwindexxFont)

private val defaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = Raleway),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = Raleway),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = Raleway),
    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = Raleway),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = Raleway),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = Raleway),
    titleLarge = defaultTypography.titleLarge.copy(fontFamily = Raleway),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = Raleway),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = Raleway),
    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = Raleway),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = Raleway),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = Raleway),
    labelLarge = defaultTypography.labelLarge.copy(fontFamily = Raleway),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = Raleway),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = Raleway)
)

val LineIconStyle = defaultTypography.bodyMedium
