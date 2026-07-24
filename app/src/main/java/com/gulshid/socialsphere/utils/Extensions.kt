package com.example.socialsphere.utils

import android.content.Context
import android.util.Patterns
import android.view.View
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun String.isValidEmail(): Boolean =
    isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

/**
 * Converts a timestamp (millis) into a compact, human-readable
 * relative time string, e.g. "5m", "2h", "3d", or a date for older posts.
 */
fun Long.toRelativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))
    }
}

fun Int.toCompactCount(): String = when {
    this >= 1_000_000 -> String.format(Locale.getDefault(), "%.1fM", this / 1_000_000.0)
    this >= 1_000 -> String.format(Locale.getDefault(), "%.1fK", this / 1_000.0)
    else -> this.toString()
}
