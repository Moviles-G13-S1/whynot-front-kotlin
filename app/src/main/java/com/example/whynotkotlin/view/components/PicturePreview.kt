package com.example.whynotkotlin.view.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.whynotkotlin.app.LocalAppDependencies
import com.example.whynotkotlin.controller.PictureController
import com.example.whynotkotlin.model.ImageLinks
import com.example.whynotkotlin.model.InputRules
import com.example.whynotkotlin.model.UserFacingException
import com.example.whynotkotlin.model.repositories.PictureFolder
import com.example.whynotkotlin.view.theme.WhyNotBeige
import com.example.whynotkotlin.view.theme.WhyNotErrorText
import com.example.whynotkotlin.view.theme.whyNotMuted
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * The easy way to add a picture: "Choose from gallery" opens the system photo
 * picker (no permission needed), the photo is uploaded and its link fills the
 * picture field through [onPicked]. Pasting a link still works; whatever the
 * field holds is previewed, so a link that is not an image is seen before
 * saving.
 */
@Composable
fun PicturePreview(
    link: String,
    folder: PictureFolder,
    onPicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pictureController = LocalAppDependencies.current.pictureController
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        uploadError = null
        uploading = true
        scope.launch {
            try {
                onPicked(pictureController.upload(folder, uri.toString()))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: UserFacingException) {
                android.util.Log.e("WhyNotPicture", "Gallery upload refused", error)
                uploadError = error.message
            } catch (error: Exception) {
                android.util.Log.e("WhyNotPicture", "Gallery upload failed", error)
                uploadError = PictureController.UPLOAD_FAILED
            } finally {
                uploading = false
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        // Off to mirror the Flutter app, where product and wishlist pictures
        // are pasted links (its gallery picker is only for the profile photo).
        if (GALLERY_ENABLED) Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WhyNotPillButton(
                text = if (uploading) "Uploading photo…" else "Choose from gallery",
                enabled = !uploading,
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            )
            if (uploading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }

        uploadError?.let {
            Text(text = it, style = whyNotMuted(12, WhyNotErrorText), modifier = Modifier.padding(top = 6.dp))
        }

        LinkPreview(link)
    }
}

private const val GALLERY_ENABLED = true

/** Thumbnail of what the picture field holds, with whether it loaded. */
@Composable
private fun LinkPreview(link: String) {
    val trimmed = link.trim()
    if (trimmed.isEmpty() || InputRules.optionalLink(trimmed, "picture link") != null) return

    val url = ImageLinks.amazonPicture(trimmed) ?: ImageLinks.secure(trimmed)
    var loaded by remember(url) { mutableStateOf<Boolean?>(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(WhyNotBeige)
        ) {
            AsyncImage(
                model = url,
                contentDescription = "Picture preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onSuccess = { loaded = true },
                onError = { loaded = false }
            )
        }

        Text(
            text = when (loaded) {
                true -> "Picture ready."
                false -> "This link is not an image. When you save, we'll try to take the main picture from the page."
                null -> "Loading preview…"
            },
            style = whyNotMuted(12),
            modifier = Modifier.weight(1f)
        )
    }
}
