@file:Suppress("unused")

package io.github.dominicschaff.bible

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

const val BOOK_ID = "bookId"
const val CHAPTER_ID = "chapterId"


data class Book(
    val id: Long,
    val book: String,
    val abbreviation: String,
    val name: String,
    val nameAf: String
)

data class Chapter(
    val id: Long,
    val book: Long,
    val title: String

)

data class Verse(
    val id: Long,
    val chapter: Long,
    val text: String
)

const val DATABASE_VERSION = 1
const val DATABASE_NAME = "books.db"

class Db(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE book (id INTEGER PRIMARY KEY, book TEXT, abbreviation TEXT, name TEXT, name_af TEXT)")
        db.execSQL("CREATE TABLE chapter (id INTEGER PRIMARY KEY, book long, title TEXT)")
        db.execSQL("CREATE TABLE verse (id INTEGER, chapter INTEGER, content TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS book")
        db.execSQL("DROP TABLE IF EXISTS chapter")
        db.execSQL("DROP TABLE IF EXISTS verse")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun add(book: Book) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("id", book.id)
            put("book", book.book)
            put("abbreviation", book.abbreviation)
            put("name", book.name)
            put("name_af", book.nameAf)
        }

        db.insert("book", null, values)
    }

    fun add(chapter: Chapter) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("id", chapter.id)
            put("book", chapter.book)
            put("title", chapter.title)
        }

        db.insert("chapter", null, values)
    }

    fun add(verse: Verse) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("id", verse.id)
            put("chapter", verse.chapter)
            put("content", verse.text)
        }

        db.insert("verse", null, values)
    }

    fun getBooks(): Array<Book> {
        val db = readableDatabase

        val cursor = db.query(
            "book",
            arrayOf("id", "book", "abbreviation", "name", "name_af"),
            null,
            null,
            null,
            null,
            "id ASC"
        )
        val books = mutableListOf<Book>()
        with(cursor) {
            while (moveToNext()) {
                books.add(
                    Book(
                        getLong(0),
                        getString(1),
                        getString(2),
                        getString(3),
                        getString(4)
                    )
                )
            }
        }
        return books.toTypedArray()
    }

    fun getChapters(book: Long): Array<Chapter> {
        val db = readableDatabase

        val cursor = db.query(
            "chapter",
            arrayOf("id", "book", "title"),
            "book = $book",
            null,
            null,
            null,
            "id ASC"
        )
        val chapters = mutableListOf<Chapter>()
        with(cursor) {
            while (moveToNext()) {
                chapters.add(
                    Chapter(
                        getLong(0),
                        getLong(1),
                        getString(2)
                    )
                )
            }
        }
        return chapters.toTypedArray()
    }

    fun getVerses(chapter: Long): Array<Verse> {
        val db = readableDatabase

        val cursor = db.query(
            "verse",
            arrayOf("id", "chapter", "content"),
            "chapter = $chapter",
            null,
            null,
            null,
            "id ASC"
        )
        val verses = mutableListOf<Verse>()
        with(cursor) {
            while (moveToNext()) {
                verses.add(
                    Verse(
                        getLong(0),
                        getLong(1),
                        getString(2)
                    )
                )
            }
        }
        return verses.toTypedArray()
    }

    fun getBookFromBookId(book:Long):Book? {
        val db = readableDatabase

        val cursor = db.query(
            "book",
            arrayOf("id", "book", "abbreviation", "name", "name_af"),
            "id = $book",
            null,
            null,
            null,
            "id ASC"
        )
        val books = mutableListOf<Book>()
        with(cursor) {
            while (moveToNext()) {
                books.add(
                    Book(
                        getLong(0),
                        getString(1),
                        getString(2),
                        getString(3),
                        getString(4)
                    )
                )
            }
        }
        return if (books.size == 0) null else books[0]
    }

    fun getBookFromChapterId(chapter:Long):Book? {
        val chapter = getChapterById(chapter) ?: return null
        val db = readableDatabase

        val cursor = db.query(
            "book",
            arrayOf("id", "book", "abbreviation", "name", "name_af"),
            "id = ${chapter.book}",
            null,
            null,
            null,
            "id ASC"
        )
        val books = mutableListOf<Book>()
        with(cursor) {
            while (moveToNext()) {
                books.add(
                    Book(
                        getLong(0),
                        getString(1),
                        getString(2),
                        getString(3),
                        getString(4)
                    )
                )
            }
        }
        return if (books.size == 0) null else books[0]
    }

    fun getChapterById(chapter:Long):Chapter? {
        val db = readableDatabase

        val cursor = db.query(
            "chapter",   // The table to query
            arrayOf("id", "book", "title"),
            "id = $chapter",
            null,
            null,
            null,
            "id ASC"
        )
        val chapters = mutableListOf<Chapter>()
        with(cursor) {
            while (moveToNext()) {
                chapters.add(
                    Chapter(
                        getLong(0),
                        getLong(1),
                        getString(2)
                    )
                )
            }
        }

        return if (chapters.size == 0) null else chapters[0]
    }
}