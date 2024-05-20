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

        // 初回起動時はdbがないのでassetsからデータベースを端末内にコピーをする
        // ToDo:dbのバージョン更新時もコピーを行うようにする？
        if (!dbFile.exists()) {
            copyDatabaseFromAssets()
        }
    }

    /** アセットからデータベースをコピーする */
    private fun copyDatabaseFromAssets() {

        // InputStreamは、連続するデータを順次に必要な分だけ読み込むJavaの標準ライブラリのクラス(OutputStreamはデータを書き込むためのクラス)
        val inputStream: InputStream = assets.open("test.db")

        // アプリのデータベースディレクトリ内に "test.db" というファイルパスを取得します。
        val outFile: File = getDatabasePath("test.db")

        // 親ディレクトリが存在しない場合は、そのディレクトリを作成するために outFile.parentFile?.mkdirs() を呼び出します。
        outFile.parentFile?.mkdirs()

        // outFile にデータを書き込むための FileOutputStream オブジェクトを作成
        val outputStream = FileOutputStream(outFile)

        // 1024バイトごとにデータをコピーする
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } > 0) {
            outputStream.write(buffer, 0, length)
        }

        // 一部の特定の状況下では、データを即座に書き込みたい場合にのみ、flush()を使用します。
        // 例えば、プログラムの実行中にデータが不足している場合や、大量のデータを書き込む場合など
        outputStream.flush()

        // ファイルの書き込みが終了した後はファイルリソースを解放する
        outputStream.close()

        // 入力ストリームをクローズします。ファイルの読み取りが終了した後は、リソースリークを防ぐため
        inputStream.close()
    }

    /** データクラスに追加する */
    private fun writeDB() {
        // 入力フォームのテキストを取得
        val text = editText.text.toString()

        // GlobalScope.launch { ... } は、非同期の処理を実行するためのCoroutinesのスコープを作成
        GlobalScope.launch {

            // Roomデータベースのインスタンスを作成
            val database = Room.databaseBuilder(this@MainActivity, TestDB::class.java, "test.db")
                .build()

            // RoomのDAO（Data Access Object）を取得します。これにより、データベースへの操作が行われる
            val dao = database.userDao()

            // 新しいユーザーのデータオブジェクトを作成します。ここでは、入力されたテキストが名前として使用される
            val data = User(name = text)

            // 作成したデータオブジェクトをデータベースに挿入
            dao.insert(data)
        }
    }

    /** データベースから読み込む */
    private fun loadDB() {

        // GlobalScope.launch(Dispatchers.Main) {は非同期でUIを操作するためのスコープを作成
        GlobalScope.launch(Dispatchers.Main) {

            // 表示するテキストエリアを初期化
            name_textview.text = ""

            // このブロックはIOスレッド上で実行されます。データベースからの読み取りはIO処理なので、IOスレッドで実行される必要があります。
            // withContext関数は、指定されたコンテキスト（この場合はDispatchers.IO）でブロック内の処理を実行し、その結果を返します。
            // ※IO（Input/Output）スレッドは、主にファイル読み書き、ネットワーク通信、データベースアクセスなどの入出力操作を行うためのスレッド
            // ※非同期I/O（asynchronous I/O）とは、コンピュータ内部のCPUと周辺装置のデータ入出力（I/O）において、
            // データの送受信の完了を待たずに他の処理を開始する方式。処理の流れとI/Oが並行する並列処理の一種である。
            val list = withContext(Dispatchers.IO) {

                // Roomデータベースのインスタンスを構築します。databaseBuilderメソッドには、
                // アクティビティのコンテキスト、データベースクラス（TestDB::class.java）、
                // およびデータベースのファイル名（"test.db"）が渡されます。
                // ※TestDB は、Roomライブラリを使用してデータベースにアクセスするためのデータベースクラスです。
                // Roomでは、データベースアクセスを簡素化し、SQLiteデータベースとのやり取りを効率的に行うための高レベルの抽象化を提供します。
                // TestDB クラスは、RoomDatabase を継承し、データベースのインスタンスを作成するために使用されます。
                // このクラスを使用することで、データベースに対するクエリやトランザクションを実行し、アプリケーションのデータを永続化することができます。
                val database = Room.databaseBuilder(this@MainActivity, TestDB::class.java, "test.db")
                    .build()

                // RoomのDAO（Data Access Object）を取得します。これにより、データベースへの操作が行われます。
                val dao = database.userDao()

                // 作成したデータオブジェクトをデータベースに挿入します。
                dao.getAll()
            }

            // データベースから取得した User オブジェクトのリストをループして、それぞれのユーザーの名前を name_textview というテキストビューに表示
            list.forEach {
                name_textview.append("${it.name}\n")
            }
        }
    }
}
