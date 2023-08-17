package com.example.wildrunning

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.properties.Delegates

class LoginActivity : AppCompatActivity() {
    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String
        private lateinit var context: Context

    }
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private var confirmPassword  by Delegates.notNull<String>()
    private lateinit var etEmail:EditText
    private lateinit var tilPassword:TextInputLayout
    private lateinit var tilConfimPassword:TextInputLayout
    private lateinit var etPassword:TextInputEditText
    private lateinit var etConfirmPassword:TextInputEditText
    private lateinit var lyTerms:LinearLayout

   lateinit var mAuth: FirebaseAuth
    private var RESULT_CODE_GOOGLE_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        lyTerms = findViewById(R.id.lyTerms)
        lyTerms.visibility = View.INVISIBLE
        tilPassword= findViewById(R.id.tilPassword)
        tilConfimPassword = findViewById(R.id.tilConfirmPassword)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tilConfimPassword.visibility= View.GONE
        etConfirmPassword.visibility= View.GONE

        mAuth = FirebaseAuth.getInstance()
        
        manageButtonLogin()
            etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin()}
            etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin()}

        

    }

    override fun onStart() {
        super.onStart()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser !=null){
            goHome(currentUser.email.toString(),currentUser.providerId)
        }
    }

    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
    private fun manageButtonLogin(){
        var tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        if(TextUtils.isEmpty(password) || ValidateEmail.isEmail(email) == false ){
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false

        }else{
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
            tvLogin.isEnabled = true

        }
    }
    private fun confirmPassword(){

    }
    fun login(view: View){
        loginUser()
    }



    private fun loginUser(){
        email = etEmail.text.toString()
        password= etPassword.text.toString()
        confirmPassword = etConfirmPassword.text.toString()
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this){task ->
            if(task.isSuccessful) goHome(email,"email")
            else{
                if(lyTerms.visibility == View.INVISIBLE){
                    lyTerms.visibility = View.VISIBLE
                    tilConfimPassword.visibility=View.VISIBLE
                    etConfirmPassword.visibility= View.VISIBLE
                }
                else {
                    if( password == confirmPassword ){
                        var cbAcept = findViewById<CheckBox>(R.id.cbAcept)
                        if(cbAcept.isChecked) {
                            register()
                        }else Toast.makeText(this,"Acepte los terminos y condiciones para usar nuestra aplicación",Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this,"No coinciden la contraseña", Toast.LENGTH_SHORT).show()
                    }

                }
            }

        }

    }
    private fun goHome(email:String,provider:String){
        useremail = email
        providerSession = provider
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
    private fun register(){
        email = etEmail.text.toString()
        password= etPassword.text.toString()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener{
            if(it.isSuccessful){
                var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
                var dbRegister = FirebaseFirestore.getInstance()
                dbRegister.collection("users").document(email).set(hashMapOf(
                    "user" to email,
                    "dateRegister" to dateRegister
                ))
                goHome(email,"email")
            }
            else Toast.makeText(this,"Error, algo ha ido mal :(",Toast.LENGTH_SHORT).show()
        }

    }
    fun goTerms(v: View){
        val intent = Intent(this, TermsActivity::class.java)
    }
    fun forgotPassword(v: View){
    //   val intent = Intent(this, ForgotPasswordActivity::class.java)
        resetPassword()
    }
    private fun resetPassword() {
        var e= etEmail.text.toString()
        if(!TextUtils.isEmpty(e)){
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) Toast.makeText(this,"Email Enviado a $e",Toast.LENGTH_SHORT).show()
                    else Toast.makeText(this,"No se encontró el usuario con este correo",Toast.LENGTH_SHORT).show()

                }
         }
        else Toast.makeText(this,"Indica un email",Toast.LENGTH_SHORT).show()

    }

    fun callSignInGoogle(view:View){
        signInGoogle()
    }
    private fun signInGoogle(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        var googleSignInClient = GoogleSignIn.getClient(this, gso)

        startActivityForResult(googleSignInClient.signInIntent, RESULT_CODE_GOOGLE_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_GOOGLE_SIGN_IN) {

            try {
                // Google Sign In was successful, authenticate with Firebase
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                if(account != null){
                    email = account.email!!
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information

                                val user = mAuth.currentUser
                                goHome(email,"Google")
                            } else {
                                // If sign in fails, display a message to the user.

                            }
                        }
                }

            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
              Toast.makeText(this,"Error en la conexion con Google",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {

    }
}
