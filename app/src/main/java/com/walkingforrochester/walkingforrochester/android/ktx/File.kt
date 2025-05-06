package com.walkingforrochester.walkingforrochester.android.ktx

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.exifinterface.media.ExifInterface
import timber.log.Timber
import java.io.File

fun File.compressImage(
    targetFile: File,
    targetWidth: Int,
    targetHeight: Int,
    targetJPegQuality: Int = 75
): Boolean {

    if (!this.exists()) {
        Timber.w("File does not exist: %s", this.absolutePath)
        return false
    }

    val bitmapOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }

    this.inputStream().use { input ->
        BitmapFactory.decodeStream(input, null, bitmapOptions)
    }

    //Timber.d("old dimensions %dw x %dh", bitmapOptions.outWidth, bitmapOptions.outHeight)

    if (bitmapOptions.outWidth == 0 || bitmapOptions.outHeight == 0) {
        Timber.w("Failed to decode bitmap %s", this.absolutePath)
        return false
    }

    val exifOrig = readExifData(this)

    val landscapePhoto = bitmapOptions.outHeight < bitmapOptions.outWidth
    bitmapOptions.inSampleSize = calculateInSampleSize(
        options = bitmapOptions,
        landscapePhoto = landscapePhoto,
        targetWidth = targetWidth,
        targetHeight = targetHeight
    )

    bitmapOptions.inJustDecodeBounds = false
    bitmapOptions.inMutable = true
    bitmapOptions.inScaled = true
    bitmapOptions.inDensity =
        if (landscapePhoto) bitmapOptions.outHeight else bitmapOptions.outWidth
    bitmapOptions.inTargetDensity =
        bitmapOptions.inSampleSize * if (landscapePhoto) targetWidth else targetHeight

    var decodedBitmap: Bitmap? = this.inputStream().use { input ->
        BitmapFactory.decodeStream(input, null, bitmapOptions)
    }

    if (decodedBitmap == null) {
        Timber.w("Failed to decode bitmap")
        return false
    }

    targetFile.outputStream().use { os ->
        decodedBitmap.compress(Bitmap.CompressFormat.JPEG, targetJPegQuality, os)
    }

    //Timber.d("New dimensions %dw x %dh", decodedBitmap.width, decodedBitmap.height)

    val exifTarget = readExifData(targetFile)
    exifTarget?.apply {
        val orientation = exifOrig?.getAttribute(ExifInterface.TAG_ORIENTATION)
            ?: ExifInterface.ORIENTATION_NORMAL.toString()
        setAttribute(ExifInterface.TAG_ORIENTATION, orientation)
        setAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION, decodedBitmap.width.toString())
        setAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION, decodedBitmap.height.toString())
        saveExifData(this)
    }

    decodedBitmap.recycle()
    return true
}

private fun readExifData(file: File): ExifInterface? {
    return try {
        ExifInterface(file)
    } catch (e: Exception) {
        Timber.w(e, "Unable to read exif data")
        null
    }
}

private fun saveExifData(exifInterface: ExifInterface) {
    try {
        exifInterface.saveAttributes()
    } catch (e: Exception) {
        Timber.w(e, "Failed to save attributes")
    }
}

private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    landscapePhoto: Boolean,
    targetWidth: Int,
    targetHeight: Int
): Int {
    // Raw height and width of image
    val (width: Int, height: Int) = if (landscapePhoto)
        options.run { outHeight to outWidth }
    else {
        options.run { outWidth to outHeight }
    }

    var inSampleSize = 1

    if (height > targetHeight || width > targetWidth) {

        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while (halfHeight / inSampleSize >= targetHeight && halfWidth / inSampleSize >= targetWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}