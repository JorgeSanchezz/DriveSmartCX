package com.drivesmart.cx.ui.mobile.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.drivesmart.cx.ui.viewmodel.MicodusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicodusLoginScreen(
    viewModel: MicodusViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    
    Scaffold(
        topBar = { TopAppBar(title = { Text("MiCODUS Web Login") }) }
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            
                            // LOG PARA DEBUG: Ver todas las URLs
                            android.util.Log.d("MicodusWeb", "Página cargada: $url")
                            
                            // Si detectamos cualquier página que parezca el dashboard
                            if (url?.contains("aspx") == true && !url.contains("Login2.aspx")) {
                                view?.evaluateJavascript("""
                                    (function() {
                                        var uid = document.getElementById('hidUserID') ? document.getElementById('hidUserID').value : '0';
                                        var did = document.getElementById('hidDeviceID') ? document.getElementById('hidDeviceID').value : '0';
                                        return uid + '|' + did;
                                    })();
                                """.trimIndent()) { result ->
                                    val ids = result?.replace("\"", "")?.split("|")
                                    val userId = ids?.getOrNull(0)?.toIntOrNull() ?: 0
                                    val deviceId = ids?.getOrNull(1)?.toIntOrNull() ?: 0
                                    
                                    if (userId > 0) {
                                        Toast.makeText(context, "¡Conectado! Cargando tu vehículo...", Toast.LENGTH_SHORT).show()
                                        viewModel.onWebLoginSuccess(userId, deviceId)
                                        onLoginSuccess()
                                    }
                                }
                            }
                        }
                    }
                    loadUrl("https://www.micodus.net/Login2.aspx")
                }
            }
        )
    }
}
