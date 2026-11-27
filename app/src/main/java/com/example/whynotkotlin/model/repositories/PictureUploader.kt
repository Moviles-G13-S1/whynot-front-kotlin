package com.example.whynotkotlin.model.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/** Where a picked picture belongs; each folder has its own Storage rule. */
enum class PictureFolder(val path: String) {
    PRODUCT("productPhotos"),
    WISHLIST("wishlistPhotos")
}

/**
 * Uploads a picture the user picked on the phone and returns a link every
 * client (Android, iOS, Flutter) can display, which is saved as `imageUrl`.
 */
interface PictureUploader {

    /** [source] is the content URI given by the photo picker. */
    suspend fun upload(ownerId: String, folder: PictureFolder, source: String): String
}

/** Used where no Storage is wired (tests); picking a photo then fails politely. */
object UnavailablePictureUploader : PictureUploader {
    override suspend fun upload(ownerId: String, folder: PictureFolder, source: String): String =
        throw IllegalStateException("Picture upload is not available")
}

class FirebasePictureUploader(
    private val context: Context,
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) : PictureUploader {

    override suspend fun upload(ownerId: String, folder: PictureFolder, source: String): String {
        // Decoding and compressing a camera photo is CPU work, off the main thread.
        val bytes = withContext(Dispatchers.Default) { compressed(Uri.parse(source)) }

        val name = "${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(8)}.jpg"
        val ref = storage.reference.child("${folder.path}/$ownerId/$name")
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()

        ref.putBytes(bytes, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * A phone photo is often 4–12 MB; it is scaled so its long side is at most
     * [MAX_SIDE] px and saved as JPEG, which keeps it well under the 5 MB rule
     * and loads fast on every screen.
     */
    private fun compressed(uri: Uri): ByteArray {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IllegalArgumentException(NOT_A_PICTURE)

        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2

        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: throw IllegalArgumentException(NOT_A_PICTURE)

        val rotated = rotatedUpright(decoded, uri)
        val scale = MAX_SIDE.toFloat() / maxOf(rotated.width, rotated.height)
        val finalBitmap = if (scale < 1f) {
            Bitmap.createScaledBitmap(
                rotated,
                (rotated.width * scale).toInt().coerceAtLeast(1),
                (rotated.height * scale).toInt().coerceAtLeast(1),
                true
            )
        } else {
            rotated
        }

        return ByteArrayOutputStream().use { out ->
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
    }

    /** Camera photos are often stored sideways with an EXIF rotation flag. */
    private fun rotatedUpright(bitmap: Bitmap, uri: Uri): Bitmap {
        val degrees = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        }.getOrDefault(0f)

        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private companion object {
        const val MAX_SIDE = 1280
        const val JPEG_QUALITY = 85
        const val NOT_A_PICTURE = "That file is not a picture"
    }
}
