package de.hbch.traewelling.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import de.hbch.traewelling.R

val RalewayFont = Font(R.font.raleway)
val TwindexxFont = Font(R.font.ae_matrix16_btmodern_standard)
val Raleway = FontFamily(RalewayFont)
val BTModern = FontFamily(TwindexxFont)

val DefaultTypography = Typography()
val AppTypography = Typography(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = Raleway),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = Raleway),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = Raleway),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = Raleway),
    headlineMedium = DefaultTypography.headlineMedium.copy(fontFamily = Raleway),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = Raleway),
    titleLarge = DefaultTypography.titleLarge.copy(fontFamily = Raleway),
    titleMedium = DefaultTypography.titleMedium.copy(fontFamily = Raleway),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = Raleway),
    bodyLarge = DefaultTypography.bodyLarge.copy(fontFamily = Raleway),
    bodyMedium = DefaultTypography.bodyMedium.copy(fontFamily = Raleway),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = Raleway),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = Raleway),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = Raleway),
    labelSmall = DefaultTypography.labelSmall.copy(fontFamily = Raleway)
)

val LineIconStyle = DefaultTypography.bodyMedium
