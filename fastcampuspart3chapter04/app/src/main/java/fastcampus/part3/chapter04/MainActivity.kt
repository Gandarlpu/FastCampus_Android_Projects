package fastcampus.part3.chapter04

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import fastcampus.adapter.BookAdapter
import fastcampus.adapter.HistoryAdapter
import fastcampus.api.BookService
import fastcampus.model.BestSellerDto
import fastcampus.model.History
import fastcampus.model.SearchBookDto
import fastcampus.part3.chapter04.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.Key

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var adapter : BookAdapter
    private lateinit var bookService : BookService // 북서비스 구현
    private lateinit var db : AppDatabase // DB초기화
    private lateinit var historyAdapter : HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBookRecyclerView()
        initHistoryRecyclerView()

        db = getAppDatabse(this)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://book.interpark.com")
            .addConverterFactory(GsonConverterFactory.create()) //gson
            .build()

        bookService = retrofit.create(BookService::class.java)

        // bestSeller API찍기
        bookService.getBestSellerBooks(getString(R.string.interparkAPIkey))
            .enqueue(object : Callback<BestSellerDto>{
                override fun onResponse(
                    call: Call<BestSellerDto>,
                    response: Response<BestSellerDto>
                ) {
                    // 성공
                    if(response.isSuccessful.not()){
                        return
                    }

                    response.body()?.let {
                        Log.d(TAG , it.toString())

                        it.books.forEach{ book ->
                            Log.d(TAG, book.toString())
                        }
                        // 리스트 채인지
                        adapter.submitList(it.books)
                    }
                }

                override fun onFailure(call: Call<BestSellerDto>, t: Throwable) {
                    // 실패

                }
            })


    }

    private fun initHistoryRecyclerView() {
        historyAdapter = HistoryAdapter(historyDeleteClickListener = {
            deleteSearchKeyword(it)
        })
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = historyAdapter

        initSearchEditText()
    }

    private fun initSearchEditText(){
        // 키보드에서 엔터를 눌럿을 떄 이벤트 처리(검색에서 엔터클릭 시)
        binding.searchEditText.setOnKeyListener { view, KeyCode, keyEvent ->
            // 엔터를 눌럿을 때(키 다운)와 뗏을 때 (키 업)이 입력된다.
            if(KeyCode == KeyEvent.KEYCODE_ENTER && keyEvent.action == MotionEvent.ACTION_DOWN){
                search(binding.searchEditText.text.toString())
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        binding.searchEditText.setOnTouchListener { view, event ->
            if(event.action == MotionEvent.ACTION_DOWN){
                showHistoryView()
            }
            return@setOnTouchListener false
        }

    }

    // 검색API
    private fun search(keyword: String) {
        bookService.getBooksByName(getString(R.string.interparkAPIkey) , keyword)
            .enqueue(object : Callback<SearchBookDto>{
                override fun onResponse(
                    call: Call<SearchBookDto>,
                    response: Response<SearchBookDto>
                ) {
                    // DB저장
                    hideHistoryView()
                    saveSearchKeyword(keyword)

                    // 성공
                    if(response.isSuccessful.not()){
                        return
                    }
                    adapter.submitList(response.body()?.books.orEmpty())

                }

                override fun onFailure(call: Call<SearchBookDto>, t: Throwable) {
                    // 실패
                    hideHistoryView()
                }
            })
    }



    private fun saveSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().insertHistory(History(null , keyword))
        }.start()
    }

    private fun deleteSearchKeyword(keyword: String) {
        Thread{
            db.historyDao().delete(keyword)
            showHistoryView()
        }.start()
    }

    private fun showHistoryView(){
        Thread{
            val keywords = db.historyDao().getAll().reversed()

            runOnUiThread{
                binding.historyRecyclerView.isVisible = true
                historyAdapter.submitList(keywords.orEmpty())
            }
        }.start()

        binding.historyRecyclerView.isVisible = true
    }

    private fun hideHistoryView(){
        binding.historyRecyclerView.isVisible = false
    }


    fun initBookRecyclerView(){
        adapter = BookAdapter(itemClickListener = {
            // intent시 class를 통째로 넘기려면 직렬화(@Parcelize)로 묶어서 넘겨주면 됨.
            val intent = Intent(this , DetailActivity::class.java)
            intent.putExtra("bookModel", it)
            startActivity(intent)
        })

        binding.bookRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookRecyclerView.adapter = adapter
    }

    companion object{
        private const val TAG = "MainActivity"
    }
}