package com.example.fldr_player

import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Slider
import kotlinx.coroutines.delay
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton





fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%d:%02d".format(minutes, seconds)
}


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
    onClick: () -> Unit,
    onAddToCurrentQueue: () -> Unit,
    onAddToNewQueue: () -> Unit
) {
    var metadata by remember(audioFile.uri) {
        mutableStateOf<TrackMetadata?>(null)
    }

    var menuOpen by remember {
        mutableStateOf(false)
    }

    androidx.compose.runtime.LaunchedEffect(audioFile.uri) {
        viewModel.readMetadata(audioFile) { result ->
            metadata = result
        }
    }

    val artworkBitmap = remember(metadata?.artwork) {
        metadata?.artwork?.let { artwork ->
            BitmapFactory
                .decodeByteArray(
                    artwork,
                    0,
                    artwork.size
                )
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

        IconButton(
            onClick = {
                menuOpen = true
            }
        ) {
            Text("⋮")
        }
    }

    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = {
            menuOpen = false
        }
    ) {
        DropdownMenuItem(
            text = {
                Text("Add to current queue")
            },
            onClick = {
                menuOpen = false
                onAddToCurrentQueue()
            }
        )

        DropdownMenuItem(
            text = {
                Text("Add to new queue")
            },
            onClick = {
                menuOpen = false
                onAddToNewQueue()
            }
        )
    }

    HorizontalDivider()
}

@Composable
fun SongSkeletonRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                )
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
            )

            Spacer(
                modifier = Modifier.height(7.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }

        Spacer(
            modifier = Modifier.size(48.dp)
        )
    }

    HorizontalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
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

    var songMetadata by remember {
        mutableStateOf<Map<String, TrackMetadata>>(emptyMap())
    }

    var songsLoading by remember {
        mutableStateOf(false)
    }

    var metadataLoadedCount by remember {
        mutableStateOf(0)
    }

    var currentPosition by remember { mutableStateOf(0L) }
    var totalDuration by remember { mutableStateOf(0L) }

    val progress =
        if (totalDuration > 0L) {
            (currentPosition.toFloat() / totalDuration.toFloat())
                .coerceIn(0f, 1f)
        } else {
            0f
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

    LaunchedEffect(selectedAudioFile?.uri) {
        while (true) {
            currentPosition = musicPlayer.getCurrentPosition()
            totalDuration = musicPlayer.getDuration()

            delay(500)
        }
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
                songMetadata = emptyMap()
                metadataLoadedCount = 0
                songsLoading = files.isNotEmpty()

                if (files.isEmpty()) {
                    songsLoading = false
                }

                files.forEach { audioFile ->
                    viewModel.readMetadata(audioFile) { metadata ->
                        songMetadata = songMetadata + (
                                audioFile.uri to metadata
                                )
                        metadataLoadedCount += 1

                        if (metadataLoadedCount == files.size) {
                            songsLoading = false
                        }
                    }
                }
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

                Text(
                    text = "Folder View",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 12.dp
                        )
                )

                HorizontalDivider()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    // Parent-folder entry
                    if (
                        currentFolder != null &&
                        selectedFolder != null &&
                        folderHistory.size > 1
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 14.dp
                                    )
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
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "../",
                                    modifier = Modifier.weight(1f)
                                )

                                // Empty space matching the folder menu button
                                Spacer(
                                    modifier = Modifier.size(48.dp)
                                )
                            }

                            HorizontalDivider()
                        }
                    }

                    // Normal folders
                    items(
                        items = musicFolders,
                        key = { it.uri }
                    ) { folder ->

                        var folderMenuOpen by remember(folder.uri) {
                            mutableStateOf(false)
                        }

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
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    folderMenuOpen = true
                                }
                            ) {
                                Text("⋮")
                            }
                        }

                        DropdownMenu(
                            expanded = folderMenuOpen,
                            onDismissRequest = {
                                folderMenuOpen = false
                            }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text("Add to current queue")
                                },
                                onClick = {
                                    folderMenuOpen = false

                                    // Folder queue logic will go here
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text("Add to new queue")
                                },
                                onClick = {
                                    folderMenuOpen = false

                                    // Folder queue logic will go here
                                }
                            )
                        }

                        HorizontalDivider()
                    }

                    // Songs
                    if (songsLoading) {
                        items(
                            count = audioFiles.size
                        ) {
                            SongSkeletonRow()
                        }
                    } else {
                        items(
                            items = audioFiles.sortedWith(
                                compareBy<AudioFile> {
                                    val trackNumber =
                                        songMetadata[it.uri]?.trackNumber

                                    if (
                                        trackNumber != null &&
                                        trackNumber > 0
                                    ) {
                                        0
                                    } else {
                                        1
                                    }
                                }.thenBy {
                                    songMetadata[it.uri]?.trackNumber
                                        ?: Int.MAX_VALUE
                                }
                            ),
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
                                    }

                                    selectedTab = 0
                                },
                                onAddToCurrentQueue = {
                                    // Queue logic will go here
                                },
                                onAddToNewQueue = {
                                    // Queue logic will go here
                                }
                            )
                        }
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
                                    modifier = Modifier
                                        .size(400.dp)
                                        .offset(y = (-38).dp)
                                        .clickable{
                                            musicPlayer.togglePlayPause()
                                        }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.size(48.dp))
                        Text(
                            text = metadata.title ?: "Unknown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = metadata.artist ?: "Unknown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = metadata.album ?: "Unknown",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Spacer(modifier = Modifier.size(28.dp))
                        Row(
                            modifier = Modifier.width(400.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTime(currentPosition),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(42.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Slider(
                                value = progress,
                                onValueChange = { newProgress ->
                                    val newPosition =
                                        (newProgress * totalDuration.toFloat()).toLong()

                                    currentPosition = newPosition
                                    musicPlayer.seekTo(newPosition)
                                },
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                                thumb = {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                },
                                track = { sliderState ->
                                    SliderDefaults.Track(
                                        sliderState = sliderState,
                                        modifier = Modifier.height(4.dp)
                                    )
                                }
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = formatTime(totalDuration),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(42.dp),
                                textAlign = TextAlign.End
                            )
                        }
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

