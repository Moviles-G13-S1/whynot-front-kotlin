package com.example.whynotkotlin.view.screens.directory

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.model.DeviceLocation
import com.example.whynotkotlin.model.repositories.DirectorySnapshot
import com.example.whynotkotlin.model.repositories.DirectoryStore
import com.example.whynotkotlin.view.components.rememberIsOnline
import com.example.whynotkotlin.view.navigation.WhyNotNavigator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.Locale

private val Context.directoryPreferences by preferencesDataStore("directory_filters")

@Composable
fun StoreDirectoryRoute(navigator: WhyNotNavigator, storeId: String? = null) {
    val dependencies = LocalAppDependencies.current
    val repository = requireNotNull(dependencies.storeDirectory)
    val context = LocalContext.current
    val session by dependencies.authController.session.collectAsStateWithLifecycle()
    val uid = session?.getOrNull()?.uid ?: return
    val key = remember(uid) { stringPreferencesKey("category_$uid") }
    val selected by remember(context, key) {
        context.directoryPreferences.data.map { it[key].orEmpty() }
            .catch { if (it is IOException) emit("") else throw it }
    }.collectAsStateWithLifecycle(initialValue = "")
    val scope = rememberCoroutineScope()
    var failure by remember(uid) { mutableStateOf<String?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    val online = rememberIsOnline()
    val feed = remember(repository, uid, attempt) {
        repository.observe().catch { failure = it.localizedMessage ?: "Could not load stores." }
    }
    val snapshot by feed.collectAsStateWithLifecycle(initialValue = null as DirectorySnapshot?)

    var position by remember { mutableStateOf<DeviceLocation?>(null) }
    var locationMessage by remember { mutableStateOf<String?>(null) }
    var locating by remember { mutableStateOf(false) }
    fun locate() {
        if (locating) return
        locating = true
        scope.launch {
            try {
                position = dependencies.locationRepository.getCurrentLocation()
                locationMessage = null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                locationMessage = "Location unavailable. You can still browse stores."
            } finally {
                locating = false
            }
        }
    }
    val permission = Manifest.permission.ACCESS_COARSE_LOCATION
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locate() else locationMessage = "Location permission declined."
    }
    LaunchedEffect(uid) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) locate()
    }

    Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 16.dp)) {
        TextButton(onClick = navigator::back) { Text("Back") }
        Text(if (storeId == null) "Store directory" else "Store details", style = MaterialTheme.typography.headlineMedium)
        if (!online) Text("Offline · showing stores available on this device.")
        else if (snapshot?.fromDisk == true) Text("Checking for directory updates…")
        if (position == null) {
            TextButton(enabled = !locating, onClick = { launcher.launch(permission) }) {
                Text(if (locating) "Finding location…" else "Show distance from me")
            }
        }
        locationMessage?.let { Text(it) }
        failure?.let { message ->
            Text(message)
            TextButton(onClick = { failure = null; attempt++ }) { Text("Retry") }
        }
        val data = snapshot
        if (data == null) {
            if (failure == null && online) CircularProgressIndicator()
            return@Column
        }
        if (storeId != null) {
            val store = data.index.byId[storeId]
            if (store != null) StoreDetail(store, position)
            else Text(if (!online || data.fromDisk) "Store details unavailable on this device." else "Store no longer available.")
            return@Column
        }
        val category = selected.takeIf { it in data.index.byCategory }.orEmpty()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("") + data.index.byCategory.keys, key = { it }) { id ->
                FilterChip(
                    selected = id == category,
                    onClick = {
                        scope.launch {
                            try { context.directoryPreferences.edit { it[key] = id } }
                            catch (_: IOException) { failure = "Could not save category." }
                        }
                    },
                    label = { Text(id.ifBlank { "All" }) }
                )
            }
        }
        val stores = data.index.byCategory[category] ?: data.index.all
        if (stores.isEmpty()) Text(
            if (!online || data.fromDisk) "No stores saved on this device yet." else "No stores in the directory yet."
        )
        LazyColumn {
            items(stores, key = { it.id }) { store ->
                Row(
                    Modifier.fillMaxWidth().clickable { navigator.toDirectoryStore(store.id) }.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StorePhoto(store, Modifier.size(64.dp))
                    Column {
                        Text(store.name, style = MaterialTheme.typography.titleMedium)
                        Text(store.categories.joinToString())
                        Distance(store, position)
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun StoreDetail(store: DirectoryStore, position: DeviceLocation?) {
    val handler = LocalUriHandler.current
    var linkError by remember(store.website) { mutableStateOf(false) }
    val uri = remember(store.website) { Uri.parse(store.website) }
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StorePhoto(store, Modifier.fillMaxWidth().height(180.dp))
        Text(store.name, style = MaterialTheme.typography.headlineSmall)
        Text(store.categories.joinToString().ifBlank { "No category" })
        Text(store.address.ifBlank { "Address unavailable" })
        Distance(store, position)
        if (uri.scheme in listOf("https", "http") && !uri.host.isNullOrBlank()) {
            OutlinedButton(onClick = {
                try { handler.openUri(store.website) }
                catch (_: IllegalArgumentException) { linkError = true }
            }) { Text("Visit website") }
            if (linkError) Text("No browser available.")
        } else Text("Website unavailable")
    }
}

@Composable
private fun Distance(store: DirectoryStore, position: DeviceLocation?) {
    val distance = remember(store.latitude, store.longitude, position) {
        if (position == null || store.latitude == null || store.longitude == null) null
        else {
            val meters = FloatArray(1)
            Location.distanceBetween(position.latitude, position.longitude, store.latitude, store.longitude, meters)
            String.format(Locale.getDefault(), "%.1f km away", meters[0] / 1000)
        }
    }
    Text(distance ?: "Distance unavailable")
}

@Composable
private fun StorePhoto(store: DirectoryStore, modifier: Modifier) {
    val context = LocalContext.current
    val online = rememberIsOnline()
    var failed by remember(store.image, online) { mutableStateOf(false) }
    if (store.image.isBlank() || failed) {
        val icon = when (store.categories.firstOrNull()) {
            "fashion" -> Icons.Outlined.Checkroom
            "technology" -> Icons.Outlined.Computer
            "beauty" -> Icons.Outlined.Spa
            "travel" -> Icons.Outlined.Flight
            "home" -> Icons.Outlined.Home
            "gifts" -> Icons.Outlined.CardGiftcard
            "accessories" -> Icons.Outlined.Watch
            else -> Icons.Outlined.Store
        }
        Box(modifier, contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = "Category illustration", modifier = Modifier.size(32.dp))
        }
    } else {
        val request = remember(context, store.image) {
            ImageRequest.Builder(context)
                .data(store.image)
                .crossfade(false)
                .build()
        }
        AsyncImage(
            model = request, contentDescription = store.name, modifier = modifier,
            contentScale = ContentScale.Crop, onError = { failed = true }
        )
    }
}
