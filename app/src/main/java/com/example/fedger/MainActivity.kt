package com.example.fedger

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fedger.ui.PasswordViewModel
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.BottomBar
import com.example.fedger.ui.components.PasswordBottomBar
import com.example.fedger.ui.components.ANIMATION_DURATION_MEDIUM
import com.example.fedger.ui.components.ANIMATION_DURATION_SHORT
import com.example.fedger.ui.navigation.Screen
import com.example.fedger.ui.screens.*
import com.example.fedger.ui.theme.FedgerTheme
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Initialize application and get repository
            val fedgerApplication = application as FedgerApplication
            
            setContent {
                FedgerTheme {
                    val navController = rememberNavController()
                    
                    // Create the view models
                    val ledgerViewModel: PersonViewModel = viewModel(
                        factory = PersonViewModel.Factory(fedgerApplication.repository)
                    )
                    
                    val passwordViewModel: PasswordViewModel = viewModel(
                        factory = PasswordViewModel.Factory(fedgerApplication.passwordRepository)
                    )
                    
                    // For error handling
                    val snackbarHostState = remember { SnackbarHostState() }
                    val coroutineScope = rememberCoroutineScope()
                    
                    // Handle ledger errors
                    val ledgerErrorState by ledgerViewModel.error.collectAsState()
                    ledgerErrorState?.let { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = error,
                                duration = SnackbarDuration.Short
                            )
                            // Don't clear the error here since the ViewModel will auto-clear it
                            // This prevents race conditions between auto-dismissal and manual dismissal
                        }
                    }
                    
                    // Handle password manager errors
                    val passwordErrorState by passwordViewModel.error.collectAsState()
                    passwordErrorState?.let { error ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = error,
                                duration = SnackbarDuration.Short
                            )
                            // Don't clear the error here since the ViewModel will auto-clear it
                            // This prevents race conditions between auto-dismissal and manual dismissal
                        }
                    }
                    
                    // Determine if bottom bar should be visible based on current route
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    
                    // Show ledger bottom bar only on main ledger screens
                    val showLedgerBottomBar by remember(currentRoute) {
                        derivedStateOf {
                            when (currentRoute) {
                                Screen.PersonList.route,
                                Screen.AddPerson.route,
                                Screen.AddTransactionGlobal.route,
                                Screen.BalanceSummary.route -> true
                                else -> false
                            }
                        }
                    }
                    
                    // Show password bottom bar only on main password screens
                    val showPasswordBottomBar by remember(currentRoute) {
                        derivedStateOf {
                            when (currentRoute) {
                                Screen.PasswordList.route,
                                Screen.AddPasswordEntry.route -> true
                                else -> false
                            }
                        }
                    }
                    
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            // Use a Box to ensure only one bottom bar is rendered
                            Box {
                                // Ledger bottom bar - only show if active and no password bar is shown
                                BottomBar(
                                    navController = navController,
                                    visible = showLedgerBottomBar && !showPasswordBottomBar
                                )
                                
                                // Password bottom bar - has priority if both would be shown
                                PasswordBottomBar(
                                    navController = navController,
                                    visible = showPasswordBottomBar
                                )
                            }
                        }
                    ) { paddingValues ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Screen.AppSelection.route
                            ) {
                                // App Selection Screen
                                composable(
                                    route = Screen.AppSelection.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    AppSelectionScreen(
                                        onLedgerSelected = {
                                            navController.navigate(Screen.PersonList.route) {
                                                popUpTo(Screen.AppSelection.route) { inclusive = true }
                                            }
                                        },
                                        onPasswordManagerSelected = {
                                            navController.navigate(Screen.PasswordList.route) {
                                                popUpTo(Screen.AppSelection.route) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                
                                // Ledger screens
                                composable(
                                    route = Screen.PersonList.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    popEnterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    popExitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    PersonListScreen(
                                        onPersonClick = { person ->
                                            navController.navigate(Screen.PersonDetails.createRoute(person.id))
                                        },
                                        onAddClick = {
                                            navController.navigate(Screen.AddPerson.route)
                                        },
                                        onDeleteClick = { person ->
                                            ledgerViewModel.deletePerson(person)
                                        },
                                        onImportExportClick = {
                                            navController.navigate(Screen.DataImportExport.route)
                                        },
                                        viewModel = ledgerViewModel,
                                        navController = navController
                                    )
                                }
                                
                                composable(
                                    route = Screen.AddPerson.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    AddPersonScreen(
                                        onPersonAdded = { person ->
                                            ledgerViewModel.addPerson(person)
                                            navController.navigateUp()
                                        },
                                        onBackClick = {
                                            navController.navigateUp()
                                        }
                                    )
                                }

                                composable(
                                    route = Screen.PersonDetails.route,
                                    arguments = listOf(
                                        navArgument("personId") { type = NavType.IntType }
                                    ),
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) { backStackEntry ->
                                    val personId = backStackEntry.arguments?.getInt("personId") ?: return@composable
                                    
                                    PersonDetailsScreen(
                                        personId = personId,
                                        viewModel = ledgerViewModel,
                                        onBackClick = {
                                            navController.navigateUp()
                                        },
                                        onAddTransactionClick = {
                                            navController.navigate(Screen.AddTransaction.createRoute(personId))
                                        },
                                        navController = navController
                                    )
                                }

                                composable(
                                    route = Screen.AddTransaction.route,
                                    arguments = listOf(
                                        navArgument("personId") { type = NavType.IntType }
                                    ),
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) { backStackEntry ->
                                    val personId = backStackEntry.arguments?.getInt("personId") ?: return@composable
                                    val personFlow = ledgerViewModel.getPersonById(personId)
                                    val personState by personFlow.collectAsState(initial = null)
                                    
                                    personState?.let { person ->
                                        AddTransactionScreen(
                                            preselectedPerson = person,
                                            onTransactionAdded = { transaction ->
                                                ledgerViewModel.addTransaction(transaction)
                                                ledgerViewModel.refreshTransactions()
                                                navController.popBackStack(
                                                    route = Screen.PersonDetails.createRoute(personId),
                                                    inclusive = false
                                                )
                                            },
                                            onBackClick = {
                                                navController.navigateUp()
                                            }
                                        )
                                    }
                                }
                                
                                // Add the new global transaction screen
                                composable(
                                    route = Screen.AddTransactionGlobal.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    AddTransactionScreen(
                                        viewModel = ledgerViewModel,
                                        onTransactionAdded = { transaction ->
                                            ledgerViewModel.addTransaction(transaction)
                                            ledgerViewModel.refreshTransactions()
                                            navController.navigateUp()
                                        },
                                        onBackClick = {
                                            navController.navigateUp()
                                        }
                                    )
                                }
                                
                                // Add the Balance Summary screen
                                composable(
                                    route = Screen.BalanceSummary.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    BalanceSummaryScreen(
                                        viewModel = ledgerViewModel,
                                        onBackClick = {
                                            navController.navigateUp()
                                        },
                                        navController = navController
                                    )
                                }
                                
                                // Add the Import/Export screen
                                composable(
                                    route = Screen.DataImportExport.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    DataImportExportScreen(
                                        viewModel = ledgerViewModel,
                                        onBackClick = {
                                            navController.navigateUp()
                                        }
                                    )
                                }
                                
                                // Password Manager screens
                                composable(
                                    route = Screen.PasswordList.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    PasswordListScreen(
                                        viewModel = passwordViewModel,
                                        onPasswordEntryClick = { entry ->
                                            navController.navigate(Screen.PasswordDetails.createRoute(entry.id))
                                        },
                                        onAddEntryClick = {
                                            navController.navigate(Screen.AddPasswordEntry.route)
                                        },
                                        onImportExportClick = {
                                            navController.navigate(Screen.PasswordImportExport.route)
                                        },
                                        navController = navController
                                    )
                                }
                                
                                // Add Password Import/Export screen
                                composable(
                                    route = Screen.PasswordImportExport.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    PasswordImportExportScreen(
                                        viewModel = passwordViewModel,
                                        onBackClick = {
                                            navController.navigateUp()
                                        }
                                    )
                                }
                                
                                composable(
                                    route = Screen.AddPasswordEntry.route,
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) {
                                    AddEditPasswordScreen(
                                        viewModel = passwordViewModel,
                                        onSaveClick = {
                                            navController.popBackStack(
                                                route = Screen.PasswordList.route,
                                                inclusive = false
                                            )
                                        },
                                        onCancelClick = {
                                            navController.navigateUp()
                                        }
                                    )
                                }
                                
                                composable(
                                    route = Screen.PasswordDetails.route,
                                    arguments = listOf(
                                        navArgument("entryId") { type = NavType.IntType }
                                    ),
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) { backStackEntry ->
                                    val entryId = backStackEntry.arguments?.getInt("entryId") ?: return@composable
                                    
                                    PasswordDetailsScreen(
                                        entryId = entryId,
                                        viewModel = passwordViewModel,
                                        onEditClick = { id ->
                                            navController.navigate(Screen.EditPasswordEntry.createRoute(id))
                                        },
                                        onBackClick = {
                                            navController.navigateUp()
                                        },
                                        navController = navController
                                    )
                                }
                                
                                composable(
                                    route = Screen.EditPasswordEntry.route,
                                    arguments = listOf(
                                        navArgument("entryId") { type = NavType.IntType }
                                    ),
                                    enterTransition = {
                                        fadeIn(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    },
                                    exitTransition = {
                                        fadeOut(animationSpec = tween(ANIMATION_DURATION_SHORT))
                                    }
                                ) { backStackEntry ->
                                    val entryId = backStackEntry.arguments?.getInt("entryId") ?: return@composable
                                    
                                    AddEditPasswordScreen(
                                        viewModel = passwordViewModel,
                                        entryId = entryId,
                                        onSaveClick = {
                                            navController.popBackStack(
                                                route = Screen.PasswordDetails.createRoute(entryId),
                                                inclusive = false
                                            )
                                        },
                                        onCancelClick = {
                                            navController.navigateUp()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing application", e)
            // Show a recovery UI or gracefully handle the error
            setContent {
                FedgerTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "An error occurred while starting the app. Please restart.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}