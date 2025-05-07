package com.example.notes.pages

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.DisplayNote
import com.example.notes.NotesViewModel
import com.example.notes.R
import com.example.notes.SelectionTopBar
import com.example.notes.colorDialog
import com.example.notes.data.Note
import com.example.notes.data.colors
import com.example.notes.utils.searchNotes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchNotes(onClickNote: (Note) -> Unit,onBackClick: () -> Unit,notesViewModel: NotesViewModel=viewModel(),shareOrder: (String,String)->Unit={sum,tit->}){

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchString by rememberSaveable { mutableStateOf("") }
    val notes=notesViewModel.notes.collectAsState()
    // Process search terms for highlighting
    val searchTerms = if (searchString.isNotEmpty()) {
        searchString.trim().split(Regex("\\s+"))
    } else {
        emptyList()
    }

    // Get search results using the improved algorithm
    val searchResult = if (searchString.isNotEmpty()) {
        searchNotes(notes.value, searchString)
    } else {
        emptyList()
    }

    val disNotes=searchResult.filter{!it.isArchived}
    val archives=searchResult.filter{it.isArchived}
    var selectedNotes by rememberSaveable { mutableStateOf(setOf<Note>()) }
    var isColorDialogVisible by rememberSaveable { mutableStateOf(false) }
    val isSelectedNotPin=selectedNotes.any{!it.isPin}
    val isSelectedNotArchive=selectedNotes.any{!it.isArchived}
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
                SearchTopBar(
                    onValueChange = { it -> searchString = it },
                    searchString = searchString,
                    onBackClick = onBackClick,
                    onCrossClick = { searchString = "" },
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
                    isNotArchive=isSelectedNotArchive,
                    isSelectedNotPin=isSelectedNotPin,
                    onColorClick={
                        isColorDialogVisible=!isColorDialogVisible
                    },
                    isMoreThanOne=selectedNotes.size>1,
                    onArchiveClick = {
                        notesViewModel.setArchive(selectedNotes,isSelectedNotArchive)
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
    ) {innerPadding->
        if(searchString.isNotEmpty()) {
            if (searchResult.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)){
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                                    .nestedScroll(scrollBehavior.nestedScrollConnection).padding(top=10.dp,start=5.dp,end=5.dp),
                        verticalItemSpacing = 10.dp,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(disNotes) { note ->
                            DisplayNote(
                                note = note, onClickNote = onClickNote,
                                onSelected = { note ->
                                    selectedNotes += note
                                },
                                onDeselected = { note ->
                                    selectedNotes -= note
                                },
                                isSelected = note in selectedNotes,
                                isAnySelected = (selectedNotes.size) > 0,
                                searchTerms = searchTerms
                            )
                        }

                        if (archives.isNotEmpty()) {
                            item(span = StaggeredGridItemSpan.FullLine) {
                                Text(
                                    text = stringResource(R.string.archived),
                                    style = typography.titleMedium,
                                    modifier = Modifier.padding(start = 17.dp, top = 10.dp)
                                )
                            }
                            items(archives) { note ->
                                DisplayNote(
                                    note = note, onClickNote = onClickNote,
                                    onSelected = { note ->
                                        selectedNotes += note
                                    },
                                    onDeselected = { note ->
                                        selectedNotes -= note
                                    },
                                    isSelected = note in selectedNotes,
                                    isAnySelected = (selectedNotes.size) > 0,
                                    searchTerms = searchTerms,
                                )
                            }
                        }
                    }
                }
            } else
                EmptySearch(icon=painterResource(R.drawable.not_found),text=stringResource(R.string.no_notes_found),modifier = Modifier.padding(innerPadding))
        }else {
            EmptySearch(icon=painterResource(R.drawable.search), text=stringResource(R.string.search_to_find_notes), modifier = Modifier.padding(innerPadding))
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

    }
}
@Composable
fun EmptySearch(icon: Painter,text: String,modifier: Modifier=Modifier){
    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .size(150.dp)
                .padding(20.dp)
        )
        Text(
            text = text,
            style = typography.titleMedium,
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(scrollBehavior: TopAppBarScrollBehavior,onCrossClick: () -> Unit,onValueChange: (String) -> Unit, searchString: String, onBackClick: () -> Unit) {
    Surface(
        color=MaterialTheme.colorScheme.surfaceColorAtElevation(1.5.dp)
    ) {
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                    1.5.dp
                )
            ),
            navigationIcon = {
                IconButton(onClick = onBackClick, modifier = Modifier.size(50.dp)) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button),
                        modifier = Modifier.size(25.dp)
                    )
                }
            },
            title = {
                var focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
                TextField(
                    placeholder = { Text(stringResource(R.string.search_your_notes)) },
                    onValueChange = { onValueChange(it) },
                    value = searchString,
                    trailingIcon = {
                        if (searchString.isNotEmpty()) {
                            IconButton(
                                onClick = onCrossClick
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.clear_search),
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                )
            },
            scrollBehavior = scrollBehavior
        )
    }
}

