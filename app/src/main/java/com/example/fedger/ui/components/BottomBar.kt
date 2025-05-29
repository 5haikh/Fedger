package com.example.fedger.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fedger.ui.navigation.Screen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
) {
    object Home : BottomNavItem(
        route = Screen.PersonList.route,
        icon = Icons.Default.Home,
        title = "Home"
    )
    
    object AddContact : BottomNavItem(
        route = Screen.AddPerson.route,
        icon = Icons.Default.Person,
        title = "Add Contact"
    )
    
    object AddTransaction : BottomNavItem(
        route = Screen.AddTransactionGlobal.route,
        icon = Icons.Default.Add,
        title = "Add Transaction"
    )
    
    object Balance : BottomNavItem(
        route = Screen.BalanceSummary.route,
        icon = Icons.Default.PieChart,
        title = "Balance"
    )
}

@Composable
fun BottomBar(
    navController: NavController,
    visible: Boolean = true
) {
    if (!visible) return
    
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.AddContact,
        BottomNavItem.AddTransaction,
        BottomNavItem.Balance
    )
    
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination
    
    // Custom bottom bar with better design control
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 15.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Changed
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                
                BottomNavItem(
                    item = item, 
                    isSelected = selected,
                    onClick = {
                        if (currentDestination?.route != item.route) {
                            navController.navigate(item.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent // Changed
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Changed
    val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = item.title,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
} 