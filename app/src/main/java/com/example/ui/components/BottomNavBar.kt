package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Luggage
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextMuted

enum class NavTab(val label: String) {
  EXPLORE("Explore"),
  MY_TRIPS("My Trips"),
  OWNER_HUB("Owner Hub"),
  ADMIN("Admin")
}

@Composable
fun BottomNavBar(
  currentTab: NavTab,
  onTabSelected: (NavTab) -> Unit,
  isAdmin: Boolean,
  pendingRequestsCount: Int = 0,
  activeTripsCount: Int = 0
) {
  NavigationBar(
    containerColor = Color.White,
    tonalElevation = 8.dp,
    modifier = Modifier
      .testTag("bottom_nav_bar")
      .windowInsetsPadding(WindowInsets.navigationBars)
  ) {
    // 1. Explore
    NavigationBarItem(
      selected = currentTab == NavTab.EXPLORE,
      onClick = { onTabSelected(NavTab.EXPLORE) },
      icon = {
        Icon(
          imageVector = Icons.Default.Search,
          contentDescription = "Explore Ertigas",
          modifier = Modifier.size(24.dp)
        )
      },
      label = { Text("Explore", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyPrimary,
        selectedTextColor = NavyPrimary,
        indicatorColor = AmberContainer,
        unselectedIconColor = TextMuted,
        unselectedTextColor = TextMuted
      ),
      modifier = Modifier.testTag("nav_tab_explore")
    )

    // 2. My Trips
    NavigationBarItem(
      selected = currentTab == NavTab.MY_TRIPS,
      onClick = { onTabSelected(NavTab.MY_TRIPS) },
      icon = {
        if (activeTripsCount > 0) {
          BadgedBox(badge = { Badge { Text(activeTripsCount.toString()) } }) {
            Icon(
              imageVector = Icons.Default.Luggage,
              contentDescription = "My Trips",
              modifier = Modifier.size(24.dp)
            )
          }
        } else {
          Icon(
            imageVector = Icons.Default.Luggage,
            contentDescription = "My Trips",
            modifier = Modifier.size(24.dp)
          )
        }
      },
      label = { Text("My Trips", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyPrimary,
        selectedTextColor = NavyPrimary,
        indicatorColor = AmberContainer,
        unselectedIconColor = TextMuted,
        unselectedTextColor = TextMuted
      ),
      modifier = Modifier.testTag("nav_tab_trips")
    )

    // 3. Owner Hub
    NavigationBarItem(
      selected = currentTab == NavTab.OWNER_HUB,
      onClick = { onTabSelected(NavTab.OWNER_HUB) },
      icon = {
        if (pendingRequestsCount > 0) {
          BadgedBox(badge = { Badge(containerColor = AmberAccent) { Text(pendingRequestsCount.toString()) } }) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = "Owner Hub",
              modifier = Modifier.size(24.dp)
            )
          }
        } else {
          Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = "Owner Hub",
            modifier = Modifier.size(24.dp)
          )
        }
      },
      label = { Text("Owner Hub", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = NavyPrimary,
        selectedTextColor = NavyPrimary,
        indicatorColor = AmberContainer,
        unselectedIconColor = TextMuted,
        unselectedTextColor = TextMuted
      ),
      modifier = Modifier.testTag("nav_tab_owner")
    )

    // 4. Admin (Visible if user has admin privileges)
    if (isAdmin) {
      NavigationBarItem(
        selected = currentTab == NavTab.ADMIN,
        onClick = { onTabSelected(NavTab.ADMIN) },
        icon = {
          Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = "Admin Console",
            modifier = Modifier.size(24.dp)
          )
        },
        label = { Text("Admin", fontSize = 12.sp, fontWeight = FontWeight.Medium) },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = NavyPrimary,
          selectedTextColor = NavyPrimary,
          indicatorColor = AmberContainer,
          unselectedIconColor = TextMuted,
          unselectedTextColor = TextMuted
        ),
        modifier = Modifier.testTag("nav_tab_admin")
      )
    }
  }
}
