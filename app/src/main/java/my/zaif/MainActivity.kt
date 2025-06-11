package my.zaif

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import my.zaif.navigation.NavigationItem
import my.zaif.navigation.NavigationRoutes
import my.zaif.screens.CredentialsScreen
import my.zaif.screens.EntityDetailScreen
import my.zaif.screens.LedgerScreen
import my.zaif.screens.PersonDetailScreen
import my.zaif.screens.SettingsScreen
import my.zaif.ui.theme.ZaifTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZaifTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        NavigationItem.Ledger,
        NavigationItem.Credentials,
        NavigationItem.Settings
    )
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController, 
            startDestination = NavigationItem.Ledger.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavigationItem.Ledger.route) { 
                LedgerScreen(navController = navController) 
            }
            composable(NavigationItem.Credentials.route) { 
                CredentialsScreen(navController = navController) 
            }
            composable(NavigationItem.Settings.route) { 
                SettingsScreen() 
            }
            composable(
                route = NavigationRoutes.PERSON_DETAIL,
                arguments = listOf(
                    navArgument("personId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val personId = backStackEntry.arguments?.getLong("personId") ?: -1L
                PersonDetailScreen(personId = personId, navController = navController)
            }
            
            composable(
                route = NavigationRoutes.ENTITY_DETAIL,
                arguments = listOf(
                    navArgument("entityId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val entityId = backStackEntry.arguments?.getLong("entityId") ?: -1L
                EntityDetailScreen(entityId = entityId, navController = navController)
            }
        }
    }
}