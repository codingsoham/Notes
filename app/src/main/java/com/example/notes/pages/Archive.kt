package com.example.notes.pages

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
fun ArchiveScreen(
    onClickNote: (Note)->Unit={},
    modifier: Modifier = Modifier,
    notesViewModel: NotesViewModel=viewModel(),
    onHamburgerClick: () -> Unit={},
    onSearchClick: () -> Unit,
    shareOrder: (String,String)->Unit={sum,tit->}
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val archives=notesViewModel.archivedNotes.collectAsState()

    var isGrid by rememberSaveable { mutableStateOf(true) }
    var selectedNotes by rememberSaveable { mutableStateOf(setOf<Note>()) }
    var isColorDialogVisible by rememberSaveable { mutableStateOf(false) }
    val isSelectedNotPin=selectedNotes.any{!it.isPin}
    LaunchedEffect(selectedNotes.size){
        if(selectedNotes.isNotEmpty()){
            Log.d("Selected", "Size: ${selectedNotes.size}")
        }
    }
    BackHandler(enabled = selectedNotes.isNotEmpty()) {
        selectedNotes = emptySet()
    }

    Scaffold(
        topBar = {
            if(selectedNotes.isEmpty()){
                TopApp(
                    onHamburgerClick = onHamburgerClick,
                    scrollBehavior = scrollBehavior,
                    onSearchClick = onSearchClick,
                    isGrid=isGrid,
                    onGridClick = {isGrid=!isGrid},
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
                    isNotArchive = false,
                    onColorClick={
                        isColorDialogVisible=!isColorDialogVisible
                    },
                    isMoreThanOne=selectedNotes.size>1,
                    onArchiveClick = {
                        notesViewModel.setArchive(selectedNotes,false)
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
        }
    ) { innerPadding ->
        if (archives.value.isEmpty())
            ScreenEmpty(desc=stringResource(R.string.archive_empty),icon=painterResource(id = R.drawable.archive),modifier = Modifier.padding(innerPadding))
        else {
            if (isGrid) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 5.dp,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    content = {
                        items(archives.value) { note ->
                            DisplayNote(note = note, onClickNote = onClickNote,
                                onSelected = {note->
                                    selectedNotes+=note
                                },
                                onDeselected = {note->
                                    selectedNotes-=note
                                },
                                isSelected = note in selectedNotes,
                                isAnySelected = (selectedNotes.size)>0
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .padding(top = 10.dp, start = 5.dp, end = 10.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(start = 5.dp, end = 10.dp, top = 10.dp)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(archives.value) { note ->
                        DisplayNote(note = note, onClickNote = onClickNote,
                            onSelected = {note->
                                selectedNotes+=note
                            },
                            onDeselected = {note->
                                selectedNotes-=note
                            },
                            isSelected = note in selectedNotes,
                            isAnySelected = (selectedNotes.size)>0
                        )
                    }
                }
            }
        }
        val currentColor = if (selectedNotes.isNotEmpty() &&
            selectedNotes.all { selectedNotes.first().color == it.color }) {
            colors.getColor(selectedNotes.first().color, isSystemInDarkTheme())
        } else {
            Color.Unspecified
        }
        if(isColorDialogVisible)
            colorDialog(onDismissRequest = {isColorDialogVisible=false}, onColorClick = {color->
                notesViewModel.colorNotes(selectedNotes,color)
                selectedNotes=emptySet()
            }, currentColor = currentColor)
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopApp(
    scrollBehavior: TopAppBarScrollBehavior,
    onHamburgerClick: () -> Unit = {},
    onSearchClick:() ->Unit={},
    onGridClick:()->Unit={},
    isGrid: Boolean,
) {
    TopAppBar(
        modifier = Modifier.statusBarsPadding(),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onHamburgerClick,
                modifier = Modifier.size(45.dp)
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
                text = "Archive",
                style = typography.titleMedium
            )
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.search),
                        contentDescription = stringResource(R.string.search),
                        modifier = Modifier.size(25.dp)
                    )
                }
                IconButton(
                    onClick = onGridClick,
                    modifier = Modifier.size(45.dp)
                ) {
                    Icon(
                        painter = if(isGrid) painterResource(R.drawable.splitscreen) else painterResource(R.drawable.grid),
                        contentDescription = stringResource(R.string.grid),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    )
}
