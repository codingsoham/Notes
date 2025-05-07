package com.example.notes

import android.R.style.Animation
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.data.Note
import com.example.notes.pages.ArchiveScreen
import com.example.notes.pages.ForgotScreen
import com.example.notes.pages.GetStarted
import com.example.notes.pages.HomePage
import com.example.notes.pages.LoginScreen
import com.example.notes.pages.NotesView
import com.example.notes.pages.SearchNotes
import com.example.notes.pages.SignUpScreen
import com.example.notes.pages.TrashScreen
import com.google.gson.Gson
import kotlinx.coroutines.launch

enum class NotesAppScreen(){
    GetStarted,
    Login,
    SignUp,
    ForgotPassword,
    Home,
    Notes,
    Search,
    Reminders,
    Label,
    Archive,
    Trash,
    Settings
}
@Composable
fun NotesAppScreen(
    viewModel: AuthViewModel= viewModel(),
    navController: NavHostController= rememberNavController(),
    modifier: Modifier = Modifier
) {
    val authState= viewModel.authState.observeAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryFlow.collectAsStateWithLifecycle(
        initialValue = null
    )

    var selectedState by rememberSaveable {
        mutableStateOf(NotesAppScreen.Home.name)
    }

    // Update selected state whenever destination changes
    LaunchedEffect(currentBackStackEntry) {
        currentBackStackEntry?.destination?.route?.let { route ->
            // Handle parametrized routes
            val baseRoute = route.split("/")[0].split("?")[0]
            selectedState = baseRoute
        }
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent={
            ModalDrawerSheet(modifier= Modifier.width(320.dp), drawerContainerColor = MaterialTheme.colorScheme.surfaceDim) {
                Column(modifier = Modifier.statusBarsPadding().padding(horizontal = 10.dp)
                    .verticalScroll(rememberScrollState())
                ){
                    Text("Notes",modifier = Modifier.padding(16.dp), style = typography.titleLarge)
                    val containerColor=MaterialTheme.colorScheme.primaryContainer
                    NavigationDrawerItem(
                        label = { Text("Notes",style=typography.bodyLarge) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.light),
                                contentDescription = "Notes",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        selected = selectedState==NotesAppScreen.Home.name,
                        onClick = {
                            selectedState=NotesAppScreen.Home.name
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(NotesAppScreen.Home.name){
                                popUpTo(NotesAppScreen.Home.name) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = containerColor)
                    )
                    NavigationDrawerItem(
                        label={ Text("Reminders",style=typography.bodyLarge) },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.not),
                                contentDescription = "Reminders",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        selected = selectedState== NotesAppScreen.Reminders.name,
                        onClick = {
                            selectedState=NotesAppScreen.Reminders.name
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = containerColor)
                    )
                    NavigationDrawerItem(
                        label={ Text("Create new label",style=typography.bodyLarge)},
                        icon={
                            Icon(
                                painter = painterResource(id = R.drawable.add),
                                contentDescription = "Archive",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        selected =selectedState==NotesAppScreen.Label.name,
                        onClick={
                            selectedState=NotesAppScreen.Label.name
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = containerColor)
                    )
                    NavigationDrawerItem(
                        label={ Text("Archive",style=typography.bodyLarge)},
                        icon={
                            Icon(
                                painter = painterResource(id = R.drawable.archive),
                                contentDescription = "Archive",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        selected =selectedState==NotesAppScreen.Archive.name,
                        onClick={
                            selectedState=NotesAppScreen.Archive.name
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(NotesAppScreen.Archive.name){
                                popUpTo(NotesAppScreen.Home.name) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = containerColor)
                    )
                    NavigationDrawerItem(
                        label={ Text("Trash",style=typography.bodyLarge)},
                        icon={
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Archive",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        selected =selectedState==NotesAppScreen.Trash.name,
                        onClick={
                            selectedState=NotesAppScreen.Trash.name
                            scope.launch {
                                drawerState.close()
                            }
                            navController.navigate(NotesAppScreen.Trash.name){
                                popUpTo(NotesAppScreen.Home.name) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = containerColor)
                    )
                    NavigationDrawerItem(
                        label = { Text("Settings",style=typography.bodyLarge) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        selected = selectedState==NotesAppScreen.Settings.name,
                        onClick = {
                            selectedState=NotesAppScreen.Settings.name
                            scope.launch {
                                drawerState.close()
                            }

                        },
                        colors = NavigationDrawerItemDefaults.colors(selectedContainerColor = containerColor)
                    )

                }
            }
        },

        ) {
        NavHost(
            navController = navController,
            startDestination = if (authState.value == AuthState.Authenticated) NotesAppScreen.Home.name else NotesAppScreen.GetStarted.name
        ) {
            composable(
                route = NotesAppScreen.GetStarted.name,
            ) {
                GetStarted(
                    onGetStartedClick = {
                        navController.navigate(NotesAppScreen.Login.name)
                    }
                )
            }
            composable(
                route = NotesAppScreen.Login.name,
            ) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(NotesAppScreen.Home.name) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onSignUpClick = {
                        navController.navigate(NotesAppScreen.SignUp.name)
                    },
                    onForgotClick = {
                        navController.navigate(NotesAppScreen.ForgotPassword.name)
                    }
                )
            }
            composable(
                route = NotesAppScreen.ForgotPassword.name,
            ) {
                ForgotScreen(
                    onResetSuccess = {
                        navController.navigateUp()
                    }
                )
            }
            composable(
                route = NotesAppScreen.SignUp.name,
            ) {
                SignUpScreen(
                    onAccountCreated = {
                        navController.navigate(NotesAppScreen.Home.name) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onSignInClick = {
                        navController.navigateUp()
                    }
                )
            }
            composable(route = NotesAppScreen.Home.name) {
                val context=LocalContext.current
                HomePage(
                    onSignOut = {
                        navController.navigate(NotesAppScreen.Login.name) {
                            popUpTo(0) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onAddNote = {
                        navController.navigate("${NotesAppScreen.Notes.name}/new")
                    },
                    onClickNote = { note ->
                        // Convert note to JSON and use it as a navigation parameter
                        val noteJson = Gson().toJson(note)
                        navController.navigate("${NotesAppScreen.Notes.name}?noteJson=$noteJson")
                    },
                    onSearchClick = {
                        navController.navigate(NotesAppScreen.Search.name)
                    },
                    onHamburgerClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    shareOrder={sum,title->
                        shareOrder(context,summary=sum, subject = title)
                    },
                )
            }
            composable(
                route = "${NotesAppScreen.Notes.name}/new",
            ) {// For new notes, we don't pass any note object

                val context=LocalContext.current
                NotesView(note = null, onBackClick = { navController.navigateUp() },
                    shareOrder={sum,title->
                        shareOrder(context,summary=sum, subject = title)
                    }
                )
            }
            composable(
                route = "${NotesAppScreen.Notes.name}?noteJson={noteJson}&isTrash={isTrash}",
                arguments = listOf(
                    navArgument("noteJson") {
                        type = NavType.StringType
                        nullable = false
                    },
                    navArgument("isTrash") {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                backStackEntry ->
                val context=LocalContext.current
                val noteJson = backStackEntry.arguments?.getString("noteJson")
                val isTrash = backStackEntry.arguments?.getBoolean("isTrash") ?: false
                val note = Gson().fromJson(noteJson, Note::class.java)
                NotesView(
                    note = note,
                    isTrash = isTrash,
                    onBackClick = { navController.navigateUp()},
                    shareOrder={sum,title->
                        shareOrder(context,summary=sum, subject = title)
                    }
                )
            }
            composable(route = NotesAppScreen.Search.name) {
                val context=LocalContext.current
                SearchNotes(
                    onBackClick = { navController.navigateUp() },
                    onClickNote = { note ->
                        // Convert note to JSON and use it as a navigation parameter
                        val noteJson = Gson().toJson(note)
                        navController.navigate("${NotesAppScreen.Notes.name}?noteJson=$noteJson")
                    },
                    shareOrder={sum,title->
                        shareOrder(context,summary=sum, subject = title)
                    }
                )
            }
            composable(route = NotesAppScreen.Trash.name){
                TrashScreen(
                    onHamburgerClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    onClickNote = { note ->
                        // Convert note to JSON and use it as a navigation parameter
                        val noteJson = Gson().toJson(note)
                        navController.navigate("${NotesAppScreen.Notes.name}?noteJson=$noteJson&isTrash=true")
                    },
                )
            }
            composable(route = NotesAppScreen.Archive.name) {
                val context=LocalContext.current
                ArchiveScreen(
                    onClickNote = { note ->
                        // Convert note to JSON and use it as a navigation parameter
                        val noteJson = Gson().toJson(note)
                        navController.navigate("${NotesAppScreen.Notes.name}?noteJson=$noteJson")
                    },
                    onHamburgerClick = {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    },
                    onSearchClick = {
                        navController.navigate(NotesAppScreen.Search.name)
                    },
                    shareOrder={sum,title->
                        shareOrder(context,summary=sum, subject = title)
                    }
                )
            }
        }
    }

}