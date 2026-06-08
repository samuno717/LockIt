package com.example.lockit

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lockit.util.LocaleHelper
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.lockit.ui.Screen
import com.example.lockit.ui.screens.*
import com.example.lockit.ui.theme.LockItTheme
import com.example.lockit.viewmodel.LockItViewModel

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LockItViewModel = viewModel(factory = LockItViewModel.Factory)
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            
            LockItTheme(darkTheme = isDarkMode) {
                LockItApp(viewModel)
            }
        }
    }
}

@Composable
fun LockItApp(viewModel: LockItViewModel) {
    val navController = rememberNavController()
    val user by viewModel.currentUser.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Screen.Locker.route, Screen.Settings.route, Screen.PassMeter.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface) {
                    NavigationBarItem(
                        selected = currentRoute == Screen.PassMeter.route,
                        onClick = { navController.navigate(Screen.PassMeter.route) },
                        icon = { Icon(Icons.Default.GridView, contentDescription = null) },
                        label = { Text("Passtools", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Locker.route,
                        onClick = { navController.navigate(Screen.Locker.route) },
                        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        label = { Text("Locker", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = currentRoute == Screen.Settings.route,
                        onClick = { navController.navigate(Screen.Settings.route) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Settings", fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (user == null) Screen.Register.route else Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navController.navigate(Screen.Locker.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = viewModel,
                    onRegisterSuccess = { navController.navigate(Screen.Locker.route) { popUpTo(Screen.Register.route) { inclusive = true } } }
                )
            }
            composable(Screen.Locker.route) {
                LockerScreen(
                    viewModel = viewModel,
                    onNavigateToAdd = { navController.navigate(Screen.AddPassword.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToAccount = { navController.navigate(Screen.Account.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToVideo = { navController.navigate("video_player") },
                    onNavigateToAudio = { navController.navigate("audio_player") },
                    onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }
                )
            }
            composable(Screen.PassMeter.route) {
                PassToolsScreen(onBack = { navController.navigate(Screen.Locker.route) })
            }
            composable(Screen.AddPassword.route) {
                AddPasswordScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Account.route) {
                AccountScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }
            composable(
                route = Screen.PasswordDetails.route,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: 0L
                PasswordDetailsScreen(id = id, viewModel = viewModel, onBack = { navController.popBackStack() })
            }
            composable("video_player") {
                VideoPlayerScreen(onBack = { navController.popBackStack() })
            }
            composable("audio_player") {
                AudioPlayerScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
