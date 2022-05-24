package fastcampus.part3.chapter05

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        // FirebaseAuth.getInstance 랑 같은 의미

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)


        // 범위지정+Ctrl+Alt+M 단축키 = 메소드 만들기
        initLoginButton()
        initSignUpButton()
        // EditText가 null일 때 auth메소드에 그냥 넘기면 에러가 발생할 수 있다. 따라서 예외처리해주자
        initEmailAndPasswordEditText()

    }



    private fun initLoginButton() {
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.signInWithEmailAndPassword(email , password)
                .addOnCompleteListener(this) { task ->
                    // 반환값이 Task<AuthResult>이므로 addOnCompleteListener
                    if(task.isSuccessful){
                        // 성공 시 FirebaseAuth에 저장될 것이고 LoginActivity의 역할은 끝
                        handleSuccessLogin()
                    }else{
                        Toast.makeText(this , "로그인 실패" , Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }


    private fun initSignUpButton() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.createUserWithEmailAndPassword(email , password)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this , "회원가입 성공" , Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this , "회원가입 실패" , Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun initEmailAndPasswordEditText() {
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        emailEditText.addTextChangedListener {
            // 입력될때마다 감지지
            val enable = emailEditText.text.isNotEmpty() and passwordEditText.text.isNotEmpty()

            signUpButton.isEnabled = enable
            loginButton.isEnabled = enable

        }

        passwordEditText.addTextChangedListener {
            // 입력될때마다 감지지
            val enable = emailEditText.text.isNotEmpty() and passwordEditText.text.isNotEmpty()

            signUpButton.isEnabled = enable
            loginButton.isEnabled = enable

        }

    }

    private fun getInputEmail() : String{
        return findViewById<EditText>(R.id.emailEditText).text.toString()
    }
    private fun getInputPassword() : String{
        return findViewById<EditText>(R.id.passwordEditText).text.toString()
    }

    private fun handleSuccessLogin(){
        if(auth.currentUser == null){
            Toast.makeText(this , "로그인에 실패했습니다." , Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid.orEmpty()
        val currentUserDB = Firebase.database.reference.child("Users").child(userId)
        val user = mutableMapOf<String , Any>()
        user["userId"] = userId
        currentUserDB.updateChildren(user)

        finish()

    }

}