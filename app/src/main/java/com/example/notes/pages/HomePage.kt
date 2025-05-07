package com.example.notes.pages

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.AuthState
import com.example.notes.AuthViewModel
import com.example.notes.DisplayNote
import com.example.notes.NotesViewModel
import com.example.notes.R
import com.example.notes.ScreenEmpty
import com.example.notes.SelectionTopBar
import com.example.notes.colorDialog
import com.example.notes.data.Note
import com.example.notes.data.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    onSignOut: () -> Unit={},
    onAddNote: () -> Unit={},
    onClickNote: (Note)->Unit={},
    onSearchClick: () -> Unit={},
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel= viewModel(),
    notesViewModel: NotesViewModel=viewModel(),
    onHamburgerClick: () -> Unit={},
    shareOrder: (String,String)->Unit={sum,tit->}
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val authState = authViewModel.authState.observeAsState()
    var selectedNotes by rememberSaveable { mutableStateOf(setOf<Note>()) }
    LaunchedEffect(authState.value) {
        if (authState.value == AuthState.Unauthenticated){
            onSignOut()
        }else
            Unit
    }
    LaunchedEffect(selectedNotes.size){
        if(selectedNotes.isNotEmpty()){
            Log.d("Selected", "Size: ${selectedNotes.size}")
        }
    }
    val notes=notesViewModel.notes.collectAsState()
    val disNotes=notes.value.filter{
        !it.isArchived
    }
    val pinNotes =disNotes.filter {
        it.isPin
    }
    val unPinNotes = disNotes.filter {
        !it.isPin
    }
    val isSelectedNotPin=selectedNotes.any{!it.isPin}
    var isGrid by rememberSaveable { mutableStateOf(true) }
    var isColorDialogVisible by rememberSaveable { mutableStateOf(false) }
    var profileSheetVisible by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = selectedNotes.isNotEmpty()) {
        selectedNotes = emptySet()
    }
    BackHandler(enabled = profileSheetVisible) {
        profileSheetVisible=false
    }
        Scaffold(
            topBar = {
                Log.d("Selected", "Size: ${selectedNotes.size}")
                if(selectedNotes.isEmpty()) {
                    TopApp(
                        onSearchClick = onSearchClick,
                        onProfileClick = {profileSheetVisible=!profileSheetVisible},
                        onGridClick = { isGrid = !isGrid },
                        onHamburgerClick = onHamburgerClick,
                        isGrid = isGrid,
                        scrollBehavior = scrollBehavior
                    )
                }else{
                    SelectionTopBar(
                        size=selectedNotes.size,
                        onPinClick = {
                            notesViewModel.pinNotes(selectedNotes,isSelectedNotPin)
                            selectedNotes=emptySet()
                        },
                        onCloseClick={
                            selectedNotes=emptySet()
                        },
                        isSelectedNotPin=isSelectedNotPin,
                        isNotArchive = true,
                        onColorClick={
                            isColorDialogVisible=!isColorDialogVisible
                        },
                        isMoreThanOne=selectedNotes.size>1,
                        onArchiveClick = {
                            notesViewModel.setArchive(selectedNotes,true)
                            selectedNotes=emptySet()
                        },
                        onDeleteClick = {
                            notesViewModel.deleteNotes(selectedNotes)
                            selectedNotes=emptySet()
                        },
                        onCopyClick ={
                            notesViewModel.copyNote(selectedNotes.first())
                            selectedNotes=emptySet()
                        },
                        onSendClick={
                            shareOrder(selectedNotes.first().content,selectedNotes.first().title)
                            selectedNotes=emptySet()
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { onAddNote() },
                    containerColor = MaterialTheme.colorScheme.primary,
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            },


            ) { innerPadding ->
            if (disNotes.isEmpty())
                ScreenEmpty(desc=stringResource(R.string.no_notes_found),icon=painterResource(R.drawable.ic_notes),modifier = Modifier.padding(innerPadding))
            else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (isGrid) {
                        LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(2),
                                verticalItemSpacing = 5.dp,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(top = 10.dp, start = 5.dp, end = 10.dp)
                                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                                    .animateContentSize(),
                            content = {
                                if (pinNotes.isNotEmpty()) {
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        Text(
                                            text = stringResource(R.string.pinned),
                                            style = typography.bodyMedium,
                                            modifier = Modifier.padding(
                                                start = 17.dp,
                                                top = 5.dp,
                                                bottom = 5.dp
                                            )
                                        )
                                    }
                                    items(pinNotes) { note ->
                                        DisplayNote(
                                            note = note, onClickNote = onClickNote,
                                            onSelected = { note ->
                                                selectedNotes += note
                                            },
                                            onDeselected = { note ->
                                                selectedNotes -= note
                                            },
                                            isSelected = note in selectedNotes,
                                            isAnySelected = (selectedNotes.size) > 0
                                        )
                                    }
                                    item(span = StaggeredGridItemSpan.FullLine) {
                                        if (unPinNotes.isNotEmpty()) {
                                            Text(
                                                text = stringResource(R.string.others),
                                                style = typography.bodyMedium,
                                                modifier = Modifier.padding(
                                                    start = 17.dp,
                                                    top = 10.dp,
                                                    bottom = 5.dp
                                                )
                                            )
                                        }
                                    }
                                }
                                items(unPinNotes) { note ->
                                    DisplayNote(
                                        note = note, onClickNote = onClickNote,
                                        onSelected = { note ->
                                            selectedNotes += note
                                        },
                                        onDeselected = { note ->
                                            selectedNotes -= note
                                        },
                                        isSelected = note in selectedNotes,
                                        isAnySelected = (selectedNotes.size) > 0
                                    )
                                }
                            }
                        )
                } else {
                    LazyColumn(
                        state = rememberLazyListState(),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(top = 10.dp, start = 5.dp, end = 10.dp)
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .animateContentSize(),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        if (pinNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.pinned),
                                    style = typography.bodyMedium,
                                    modifier = Modifier.padding(
                                        start = 17.dp,
                                        top = 5.dp,
                                        bottom = 5.dp
                                    )
                                )
                            }

                            items(pinNotes) { note ->
                                DisplayNote(
                                    note = note,
                                    onClickNote = onClickNote,
                                    onSelected = { selectedNotes += it },
                                    onDeselected = { selectedNotes -= it },
                                    isSelected = note in selectedNotes,
                                    isAnySelected = selectedNotes.isNotEmpty()
                                )
                            }

                            if (unPinNotes.isNotEmpty()) {
                                item {
                                    Text(
                                        text = stringResource(R.string.others),
                                        style = typography.bodyMedium,
                                        modifier = Modifier.padding(
                                            start = 17.dp,
                                            top = 10.dp,
                                            bottom = 5.dp
                                        )
                                    )
                                }
                            }
                        }

                        items(unPinNotes) { note ->
                            DisplayNote(
                                note = note,
                                onClickNote = onClickNote,
                                onSelected = { selectedNotes += it },
                                onDeselected = { selectedNotes -= it },
                                isSelected = note in selectedNotes,
                                isAnySelected = selectedNotes.isNotEmpty()
                            )
                        }
                    }

                }
            }
            }
        }

            val currentColor = if (selectedNotes.isNotEmpty() &&
                selectedNotes.all { selectedNotes.first().color == it.color }) {
                colors.getColor(selectedNotes.first().color)
            } else {
                Color.Unspecified
            }
            if(isColorDialogVisible)
                colorDialog(onDismissRequest = {isColorDialogVisible=false}, onColorClick = {color->
                    notesViewModel.colorNotes(selectedNotes,color)
                    selectedNotes=emptySet()
                }, currentColor = currentColor)
            if(profileSheetVisible)
                UserDialog(email=authViewModel.getEmail(),onSignOut={authViewModel.signOut()}, onDismissRequest = {profileSheetVisible=!profileSheetVisible})
        }

@Composable
fun UserDialog(email: String,onSignOut: () -> Unit = {}, onDismissRequest: () -> Unit = {}) {
    // Use Popup instead of Dialog for custom positioning
    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        // Semi-transparent scrim
        Box(contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismissRequest() }
        ) {

            // Dialog content
            Card(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 70.dp).widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    // Prevent clicks from propagating to the scrim
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume click */ }
                    .pointerInput(Unit) {
                        detectTapGestures { /* Consume tap */ }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                        elevation = 5.dp
                    )
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(10.dp)
                        .padding(bottom = 30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = CenterStart) {
                            IconButton(onClick = onDismissRequest) {
                                Icon(
                                    painter = painterResource(id = R.drawable.close),
                                    contentDescription = "Close",
                                )
                            }
                        }
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(25.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 15.dp, horizontal = 15.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.photo),
                                    contentDescription = "Photo",
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                )
                                Column(modifier = Modifier.padding(start = 15.dp)) {
                                    Text(
                                        text = "Master User",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                            Card(
                                colors = CardDefaults.cardColors(Color.Transparent),
                                shape = RoundedCornerShape(7.dp),
                                border = BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .padding(
                                        start = 70.dp,
                                        bottom = 20.dp
                                    )
                                    .clickable(onClick = {})
                            ) {
                                Text(
                                    text = "Manage your Account",
                                    modifier = Modifier.padding(
                                        vertical = 5.dp,
                                        horizontal = 10.dp
                                    ),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                            Divider(
                                modifier = Modifier.height(2.dp),
                                color = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                    elevation = 5.dp
                                )
                            )
                            Row(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        onDismissRequest()
                                        onSignOut()
                                    })
                                    .fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.log_out),
                                    contentDescription = "Log out",
                                    modifier = Modifier.padding(
                                        start = 27.dp,
                                        end = 20.dp,
                                        top = 10.dp,
                                        bottom = 15.dp
                                    )
                                )
                                Text(
                                    text = "Sign Out",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 10.dp, bottom = 15.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopApp(
scrollBehavior: TopAppBarScrollBehavior,
onHamburgerClick: () -> Unit={},
onSearchClick: () -> Unit={},
onProfileClick: () -> Unit={},
onGridClick: () -> Unit={},
isGrid: Boolean
){
    Surface(modifier = Modifier.padding(horizontal = 10.dp)) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .clip(RoundedCornerShape(50.dp))
                .clickable(onClick = onSearchClick),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.5.dp)
            ),
            navigationIcon = {
                IconButton(
                    onClick = onHamburgerClick,
                    modifier = Modifier
                        .size(45.dp)
                        .padding(start = 20.dp)
                        .fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.menu),
                        contentDescription = "Menu",
                        modifier = Modifier.size(25.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Search your notes",
                    style = typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            actions = {
                IconButton(
                    onClick = onGridClick,
                    modifier = Modifier
                        .size(45.dp)
                        .fillMaxSize()
                ) {
                    Icon(
                        painter = if (isGrid) {
                            painterResource(id = R.drawable.splitscreen)
                        } else {
                            painterResource(id = R.drawable.grid)
                        },
                        contentDescription = "Grid",
                        modifier = Modifier.size(25.dp)
                    )
                }
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(45.dp)
                        .fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.photo),
                        contentDescription = "Profile photo",
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                    )
                }
            },
            scrollBehavior = scrollBehavior
        )
    }
}

@Preview
@Composable
fun NoteCardPreview(){
    DisplayNote(
        note = Note(
            id = "1",
            title = "Sample Note",
            content = "This is a sample note content.",
        )
    )
}
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserDialogPreview(){
    UserDialog(email = "")
}
@Preview(showBackground = true)
@Composable
fun HomePagePreview() {
    HomePage()
}
