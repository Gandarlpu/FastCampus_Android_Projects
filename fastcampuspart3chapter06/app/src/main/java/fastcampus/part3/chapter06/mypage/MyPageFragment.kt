package fastcampus.part3.chapter06.mypage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import fastcampus.part3.chapter06.R
import fastcampus.part3.chapter06.databinding.FragmentMypageBinding
import kotlin.math.sign

class MyPageFragment : Fragment(R.layout.fragment_mypage) {

    private var binding : FragmentMypageBinding? = null
    private val auth : FirebaseAuth by lazy {
        Firebase.auth
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentMypageBinding = FragmentMypageBinding.bind(view)
        binding = fragmentMypageBinding

        fragmentMypageBinding.signInOutButton.setOnClickListener {
            binding?.let{ binding ->
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                if(auth.currentUser == null){
                    // 로그인
                    auth.signInWithEmailAndPassword(email , password)
                        .addOnCompleteListener(requireActivity()) { task ->
                            //addOnComplete의 매개변수에는 activity를 넣어야 되는데
                            // Activity는 null이 될수도 잇기 때문에 requireActivity를 넣어야 한다.
                            // null이 들어오지 않는다고 확신하면 requireActivity를 쓰지만
                            // 그렇지 않다면 activiry?.let{}으로 null처리를 해주자
                            if(task.isSuccessful){
                                successSignIn()
                            }else{
                                Toast.makeText(context , "로그인 실패" , Toast.LENGTH_SHORT).show()
                            }

                        }

                }else{
                    // 로그아웃
                    auth.signOut()
                    binding.emailEditText.text.clear()
                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.text.clear()
                    binding.passwordEditText.isEnabled = true

                    binding.signInOutButton.text = "로그인"
                    binding.signInOutButton.isEnabled = true
                    binding.signUpButton.isEnabled = false
                }

            }
        }


        fragmentMypageBinding.signUpButton.setOnClickListener {
            binding?.let{ binding ->
                val email = binding.emailEditText.text.toString()
                val password = binding.passwordEditText.text.toString()

                auth.createUserWithEmailAndPassword(email , password)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if(task.isSuccessful){
                            Toast.makeText(context , "회원가입 성공" , Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context , "회원가입 실패" , Toast.LENGTH_SHORT).show()

                        }
                    }

            }
        }

        // enabled버튼 활성, 비활성하기 위한 코드
        fragmentMypageBinding.emailEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                binding.signInOutButton.isEnabled = enable
            }
        }

        fragmentMypageBinding.passwordEditText.addTextChangedListener {
            binding?.let { binding ->
                val enable = binding.emailEditText.text.isNotEmpty() && binding.passwordEditText.text.isNotEmpty()
                binding.signUpButton.isEnabled = enable
                binding.signInOutButton.isEnabled = enable
            }
        }

    }

    override fun onStart() {
        super.onStart()

        if(auth.currentUser == null){
            // 로그인 X
            binding?.let { binding ->
                binding.emailEditText.text.clear()
                binding.passwordEditText.text.clear()
                binding.emailEditText.isEnabled = true
                binding.passwordEditText.isEnabled = true

                binding.signInOutButton.text = "로그인"
                binding.signInOutButton.isEnabled = false
                binding.signUpButton.isEnabled = false
            }
        }else{
            // 로그인 O
            binding?.let { binding ->
                binding.emailEditText.setText(auth.currentUser!!.email)
                binding.passwordEditText.setText("**********")
                binding.emailEditText.isEnabled = false
                binding.passwordEditText.isEnabled = false

                binding.signInOutButton.text = "로그아웃"
                binding.signInOutButton.isEnabled = true
                binding.signUpButton.isEnabled = false
            }
        }
    }

    private fun successSignIn() {
        if(auth.currentUser == null){
            Toast.makeText(context , "로그인 실패" , Toast.LENGTH_SHORT).show()
            return
        }

        binding?.emailEditText?.isEnabled = false
        binding?.passwordEditText?.isEnabled = false
        binding?.signUpButton?.isEnabled = false
        binding?.signInOutButton?.text = "로그아웃"

    }
}