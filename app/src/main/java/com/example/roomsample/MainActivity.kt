package com.example.roomsample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.roomsample.data.db.TestDB
import com.example.roomsample.data.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var editText: EditText
    private lateinit var add_button: Button
    private lateinit var load_button: Button
    private lateinit var name_textview: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText = findViewById(R.id.editText)
        add_button = findViewById(R.id.add_button)
        load_button = findViewById(R.id.load_button)
        name_textview = findViewById(R.id.name_textview)

        // データベースのセットアップ
        setupDatabase()

        // 読み込み
        loadDB()

        // 読み込みボタン押したら読み込む
        load_button.setOnClickListener {
            loadDB()
        }

        // 書き込む
        add_button.setOnClickListener {
            writeDB()
        }
    }

    private fun copyDatabaseFile() {
        val inputStream = assets.open("test.db")
        val outputStream = FileOutputStream(getDatabasePath("TestDB"))

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun setupDatabase() {
        copyDatabaseFile()
        val databasePath = getDatabasePath("TestDB")
        val database =
            Room.databaseBuilder(this@MainActivity, TestDB::class.java, databasePath.absolutePath)
                .build()
    }

    /** データクラスに追加する */
    private fun writeDB() {
        val text = editText.text.toString()
        GlobalScope.launch {
            // データベース用意。「TestDB」は実際に作られるデータベースのファイルの名前
            val databasePath = getDatabasePath("TestDB")
            val database =
                Room.databaseBuilder(this@MainActivity, TestDB::class.java, databasePath.absolutePath)
                    .build()
            val dao = database.userDao()
            // 書き込むデータクラス作る
            val data = User(name = text)
            // 書き込む
            dao.insert(data)
        }
    }

    /** データベースから読み込む */
    private fun loadDB() {
        GlobalScope.launch(Dispatchers.Main) {
            // まっさらに
            name_textview.text = ""
            // UIスレッドでは実行できないためコルーチン
            val list = withContext(Dispatchers.IO) {
                // データベース用意
                val databasePath = getDatabasePath("TestDB")
                val database =
                    Room.databaseBuilder(this@MainActivity, TestDB::class.java, databasePath.absolutePath)
                        .build()
                val dao = database.userDao()
                dao.getAll()
            }
            // TextViewに表示
            list.forEach {
                name_textview.append("${it.name}\n")
            }
        }
    }
}
