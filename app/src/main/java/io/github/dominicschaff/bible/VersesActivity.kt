package io.github.dominicschaff.bible

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_verses.*

class VersesActivity : AppCompatActivity() {

    private val verses = ArrayList<Verse>()
    private lateinit var adapter: MyVerseAdapter
    private lateinit var book: Book
    private lateinit var chapter: Chapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verses)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val chapterId = intent.extras?.getLong(CHAPTER_ID) ?: -1L
        if (chapterId == -1L) {
            finish()
            return
        }
        book = Db(this).getBookFromChapterId(chapterId)!!
        chapter = Db(this).getChapterById(chapterId)!!
        title = "${book.name} : ${chapter.id + 1}"


        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
        recycler_view.layoutManager = layoutManager
        adapter = MyVerseAdapter(this, book, chapter, verses)
        recycler_view.adapter = adapter

        Refresh(this, chapterId) { result: Array<Verse> ->
            verses.clear()
            verses.addAll(result)
            "${result.size} verses".log()
            adapter.notifyDataSetChanged()
        }.execute()
    }

    class Refresh(
        val context: Context,
        private val chapterId: Long,
        val f: (Array<Verse>) -> Unit
    ) :
        AsyncTask<Void, Void, Array<Verse>>() {
        override fun doInBackground(vararg params: Void?): Array<Verse> =
            Db(context).getVerses(chapterId)

        override fun onPostExecute(result: Array<Verse>) = f(result)
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val number: TextView = view.findViewById(R.id.number)
        val text: TextView = view.findViewById(R.id.text)
    }

    class MyVerseAdapter(
        private val activity: VersesActivity,
        private val book: Book,
        private val chapter: Chapter,
        private val verses: ArrayList<Verse>
    ) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(
                    R.layout.view_verse,
                    viewGroup,
                    false
                )
            )

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val verse = verses[i]
            val verseLocation = "${book.abbreviation} ${chapter.id + 1}:$i"

            viewHolder.number.text = (verse.id + 1).toString()
            viewHolder.text.text = verse.text
            viewHolder.view.setOnLongClickListener {
                activity.chooser(verseLocation, activity.resources.getStringArray(R.array.verse_share), callback = { action, _ ->
                    when (action) {
                        0 -> activity.share(verseLocation)
                        1 -> activity.share("${book.name} ${chapter.id + 1}:$i\n${verse.text}")
                        2 -> {
                            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(verseLocation, verseLocation))
                            activity.toast("Verse reference has been copied to clipboard")
                        }
                        3 -> {
                            val v = "${book.name} ${chapter.id + 1}:$i\n${verse.text}"
                            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText(v, v))
                            activity.toast("Verse and verse reference has been copied to clipboard")
                        }
                    }
                })
                true
            }
        }

        override fun getItemCount(): Int = verses.size
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> consume { finish() }
        else -> super.onOptionsItemSelected(item)
    }
}
