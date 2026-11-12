package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = AmberAccent,
  onPrimary = AmberOnAccent,
  primaryContainer = AmberContainer,
  onPrimaryContainer = AmberOnContainer,
  secondary = NavyPrimary,
  onSecondary = NavyOnPrimary,
  secondaryContainer = ChipBackground,
  onSecondaryContainer = NavyPrimary,
  tertiary = EmeraldGreen,
  onTertiary = Color.White,
  tertiaryContainer = EmeraldLight,
  onTertiaryContainer = EmeraldDark,
  background = WarmWhite,
  onBackground = TextPrimary,
  surface = PureWhite,
  onSurface = TextPrimary,
  surfaceVariant = ChipBackground,
  onSurfaceVariant = TextMuted,
  outline = SurfaceBorder,
  error = CrimsonRed,
  onError = Color.White,
  errorContainer = CrimsonLight,
  onErrorContainer = CrimsonRed
)

private val DarkColorScheme = darkColorScheme(
  primary = AmberAccent,
  onPrimary = AmberOnAccent,
  primaryContainer = NavyContainer,
  onPrimaryContainer = AmberContainer,
  secondary = ChipBackground,
  onSecondary = NavyPrimary,
  tertiary = EmeraldGreen,
  onTertiary = Color.White,
  background = NavyPrimary,
  onBackground = Color.White,
  surface = NavyContainer,
  onSurface = Color.White,
  surfaceVariant = NavyPrimary,
  onSurfaceVariant = TextMuted,
  outline = SurfaceBorder,
  error = CrimsonRed,
  onError = Color.White
)

@Composable
fun BookMyErtigaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
