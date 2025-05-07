package com.example.notes.pages

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.DisplayNote
import com.example.notes.NotesViewModel
import com.example.notes.R
import com.example.notes.data.Note
import com.example.notes.ScreenEmpty
import com.example.notes.SelectionTopBar
import com.example.notes.colorDialog
import com.example.notes.data.colors
import kotlin.collections.minus
import kotlin.collections.plus


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    onClickNote: (Note)->Unit={},
    modifier: Modifier = Modifier,
    notesViewModel: NotesViewModel=viewModel(),
    onHamburgerClick: () -> Unit={}
){
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val deletedNotes=notesViewModel.trash.collectAsState()
    var selectedNotes by rememberSaveable { mutableStateOf(setOf<Note>()) }

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
            if(selectedNotes.isEmpty()) {
                TopApp(
                    onHamburgerClick = onHamburgerClick,
                    scrollBehavior = scrollBehavior,
                    onEmptyTrash = {
                        notesViewModel.emptyTrash()
                    },
                    isEmpty = deletedNotes.value.isEmpty()
                )
            }else{
                SelectionTopBar(
                    isTrash = true,
                    onRestoreClick = {
                        notesViewModel.restoreNotes(selectedNotes)
                        selectedNotes = emptySet()
                    },
                    onEmptyTrash = {
                        notesViewModel.deleteForever(selectedNotes)
                        selectedNotes = emptySet()
                    },
                    size = selectedNotes.size,
                    onCloseClick={
                        selectedNotes=emptySet()
                    },
                )
            }
        }
        ) { innerPadding ->
        if (deletedNotes.value.isEmpty())
            ScreenEmpty(desc=stringResource(R.string.no_notes_in_trash),icon=painterResource(id = R.drawable.delete),modifier = Modifier.padding(innerPadding))
        else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                verticalItemSpacing = 5.dp,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = {
                    items(deletedNotes.value) { note ->
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
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopApp(
    scrollBehavior: TopAppBarScrollBehavior,
    onHamburgerClick: () -> Unit = {},
    onEmptyTrash: () -> Unit = {},
    isEmpty: Boolean=false
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
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
                text = "Trash",
                style = typography.titleMedium
            )
        },
        actions = {
            if(!isEmpty) {
                Box(contentAlignment = Alignment.CenterEnd) {
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(45.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = "More options",
                            modifier = Modifier.size(25.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { isExpanded = false },
                        offset = DpOffset(
                            x = 0.dp,
                            y = (-45).dp
                        )
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = "Empty Trash") },
                            onClick = onEmptyTrash
                        )
                    }
                }
            }
        }
    )
}

