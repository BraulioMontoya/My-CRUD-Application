package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.Students
import com.example.myapplication.data.Users
import com.example.myapplication.model.Degree
import com.example.myapplication.model.Role
import com.example.myapplication.model.Student
import com.example.myapplication.model.User
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                App()
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun App() {
    val navController = rememberNavController()

    val users = Users().apply {
        insertUser("Admin", "Admin1234", Role.ADMIN)
        insertUser("Editor", "Editor1234", Role.EDITOR)
        insertUser("Consultor", "Consultor1234", Role.CONSULTOR)
    }

    val students = remember {
        Students().apply {
            insertStudent("Juan Campos", Degree.LITC, "8-A")
            insertStudent("Pedro Luna", Degree.LITC, "8-A")
            insertStudent("Francisco Lopez", Degree.LITC, "8-A")
            insertStudent("Jesus Vazquez", Degree.LITC, "8-A")
        }
    }

    var currentUser by remember { mutableStateOf<User?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    NavHost(
        navController = navController,
        startDestination = "Login"
    ) {
        composable("Login") {
            LoginApp(snackbarHostState) { username, password ->
                val user = users.consultUser(username, password)

                if (user != null) {
                    currentUser = user
                    navController.navigate("Profile")
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Invalid username or password",
                            withDismissAction = true
                        )
                    }
                }
            }
        }

        composable("Profile") {
            currentUser?.let { user ->
                ProfileApp(
                    user = user,
                    onLogout = {
                        currentUser = null
                        navController.navigate("Login") {
                            popUpTo("Login") { inclusive = true }
                        }
                    },
                    students = students
                )
            }
        }
    }
}

@Composable
fun LoginApp(
    snackbarHostState: SnackbarHostState,
    onClick: (String, String) -> Unit
) {
    Scaffold(
        topBar = { TopBar() },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LoginScreen(
            Modifier.padding(innerPadding),
            onClick
        )
    }
}

@Composable
fun ProfileApp(
    user: User,
    onLogout: () -> Unit,
    students: Students
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }

    if (showDialog) {
        ProfileDialog(
            student = selectedStudent,
            onDismiss = {
                showDialog = false
                selectedStudent = null
            },
            onConfirm = { name, degree, group ->
                if (selectedStudent == null) {
                    students.insertStudent(name, degree, group)
                } else {
                    students.updateStudent(selectedStudent!!.id, name, degree, group)
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge
                )

                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    icon = { Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Logout")
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(onOpenDrawer = {
                    scope.launch { drawerState.open() }
                })
            },
            floatingActionButton = {
                if (user.role == Role.ADMIN) {
                    FloatingActionButton(onClick = {
                        selectedStudent = null
                        showDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Student")
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn (Modifier.padding(innerPadding)) {
                items(students.consultStudents()) { student ->
                    ProfileCard(
                        user.role,
                        student,
                        onEdit = {
                            selectedStudent = it
                            showDialog = true
                        },
                        onDelete = { students.deleteStudent(it.id) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(onOpenDrawer: (() -> Unit)? = null) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = "My Application",
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                )

                Spacer(Modifier.width(8.dp))

                Text(text = "My Application")
            }
        },
        navigationIcon = {
            if (onOpenDrawer != null) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            }
        }
    )
}
