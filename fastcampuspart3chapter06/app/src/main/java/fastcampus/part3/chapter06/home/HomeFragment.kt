package fastcampus.part3.chapter06.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import fastcampus.part3.chapter06.R
import fastcampus.part3.chapter06.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding : FragmentHomeBinding? = null
    private lateinit var articleAdapter : ArticleAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Activity의 onCreate == onViewCreated

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding
        // binding이라는 전역변수가 nullable이기 때문에 사용할 때마다 null을 풀어주야함
        // 따라서 절대 null이 될 수 없도록 지역변수로 설정

        articleAdapter = ArticleAdapter()
        articleAdapter.submitList(mutableListOf<ArticleModel>().apply {
            add(ArticleModel("0","aaa",1000000,"5000원",""))
            add(ArticleModel("0","bbb",2000000,"2000원",""))
        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        // Activity는 context가 가능하지만 Fragment는 불가능
        // 따라서 getContext해야함 = 코틀린은 get생략 가능 = context
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

    }


}