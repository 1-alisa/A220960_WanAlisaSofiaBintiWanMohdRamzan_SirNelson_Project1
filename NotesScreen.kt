package com.example.a220960_sirnelson_lab01

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.a220960_sirnelson_lab01.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.Brush

// DATA MODEL
data class BetterNote(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val date: String,
    val color: Color
)

// VIEWMODEL (pegang Data supaya xhilang)
class NotesViewModel : ViewModel() {
    // Data disimpan di sini supaya tak hilang bila rotate/navigate
    val notesList = mutableStateListOf<BetterNote>()

    fun addNote(note: BetterNote) {
        notesList.add(0, note)
    }

    fun updateNote(id: Long, updatedNote: BetterNote) {
        val index = notesList.indexOfFirst { it.id == id }
        if (index != -1) {
            notesList[index] = updatedNote
        }
    }

    fun deleteNote(note: BetterNote) {
        notesList.remove(note)
    }
}

//  MAIN SCREEN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    navController: NavController,
    viewModel: NotesViewModel = viewModel()
) {
    val noteColors = listOf(
        Color(0xFFFFB7B2), Color(0xFFFFDAC1), Color(0xFFE2F0CB),
        Color(0xFFB5EAD7), Color(0xFFC7CEEA), Color(0xFFFDCFED)
    )

    var isInputVisible by rememberSaveable { mutableStateOf(false) }
    var noteTitle by rememberSaveable { mutableStateOf("") }
    var noteContent by rememberSaveable { mutableStateOf("") }
    var editingNoteId by rememberSaveable { mutableLongStateOf(-1L) }
    var selectedColorInt by rememberSaveable { mutableIntStateOf(noteColors[0].toArgb()) }
    val selectedColor = Color(selectedColorInt)

    // background color
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A1A3A), Color.Black)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("MY NOTES", color = Color(0xFFF8B72C), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                // Jadikan TopAppBar lutsinar supaya gradient nampak penuh
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFFF8B72C)
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    isInputVisible = !isInputVisible
                    if (!isInputVisible) {
                        noteTitle = ""
                        noteContent = ""
                        editingNoteId = -1L
                    }
                },
                containerColor = Color(0xFFF8B72C),
                contentColor = Color.Black
            ) {
                Icon(if (isInputVisible) Icons.Default.Close else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isInputVisible) "CLOSE" else "NEW NOTE")
            }
        },
        containerColor = Color.Transparent // Penting: Biar scaffold nampak background Box
    ) { paddingValues ->
        // Box utama untuk memegang gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                AnimatedVisibility(visible = isInputVisible) {
                    BetterInputPanel(
                        title = noteTitle,
                        content = noteContent,
                        selectedColor = selectedColor,
                        colors = noteColors,
                        isEditing = editingNoteId != -1L,
                        onTitleChange = { noteTitle = it },
                        onContentChange = { noteContent = it },
                        onColorChange = { selectedColorInt = it.toArgb() },
                        onSave = {
                            if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                if (editingNoteId == -1L) {
                                    viewModel.addNote(
                                        BetterNote(
                                            title = noteTitle,
                                            content = noteContent,
                                            date = sdf.format(Date()),
                                            color = selectedColor
                                        )
                                    )
                                } else {
                                    viewModel.updateNote(
                                        editingNoteId,
                                        BetterNote(
                                            id = editingNoteId,
                                            title = noteTitle,
                                            content = noteContent,
                                            color = selectedColor,
                                            date = "Edited: ${sdf.format(Date())}"
                                        )
                                    )
                                }
                                noteTitle = ""
                                noteContent = ""
                                editingNoteId = -1L
                                isInputVisible = false
                            }
                        }
                    )
                }

                if (viewModel.notesList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notes. Tap + to start!", color = Color.Gray)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 100.dp, start = 8.dp, end = 8.dp, top = 8.dp)
                    ) {
                        items(viewModel.notesList, key = { it.id }) { note ->
                            StickyNoteCard(
                                note = note,
                                onDelete = { viewModel.deleteNote(note) },
                                onEdit = {
                                    noteTitle = note.title
                                    noteContent = note.content
                                    selectedColorInt = note.color.toArgb()
                                    editingNoteId = note.id
                                    isInputVisible = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// STICKY NOTE COMPONENT
@Composable
fun StickyNoteCard(note: BetterNote, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = note.color)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(note.title, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Row {
                    Icon(Icons.Default.Edit, null, tint = Color.Black.copy(0.3f), modifier = Modifier.size(16.dp).clickable { onEdit() })
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.DeleteOutline, null, tint = Color.Black.copy(0.3f), modifier = Modifier.size(16.dp).clickable { onDelete() })
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(note.content, color = Color.DarkGray, fontSize = 13.sp, maxLines = 4)
            Spacer(Modifier.height(10.dp))
            Text(note.date, color = Color.Black.copy(0.3f), fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
        }
    }
}

// INPUT PANEL COMPONENT
@Composable
fun BetterInputPanel(
    title: String, content: String, selectedColor: Color, colors: List<Color>,
    isEditing: Boolean,
    onTitleChange: (String) -> Unit, onContentChange: (String) -> Unit,
    onColorChange: (Color) -> Unit, onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (isEditing) "Edit Note" else "New Note", color = Color.White, fontSize = 12.sp)
            TextField(
                value = title, onValueChange = onTitleChange,
                placeholder = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            TextField(
                value = content, onValueChange = onContentChange,
                placeholder = { Text("Note content...") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier.padding(end = 6.dp).size(25.dp).clip(CircleShape).background(color)
                            .clickable { onColorChange(color) }
                            .border(if (selectedColor == color) 2.dp else 0.dp, Color.White, CircleShape)
                    )
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = onSave, colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) Color.White else Color(0xFFF8B72C))) {
                    Text(if (isEditing) "UPDATE" else "SAVE", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    AppTheme {
        val mockNavController = androidx.navigation.compose.rememberNavController()
        NotesScreen(navController = mockNavController)
    }
}