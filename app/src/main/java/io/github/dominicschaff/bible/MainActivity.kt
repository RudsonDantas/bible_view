package io.github.dominicschaff.bible

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (getPreferences(Context.MODE_PRIVATE).getBoolean("dataLoaded", false)) {
            done()
        } else {
            Refresh(this) {
                getPreferences(Context.MODE_PRIVATE).edit().putBoolean("dataLoaded", true).apply()
                done()
            }.execute()
        }
    }

    class Refresh(val context: Context, val f: () -> Unit) :
        AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {

            val db = Db(context)

            val bible = context.loadJSONFromAsset("en_bbe.json").fileAsJsonArray()

            bible.forEachIndexed { index, jsonElement ->
                val bookJson = jsonElement.asJsonObject
                val book = Book(
                    index.toLong(),
                    bookJson.s("abbrev"),
                    bookJson.s("name"),
                    bookJson.s("abbrev").afrikaansBookName()
                )
                db.add(book)
                bookJson.a("chapters").forEachIndexed { chapterIndex, chapterElement ->
                    val chapter = Chapter(book.id * 1000 + chapterIndex, book.id)
                    db.add(chapter)
                    chapterElement.asJsonArray.forEachIndexed { verseIndex, verseElement ->
                        val verse = Verse(verseIndex.toLong(), chapter.id, verseElement.asString)
                        db.add(verse)
                    }
                }
            }
            return null
        }

        override fun onPostExecute(result: Void?) = f()
    }

    private fun done() {
        goto(
            BookListActivity::class.java,
            options = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        finish()
    }
}
