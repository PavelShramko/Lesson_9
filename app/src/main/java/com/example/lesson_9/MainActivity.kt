package com.example.lesson_9

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.example.lesson_9.databinding.ActivityMainBinding
import java.io.File
import java.lang.Exception
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {


    companion object{
        private const val STR_KEY = "STR_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val eText = binding.setText.text

        val prefs = getPreferences(MODE_PRIVATE)

        binding.setSp.setOnClickListener {
            if(binding.eText.text.isNotEmpty()){
                prefs.edit().apply {
                    putString(STR_KEY, binding.eText.text.toString())
                    apply()
                    binding.eText.text.clear()
                }
                textSet()
            }
            else textIsEmpty()
        }

        binding.getSp.setOnClickListener {
            binding.textView.text = prefs.getString(STR_KEY, "")
            textGot()
        }


        val file = File(filesDir, "text.txt")


        binding.setIn.setOnClickListener {
            if(binding.eText.text.isNotEmpty()) {
                try {
                    val output = file.outputStream()
                    output.write(binding.eText.text.toString().toByteArray())
                    output.close()
                    textSet()
                    binding.eText.text.clear()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            } else textIsEmpty()
        }

        binding.getIn.setOnClickListener {
            try {
                val input = file.inputStream()
                binding.textView.text = input.readBytes().decodeToString()
                input.close()
                textGot()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }


        val db = Room.databaseBuilder(this, WordsDatabase::class.java, "database")
            .fallbackToDestructiveMigration()
            .build()
        val message: MutableLiveData<String?> = MutableLiveData()

        binding.setBD.setOnClickListener {
            if(binding.eText.text.isNotEmpty()) {
                thread {
                    db.getsWordsDao().setWord(binding.eText.text.toString())
                }
                runOnUiThread {
                    binding.eText.text.clear()
                    textSet()
                }
            } else textIsEmpty()
        }

        binding.getBD.setOnClickListener {
            thread {
                message.postValue(db.getsWordsDao().getAllWord().toString())
            }
            binding.textView.text = message.value.toString()
            textGot()
        }

        binding.setEx.setOnClickListener {
            if (binding.eText.text.isNotEmpty()) {
                if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                    Log.d("LOG_TAG", "SD-карта не доступна: " + Environment.getExternalStorageState());
                }
                try {
                    val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            "text").outputStream()
                    file.write(binding.eText.text.toString().toByteArray())
                    file.close()
                    binding.eText.text.clear()
                    textSet()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else textIsEmpty()
        }


        binding.getEx.setOnClickListener{
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                Log.d("LOG_TAG", "SD-карта не доступна: " + Environment.getExternalStorageState());
            }
                try {
                    val file = File(this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                            "text").inputStream()
                    binding.textView.text = file.readBytes().decodeToString()
                    file.close()
                    textGot()
                }
                catch (e : Exception) {
                    e.printStackTrace()
                }
            }


    }



    private fun textSet(){
        Toast.makeText(this, "Текст сохранен", Toast.LENGTH_SHORT).show()
    }

    private fun textIsEmpty(){
        Toast.makeText(this, "Текст пустой, сохраните текст", Toast.LENGTH_SHORT).show()
    }

    private fun textGot(){
        Toast.makeText(this, "Текст показан", Toast.LENGTH_SHORT).show()
    }
}
    @Entity(tableName = "wordsTable")
    data class Word(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,
        val text: String
    )
    @Dao
    abstract class WordsDao {

        @Insert
        abstract fun insertWord(word: Word)

        @Query("SELECT * from wordsTable")
        abstract fun getAllWord(): Word

        @Query("INSERT OR REPLACE INTO wordsTable(id,text) VALUES (0, :text)")
        abstract fun setWord(text: String)
    }

    @Database(entities = [Word::class], version = 1)
    abstract class WordsDatabase: RoomDatabase(){
        abstract fun getsWordsDao(): WordsDao
    }



