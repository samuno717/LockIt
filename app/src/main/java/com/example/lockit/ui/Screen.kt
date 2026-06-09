package com.example.lockit.ui

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Locker : Screen("locker")
    object Settings : Screen("settings")
    object PassTools : Screen("pass_tools")
    object PassMeter : Screen("pass_meter")
    object PassMaker : Screen("passmaker")
    object AddPassword : Screen("add_password")
    object Account : Screen("account")
    object Notifications : Screen("notifications")
    object PasswordDetails : Screen("password_details/{id}") {
        fun createRoute(id: Long) = "password_details/$id"
    }
}
