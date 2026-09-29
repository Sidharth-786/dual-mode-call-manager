package com.example.dualmodecallmanager

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dualmodecallmanager.data.model.ContactCategory
import com.example.dualmodecallmanager.theme.DualModeCallManagerTheme
import com.example.dualmodecallmanager.ui.screens.CallLogScreen
import com.example.dualmodecallmanager.ui.screens.ContactsScreen
import com.example.dualmodecallmanager.ui.screens.DashboardScreen
import com.example.dualmodecallmanager.ui.screens.SettingsScreen
import com.example.dualmodecallmanager.ui.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Dashboard : Screen("dashboard", "Dashboard", { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") })
    object Contacts : Screen("contacts", "Contacts", { Icon(Icons.Default.Contacts, contentDescription = "Contacts") })
    object CallHistory : Screen("history", "History", { Icon(Icons.Default.History, contentDescription = "Call History") })
    object Settings : Screen("settings", "Settings", { Icon(Icons.Default.Settings, contentDescription = "Settings") })
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val roleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        viewModel.checkCallScreeningRoleStatus(this)
    }

    private val contactsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.importPhoneContacts(this, ContactCategory.WORK) { count ->
                Toast.makeText(this, "Imported $count contacts as Work Contacts", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Contacts permission required to import phone contacts", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.checkCallScreeningRoleStatus(this@MainActivity)
            }

            DualModeCallManagerTheme(activeMode = uiState.activeMode) {
                val navController = rememberNavController()
                val screens = listOf(Screen.Dashboard, Screen.Contacts, Screen.CallHistory, Screen.Settings)
                var currentRoute by remember { mutableStateOf(Screen.Dashboard.route) }

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            screens.forEach { screen ->
                                NavigationBarItem(
                                    selected = currentRoute == screen.route,
                                    onClick = {
                                        currentRoute = screen.route
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = screen.icon,
                                    label = { Text(screen.title) }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Dashboard.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) {
                            DashboardScreen(
                                state = uiState,
                                onToggleMode = { viewModel.toggleMode() },
                                onRequestRole = { requestCallScreeningRole() },
                                onNavigateToContacts = {
                                    currentRoute = Screen.Contacts.route
                                    navController.navigate(Screen.Contacts.route)
                                },
                                onNavigateToCallLogs = {
                                    currentRoute = Screen.CallHistory.route
                                    navController.navigate(Screen.CallHistory.route)
                                }
                            )
                        }

                        composable(Screen.Contacts.route) {
                            ContactsScreen(
                                state = uiState,
                                onCategoryFilterChange = { category -> viewModel.setCategoryFilter(category) },
                                onSearchQueryChange = { query -> viewModel.setSearchQuery(query) },
                                onAddContact = { name, number, category -> viewModel.addContact(name, number, category) },
                                onUpdateContact = { contact -> viewModel.updateContact(contact) },
                                onDeleteContact = { contact -> viewModel.deleteContact(contact) },
                                onRequestImportContacts = { requestImportContacts() }
                            )
                        }

                        composable(Screen.CallHistory.route) {
                            CallLogScreen(
                                state = uiState,
                                onClearLogs = { viewModel.clearCallLogs() }
                            )
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(
                                state = uiState,
                                onAllowWorkUnknownChange = { allow -> viewModel.setAllowUnknownWork(allow) },
                                onAllowPersonalUnknownChange = { allow -> viewModel.setAllowUnknownPersonal(allow) },
                                onRequestRole = { requestCallScreeningRole() }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkCallScreeningRoleStatus(this)
    }

    private fun requestCallScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as? RoleManager
            if (roleManager != null && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                roleLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Call screening role is already active!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Call Screening Service is enabled in System Settings.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestImportContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            viewModel.importPhoneContacts(this, ContactCategory.WORK) { count ->
                Toast.makeText(this, "Imported $count contacts as Work Contacts", Toast.LENGTH_SHORT).show()
            }
        } else {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }
}
