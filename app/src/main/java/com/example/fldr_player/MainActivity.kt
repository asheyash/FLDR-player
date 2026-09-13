package com.example.fldr_player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fldr_player.ui.theme.FLDRplayerTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FLDRplayerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    FLDRHome(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun FLDRHome(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val viewModel: FLDRViewModel = viewModel()


    var selectedFolder by remember {
        mutableStateOf<String?>(null)
    }

    var audioFiles by remember {
        mutableStateOf<List<AudioFile>>(emptyList())
    }

    var selectedMetadata by remember {
        mutableStateOf<TrackMetadata?>(null)
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->

        if (uri != null) {

            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            context.contentResolver.takePersistableUriPermission(
                uri,
                takeFlags
            )

            selectedFolder = uri.toString()

            viewModel.scanFolder(uri.toString()) { files ->
                audioFiles = files
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Button(
            onClick = {
                folderPicker.launch(null)
            },
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Choose Music Folder")
        }

        Text(
            text = "${audioFiles.size} audio files found",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            items(audioFiles) { audioFile ->

                Button(
                    onClick = {
                        viewModel.readMetadata(audioFile) { metadata ->
                            selectedMetadata = metadata
                        }
                    }
                ) {
                    Text(audioFile.name)
                }
            }
        }
        selectedMetadata?.let { metadata ->
            Text("Title: ${metadata.title ?: "Unknown"}")
            Text("Artist: ${metadata.artist ?: "Unknown"}")
            Text("Album: ${metadata.album ?: "Unknown"}")
            Text("Album Artist: ${metadata.albumArtist ?: "Unknown"}")
            Text("Track: ${metadata.trackNumber ?: "Unknown"}")
            Text("Disc: ${metadata.discNumber ?: "Unknown"}")
            Text("Year: ${metadata.year ?: "Unknown"}")
            Text("Genre: ${metadata.genre ?: "Unknown"}")
        }
    }
}

