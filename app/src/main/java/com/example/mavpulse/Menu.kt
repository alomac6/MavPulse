package com.example.mavpulse

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    username: String?,
    onMenuClick: () -> Unit, 
    onHomeClick: () -> Unit, 
    onProfileClick: () -> Unit, 
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                IconButton(onClick = onHomeClick) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f), // 10% height
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0064B1), // UTA Blue
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(48.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                if (username != null) {
                    Box(
                        contentAlignment = Alignment.Center, 
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Black) // Set background to black when logged in
                    ) {
                        Text(text = username.first().uppercase(), fontSize = 24.sp, color = Color.White)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = "User Profile",
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun MavPulseDrawer(
    drawerState: DrawerState,
    isUserLoggedIn: Boolean,
    onEventsClick: () -> Unit,
    onLoginClick: () -> Unit,
    onFavoriteNotesClick: () -> Unit,
    onCoursesClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val scope = rememberCoroutineScope()

    IconButton(
        onClick = { scope.launch { drawerState.close() } },
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Menu",
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.6f)
            .padding(start = 32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Events", fontSize = 22.sp, modifier = Modifier
            .padding(vertical = 52.dp)
            .clickable { onEventsClick() })
        Text("Calendar", fontSize = 22.sp, modifier = Modifier.padding(vertical = 52.dp))
        Text("Class Notes", fontSize = 22.sp, modifier = Modifier.padding(vertical = 52.dp).clickable { onCoursesClick() })

        if (isUserLoggedIn) {
            Text("Favorite Notes", fontSize = 22.sp, modifier = Modifier.padding(vertical = 52.dp).clickable { onFavoriteNotesClick() })
            Text("Logout", fontSize = 22.sp, modifier = Modifier
                .padding(vertical = 52.dp)
                .clickable { onLogoutClick() })
        } else {
            Text("Login", fontSize = 22.sp, modifier = Modifier
                .padding(vertical = 52.dp)
                .clickable { onLoginClick() })
        }
    }
}
