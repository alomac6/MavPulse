package com.example.mavpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mavpulse.ui.theme.MavPulseTheme
import com.example.mavpulse.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MavPulseTheme {
                MavPulseApp()
            }
        }
    }
}

// A simple sealed class for page titles, NOT for state saving.
sealed class Page(val title: String) {
    object Home : Page("Home")
    object Events : Page("Events")
    object Login : Page("Login")
    object Register : Page("Register")
    object Departments : Page("Departments")
    object Courses : Page("Courses")
}

@PreviewScreenSizes
@Composable
fun MavPulseApp(authViewModel: AuthViewModel = viewModel()) {
    var currentPageTitle by rememberSaveable { mutableStateOf(Page.Home.title) }
    var currentDepartmentName by rememberSaveable { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxSize()) {
                MavPulseDrawer(
                    drawerState = drawerState,
                    onEventsClick = {
                        currentPageTitle = Page.Events.title
                        scope.launch { drawerState.close() }
                    },
                    onLoginClick = {
                        currentPageTitle = Page.Login.title
                        scope.launch { drawerState.close() }
                    },
                    onCoursesClick = {
                        currentPageTitle = Page.Departments.title
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onHomeClick = { currentPageTitle = Page.Home.title },
                    onProfileClick = { currentPageTitle = Page.Login.title }
                )
            }
        ) { innerPadding ->
            when (currentPageTitle) {
                Page.Home.title -> MainPageContent(modifier = Modifier.padding(innerPadding))
                Page.Events.title -> EventsPage(modifier = Modifier.padding(innerPadding))
                Page.Login.title -> LoginPage(
                    modifier = Modifier.padding(innerPadding),
                    onRegisterClick = { currentPageTitle = Page.Register.title },
                    onLoginSuccess = { currentPageTitle = Page.Home.title },
                    authViewModel = authViewModel
                )
                Page.Register.title -> RegisterPage(
                    modifier = Modifier.padding(innerPadding),
                    onRegisterSuccess = { currentPageTitle = Page.Login.title },
                    onBackToLogin = { currentPageTitle = Page.Login.title },
                    authViewModel = authViewModel
                )
                Page.Departments.title -> DepartmentsPage(
                    modifier = Modifier.padding(innerPadding),
                    onDepartmentClick = { departmentName ->
                        currentDepartmentName = departmentName
                        currentPageTitle = Page.Courses.title
                    }
                )
                Page.Courses.title -> CoursesPage(
                    modifier = Modifier.padding(innerPadding),
                    departmentName = currentDepartmentName ?: ""
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainPageContent(modifier: Modifier = Modifier) {
    val items = listOf(
        "Slide 1" to "This is the first slide's description.",
        "Slide 2" to "A description for the second slide.",
        "Slide 3" to "And finally, the third slide."
    )
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(60.dp)) // Pushes content down

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                scope.launch {
                    if (pagerState.currentPage > 0) {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Slide")
            }

            ImageSlider(
                modifier = Modifier
                    .weight(1f)
                    .height(250.dp), // Fixed height
                items = items,
                pagerState = pagerState
            )

            IconButton(onClick = {
                scope.launch {
                    if (pagerState.currentPage < items.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Slide")
            }
        }

        Spacer(Modifier.height(100.dp))

        EventCalendar()
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MavPulseTheme {
        MavPulseApp()
    }
}
