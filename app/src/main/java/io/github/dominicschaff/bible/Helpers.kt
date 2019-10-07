@file:Suppress("NOTHING_TO_INLINE")

package io.github.dominicschaff.bible

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.alert_chooser.view.*
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader


fun Activity.goto(c: Class<*>, options: Int = 0, custom: (Intent.() -> Unit)? = null) {

    val intent = Intent(this, c)
    if (options != 0) intent.flags = options

    if (custom != null) intent.custom()

    startActivity(intent)
}

inline fun String.log(type: String = "App") = Log.i(type, this)

inline fun String.fileAsJsonObject(): JsonObject =
    JsonParser().parse(BufferedReader(StringReader(this))).asJsonObject

inline fun String.fileAsJsonArray(): JsonArray =
    JsonParser().parse(BufferedReader(StringReader(this))).asJsonArray

fun Context.loadJSONFromAsset(file: String): String = try {
    val stream = assets.open(file)
    val size = stream.available()
    val buffer = ByteArray(size)
    stream.read(buffer)
    stream.close()
    String(buffer)
} catch (ex: IOException) {
    ex.printStackTrace()
    "[]"
}

inline fun <T> JsonArray.mapObject(f: JsonObject.() -> T): List<T> =
    this.map { it.asJsonObject.f() }


fun JsonObject.s(key: String, defaultValue: String = ""): String =
    { if (has(key)) get(key).asString else defaultValue }.or { defaultValue }

fun JsonObject.l(key: String, defaultValue: Long = 0): Long =
    { if (has(key)) get(key).asLong else defaultValue }.or { defaultValue }

fun JsonObject.a(key: String, defaultValue: JsonArray = JsonArray()): JsonArray =
    { if (has(key)) get(key).asJsonArray else defaultValue }.or { defaultValue }

inline fun <T> (() -> T).or(f: () -> T): T =
    try {
        this()
    } catch (e: Exception) {
        f()
    }

inline fun consume(f: () -> Unit): Boolean {
    f()
    return true
}

fun Activity.chooser(
    title: String,
    options: Array<String>,
    optionsIcons: Array<Int>? = null,
    callback: (Int, String) -> Unit
) {

    val l = this.layoutInflater.inflate(R.layout.alert_chooser, null)
    val dialog = AlertDialog.Builder(this)
    dialog.setTitle(title)
    dialog.setView(l)
    val finalDialog = dialog.show()

    val grid = l.mainGrid

    if (optionsIcons != null && optionsIcons.size == options.size) {

    } else {
        options.forEachIndexed { index, option ->
            val v = layoutInflater.inflate(R.layout.alert_chooser_text, grid, false) as TextView

            v.text = option
            v.setOnClickListener {
                callback(index, option)
                finalDialog.dismiss()
            }
            grid.addView(v)
        }
    }
}

inline fun Context.toast(s: String, length: Int = Toast.LENGTH_LONG) =
    Toast.makeText(this, s, length).show()


fun Activity.share(text: String, subject: String = "") {
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    if (subject.isNotBlank())
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    sharingIntent.putExtra(Intent.EXTRA_TEXT, text)
    startActivity(Intent.createChooser(sharingIntent, "Share with..."))
}