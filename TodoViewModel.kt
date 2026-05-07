package viewmodel

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class UserViewModel : ViewModel() {
    // Simpan nama kat sini. MainScreen & SettingsScreen akan baca dari sini.
    var userName by mutableStateOf("")
}

// --- DATA MODELS ---

data class BetterNote(
    val id: Long = System.currentTimeMillis(),
    val title: String,
    val content: String,
    val date: String,
    val color: Color
)

data class FullQuizQuestion(
    val id: Long = System.currentTimeMillis(),
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)

data class TimetableItem(
    val id: Long = System.currentTimeMillis(),
    val subject: String,
    val time: String
)

// --- VIEWMODELS ---

class TodoViewModel : ViewModel() {
    var taskList = mutableStateListOf<Pair<String, Boolean>>()
        private set
    var newTaskText by mutableStateOf(TextFieldValue(""))
    var editingIndex by mutableIntStateOf(-1)

    fun addOrEditTask() {
        if (newTaskText.text.isNotBlank()) {
            if (editingIndex == -1) {
                taskList.add(newTaskText.text to false)
            } else {
                taskList[editingIndex] = newTaskText.text to taskList[editingIndex].second
                editingIndex = -1
            }
            newTaskText = TextFieldValue("")
        }
    }
    fun toggleTask(index: Int) {
        val (task, isDone) = taskList[index]
        taskList[index] = task to !isDone
    }
    fun editTask(index: Int) {
        newTaskText = TextFieldValue(taskList[index].first)
        editingIndex = index
    }
    fun deleteTask(index: Int) {
        if (index in taskList.indices) {
            taskList.removeAt(index)
            if (editingIndex == index) editingIndex = -1
        }
    }
}

class NotesViewModel : ViewModel() {
    val notesList = mutableStateListOf<BetterNote>()
    fun addNote(note: BetterNote) { notesList.add(0, note) }
    fun updateNote(id: Long, updatedNote: BetterNote) {
        val index = notesList.indexOfFirst { it.id == id }
        if (index != -1) notesList[index] = updatedNote
    }
    fun removeNote(note: BetterNote) { notesList.remove(note) }
}

class QuizViewModel : ViewModel() {
    val questions = mutableStateListOf<FullQuizQuestion>()
    fun addQuestion(q: FullQuizQuestion) { questions.add(q) }
    fun updateQuestion(index: Int, q: FullQuizQuestion) {
        if (index in questions.indices) questions[index] = q
    }
    fun deleteQuestion(index: Int) {
        if (index in questions.indices) questions.removeAt(index)
    }
}

class TimerViewModel : ViewModel() {
    var timeLeftMs by mutableLongStateOf(25 * 60 * 1000L)
    var isRunning by mutableStateOf(false)
    var isStudyMode by mutableStateOf(true)

    private val STUDY_MS = 25 * 60 * 1000L
    private val BREAK_MS = 5 * 60 * 1000L

    fun tick() {
        if (isRunning && timeLeftMs > 0) {
            timeLeftMs -= 1000L
        } else if (timeLeftMs <= 0L) {
            isRunning = false
        }
    }

    fun toggleRunning() {
        isRunning = !isRunning
    }

    fun resetTimer() {
        isRunning = false
        timeLeftMs = if (isStudyMode) STUDY_MS else BREAK_MS
    }

    // PASTIKAN FUNGSI INI ADA UNTUK SELESAIKAN ERROR ANDA
    fun changeMode(isStudy: Boolean) {
        if (isStudyMode != isStudy) {
            isStudyMode = isStudy
            isRunning = false
            timeLeftMs = if (isStudy) STUDY_MS else BREAK_MS
        }
    }
}

// --- TIMETABLE VIEWMODEL (DIBETULKAN) ---






data class TimetableProfile(
    val studentName: String = "STUDENT NAME",
    val className: String = "CLASS",
    val schoolName: String = "SCHOOL",
    val timeSlots: List<String> = List(10) { (it + 1).toString() },
    val weeklySchedule: List<List<Pair<String, Int>>> = List(5) { List(10) { "" to Color.White.toArgb() } }
)

class TimetableViewModel : ViewModel() {
    // Simpanan data semua profil
    val profiles = mutableStateMapOf<String, TimetableProfile>()

    // ID profil aktif
    var currentProfileId = mutableStateOf("")

    fun updateProfileInfo(id: String, name: String, clazz: String, school: String) {
        profiles[id] = profiles[id]?.copy(studentName = name, className = clazz, schoolName = school) ?: TimetableProfile()
    }

    fun updateTimeSlot(id: String, index: Int, newTime: String) {
        val p = profiles[id] ?: return
        val newTimes = p.timeSlots.toMutableList()
        newTimes[index] = newTime
        profiles[id] = p.copy(timeSlots = newTimes)
    }

    fun updateSubject(id: String, dayIdx: Int, slotIdx: Int, newSub: String, color: Int) {
        val p = profiles[id] ?: return
        val newSched = p.weeklySchedule.map { it.toMutableList() }
        newSched[dayIdx][slotIdx] = newSub to color
        profiles[id] = p.copy(weeklySchedule = newSched)
    }

    fun addNewProfile(id: String) {
        if (!profiles.containsKey(id)) {
            profiles[id] = TimetableProfile(studentName = id.uppercase())
        }
        currentProfileId.value = id
    }

    fun deleteProfile(id: String) {
        profiles.remove(id)
        if (currentProfileId.value == id) {
            currentProfileId.value = if (profiles.isNotEmpty()) profiles.keys.first() else ""
        }
    }

    fun clearSubject(id: String, dayIdx: Int, slotIdx: Int) {
        updateSubject(id, dayIdx, slotIdx, "", Color.White.toArgb())
    }
}

