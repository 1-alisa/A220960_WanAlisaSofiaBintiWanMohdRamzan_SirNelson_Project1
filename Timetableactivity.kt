package com.example.a220960_sirnelson_lab01

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import viewmodel.TimetableViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(navController: NavController, viewModel: TimetableViewModel = viewModel()) {
    val currentId by viewModel.currentProfileId
    val profile = viewModel.profiles[currentId]

    val presetColors = listOf(
        Color.White, Color(0xFFBBDEFB), Color(0xFFC8E6C9),
        Color(0xFFFFECB3), Color(0xFFF8BBD0), Color(0xFFE1BEE7), Color(0xFFFFCDD2)
    )

    var showEditDialog by remember { mutableStateOf(false) }
    var dialogType by remember { mutableStateOf("") }
    var selectedDayIdx by remember { mutableIntStateOf(0) }
    var selectedSlotIdx by remember { mutableIntStateOf(0) }

    var input1 by remember { mutableStateOf("") }
    var input2 by remember { mutableStateOf("") }
    var input3 by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(presetColors[0]) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("TIMETABLE", color = Color(0xFFF8B72C), fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    if (profile != null) {
                        IconButton(onClick = { viewModel.deleteProfile(currentId) }) {
                            Icon(Icons.Default.Delete, "Delete Profile", tint = Color.Red)
                        }
                    }
                    IconButton(onClick = {
                        input1 = ""; dialogType = "ADD PROFILE"; showEditDialog = true
                    }) { Icon(Icons.Default.Add, null, tint = Color(0xFFF8B72C)) }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF0A1A3A))
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1A3A), Color.Black)))) {

            if (profile == null) {
                Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(100.dp), tint = Color.White.copy(0.1f))
                    Text("CLICK + TO ADD THE TIMETABLE", color = Color.White.copy(0.4f))
                }
            } else {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())) {

                    // Profile Switcher
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.profiles.keys.forEach { id ->
                            FilterChip(
                                selected = currentId == id,
                                onClick = { viewModel.currentProfileId.value = id },
                                label = { Text(id) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF8B72C), selectedLabelColor = Color.Black, labelColor = Color.White)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Header Info
                    Card(modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            input1 = profile.studentName; input2 = profile.className; input3 = profile.schoolName
                            dialogType = "PROFILE INFO"; showEditDialog = true
                        },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f)),
                        border = BorderStroke(1.dp, Color(0xFFF8B72C).copy(0.5f))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("STUDENT NAME: ${profile.studentName}", color = Color.White, fontWeight = FontWeight.Bold)
                            Text("CLASS: ${profile.className} | ${profile.schoolName}", color = Color(0xFFF8B72C), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // --- TABLE GRID ---
                    Column(Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFF8B72C), RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)) {

                        // Row Masa (Header)
                        Row(Modifier.fillMaxWidth().background(Color(0xFF2C3E50))) {
                            Box(Modifier.weight(1.5f).padding(8.dp), contentAlignment = Alignment.Center) {
                                Text("DAYS", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            profile.timeSlots.forEachIndexed { i, time ->
                                Box(Modifier
                                    .weight(1f)
                                    .border(0.5.dp, Color.White.copy(0.1f))
                                    .clickable { selectedSlotIdx = i; input1 = time; dialogType = "TIME"; showEditDialog = true }
                                    .padding(vertical = 8.dp, horizontal = 2.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text(
                                        text = time,
                                        color = Color(0xFFF8B72C),
                                        fontSize = 8.sp, // Saiz kecil sedikit untuk muat
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,      // PAKSA SEBARIS
                                        softWrap = false   // JANGAN BAGI TURUN BAWAH
                                    )
                                }
                            }
                        }

                        // Row Hari & Subjek
                        val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                        profile.weeklySchedule.forEachIndexed { dIdx, row ->
                            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                                Box(Modifier
                                    .weight(1.5f)
                                    .fillMaxHeight()
                                    .background(Color(0xFFF8B72C))
                                    .border(0.5.dp, Color.Black.copy(0.1f))
                                    .padding(4.dp),
                                    contentAlignment = Alignment.Center) {
                                    Text(days[dIdx], fontWeight = FontWeight.Bold, fontSize = 9.sp, color = Color.Black)
                                }
                                row.forEachIndexed { sIdx, (sub, col) ->
                                    Box(Modifier
                                        .weight(1f)
                                        .fillMaxHeight() // Menggantikan aspectRatio supaya fleksibel
                                        .background(Color(col))
                                        .border(0.5.dp, Color.Black.copy(0.05f))
                                        .clickable {
                                            selectedDayIdx = dIdx; selectedSlotIdx = sIdx; input1 = sub; selectedColor = Color(col); dialogType = "SUBJECT"; showEditDialog = true
                                        }
                                        .padding(2.dp),
                                        contentAlignment = Alignment.Center) {
                                        Text(
                                            text = sub,
                                            fontSize = 8.sp,
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis // Letak "..." jika terlalu panjang
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(dialogType) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = input1,
                        onValueChange = { input1 = it },
                        label = { Text(if(dialogType=="ADD PROFILE") "Profile Name" else "Input") }
                    )
                    if (dialogType == "PROFILE INFO") {
                        OutlinedTextField(value = input2, onValueChange = { input2 = it }, label = { Text("CLASS") })
                        OutlinedTextField(value = input3, onValueChange = { input3 = it }, label = { Text("SCHOOL") })
                    }
                    if (dialogType == "SUBJECT") {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            presetColors.forEach { color ->
                                Box(Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(if (selectedColor == color) 2.dp else 0.dp, Color.Black, CircleShape)
                                    .clickable { selectedColor = color })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    if (dialogType == "SUBJECT") {
                        TextButton(onClick = { viewModel.clearSubject(currentId, selectedDayIdx, selectedSlotIdx); showEditDialog = false }) {
                            Text("DELETE", color = Color.Red)
                        }
                    }
                    Button(onClick = {
                        when (dialogType) {
                            "SUBJECT" -> viewModel.updateSubject(currentId, selectedDayIdx, selectedSlotIdx, input1, selectedColor.toArgb())
                            "TIME" -> viewModel.updateTimeSlot(currentId, selectedSlotIdx, input1)
                            "PROFILE INFO" -> viewModel.updateProfileInfo(currentId, input1, input2, input3)
                            "ADD PROFILE" -> if(input1.isNotBlank()) viewModel.addNewProfile(input1)
                        }
                        showEditDialog = false
                    }) { Text("SAVE") }
                }
            }
        )
    }
}