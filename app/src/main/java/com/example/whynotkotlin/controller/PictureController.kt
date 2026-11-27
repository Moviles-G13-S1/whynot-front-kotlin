package com.example.whynotkotlin.controller

import com.example.whynotkotlin.model.ValidationException
import com.example.whynotkotlin.model.repositories.AuthRepository
import com.example.whynotkotlin.model.repositories.ConnectivityRepository
import com.example.whynotkotlin.model.repositories.PictureFolder
import com.example.whynotkotlin.model.repositories.PictureUploader
import com.example.whynotkotlin.model.repositories.requireOnline

/**
 * Lets the user add a product or wishlist picture straight from the phone's
 * gallery, instead of having to find and paste an image link.
 */
class PictureController(
    private val authRepository: AuthRepository,
    private val uploader: PictureUploader,
    private val connectivity: ConnectivityRepository? = null
) {

    /** Uploads the picked picture and returns the link to save as imageUrl. */
    suspend fun upload(folder: PictureFolder, source: String): String {
        val uid = authRepository.currentUser()?.uid
            ?: throw ValidationException("You are not signed in")
        connectivity.requireOnline(UPLOAD_OFFLINE)
        return uploader.upload(uid, folder, source)
    }

    companion object {
        const val UPLOAD_OFFLINE = "You're offline. Connect to the internet to add a photo, or paste a picture link."
        const val UPLOAD_FAILED = "We couldn't upload that photo. Try again or paste a picture link."
    }
}
