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
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.Icon
import androidx.media3.common.Player
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject



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


private const val PREFS_NAME = "fldr_preferences"
private const val SAVED_QUEUE = "saved_queue"
private const val MUSIC_FOLDER_URI = "music_folder_uri"
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FLDRHome(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val viewModel: FLDRViewModel = viewModel()
    val queueSongs by viewModel.queueSongs.collectAsState()

//    var selectedFolder by remember {
//        mutableStateOf<String?>(null)
//    }

    var audioFiles by remember {
        mutableStateOf<List<AudioFile>>(emptyList())
    }

    var selectedMetadata by remember {
        mutableStateOf<TrackMetadata?>(null)
    }

    var selectedAudioFile by remember {
        mutableStateOf<AudioFile?>(null)
    }

    val artworkOffset = remember {
        Animatable(0f)
    }
    var artworkIncomingOffset by remember {
        mutableStateOf(300f)
    }

    var artworkSwipeDirection by remember {
        mutableStateOf(0)
    }

    var artworkDragAmount by remember {
        mutableStateOf(0f)
    }

    var selectedTab by remember {
        mutableStateOf(0)
    }

    var isPlaying by remember { mutableStateOf(false) }

    var repeatMode by remember {
        mutableStateOf(Player.REPEAT_MODE_ALL)
    }

    var artworkWaitingForNewSong by remember {
        mutableStateOf(false)
    }

    val artworkScope = rememberCoroutineScope()

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
    var lastQueueIndex by remember { mutableStateOf(-1) }

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

    val preferences = remember {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    fun saveQueue(audioFiles: List<AudioFile>) {
        val jsonArray = JSONArray()

        audioFiles.forEach { audioFile ->
            val jsonObject = JSONObject()

            jsonObject.put("name", audioFile.name)
            jsonObject.put("uri", audioFile.uri)

            jsonArray.put(jsonObject)
        }

        preferences.edit()
            .putString(SAVED_QUEUE, jsonArray.toString())
            .apply()
    }

    fun restoreQueue(): List<AudioFile> {
        val savedQueue = preferences.getString(
            SAVED_QUEUE,
            null
        ) ?: return emptyList()

        val jsonArray = JSONArray(savedQueue)
        val audioFiles = mutableListOf<AudioFile>()

        for (index in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(index)

            audioFiles.add(
                AudioFile(
                    name = jsonObject.getString("name"),
                    uri = jsonObject.getString("uri")
                )
            )
        }

        return audioFiles
    }
    LaunchedEffect(Unit) {
        val savedQueue = restoreQueue()

        if (savedQueue.isNotEmpty()) {
            viewModel.restoreQueue(savedQueue)
        }
    }

    var selectedFolder by remember {
        mutableStateOf(
            preferences.getString(MUSIC_FOLDER_URI, null)
        )
    }

    LaunchedEffect(queueSongs) {
        saveQueue(queueSongs)
        musicPlayer.updateQueue(queueSongs)
    }

    LaunchedEffect(queueSongs) {
        while (true) {
            currentPosition = musicPlayer.getCurrentPosition()
            totalDuration = musicPlayer.getDuration()
            repeatMode = musicPlayer.getRepeatMode()
            isPlaying = musicPlayer.isPlaying()

            val currentIndex = musicPlayer.getCurrentMediaItemIndex()
            val currentUri = musicPlayer.getCurrentMediaItemUri()

            if (
                currentUri != null &&
                currentUri != selectedAudioFile?.uri
            ) {
                val currentAudioFile = queueSongs.firstOrNull {
                    it.uri == currentUri
                }

                if (currentAudioFile != null) {
                    selectedAudioFile = currentAudioFile

                    viewModel.readMetadata(currentAudioFile) { metadata ->
                        selectedMetadata = metadata
                    }
                }
            }

            delay(500)
        }
    }

    LaunchedEffect(selectedAudioFile?.uri) {
        if (artworkWaitingForNewSong && selectedAudioFile != null) {
            artworkOffset.snapTo(artworkIncomingOffset)

            artworkOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(220)
            )

            artworkWaitingForNewSong = false
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

    LaunchedEffect(Unit) {
        val savedUri = preferences.getString(
            MUSIC_FOLDER_URI,
            null
        ) ?: return@LaunchedEffect

        val hasPermission =
            context.contentResolver.persistedUriPermissions.any { permission ->
                permission.uri.toString() == savedUri &&
                        permission.isReadPermission
            }

        if (hasPermission) {
            selectedFolder = savedUri
            folderHistory = emptyList()

            loadFolder(
                uri = savedUri,
                addToHistory = true
            )
        } else {
            preferences.edit()
                .remove(MUSIC_FOLDER_URI)
                .apply()

            selectedFolder = null
        }
    }

    BackHandler {
        if (
            selectedTab == 1 &&
            currentFolder != null &&
            selectedFolder != null &&
            folderHistory.size > 1
        ) {
            val newHistory = folderHistory.dropLast(1)
            val previousFolder = newHistory.last()

            folderHistory = newHistory

            loadFolder(
                uri = previousFolder,
                addToHistory = false
            )
        } else {
            selectedTab = 0
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

            val takeFlags =
                Intent.FLAG_GRANT_READ_URI_PERMISSION

            context.contentResolver.takePersistableUriPermission(
                uri,
                takeFlags
            )

            preferences.edit()
                .putString(MUSIC_FOLDER_URI, uri.toString())
                .apply()

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

                            Box {
                                IconButton(
                                    onClick = {
                                        folderMenuOpen = true
                                    }
                                ) {
                                    Text("⋮")
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

                                            viewModel.addFolderToQueue(folder.uri)
                                        }
                                    )

                                    DropdownMenuItem(
                                        text = { Text("Add to new queue") },
                                        onClick = {
                                            folderMenuOpen = false

                                            viewModel.replaceQueueWithFolderRecursively(
                                                uri = folder.uri
                                            ) { newQueue ->

                                                musicPlayer.replaceQueue(
                                                    audioFiles = newQueue,
                                                    selectedIndex = 0
                                                )

                                                selectedAudioFile = newQueue.firstOrNull()
                                                selectedMetadata = null

                                                newQueue.firstOrNull()?.let { audioFile ->
                                                    viewModel.readMetadata(audioFile) { metadata ->
                                                        selectedMetadata = metadata
                                                    }
                                                }

                                                selectedTab = 0
                                            }
                                        }
                                    )
                                }
                            }
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
                                    viewModel.replaceQueueWithFolder(
                                        uri = currentFolder ?: return@SongRow
                                    ) { folderSongs ->

                                        val selectedIndex = folderSongs.indexOfFirst {
                                            it.uri == audioFile.uri
                                        }

                                        if (selectedIndex != -1) {
                                            selectedAudioFile = audioFile
                                            selectedMetadata = null

                                            musicPlayer.playQueue(
                                                audioFiles = folderSongs,
                                                selectedIndex = selectedIndex
                                            )

                                            viewModel.readMetadata(audioFile) { metadata ->
                                                selectedMetadata = metadata
                                            }

                                            selectedTab = 0
                                        }
                                    }
                                },
                                onAddToCurrentQueue = {
                                    viewModel.addToQueue(audioFile)
                                },
                                onAddToNewQueue = {
                                    viewModel.replaceQueueWithSong(audioFile) { newQueue ->
                                        musicPlayer.replaceQueue(
                                            audioFiles = newQueue,
                                            selectedIndex = 0
                                        )

                                        selectedAudioFile = audioFile
                                        selectedMetadata = null

                                        viewModel.readMetadata(audioFile) { metadata ->
                                            selectedMetadata = metadata
                                        }

                                        selectedTab = 0
                                    }
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
                    if (selectedMetadata?.artwork != null) {

                        val artwork = selectedMetadata!!.artwork!!

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
                                    .offset {
                                        IntOffset(
                                            x = artworkOffset.value.roundToInt(),
                                            y = 0
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectHorizontalDragGestures(
                                            onHorizontalDrag = { _, dragAmount ->
                                                artworkDragAmount += dragAmount

                                                artworkSwipeDirection =
                                                    if (artworkDragAmount < 0f) -1 else 1

                                                artworkScope.launch {
                                                    artworkOffset.snapTo(
                                                        artworkDragAmount.coerceIn(-30f, 30f)
                                                    )
                                                }
                                            },
                                            onDragEnd = {
                                                artworkScope.launch {
                                                    when {
                                                        artworkDragAmount < -100f -> {
                                                            artworkSwipeDirection = -1

                                                            artworkOffset.animateTo(
                                                                targetValue = -30f,
                                                                animationSpec = tween(180)
                                                            )

                                                            artworkWaitingForNewSong = true
                                                            musicPlayer.skipToNext()
                                                        }

                                                        artworkDragAmount > 100f -> {
                                                            artworkSwipeDirection = 1

                                                            artworkOffset.animateTo(
                                                                targetValue = 30f,
                                                                animationSpec = tween(180)
                                                            )

                                                            artworkWaitingForNewSong = true
                                                            musicPlayer.skipToPrevious()
                                                        }

                                                        else -> {
                                                            artworkOffset.animateTo(
                                                                targetValue = 0f,
                                                                animationSpec = tween(180)
                                                            )
                                                        }
                                                    }

                                                    artworkDragAmount = 0f
                                                    artworkSwipeDirection = 0
                                                }
                                            },
                                            onDragCancel = {
                                                artworkScope.launch {
                                                    artworkDragAmount = 0f

                                                    artworkOffset.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(180)
                                                    )

                                                    artworkSwipeDirection = 0
                                                }
                                            }
                                        )
                                    }
                                    .clickable {
                                        musicPlayer.togglePlayPause()
                                    }
                            )
                        }

                    } else {

                        Box(
                            modifier = Modifier
                                .size(400.dp)
                                .offset(y = (-38).dp)
                                .clickable {
                                    musicPlayer.playCurrentQueue(queueSongs)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "▶",
                                fontSize = 64.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(48.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .size(width = 34.dp, height = 28.dp)
                                .align(Alignment.CenterStart),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val barTransition = rememberInfiniteTransition(
                                label = "music bars"
                            )

                            val bar1 by barTransition.animateFloat(
                                initialValue = 0.25f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(700),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bar 1"
                            )

                            val bar2 by barTransition.animateFloat(
                                initialValue = 0.45f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(600),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bar 2"
                            )

                            val bar3 by barTransition.animateFloat(
                                initialValue = 0.3f,
                                targetValue = 0.9f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bar 3"
                            )

                            val bar4 by barTransition.animateFloat(
                                initialValue = 0.4f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(650),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "bar 4"
                            )

                            Row(
                                modifier = Modifier
                                    .size(width = 34.dp, height = 28.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ){
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(if (isPlaying) (24 * bar1).dp else 5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(if (isPlaying) (28 * bar2).dp else 5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(if (isPlaying) (22 * bar3).dp else 5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )

                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(if (isPlaying) (26 * bar4).dp else 5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.onSurface)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = selectedMetadata?.title ?: "Unknown",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = selectedMetadata?.artist ?: "Unknown",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Text(
                                text = selectedMetadata?.album ?: "Unknown",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        IconButton(
                            onClick = {
                                musicPlayer.toggleRepeatMode()
                            },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Icon(
                                imageVector = if (
                                    repeatMode == Player.REPEAT_MODE_ONE
                                ) {
                                    Icons.Default.RepeatOne
                                } else {
                                    Icons.Default.Repeat
                                },
                                contentDescription = if (
                                    repeatMode == Player.REPEAT_MODE_ONE
                                ) {
                                    "Repeat track"
                                } else {
                                    "Repeat queue"
                                }
                            )
                        }
                    }

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

        if (selectedTab == 3) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Albums coming soon")
            }
        }

        if (selectedTab == 4) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Artists coming soon")
            }
        }

        if (selectedTab == 5) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Queue coming soon")
            }
        }

        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(94.dp),
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play"
                    )
                },
                label = {
                    Text("Play")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 1,
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
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Library"
                    )
                },
                label = {
                    Text("Lib")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 3,
                onClick = {
                    selectedTab = 3
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = "Albums"
                    )
                },
                label = {
                    Text("Albums")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 4,
                onClick = {
                    selectedTab = 4
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Artists"
                    )
                },
                label = {
                    Text("Artists")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                },
                label = {
                    Text("Set")
                }
            )

            NavigationBarItem(
                selected = selectedTab == 5,
                onClick = {
                    selectedTab = 5
                },
                icon = {
                    BadgedBox(
                        badge = {
                            if (queueSongs.isNotEmpty()) {
                                Badge {
                                    Text(queueSongs.size.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueueMusic,
                            contentDescription = "Queue"
                        )
                    }
                },
                label = {
                    Text("Queue")
                }
            )
        }

}
}

