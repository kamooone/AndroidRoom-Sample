package com.example.roomsample

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.room.Room
import com.example.roomsample.data.db.TestDB
import com.example.roomsample.data.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // データベースを初期化
        initDatabase()

        // 読み込みボタン押したら読み込む
        load_button.setOnClickListener {
            loadDB()
        }

        // 書き込む
        add_button.setOnClickListener {
            writeDB()
        }
    }

    /** データベースを初期化する */
    private fun initDatabase() {
        val dbFile = getDatabasePath("test.db")
        if (!dbFile.exists()) {
            copyDatabaseFromAssets()
        }
    }

    /** アセットからデータベースをコピーする */
    private fun copyDatabaseFromAssets() {
        val inputStream: InputStream = assets.open("test.db")
        val outFile: File = getDatabasePath("test.db")
        outFile.parentFile?.mkdirs()
        val outputStream = FileOutputStream(outFile)

        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
    }

    /** データクラスに追加する */
    private fun writeDB() {
        val text = editText.text.toString()
        GlobalScope.launch {
            val database = Room.databaseBuilder(this@MainActivity, TestDB::class.java, "test.db")
                .build()
            val dao = database.userDao()
            val data = User(name = text)
            dao.insert(data)
        }
    }

    /** データベースから読み込む */
    private fun loadDB() {
        GlobalScope.launch(Dispatchers.Main) {
            name_textview.text = ""
            val list = withContext(Dispatchers.IO) {
                val database = Room.databaseBuilder(this@MainActivity, TestDB::class.java, "test.db")
                    .build()
                val dao = database.userDao()
                dao.getAll()
            }
            list.forEach {
                name_textview.append("${it.name}\n")
            }
        }
    }
}
