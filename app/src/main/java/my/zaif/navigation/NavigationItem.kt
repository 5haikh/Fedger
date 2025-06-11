package my.zaif.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val title: String, val icon: ImageVector) {
    data object Ledger : NavigationItem(NavigationRoutes.LEDGER, "Ledger", Icons.Default.AccountBalanceWallet)
    data object Credentials : NavigationItem(NavigationRoutes.CREDENTIALS, "Credentials", Icons.Default.Password)
    data object Settings : NavigationItem(NavigationRoutes.SETTINGS, "Settings", Icons.Default.Settings)
} 