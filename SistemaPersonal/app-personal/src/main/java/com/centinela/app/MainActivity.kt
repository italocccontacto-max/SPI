package com.centinela.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.centinela.app.admin.DeviceOwnerManager
import com.centinela.app.ui.screens.*
import com.sistemapersonal.model.Modulo
import com.sistemapersonal.ui.components.HudBackground
import com.sistemapersonal.ui.components.BottomNavBar
import com.sistemapersonal.ui.components.HeaderBar
import com.sistemapersonal.ui.components.ModuleTransition
import com.sistemapersonal.ui.components.ModuleHero
import com.sistemapersonal.ui.components.VisualSystemLayer
import com.sistemapersonal.ui.components.VisualEnergyProvider
import com.sistemapersonal.ui.components.rememberVisualEnergyController
import com.sistemapersonal.ui.theme.SistemaPersonalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (DeviceOwnerManager.esDeviceOwner(this)) {
            DeviceOwnerManager.configurarComoLauncherFijo(this)

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {  }
            })
        }

        setContent {
            var moduloSeleccionado by remember { mutableStateOf(Modulo.INICIO) }
            val visualSystem = rememberVisualEnergyController()

            LaunchedEffect(moduloSeleccionado) {
                if (visualSystem.activeModule != moduloSeleccionado.id) {
                    visualSystem.navigate(moduloSeleccionado.id)
                }
            }

            SistemaPersonalTheme(moduloActivo = moduloSeleccionado.theme) {
                VisualEnergyProvider(visualSystem) {
                val activeColors = com.sistemapersonal.ui.theme.accentFor(moduloSeleccionado.theme)
                Column(modifier = Modifier.fillMaxSize()) {
                    HeaderBar(
                        modulo = moduloSeleccionado,
                        accent = activeColors.accent,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        HudBackground(
                            accent = activeColors.accent,
                            heroScale = 1f,
                            heroAlpha = 0.12f
                        ) {
                            VisualSystemLayer(
                                accent = activeColors.accent,
                                system = visualSystem,
                                modifier = Modifier.fillMaxSize()
                            ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                ModuleHero(
                                    modulo = moduloSeleccionado,
                                    accent = activeColors.accent,
                                    alpha = if (moduloSeleccionado == Modulo.INICIO) 0.42f else 0.30f,
                                    scale = if (moduloSeleccionado == Modulo.INICIO) 1.14f else 0.98f,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .fillMaxSize(0.60f)
                                )

                                when (moduloSeleccionado) {
                                    Modulo.INICIO -> InicioScreen(onIrAModulo = { moduloSeleccionado = it })
                                    Modulo.CONSTITUCION -> ConstitucionScreen()
                                    Modulo.IDENTIDAD -> IdentidadScreen()
                                    Modulo.ANTIIDENTIDAD -> AntiidentidadScreen()
                                    Modulo.DIRECCION -> DireccionScreen()
                                    Modulo.OBJETIVOS -> ObjetivosScreen()
                                    Modulo.PUD -> PudScreen()
                                    Modulo.PROTOCOLOS -> ProtocolosScreen()
                                    Modulo.EJECUCION -> EjecucionScreen()
                                    Modulo.EVOLUCION -> EvolucionScreen()
                                    Modulo.BIBLIOTECA -> BibliotecaScreen()
                                    Modulo.GUARDIAN -> GuardianScreen()
                                }

                                ModuleTransition(
                                    transitionKey = moduloSeleccionado.id,
                                    accent = activeColors.accent,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            }
                        }
                    }
                    BottomNavBar(
                        modulos = Modulo.entries,
                        seleccionado = moduloSeleccionado,
                        onSeleccionar = { moduloSeleccionado = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                }
            }
        }
    }
}
