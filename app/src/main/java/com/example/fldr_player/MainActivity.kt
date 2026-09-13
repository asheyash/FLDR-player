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
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.style.TextAlign
import android.provider.DocumentsContract
import androidx.core.net.toUri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect







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
fun SongRow(
    audioFile: AudioFile,
    viewModel: FLDRViewModel,
    onClick: () -> Unit
) {
    var metadata by remember(audioFile.uri) {
        mutableStateOf<TrackMetadata?>(null)
    }

    androidx.compose.runtime.LaunchedEffect(audioFile.uri) {
        viewModel.readMetadata(audioFile) { result ->
            metadata = result
        }
    }

    val artworkBitmap = remember(metadata?.artwork) {
        metadata?.artwork?.let { artwork ->
            BitmapFactory
                .decodeByteArray(artwork, 0, artwork.size)
                ?.asImageBitmap()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (artworkBitmap != null) {
            Image(
                bitmap = artworkBitmap,
                contentDescription = "Album artwork",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♫",
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = metadata?.title
                    ?: audioFile.name,
                maxLines = 1
            )

            Text(
                text = metadata?.artist
                    ?: "Unknown artist",
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = metadata?.album
                    ?: "Unknown album",
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    HorizontalDivider()
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

    var selectedAudioFile by remember {
        mutableStateOf<AudioFile?>(null)
    }

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var currentFolder by remember { mutableStateOf<String?>(null) }
    var musicFolders by remember { mutableStateOf<List<MusicFolder>>(emptyList()) }

    var folderHistory by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var folderLoadId by remember { mutableStateOf(0) }

    val musicPlayer = remember {
        MusicPlayer(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            musicPlayer.release()
        }
    }

    fun loadFolder(
        uri: String,
        addToHistory: Boolean = true
    ) {
        folderLoadId += 1

        val thisLoadId = folderLoadId

        if (addToHistory) {
            folderHistory = folderHistory + uri
        }

        currentFolder = uri

        // Clear the old folder immediately
        musicFolders = emptyList()
        audioFiles = emptyList()

        viewModel.scanFolders(uri) { folders ->
            if (thisLoadId == folderLoadId) {
                musicFolders = folders
            }
        }

        viewModel.scanSongs(uri) { files ->
            if (thisLoadId == folderLoadId) {
                audioFiles = files
            }
        }
    }

    fun getParentFolder(uriString: String): String? {
        val uri = uriString.toUri()

        val treeDocumentId = DocumentsContract.getTreeDocumentId(uri)
        val parentDocumentId = treeDocumentId.substringBeforeLast('/')

        if (parentDocumentId == treeDocumentId) {
            return null
        }

        return DocumentsContract.buildTreeDocumentUri(
            uri.authority,
            parentDocumentId
        ).toString()
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
            folderHistory = emptyList()
            loadFolder(
                uri.toString(),
                addToHistory = true
            )

            selectedTab = 1
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {

            if (selectedTab == 1) {
                if (currentFolder != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Folder View",
                            modifier = Modifier.padding(end = 12.dp)
                        )

                        Text(
                            text = "Current Folder"
                        )
                    }

                    HorizontalDivider()
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    if (
                        currentFolder != null &&
                        selectedFolder != null &&
                        folderHistory.size > 1
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (folderHistory.size > 1) {
                                            val newHistory = folderHistory.dropLast(1)
                                            val previousFolder = newHistory.last()

                                            folderHistory = newHistory

                                            loadFolder(
                                                uri = previousFolder,
                                                addToHistory = false
                                            )
                                        }
                                    }
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 14.dp
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "../",
                                )
                            }
                            HorizontalDivider()
                        }
                    }

                    items(
                        items = musicFolders,
                        key = { it.uri }
                    ) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    loadFolder(folder.uri)
                                }
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 14.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = folder.name,
                            )
                        }

                        HorizontalDivider()
                    }

                    items(
                        items = audioFiles,
                        key = { it.uri }
                    ) { audioFile ->
                        SongRow(
                            audioFile = audioFile,
                            viewModel = viewModel,
                            onClick = {
                                selectedAudioFile = audioFile

                                musicPlayer.play(audioFile.uri)

                                viewModel.readMetadata(audioFile) { metadata ->
                                    selectedMetadata = metadata
                                    selectedTab = 0
                                }
                            }
                        )
                    }
                }
            }

            if (selectedTab == 2) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Music Folder")

                    Button(
                        onClick = {
                            folderPicker.launch(null)
                        },
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("Choose Music Folder")
                    }
                    Text(
                        text = if (selectedFolder == null) {
                            "No folder selected"
                        } else {
                            "Music folder selected"
                        }
                    )
                }
            }
            if (selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    selectedMetadata?.let { metadata ->

                        metadata.artwork?.let { artwork ->
                            val bitmap = remember(artwork) {
                                BitmapFactory.decodeByteArray(
                                    artwork,
                                    0,
                                    artwork.size
                                )?.asImageBitmap()
                            }
                            bitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = "Album artwork",
                                    modifier = Modifier.size(400.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(20.dp))
                        Text(
                            text = metadata.title ?: "Unknown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = metadata.artist ?: "Unknown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = metadata.album ?: "Unknown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        //Text("Title: ${metadata.title ?: "Unknown"}")
                        //Text("Artist: ${metadata.artist ?: "Unknown"}")
                        //Text("Album: ${metadata.album ?: "Unknown"}")
                        //Text("Album Artist: ${metadata.albumArtist ?: "Unknown"}")
                        //Text("Track: ${metadata.trackNumber ?: "Unknown"}")
                        //Text("Disc: ${metadata.discNumber ?: "Unknown"}")
                        //Text("Year: ${metadata.year ?: "Unknown"}")
                        //Text("Genre: ${metadata.genre ?: "Unknown"}")
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    selectedTab = 0
                }
            ) {
                Text("Play")
            }

            Button(
                onClick = {
                    if (selectedTab == 1 && selectedFolder != null) {
                        folderHistory = listOf(selectedFolder!!)

                        loadFolder(
                            uri = selectedFolder!!,
                            addToHistory = false
                        )
                    } else {
                        selectedTab = 1
                    }
                }
            ) {
                Text("Lib")
            }
            Button(
                onClick = {
                    selectedTab = 2
                }
            ) {
                Text("Set")
            }
        }

    }
}

