package com.sistemapersonal.ui.theme

import androidx.compose.ui.graphics.Color
import com.sistemapersonal.model.AccentTheme

val Void = Color(0xFF000000)
val Bg0 = Color(0xFF02060A)
val Bg1 = Color(0xFF040B10)
val Bg2 = Color(0xFF071319)
val PanelFill = Color(0xB8040E14)
val PanelFillSoft = Color(0x73040E14)
val Hairline = Color(0x248CAFBE)

val Ink0 = Color(0xFFF2F6F7)
val Ink1 = Color(0xFFB9C8CE)
val Ink2 = Color(0xFF7D939C)
val Ink3 = Color(0xFF4C626B)

val Amber = Color(0xFFFFAB13)
val AmberDim = Color(0xFF8A5A10)
val AmberGlow = Color(0x59FFAB13)

val Cyan = Color(0xFF00C2E0)
val CyanDim = Color(0xFF0A5C6B)
val CyanGlow = Color(0x5900C2E0)

val Blue = Color(0xFF0A9CFF)
val BlueDim = Color(0xFF0A4680)
val BlueGlow = Color(0x590A9CFF)

val Red = Color(0xFFFF4526)
val RedDim = Color(0xFF7A2015)
val RedGlow = Color(0x59FF4526)

val Mint = Color(0xFF00FFB3)
val MintDim = Color(0xFF0A6B4C)
val MintGlow = Color(0x5200FFB3)

val Teal = Color(0xFF00FCF5)
val TealDim = Color(0xFF0A6B68)
val TealGlow = Color(0x5200FCF5)

val Purple = Color(0xFF8926FA)
val PurpleDim = Color(0xFF4A1690)
val PurpleGlow = Color(0x618926FA)

val Neon = Color(0xFF00E5FF)
val NeonDim = Color(0xFF0A5C70)
val NeonGlow = Color(0x5900E5FF)

val Ok = Color(0xFF3DDC84)
val OkGlow = Color(0x593DDC84)
val Warn = Amber
val Danger = Red
val Info = Blue

data class AccentColors(val accent: Color, val dim: Color, val glow: Color)

fun accentFor(theme: AccentTheme): AccentColors = when (theme) {
    AccentTheme.AMBER -> AccentColors(Amber, AmberDim, AmberGlow)
    AccentTheme.CYAN -> AccentColors(Cyan, CyanDim, CyanGlow)
    AccentTheme.BLUE -> AccentColors(Blue, BlueDim, BlueGlow)
    AccentTheme.RED -> AccentColors(Red, RedDim, RedGlow)
    AccentTheme.MINT -> AccentColors(Mint, MintDim, MintGlow)
    AccentTheme.TEAL -> AccentColors(Teal, TealDim, TealGlow)
    AccentTheme.PURPLE -> AccentColors(Purple, PurpleDim, PurpleGlow)
    AccentTheme.NEON -> AccentColors(Neon, NeonDim, NeonGlow)
}
