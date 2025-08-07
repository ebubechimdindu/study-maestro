package com.example.studymaestro

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.studymaestro.presentation.NavGraphs
import com.example.studymaestro.presentation.destinations.SessionScreenRouteDestination
import com.example.studymaestro.presentation.session.StudySessionTimerService
import com.example.studymaestro.presentation.theme.StudyMaestroTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint

//AndroidEntryPoint	Tells Hilt where to inject dependencies (Activity, Fragment, etc.)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: SplashViewModel by viewModels()
    private var isBound by mutableStateOf(false)// keeps track of whether the service is currently connected (bound).
    private lateinit var timerService: StudySessionTimerService//This holds a reference to the actual service instance once the connection is established.
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as StudySessionTimerService.StudySessionTimerBinder
            timerService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isBound = false
        }
    }

    //    When your activity starts, it binds to the service.
    override fun onStart() {
        super.onStart()
        Intent(this, StudySessionTimerService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splashScreen.setKeepOnScreenCondition { viewModel.isLoading.value }
        setContent {
            if (isBound) {
                StudyMaestroTheme {
                    DestinationsNavHost(
                        navGraph = NavGraphs.root,
                        //dependenciesContainerBuilder:
                        //A function or property that allows you to inject dependencies directly into a destination (or screen/route)
                        dependenciesContainerBuilder = {
                        // dependency(...) { ... }:
                        //You're declaring a dependency to be injected when navigating to a screen or route.
                            dependency(SessionScreenRouteDestination) { timerService }
                        }
                    )
                }
            }
        }

        requestPermission()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                0
            )
        }
    }

    //    When it stops, it unbinds to avoid leaks.
    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }
}

