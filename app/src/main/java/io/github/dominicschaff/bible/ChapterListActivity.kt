package io.github.dominicschaff.bible

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_chapter_list.*

class ChapterListActivity : AppCompatActivity() {

    private val chapters = ArrayList<Chapter>()
    private lateinit var adapter: MyChapterAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chapter_list)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = androidx.recyclerview.widget.GridLayoutManager(applicationContext, 3)
        recycler_view.layoutManager = layoutManager
        adapter = MyChapterAdapter(this, chapters)
        recycler_view.adapter = adapter

        val bookId = intent.extras?.getLong(BOOK_ID) ?: -1
        if (bookId == -1L) {
            finish()
            return
        }

        title = Db(this).getBookFromBookId(bookId)?.name

        Refresh(this, bookId) { result: Array<Chapter> ->
            chapters.clear()
            chapters.addAll(result)
            adapter.notifyDataSetChanged()
        }.execute()
    }

    class Refresh(val context: Context, private val bookId: Long, val f: (Array<Chapter>) -> Unit) :
        AsyncTask<Void, Void, Array<Chapter>>() {
        override fun doInBackground(vararg params: Void?): Array<Chapter> =
            Db(context).getChapters(bookId)

        override fun onPostExecute(result: Array<Chapter>) = f(result)
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.name)
    }

    class MyChapterAdapter(
        private val activity: ChapterListActivity,
        private val chapters: ArrayList<Chapter>
    ) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(
                    R.layout.view_chapter,
                    viewGroup,
                    false
                )
            )

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val chapter = chapters[i]

            viewHolder.name.text = "${chapter.id + 1}"

            viewHolder.view.setOnClickListener {
                activity.goto(VersesActivity::class.java, custom = {
                    this.putExtra(CHAPTER_ID, chapter.id)
                })
            }
        }

        override fun getItemCount(): Int = chapters.size
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean = when (item?.itemId) {
        android.R.id.home -> consume { finish() }
        else -> super.onOptionsItemSelected(item)
    }
}
