package com.example.fldr_player

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fldr_player.ui.theme.FLDRplayerTheme
import android.os.Bundle


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FLDRplayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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

    var selectedFolder by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            // Remember permission to read this folder after restarting FLDR.
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            context.contentResolver.takePersistableUriPermission(
                uri,
                takeFlags
            )

            selectedFolder = uri.toString()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("FLDR-player")

        Button(
            onClick = {
                folderPicker.launch(null)
            }
        ) {
            Text("Choose Music Folder")
        }

        if (selectedFolder != null) {
            Text(
                text = "Music folder selected!",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}
