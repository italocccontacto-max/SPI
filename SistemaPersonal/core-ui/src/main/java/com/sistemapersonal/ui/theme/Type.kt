package com.sistemapersonal.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sistemapersonal.ui.R

val OxaniumFamily = FontFamily(
    Font(R.font.oxanium_light, FontWeight.Light),
    Font(R.font.oxanium_regular, FontWeight.Normal),
    Font(R.font.oxanium_medium, FontWeight.Medium),
    Font(R.font.oxanium_semibold, FontWeight.SemiBold),
    Font(R.font.oxanium_bold, FontWeight.Bold),
    Font(R.font.oxanium_extrabold, FontWeight.ExtraBold)
)

val ChakraPetchFamily = FontFamily(
    Font(R.font.chakra_petch_light, FontWeight.Light),
    Font(R.font.chakra_petch_regular, FontWeight.Normal),
    Font(R.font.chakra_petch_medium, FontWeight.Medium),
    Font(R.font.chakra_petch_semibold, FontWeight.SemiBold),
    Font(R.font.chakra_petch_bold, FontWeight.Bold)
)

val MichromaFamily = FontFamily(Font(R.font.michroma_regular, FontWeight.Normal))
val MartianMonoFamily = FontFamily(Font(R.font.martian_mono_regular, FontWeight.Normal))

val SistemaPersonalTypography = Typography(
    displayLarge = TextStyle(fontFamily = MichromaFamily, fontSize = 40.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.5.sp),
    displayMedium = TextStyle(fontFamily = OxaniumFamily, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
    headlineLarge = TextStyle(fontFamily = OxaniumFamily, fontSize = 28.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontFamily = OxaniumFamily, fontSize = 24.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontFamily = OxaniumFamily, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontFamily = ChakraPetchFamily, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = ChakraPetchFamily, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
    titleSmall = TextStyle(fontFamily = ChakraPetchFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontFamily = ChakraPetchFamily, fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontFamily = ChakraPetchFamily, fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontFamily = ChakraPetchFamily, fontSize = 12.sp, fontWeight = FontWeight.Light),
    labelLarge = TextStyle(fontFamily = MartianMonoFamily, fontSize = 13.sp, fontWeight = FontWeight.Normal, letterSpacing = 1.sp),
    labelMedium = TextStyle(fontFamily = MartianMonoFamily, fontSize = 11.sp, fontWeight = FontWeight.Normal, letterSpacing = 1.sp),
    labelSmall = TextStyle(fontFamily = MartianMonoFamily, fontSize = 10.sp, fontWeight = FontWeight.Normal, letterSpacing = 0.8.sp)
)
