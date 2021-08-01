package com.jasonoh.videochatingonebyonefromhowlyoutube

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.jasonoh.videochatingonebyonefromhowlyoutube.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    // todo :: 코틀린의 ::class 와 ::class.java의 차이점
    //  https://yoon-dailylife.tistory.com/46

    lateinit var binding: ActivityLoginBinding
    var googleSigninInClient: GoogleSignInClient? = null
    var GoogleLoginCode = 0

    val TAG = "LoginActivity"

    var launcher: ActivityResultLauncher<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        launcher = registerForActivityResult(SignInIntentContract()){ result: String? ->
            result?.let{
                firebaseAuthWithGoogle(it)  //tokenId를 이용해 firebase에 인증하는 함수 호출.
            }
        }

        binding.loginBtn.setOnClickListener{
            launcher!!.launch(getString(R.string.default_web_client_id))
        }
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 0)

//        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        googleSigninInClient = GoogleSignIn.getClient(this, gso)

//        binding.loginBtn.setOnClickListener {
//            var i = googleSigninInClient?.signInIntent
        // todo :: startActivityForResult --> deprecated
//            startActivityForResult(i, GoogleLoginCode)
//        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if(requestCode == GoogleLoginCode){
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            val account = task.getResult(ApiException::class.java)
//            val credential = GoogleAuthProvider.getCredential(account!!.idToken, null)
//
//            FirebaseAuth.getInstance().signInWithCredential(credential)
//                .addOnCompleteListener { task ->
//                    if(task.isSuccessful){
//                        Log.e(TAG, "LoginActivity -> onActivityResult: 로그인 성공공")
//                   }
//                }
//        }
//    }

    //tokenId를 이용해 firebase에 인증하는 함수.
    fun firebaseAuthWithGoogle(idToken: String) {
        //it가 tokenId, credential은 Firebase 사용자 인증 정보.
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        //Firebase 사용자 인증 정보(credential)를 사용해 Firebase에 인증.
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this@LoginActivity) { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this@LoginActivity, "로그인 성공", Toast.LENGTH_SHORT).show()

                    saveUserDataToDatabase(task.result!!.user)
                } else {
                    println("firebaseAuthWithGoogle => ${task.exception}")
                    Toast.makeText(
                        this@LoginActivity, "로그인 실패",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun saveUserDataToDatabase(user : FirebaseUser?){
        val email : String? = user?.email
        val uid = user?.uid

        var userDTO = UserDTO()
        userDTO.email = email

        FirebaseFirestore.getInstance().collection("users").document(uid!!).set(userDTO)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}