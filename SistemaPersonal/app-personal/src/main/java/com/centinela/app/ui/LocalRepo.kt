package com.centinela.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.sistemapersonal.data.repo.SistemaPersonalRepository
import com.sistemapersonal.domain.GamificacionEngine

@Composable
fun rememberRepo(): SistemaPersonalRepository {
    val context = LocalContext.current
    return SistemaPersonalRepository.get(context)
}

@Composable
fun rememberGamificacionEngine(): GamificacionEngine {
    val repo = rememberRepo()
    return GamificacionEngine.get(repo)
}

fun hoyYyyyMMdd(): String =
    java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())

fun semanaIso(): String {
    val cal = java.util.Calendar.getInstance()
    val year = cal.get(java.util.Calendar.YEAR)
    val week = cal.get(java.util.Calendar.WEEK_OF_YEAR)
    return "%d-W%02d".format(year, week)
}
