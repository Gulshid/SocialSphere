package com.gulshid.socialsphere.utils

import android.net.Uri
import com.gulshid.socialsphere.SocialSphereApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Uploads images to Cloudinary using an UNSIGNED upload preset, so no backend
 * server or a paid Firebase Blaze plan is required (unlike Firebase Storage).
 *
 * One-time setup on cloudinary.com:
 *   1. Sign up for a free account -> your Cloud Name is on the dashboard.
 *   2. Settings -> Upload -> Upload presets -> Add upload preset.
 *   3. Set "Signing Mode" to "Unsigned", give it a name, and save.
 *   4. Fill in CLOUD_NAME and UPLOAD_PRESET below with those two values.
 *
 * Free tier covers 25 GB storage / 25 GB monthly bandwidth, which is plenty
 * for development and small apps.
 */
object CloudinaryUploader {

    // TODO: replace with your own values from the Cloudinary dashboard.
    private const val CLOUD_NAME = "df0saqabg"
    private const val UPLOAD_PRESET = "socialsphere_preset"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads the image at [imageUri] to Cloudinary and returns its public
     * "secure_url". [folder] groups the upload in the Cloudinary media
     * library (mirrors how paths like "posts/$uid" were used with Storage).
     */
    suspend fun uploadImage(imageUri: Uri, folder: String): String =
        withContext(Dispatchers.IO) {
            check(CLOUD_NAME != "YOUR_CLOUD_NAME" && UPLOAD_PRESET != "YOUR_UNSIGNED_UPLOAD_PRESET") {
                "Cloudinary is not configured yet. Set CLOUD_NAME and UPLOAD_PRESET in CloudinaryUploader.kt."
            }

            val context = SocialSphereApp.appContext
            val tempFile = File.createTempFile("upload_${UUID.randomUUID()}", ".jpg", context.cacheDir)
            try {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    FileOutputStream(tempFile).use { output -> input.copyTo(output) }
                } ?: throw IOException("Unable to read the selected image.")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        tempFile.name,
                        tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    .addFormDataPart("upload_preset", UPLOAD_PRESET)
                    .addFormDataPart("folder", folder)
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val bodyString = response.body?.string()
                    if (!response.isSuccessful || bodyString == null) {
                        throw IOException("Cloudinary upload failed (${response.code}): ${bodyString ?: "no response body"}")
                    }
                    JSONObject(bodyString).getString("secure_url")
                }
            } finally {
                tempFile.delete()
            }
        }
}
