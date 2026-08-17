package com.centinela.familiar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.centinela.familiar.ui.DashboardScreen
import com.centinela.familiar.ui.LoginScreen
import com.centinela.familiar.ui.PairingScreen
import com.sistemapersonal.network.AuthFamiliar
import com.sistemapersonal.network.FirebaseConfig
import com.sistemapersonal.network.FirebaseFamilyApi
import com.sistemapersonal.ui.theme.Bg0
import com.sistemapersonal.ui.theme.SistemaPersonalTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseConfig.inicializar(this)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7001)
        }

        setContent {
            val auth = remember { AuthFamiliar() }
            val familyApi = remember { FirebaseFamilyApi() }
            val scope = rememberCoroutineScope()
            var sesionActiva by remember { mutableStateOf(auth.usuarioActual() != null) }
            val prefs = remember { getSharedPreferences("sistema_familiar", MODE_PRIVATE) }
            var familyId by remember { mutableStateOf(prefs.getString("family_id", null)) }
            var pairingBusy by remember { mutableStateOf(false) }
            var pairingError by remember { mutableStateOf<String?>(null) }
            var leaveBusy by remember { mutableStateOf(false) }

            SistemaPersonalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Bg0) {
                    when {
                        !FirebaseConfig.estaConfigurado -> com.centinela.familiar.ui.SinConfigurarScreen()
                        !sesionActiva -> LoginScreen(auth = auth, onLoginOk = {
                            scope.launch {
                                auth.refrescarToken()
                                sesionActiva = true
                                pairingError = null
                            }
                        })
                        familyId == null -> PairingScreen(
                            cargando = pairingBusy,
                            error = pairingError,
                            onCodigoIngresado = { codigo ->
                                pairingBusy = true
                                pairingError = null
                                scope.launch {
                                    val result = familyApi.vincularPorCodigo(codigo)
                                    pairingBusy = false
                                    result.onSuccess { id ->
                                        prefs.edit().putString("family_id", id).apply()
                                        familyId = id
                                    }.onFailure {
                                        pairingError = "No se pudo vincular. El código puede ser incorrecto, usado o vencido."
                                    }
                                }
                            }
                        )
                        else -> DashboardScreen(
                            familyId = familyId!!,
                            onCerrarSesion = {
                                auth.cerrarSesion()
                                prefs.edit().remove("family_id").apply()
                                familyId = null
                                sesionActiva = false
                            },
                            onCambiarVinculacion = {
                                if (!leaveBusy) {
                                    leaveBusy = true
                                    scope.launch {
                                        val result = familyApi.desvincular()
                                        leaveBusy = false
                                        if (result.isSuccess) {
                                            prefs.edit().remove("family_id").apply()
                                            familyId = null
                                        } else {
                                            pairingError = "No se pudo desvincular el dispositivo. Verificá tu conexión."
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
