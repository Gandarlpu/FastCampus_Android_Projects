package fastcampus.part3.chapter06.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import fastcampus.part3.chapter06.DBKey.Companion.CHILD_CHAT
import fastcampus.part3.chapter06.DBKey.Companion.DB_ARTICLES
import fastcampus.part3.chapter06.DBKey.Companion.DB_USERS
import fastcampus.part3.chapter06.R
import fastcampus.part3.chapter06.chatList.ChatListItem
import fastcampus.part3.chapter06.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var binding : FragmentHomeBinding? = null
    private lateinit var articleAdapter : ArticleAdapter
    private lateinit var articleDB : DatabaseReference
    private lateinit var userDB : DatabaseReference


    private val articleList = mutableListOf<ArticleModel>()
    private val listener = object : ChildEventListener{
        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            // ArticleAddActivity에서 set해둔 DB정보들을 get
            val articleModel = snapshot.getValue(ArticleModel::class.java)
            articleModel ?: return

            // 리스트 추가
            articleList.add(articleModel)
            articleAdapter.submitList(articleList)
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

        }

        override fun onChildRemoved(snapshot: DataSnapshot) {}

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

        override fun onCancelled(error: DatabaseError) { }

    }

    private val auth : FirebaseAuth by lazy {
        Firebase.auth
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Activity의 onCreate == onViewCreated

        val fragmentHomeBinding = FragmentHomeBinding.bind(view)
        binding = fragmentHomeBinding
        // binding이라는 전역변수가 nullable이기 때문에 사용할 때마다 null을 풀어주야함
        // 따라서 절대 null이 될 수 없도록 지역변수로 설정

        // DB가져오기
        articleList.clear() // HomeFragment인스턴스자체를 초기화
        userDB = Firebase.database.reference.child(DB_USERS)
        articleDB = Firebase.database.reference.child(DB_ARTICLES)
        articleAdapter = ArticleAdapter(onItemCLicked = { articleModel ->
            if(auth.currentUser != null){
                //로그인 한 상태
                if(auth.currentUser!!.uid != articleModel.sellerId){
                    val chatRoom = ChatListItem(
                        buyerId = auth.currentUser!!.uid,
                        sellerId = articleModel.sellerId,
                        itemTitle = articleModel.title,
                        key = System.currentTimeMillis()
                    )

                    userDB.child(auth.currentUser!!.uid)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    userDB.child(articleModel.sellerId)
                        .child(CHILD_CHAT)
                        .push()
                        .setValue(chatRoom)

                    Snackbar.make(view , "채팅방이 생성되었습니다. 채팅탭에서 확인해주세요." , Snackbar.LENGTH_LONG).show()

                }else{
                    Snackbar.make(view , "내가 올린 아이템입니다." , Snackbar.LENGTH_LONG).show()
                }

            }else{
                //로그인 안한 상태
                Snackbar.make(view , "로그인 후 사용해주세요." , Snackbar.LENGTH_LONG).show()
            }


        })

        fragmentHomeBinding.articleRecyclerView.layoutManager = LinearLayoutManager(context)
        // Activity는 context가 가능하지만 Fragment는 불가능
        // 따라서 getContext해야함 = 코틀린은 get생략 가능 = context
        fragmentHomeBinding.articleRecyclerView.adapter = articleAdapter

        fragmentHomeBinding.addFloatingButton.setOnClickListener {
            context?.let{
                if(auth.currentUser != null){
                    val intent = Intent(requireContext() , ArticleAddActivity::class.java)
                    startActivity(intent)
                }else{
                    Snackbar.make(view , "로그인 후 사용해주세요." , Snackbar.LENGTH_LONG).show()
                }
            }
        }

        // DB데이터 가져오기
        // ChildEventListener는 DB가 추가될 때마다 반영하는데, Fragment는 재활용되는 시스템이기 때문에
        // 페이지를 옮겻다가 다시 돌아오면 중복되서 붙여질 가능성이 있기 떄문에
        // 전역으로 선언 후, onCreate때 만들고 onDestroy때 remove하도록 설정하자.
        articleDB.addChildEventListener(listener)
    }

    override fun onResume() {
        // 뷰를 다시 볼 때마다 그려줌
        super.onResume()

        articleAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        // 뷰를 안 볼때 지워줌
        super.onDestroy()

        articleDB.removeEventListener(listener)
    }


}