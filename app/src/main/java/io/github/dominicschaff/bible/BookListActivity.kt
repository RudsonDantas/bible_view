package io.github.dominicschaff.bible

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_book_list.*

class BookListActivity : AppCompatActivity() {

    private val books = ArrayList<Book>()
    private lateinit var adapter: MyBookAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
        recycler_view.layoutManager = layoutManager
        adapter = MyBookAdapter(this, books)
        recycler_view.adapter = adapter

        Refresh(this) { result: Array<Book> ->
            books.clear()
            books.addAll(result)
            adapter.notifyDataSetChanged()
        }.execute()
    }

    class Refresh(val context: Context, val f: (Array<Book>) -> Unit) :
        AsyncTask<Void, Void, Array<Book>>() {
        override fun doInBackground(vararg params: Void?): Array<Book> {
            "I am here".log()
            return Db(context).getBooks()
        }

        override fun onPostExecute(result: Array<Book>) = f(result)
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val abbr: TextView = view.findViewById(R.id.abbr)
        val nameEn: TextView = view.findViewById(R.id.book_en)
        val nameAf: TextView = view.findViewById(R.id.book_af)
    }

    class MyBookAdapter(
        private val activity: BookListActivity,
        private val books: ArrayList<Book>
    ) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(
                    R.layout.view_book,
                    viewGroup,
                    false
                )
            )

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val book = books[i]

            viewHolder.abbr.text = book.abbreviation
            viewHolder.nameEn.text = book.name
            viewHolder.nameAf.text = book.nameAf

            viewHolder.view.setOnClickListener {
                activity.goto(ChapterListActivity::class.java, custom = {
                    this.putExtra(BOOK_ID, book.id)
                })
            }
        }

        override fun getItemCount(): Int = books.size
    }
}
