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

            val json = context.loadJSONFromAsset("books.json").fileAsJsonArray()
                .mapObject {
                    doBookData(db, l("id"), s("file"))
                    Book(
                        l("id"),
                        s("book"),
                        s("abbreviation"),
                        s("name"),
                        s("name_af")
                    )
                }.forEach { db.add(it) }
            return null
        }

        override fun onPostExecute(result: Void?) = f()

        private fun doBookData(db: Db, book: Long, file: String) {
            file.log()
            val chaptersDone = ArrayList<Long>()
            val json = context.loadJSONFromAsset(file).fileAsJsonObject().a("verses")
                .mapObject {
                    val chapter = l("chapter")
                    val chapterId = book * 1000 + chapter
                    if (!chaptersDone.contains(chapter)) {
                        db.add(Chapter(chapterId, book, "$chapter"))
                    }
                    db.add(Verse(l("verse"), chapterId, s("text")))
                }
        }
    }

    private fun done() {
        goto(
            BookListActivity::class.java,
            options = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        finish()
    }
}
