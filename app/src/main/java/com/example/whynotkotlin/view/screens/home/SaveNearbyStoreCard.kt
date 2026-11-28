package com.example.whynotkotlin.view.screens.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.whynotkotlin.model.NearbyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
internal fun SaveNearbyStoreCard(store: NearbyStore) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingCard by rememberSaveable { mutableStateOf<String?>(null) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val card = pendingCard
        pendingCard = null
        if (uri != null && card != null) {
            scope.launch {
                val message = try {
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openOutputStream(uri, "wt")
                            ?: throw IOException("Document could not be opened")
                        stream.bufferedWriter(Charsets.UTF_8).use { it.write(card) }
                    }
                    "Store card saved"
                } catch (_: IOException) {
                    "Could not save the store card"
                } catch (_: SecurityException) {
                    "Access to the selected document was denied"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    TextButton(onClick = {
        pendingCard = listOf(store.name, store.address, store.categoryIds.joinToString(), store.websiteUrl)
            .filter { it.isNotBlank() }.joinToString("\n")
        picker.launch("WhyNot-store.txt")
    }) { Text("Save offline store card") }
}
