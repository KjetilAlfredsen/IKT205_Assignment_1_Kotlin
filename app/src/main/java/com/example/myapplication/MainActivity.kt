package com.example.myapplication

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument


data class Note(
    val id: Int,
    val title: String,
    val body: String

)

class NotesViewModel : ViewModel() {
    private val _notes = mutableStateListOf<Note>()
    private var idCounter = 0
    val notes: List<Note>
        get() = _notes

    fun newNote(title: String, body: String){
        val createdNote = Note(
            id = idCounter,
            title = title,
            body = body
        )
        _notes.add(createdNote)
        idCounter++
    }

    fun fetchNote(id:Int): Note? {
        return _notes.find{it.id == id}
    }
}

@Composable
fun CreateNoteScreen(
    onSaveClick: (String, String) -> Unit,
    onReturnClick: () -> Unit
){
    var title by remember { mutableStateOf("")}
    var content by remember {mutableStateOf("")}

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = content,
                onValueChange = { content = it},
                label = { Text("Note content")},
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    )

            Button(
                onClick = {onSaveClick(title, content)},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Note")
            }
        } }
}

@Composable
fun NoteContentScreen(
    noteId: Int,
    viewModel: NotesViewModel,
    onReturnClick: () -> Unit
) {
    val note = viewModel.fetchNote(noteId)

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(10.dp)
                .fillMaxWidth()
        ) {
            if (note != null) {
                Text(
                    text = note.title,
                    style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = note.body)

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onReturnClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Return")
                }
            } else {
                Text("Note not found")

                Button(onClick = onReturnClick)
                {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: NotesViewModel,
               onCreateNoteClick: () -> Unit,
               onNoteClick: (Int) -> Unit,
               modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNoteClick,
                content = { Text("Add New Note")
                } )
        } )
    {innerpadding ->
        LazyColumn(contentPadding = innerpadding) {
            items(viewModel.notes) { note ->
                ListItem(
                    headlineContent = { Text(note.title) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNoteClick(note.id) }
                )
            }
        }
    }}

class MainActivity : ComponentActivity() {
    private val viewModel = NotesViewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val viewModel = viewModel

                NavHost(navController = navController, startDestination = "main"){

                    composable("main") {
                        MainScreen(
                            viewModel = viewModel,
                            onCreateNoteClick = { navController.navigate("create") },
                            onNoteClick = { id -> navController.navigate("read/$id")},
                        )
                    }

                    composable("create"){
                        CreateNoteScreen(
                            onSaveClick = {title, body ->
                                viewModel.newNote(title, body)
                                navController.popBackStack()
                            },
                            onReturnClick = { navController.popBackStack()}
                        )
                    }

                    composable(route = "read/{noteId}",
                        arguments = listOf(navArgument("noteId") { type = NavType.IntType})
                    ) {backStackEntry ->
                        val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
                        NoteContentScreen(
                            noteId = noteId,
                            viewModel = viewModel,
                            onReturnClick = {navController.popBackStack()}
                        )
                    }

                    }
                }
            }
        }
    }