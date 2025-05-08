package com.example.notes.pages

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notes.NotesViewModel
import com.example.notes.R
import com.example.notes.data.Note
import com.example.notes.data.colors
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesView(
    noteViewModel: NotesViewModel= viewModel(),
    onBackClick: () -> Unit={},
    note: Note?=null,
    modifier: Modifier = Modifier,
    shareOrder:(String,String)->Unit={st1,st2->},
    isTrash: Boolean=false,
) {
    val noteUiState by noteViewModel.uiState.collectAsState()
    LaunchedEffect(note) {
        if (note != null) {
            noteViewModel.initializeWithNote(note)
        }
    }
    LaunchedEffect(Unit) {
        Log.d("NotesView", "Composable initialized with noteId: ${noteUiState.id}")
    }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    val colorSheetState = rememberModalBottomSheetState()
    var showColorSheet by rememberSaveable { mutableStateOf(false) }
    val currentColor= colors.getColor(noteUiState.color, darkTheme = isSystemInDarkTheme())

    val scrollState = rememberScrollState()

    // Add state to track content height

    // Calculate blended color
    val blendedColor = if (currentColor == Color.Transparent) {
        val scrolledRatio = if (scrollState.maxValue == 0) 0f
        else (scrollState.value.toFloat() / scrollState.maxValue).coerceIn(0f, 1f)
        lerp(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainer, scrolledRatio)
    } else currentColor

    Scaffold(containerColor = currentColor,
        topBar = {
            Box(
                modifier = Modifier.background(blendedColor)
            ) {
                TopBar(
                    isArchive = noteUiState.isArchived,
                    isPin = noteUiState.isPin,
                    onBackClick = onBackClick,
                    isTrash = isTrash,
                    onPinClick = { noteViewModel.setPin() },
                    onArchiveClick = { noteViewModel.setArchive() })
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier.background(blendedColor).imePadding()
            ) {
                BottomBar(
                    note = noteUiState,
                    onClickMore = {
                        showBottomSheet = !showBottomSheet
                    },
                    onColorClick = {
                        showColorSheet = !showColorSheet
                    },
                    isTrash = isTrash
                )
            }
        }
    ) {innerPadding ->
        Column(modifier=Modifier
            .padding(innerPadding).verticalScroll(scrollState)
            .padding(20.dp)){
            ThemedBasicTextField(
                value = noteUiState.title,
                onValueChange = {
                    noteViewModel.setTitle(it)
                },
                placeholder = stringResource(R.string.title),
                textStyle = typography.headlineSmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                isTrash=isTrash
            )

            ThemedBasicTextField(
                value = noteUiState.content,
                onValueChange = {
                    noteViewModel.setContent(it)
                    scope.launch {
                        scrollState.animateScrollTo(scrollState.maxValue)
                    }
                },
                placeholder = stringResource(R.string.note),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                textStyle = typography.bodyLarge,
                isTrash = isTrash
            )
        }
        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState,
                dragHandle = null,
                containerColor = if(currentColor==Color.Transparent) MaterialTheme.colorScheme.surfaceContainer else currentColor
            ) {
                Column(modifier=Modifier.fillMaxWidth()){
                    if(isTrash){
                        ActionItem(
                            label = stringResource(R.string.restore),
                            onClick = {
                                showBottomSheet = false
                                onBackClick()
                                noteViewModel.restoreNote(noteUiState)
                            },
                            icon = painterResource(R.drawable.restore),
                        )
                        ActionItem(
                            label = stringResource(R.string.delete_forever),
                            onClick = {
                                showBottomSheet = false
                                onBackClick()
                                noteViewModel.deleteTrash(noteUiState.id)
                            },
                            icon = painterResource(R.drawable.delete_forever)
                        )
                    }else {
                        ActionItem(
                            label = stringResource(R.string.delete),
                            onClick = {
                                showBottomSheet = false
                                onBackClick()
                                noteViewModel.deleteNote(noteUiState)
                            },
                            icon = painterResource(R.drawable.delete),
                        )
                        ActionItem(
                            label = stringResource(R.string.make_a_copy),
                            onClick = {
                                noteViewModel.copyNote(noteUiState)
                                showBottomSheet = false
                            },
                            icon = painterResource(R.drawable.copy)
                        )
                        ActionItem(
                            label = stringResource(R.string.send),
                            onClick = {shareOrder(noteUiState.content,noteUiState.title)},
                            icon = painterResource(R.drawable.share),
                        )
                        ActionItem(
                            label = stringResource(R.string.labels),
                            onClick = { /* Do something... */ },
                            icon = painterResource(R.drawable.label)
                        )
                    }
                }
            }
        }

        if (showColorSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showColorSheet = false
                },
                sheetState = colorSheetState,
                dragHandle = null,
                containerColor = if(currentColor==Color.Transparent) MaterialTheme.colorScheme.surfaceContainer else currentColor
            ) {
                var darkTheme=isSystemInDarkTheme()
                Column(modifier=Modifier.fillMaxWidth().padding(vertical = 20.dp)){
                    Text(
                        text="Color",
                        style=typography.bodySmall,
                        modifier= Modifier.padding(start = 20.dp)
                    )
                    LazyRow(modifier= Modifier.fillMaxWidth().padding(top=10.dp)){
                        items(colors.noteColors(darkTheme = darkTheme)){item->
                            CircleColor(color=item, currentColor=currentColor, onClick = {
                                noteViewModel.setColor(colors.setColor(item, darkTheme = darkTheme))
                            })
                        }
                    }
                }
            }
        }

    }
}
@Composable
fun CircleColor(onClick: () -> Unit={}, color: Color=Color.Transparent, currentColor: Color=Color.Transparent){
    Box(
        modifier = Modifier.padding(10.dp)
            .size(45.dp)
            .background(color, RoundedCornerShape(100))
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .border(
                width = if (currentColor == color) 2.dp else 1.dp,
                color = if (currentColor == color) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(100)
            )
        ,
        contentAlignment = Alignment.Center
    ) {
        if (currentColor == color) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = "Check",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (color == Color.Transparent) {
            Icon(
                painter = painterResource(R.drawable.color_reset),
                contentDescription = "Color Reset",
                modifier = Modifier.size(30.dp),
            )
        }
    }
}
@Composable
fun ActionItem(
    label: String,
    onClick: () -> Unit,
    icon: Painter,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = typography.bodyLarge
        )
    }
}
@Composable
fun ThemedBasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle = TextStyle.Default,
    modifier: Modifier = Modifier,
    isTrash: Boolean=false,
) {
    val themeTextStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.onSurface
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = themeTextStyle,
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier,
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
            }
        },
        enabled = !isTrash
    )
}
@Composable
fun TopBar(isArchive: Boolean=false,isPin: Boolean=false,onBackClick: () -> Unit = {},isTrash: Boolean=false,onArchiveClick: () -> Unit = {},onPinClick: ()->Unit={}) {
    Row(modifier=Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 10.dp)){
        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.weight(0.5f)) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back_button)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.weight(0.5f)){
            if(!isTrash) {
                IconButton(onClick = onPinClick) {
                    Icon(
                        painter = if(isPin) painterResource(R.drawable.keep_fill) else painterResource(R.drawable.keep),
                        contentDescription = stringResource(R.string.pin)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.add_alert),
                        contentDescription = stringResource(R.string.add_alert)
                    )
                }
                IconButton(
                    onClick = {
                        onArchiveClick()
                        onBackClick()
                    }
                ) {
                    Icon(
                        painter = if(isArchive) painterResource(R.drawable.unarchive) else painterResource(R.drawable.archive),
                        contentDescription = stringResource(R.string.archive)
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomBar(onColorClick: ()->Unit={},note: Note,onClickMore: ()->Unit={},isTrash: Boolean=false) {
    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding(),contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            IconButton(onClick = onColorClick, enabled = !isTrash) {
                Icon(painter = painterResource(R.drawable.pallete), contentDescription = "Color")
            }
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Edited ${formatTimestamp(note.timestamp)}",
                style = typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(onClick = { onClickMore() }) {
                Icon(
                    painter = painterResource(id = R.drawable.more_vert),
                    contentDescription = "More"
                )
            }
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Timestamp): String {
    val now = Timestamp.now()
    val diffMillis = now.seconds * 1000 - timestamp.seconds * 1000
    val diffMinutes = diffMillis / (60 * 1000)
    val diffHours = diffMillis / (60 * 60 * 1000)
    val diffDays = diffMillis / (24 * 60 * 60 * 1000)

    return when {
        diffMinutes < 1 -> "Just now"
        diffMinutes < 60 -> "$diffMinutes mins ago"
        diffHours < 24 -> "${diffHours}h ago"
        diffDays < 7 -> "${diffDays}d ago"
        else -> {
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            formatter.format(timestamp.toDate())
        }
    }
}
@Preview(showBackground = true)
@Composable
fun NotesViewPreview() {
    //NotesView()
    CircleColor(color=Color.Red, currentColor = Color.Red)
}
