package fastcampus.part3.chapter05

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.CardStackView
import com.yuyakaido.android.cardstackview.Direction
import fastcampus.part3.chapter05.DBKey.Companion.NAME
import fastcampus.part3.chapter05.DBKey.Companion.USERS

class LikeActivity : AppCompatActivity() , CardStackListener {

    private var auth : FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var userDB : DatabaseReference

    private val adapter = CardItemAdapter()
    private val cardItems = mutableListOf<CardItem>()
    private val manager by lazy {
        CardStackLayoutManager(this , this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_like)

        // DB초기화
        userDB = Firebase.database.reference.child(USERS)

        val currentUserDB = userDB.child(getCurrentUserID())
        currentUserDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // 데이터 변경 시
                if (snapshot.child(NAME).value == null){
                    showNameInputPopup()
                    return
                }
                // 유저 정보 갱신
                getUnSelectedUsers()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })

        initCardStackView()
        initSignOutButton()
        initMatchedListButton()
    }

    private fun initSignOutButton(){
        val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this , MainActivity::class.java))
            finish()
        }
    }

    private fun initMatchedListButton(){
        val matchListButton = findViewById<Button>(R.id.matchListButton)
        matchListButton.setOnClickListener {
            startActivity(Intent(this , MatchedUserActivity::class.java))
        }
    }

    private fun getUnSelectedUsers() {

        userDB.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if(snapshot.child("userId").value != getCurrentUserID()
                    && snapshot.child("likedBy").child("like").hasChild(getCurrentUserID()).not()
                    && snapshot.child("likeBy").child("disLike").hasChild(getCurrentUserID()).not()){
                    // 현재 보고잇는 유저아이디가 나랑같지않고 상대방의 like에 내가없고 내 dislike에 상대가없으면
                    // 즉, 나랑 한번도 못만난 사람

                    val userId = snapshot.child("userId").value.toString()
                    var name = "undecided"

                    if(snapshot.child("name").value != null){
                        name = snapshot.child("name").value.toString()
                    }

                    cardItems.add(CardItem(userId , name))
                    adapter.submitList(cardItems)
                    adapter.notifyDataSetChanged()

                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                // 이름이 바뀌엇을 경우
                cardItems.find{ it.userId == snapshot.key }?.let{
                    it.name = snapshot.child("name").value.toString()
                }

                adapter.submitList(cardItems)
                adapter.notifyDataSetChanged()

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun initCardStackView() {
        val stackView = findViewById<CardStackView>(R.id.cardStackView)

        stackView.layoutManager = manager
        stackView.adapter = adapter

    }

    private fun showNameInputPopup() {

        val editText = EditText(this)

        AlertDialog.Builder(this)
            .setTitle("이름을 입력해주세요.")
            .setView(editText)
            .setPositiveButton("저장"){ _, _ ->
                if (editText.text.isEmpty()){
                    showNameInputPopup()
                }else{
                    saveUserName(editText.text.toString())
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun saveUserName(name: String) {

        val userId = getCurrentUserID()
        val currentUserDB = userDB.child(userId)
        val user = mutableMapOf<String , Any>()
        user["userId"] = userId
        user["name"] = name
        currentUserDB.updateChildren(user)

        // todo 유저정보를 가져와라라
        getUnSelectedUsers()
    }

    private fun getCurrentUserID() : String{
       if(auth.currentUser == null){
            Toast.makeText(this , "로그인이 되어있지 않습니다." , Toast.LENGTH_SHORT).show()
            finish()
        }

        return auth.currentUser?.uid.orEmpty()
    }

    private fun like(){
        val card = cardItems[manager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId)
            .child("likedBy")
            .child("like")
            .child(getCurrentUserID())
            .setValue(true)

        saveMatchIfOtherUserLikedMe(card.userId)
        // todo 매칭이 된 시점을 봐야한다.

        Toast.makeText(this , "${card.name}님을 Like 하셨습니다." , Toast.LENGTH_SHORT).show()

    }

    private fun disLike(){
        val card = cardItems[manager.topPosition - 1]
        cardItems.removeFirst()

        userDB.child(card.userId)
            .child("likedBy")
            .child("disLike")
            .child(getCurrentUserID())
            .setValue(true)

        Toast.makeText(this , "${card.name}님을 disLike 하셨습니다." , Toast.LENGTH_SHORT).show()

    }

    // 서로 like를 한 유저들
    private fun saveMatchIfOtherUserLikedMe(otherUserId : String){
        val otherUserDB = userDB.child(getCurrentUserID())
            .child("likedBy")
            .child("like")
            .child(otherUserId)
        // 내가 like를 한 상대방의 userId
        otherUserDB.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value == true){
                    userDB.child(getCurrentUserID())
                        .child("likedBy")
                        .child("match")
                        .child(otherUserId)
                        .setValue(true)

                    // 상대방의 DB에도 저장
                    userDB.child(otherUserId)
                        .child("likedBy")
                        .child("match")
                        .child(getCurrentUserID())
                        .setValue(true)
                }
            }

            override fun onCancelled(error: DatabaseError) { }

        })


    }

    override fun onCardDragging(direction: Direction?, ratio: Float) { }

    override fun onCardSwiped(direction: Direction?) {
        when(direction){
            Direction.Right -> like()
            Direction.Left -> disLike()
            else -> {}
        }
    }

    override fun onCardRewound() { }

    override fun onCardCanceled() { }

    override fun onCardAppeared(view: View?, position: Int) { }

    override fun onCardDisappeared(view: View?, position: Int) { }

}