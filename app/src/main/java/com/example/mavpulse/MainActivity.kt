package com.example.mavpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mavpulse.ui.theme.MavPulseTheme
import com.example.mavpulse.viewmodels.AuthViewModel
import com.example.mavpulse.viewmodels.CourseViewModel
import com.example.mavpulse.viewmodels.DepartmentViewModel
import com.example.mavpulse.viewmodels.NotesViewModel
import com.example.mavpulse.viewmodels.ProfileViewModel
import com.example.mavpulse.viewmodels.RoomsViewModel
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
    object NotesRoomsChoice : Page("NotesRoomsChoice")
    object FavoriteNotes : Page("FavoriteNotes")
    object Notes : Page("Notes")
    object NoteViewer : Page("NoteViewer")
    object Rooms : Page("Rooms")
    object Profile : Page("Profile")
}

@PreviewScreenSizes
@Composable
fun MavPulseApp(
    authViewModel: AuthViewModel = viewModel(),
    departmentViewModel: DepartmentViewModel = viewModel(),
    courseViewModel: CourseViewModel = viewModel(),
    notesViewModel: NotesViewModel = viewModel(),
    roomsViewModel: RoomsViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    var currentPageTitle by rememberSaveable { mutableStateOf(Page.Home.title) }
    var currentDepartmentName by rememberSaveable { mutableStateOf<String?>(null) }
    var currentCourseName by rememberSaveable { mutableStateOf<String?>(null) }
    var currentCourseNumber by rememberSaveable { mutableStateOf<String?>(null) }
    var current_course_name by rememberSaveable { mutableStateOf<String?>(null) }
    var current_course_id by rememberSaveable { mutableStateOf<String?>(null) }

    var selectedNotePath by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedNoteTitle by rememberSaveable { mutableStateOf<String?>(null) }

    val loggedInUsername by authViewModel.loggedInUsername
    val userId by authViewModel.userId
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxSize()) {
                MavPulseDrawer(
                    drawerState = drawerState,
                    isUserLoggedIn = loggedInUsername != null,
                    onEventsClick = {
                        currentPageTitle = Page.Events.title
                        scope.launch { drawerState.close() }
                    },
                    onLoginClick = {
                        currentPageTitle = Page.Login.title
                        scope.launch { drawerState.close() }
                    },
                    onFavoriteNotesClick = {
                        currentPageTitle = Page.FavoriteNotes.title
                        scope.launch { drawerState.close() }
                    },
                    onCoursesClick = {
                        currentPageTitle = Page.Departments.title
                        scope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        authViewModel.logout()
                        currentPageTitle = Page.Home.title // Navigate to home after logout
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (currentPageTitle != Page.NoteViewer.title) {
                    AppTopBar(
                        username = loggedInUsername,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onHomeClick = { currentPageTitle = Page.Home.title },
                        onProfileClick = { 
                            if (loggedInUsername != null) {
                                currentPageTitle = Page.Profile.title
                            } else {
                                currentPageTitle = Page.Login.title
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentPageTitle) {
                    Page.Home.title -> MainPageContent(modifier = Modifier.fillMaxSize())
                    Page.Events.title -> EventsPage(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = { currentPageTitle = Page.Home.title }
                    )
                    Page.Login.title -> LoginPage(
                        modifier = Modifier.fillMaxSize(),
                        onRegisterClick = { currentPageTitle = Page.Register.title },
                        onLoginSuccess = { currentPageTitle = Page.Home.title },
                        onBackClick = { currentPageTitle = Page.Home.title },
                        authViewModel = authViewModel
                    )
                    Page.Register.title -> RegisterPage(
                        modifier = Modifier.fillMaxSize(),
                        onRegisterSuccess = { currentPageTitle = Page.Login.title },
                        onBackToLogin = { currentPageTitle = Page.Login.title },
                        authViewModel = authViewModel
                    )
                    Page.Departments.title -> DepartmentsPage(
                        modifier = Modifier.fillMaxSize(),
                        onDepartmentClick = { departmentName ->
                            currentDepartmentName = departmentName.trim()
                            currentPageTitle = Page.Courses.title
                        },
                        onBackClick = { currentPageTitle = Page.Home.title },
                        departmentViewModel = departmentViewModel
                    )
                    Page.Courses.title -> CoursesPage(
                        modifier = Modifier.fillMaxSize(),
                        departmentName = currentDepartmentName ?: "",
                        onCourseClick = { course ->
                            currentCourseName = course.name
                            currentCourseNumber = course.number
                            current_course_name = course.backendName
                            current_course_id = course.course_id
                            currentPageTitle = Page.NotesRoomsChoice.title
                        },
                        onBackClick = { currentPageTitle = Page.Departments.title },
                        courseViewModel = courseViewModel
                    )
                    Page.NotesRoomsChoice.title -> NotesRoomsChoicePage(
                        modifier = Modifier.fillMaxSize(),
                        onNotesClick = { currentPageTitle = Page.Notes.title },
                        onRoomsClick = { currentPageTitle = Page.Rooms.title },
                        onBackClick = { currentPageTitle = Page.Courses.title }
                    )
                    Page.FavoriteNotes.title -> {
                        // TODO: Implement Favorite Notes Page
                        Column(modifier = Modifier.fillMaxSize()) {
                            Text("Favorite Notes Page")
                        }
                    }
                    Page.Notes.title -> NotesPage(
                        modifier = Modifier.fillMaxSize(),
                        course_name = current_course_name ?: "",
                        userId = userId ?: "",
                        onBackClick = { currentPageTitle = Page.NotesRoomsChoice.title },
                        onNoteClick = { note ->
                            selectedNotePath = note.filePath
                            selectedNoteTitle = note.title
                            currentPageTitle = Page.NoteViewer.title
                        },
                        notesViewModel = notesViewModel
                    )
                    Page.NoteViewer.title -> NoteViewerPage(
                        modifier = Modifier.fillMaxSize(),
                        fileUrl = selectedNotePath ?: "",
                        title = selectedNoteTitle ?: "",
                        onClose = { currentPageTitle = Page.Notes.title },
                        notesViewModel = notesViewModel
                    )
                    Page.Rooms.title -> RoomsPage(
                        modifier = Modifier.fillMaxSize(),
                        course_name = current_course_name ?: "",
                        course_id = current_course_id ?: "",
                        creator_id = userId ?: "",
                        username = loggedInUsername ?: "",
                        onRoomClick = { /* TODO */ },
                        onBackClick = { currentPageTitle = Page.NotesRoomsChoice.title },
                        roomsViewModel = roomsViewModel
                    )
                    Page.Profile.title -> ProfilePage(
                        modifier = Modifier.fillMaxSize(),
                        userId = userId ?: "",
                        username = loggedInUsername ?: "",
                        onNoteClick = { note ->
                            when(note) {
                                is FavoriteNoteDetail -> {
                                    selectedNotePath = note.filePath
                                    selectedNoteTitle = note.title
                                    currentPageTitle = Page.NoteViewer.title
                                }
                                is UserNote -> {
                                    selectedNotePath = note.filePath
                                    selectedNoteTitle = note.title
                                    currentPageTitle = Page.NoteViewer.title
                                }
                            }
                        },
                        profileViewModel = profileViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppTopBar(
    username: String?,
    onMenuClick: () -> Unit,
    onHomeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", modifier = Modifier.size(36.dp))
        }

        IconButton(onClick = onHomeClick) {
            Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(36.dp))
        }

        IconButton(onClick = onProfileClick) {
            if (username != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0064B1)) // Royal Blue
                        .border(BorderStroke(2.dp, Color(0xFFF58025)), shape = CircleShape), // Orange
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.first().uppercase(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(36.dp))
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
