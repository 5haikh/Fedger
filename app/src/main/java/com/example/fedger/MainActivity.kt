package com.example.fedger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fedger.ui.PersonViewModel
import com.example.fedger.ui.components.ANIMATION_DURATION_MEDIUM
import com.example.fedger.ui.components.ANIMATION_DURATION_SHORT
import com.example.fedger.ui.components.BottomBar
import com.example.fedger.ui.navigation.Screen
import com.example.fedger.ui.screens.AddPersonScreen
import com.example.fedger.ui.screens.AddTransactionScreen
import com.example.fedger.ui.screens.BalanceSummaryScreen
import com.example.fedger.ui.screens.DataImportExportScreen
import com.example.fedger.ui.screens.PersonDetailsScreen
import com.example.fedger.ui.screens.PersonListScreen
import com.example.fedger.ui.theme.FedgerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FedgerTheme {
                val navController = rememberNavController()
                val fedgerApplication = application as FedgerApplication
                val viewModel: PersonViewModel = viewModel(
                    factory = PersonViewModel.Factory(fedgerApplication.repository)
                )
                
                // For error handling
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()
                val errorState by viewModel.error.collectAsState()
                
                // Show error in Snackbar when it occurs
                errorState?.let { error ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = error,
                            duration = SnackbarDuration.Long
                        )
                        // Clear the error after showing it
                        viewModel.clearError()
                    }
                }
                
                // Determine if bottom bar should be visible based on current route
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Show bottom bar only on main screens
                val showBottomBar by remember(currentRoute) {
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
                
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Only show bottom bar on specific screens
                        BottomBar(
                            navController = navController,
                            visible = showBottomBar
                        )
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = Screen.PersonList.route
                        ) {
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
                                        viewModel.deletePerson(person)
                                    },
                                    onImportExportClick = {
                                        navController.navigate(Screen.DataImportExport.route)
                                    },
                                    viewModel = viewModel
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
                                        viewModel.addPerson(person)
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
                                    viewModel = viewModel,
                                    onBackClick = {
                                        navController.navigateUp()
                                    },
                                    onAddTransactionClick = {
                                        navController.navigate(Screen.AddTransaction.createRoute(personId))
                                    }
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
                                val personFlow = viewModel.getPersonById(personId)
                                val personState by personFlow.collectAsState(initial = null)
                                
                                personState?.let { person ->
                                    AddTransactionScreen(
                                        preselectedPerson = person,
                                        onTransactionAdded = { transaction ->
                                            viewModel.addTransaction(transaction)
                                            viewModel.refreshTransactions()
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
                                    viewModel = viewModel,
                                    onTransactionAdded = { transaction ->
                                        viewModel.addTransaction(transaction)
                                        viewModel.refreshTransactions()
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
                                    viewModel = viewModel,
                                    onBackClick = {
                                        navController.navigateUp()
                                    }
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
                                    viewModel = viewModel,
                                    onBackClick = {
                                        navController.navigateUp()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}