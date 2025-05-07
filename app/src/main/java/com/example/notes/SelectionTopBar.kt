package com.example.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.notes.data.colors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionTopBar(isTrash: Boolean=false,isNotArchive: Boolean=true,isMoreThanOne: Boolean=false,onColorClick:()->Unit={},isSelectedNotPin: Boolean=true,onCloseClick:()->Unit={},size: Int,onPinClick:()->Unit={},onArchiveClick:()->Unit={},onDeleteClick:()->Unit={},onCopyClick:()->Unit={},onSendClick:()->Unit={},onRestoreClick:()->Unit={},onEmptyTrash:()->Unit={}){
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    TopAppBar(colors= TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
        navigationIcon = {
            IconButton(onClick = onCloseClick) {
                Icon(
                    painter= painterResource(R.drawable.close),
                    contentDescription = "Cl0se"
                )
            }
        },
        title={
            Text(text="$size")
        },
        actions={
            if(isTrash){
                IconButton(onClick = onRestoreClick) {
                    Icon(
                        painter = painterResource(R.drawable.restore),
                        contentDescription = "Restore"
                    )
                }
            }else {
                IconButton(onClick = onPinClick) {
                    Icon(
                        painter = painterResource(if (isSelectedNotPin) R.drawable.keep else R.drawable.keep_fill),
                        contentDescription = "Pin"
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.add_alert),
                        contentDescription = "Reminder"
                    )
                }
                IconButton(onClick = onColorClick) {
                    Icon(
                        painter = painterResource(R.drawable.pallete),
                        contentDescription = "Pallete"
                    )
                }
                IconButton(onClick = {}) {
                    Icon(
                        painter = painterResource(R.drawable.label),
                        contentDescription = "Labels"
                    )
                }
            }
            Box(contentAlignment = Alignment.CenterEnd) {
                IconButton(onClick = {isExpanded=true}) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = "More"
                    )
                }
                DropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    offset = androidx.compose.ui.unit.DpOffset(
                        x = 0.dp,
                        y = (-45).dp
                    )
                ) {
                    if(isTrash){
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.empty_trash)) },
                            onClick = onEmptyTrash
                        )
                    }
                    else {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (!isNotArchive) stringResource(R.string.unarchive) else stringResource(
                                        R.string.archive
                                    )
                                )
                            },
                            onClick = onArchiveClick
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.delete)) },
                            onClick = onDeleteClick
                        )
                        if (!isMoreThanOne) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.make_a_copy)) },
                                onClick = onCopyClick
                            )
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.send)) },
                                onClick = onSendClick
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun colorDialog(onDismissRequest:()->Unit={},currentColor: Color=Color.Unspecified,onColorClick:(Int)->Unit={}){
    Dialog(onDismissRequest = onDismissRequest){
            Card(shape= RoundedCornerShape(20.dp),colors= CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Note Color",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(12.dp)
                    )
                    Column(verticalArrangement = Arrangement.SpaceBetween) {
                        for (i in 0..11 step 4) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                                for (j in i..i + 3) {
                                    CircleColor(
                                        color = colors.getColor(j),
                                        currentColor = currentColor,
                                        onClick = {
                                            onDismissRequest()
                                            onColorClick(j)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable

fun colorDialogPreview(){
    colorDialog(currentColor = Color(0xFF77172E))
}