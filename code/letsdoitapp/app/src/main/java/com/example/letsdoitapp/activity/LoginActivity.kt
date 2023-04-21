package com.example.letsdoitapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.example.letsdoitapp.R
import com.example.letsdoitapp.utils.Utility
import com.example.letsdoitapp.utils.ValidateEmail
import com.example.letsdoitapp.utils.ValidatePassword
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class LoginActivity : AppCompatActivity() {

    companion object {
        lateinit var useremail: String
        lateinit var providerSession: String

    }

    //Con by Delegates.notNull las variables no podran ser nulas
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbAcept: CheckBox
    private lateinit var lyTerms: LinearLayout
    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var dateRegister = SimpleDateFormat("dd/MM/yyyy").format(Date())
    private var RESULT_CODE_GOOGLE_SIGN_IN = 100
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var lyLoginActivity: LinearLayout
    private var hasEmailErrorShown = false
    private var hasPasswordErrorShown = false
    private var hasShownFormatDialog = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        useremail = "" //borramos los datos de usuario

        lyLoginActivity = findViewById(R.id.activity_login)
        lyTerms = findViewById(R.id.lyTerms) //Layout de Terminos
        lyTerms.visibility = View.INVISIBLE //Ocultamos el Layout de Terminos y condiciones
        etEmail = findViewById(R.id.etEmail) //Editext
        etPassword = findViewById(R.id.etPassword) //Editext
        cbAcept = findViewById<CheckBox>(R.id.cbAcept) //Checkbox
        mAuth = Firebase.auth //Autorizacion Firebase
        db = FirebaseFirestore.getInstance() //Instancia Firebase


        manageButtonLogin()
        etEmail.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && !hasShownFormatDialog) {
                //showFormatAlertDialog()
                hasShownFormatDialog = true
            }
        }
        etPassword.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && !hasShownFormatDialog) {
                //showFormatAlertDialog()
                hasShownFormatDialog = true
            }
        }

        etEmail.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
        etPassword.doOnTextChanged { text, start, before, count -> manageButtonLogin() }
    }

    private fun manageButtonLogin() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        val isValidEmail = ValidateEmail.isEmail(email)
        val isValidPassword = ValidatePassword.isPassword(password)

        if (email.isNotEmpty()) {
            if (!isValidEmail && !hasEmailErrorShown) {
                //Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT).show()
                //Utility.showCustomSnackbar(lyLoginActivity,"La contraseña no es válida",R.color.orange_strong, 2000)
                hasEmailErrorShown = true
            } else if (isValidEmail) {
                hasEmailErrorShown = false
            }
        }

        if (password.isNotEmpty()) {
            if (!isValidPassword && !hasPasswordErrorShown) {
                //Toast.makeText(this, "La contraseña no es válida", Toast.LENGTH_SHORT).show()
                //Utility.showCustomSnackbar(lyLoginActivity,"La contraseña no es válida",R.color.orange_strong, 2000)
                hasPasswordErrorShown = true
            } else if (isValidPassword) {
                hasPasswordErrorShown = false
            }
        }

        if (!isValidPassword || !isValidEmail) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_strong))
            tvLogin.isEnabled = true

            // Obtener el objeto InputMethodManager
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            // Ocultar el teclado virtual
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    fun passInfo(view: View) {
        showFormatAlertDialog()
    }

    private fun showFormatAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.formato))
            .setMessage(getString(R.string.correo))
            .setPositiveButton(R.string.acept) { dialog, which -> dialog.dismiss() }
            .show()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser != null) goHome(currentUser.email.toString(), currentUser.providerId)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }

    fun login(view: View) {
        loginUser()
    }


    private fun loginUser() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    goHome(email, "email")
                } else {
                    if (lyTerms.visibility == View.INVISIBLE) {
                        lyTerms.visibility = View.VISIBLE
                    } else {
                        if (cbAcept.isChecked) {
                            register()
                        }
                    }
                }
            }
    }

    private fun goHome(email: String, provider: String) {
        useremail = email
        providerSession = provider
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun register() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    db.collection("users").document(email).set(
                        hashMapOf(
                            "user" to email,
                            "dateRegister" to dateRegister,
                            "provider" to "email"
                        )
                    )
                    goHome(email, "email")
                } else
                    //showErrorMessage("Error, algo ha ido mal :(")
                    Utility.showCustomSnackbar(lyLoginActivity,"No se ha podido identificar el usuario",R.color.orange_strong, 2000)
            }
    }

    fun forgotPassword(view: View) {
        resetPassword()
    }

    private fun resetPassword() {
        val e = etEmail.text.toString()
        if (!TextUtils.isEmpty(e)) {
            mAuth.sendPasswordResetEmail(e)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        //showErrorMessage("Email Enviado a $e")
                    Utility.showCustomSnackbar(lyLoginActivity,"Email Enviado a $e",R.color.orange_strong, 2000)
                    else
                        //showErrorMessage("No se encontró el usuario con este correo")
                        Utility.showCustomSnackbar(lyLoginActivity,"No se encontró el usuario con este correo",R.color.orange_strong, 2000)
                }
        } else
            //showErrorMessage("Indica un email")
            Utility.showCustomSnackbar(lyLoginActivity,"Indica un email",R.color.orange_strong, 2000)
    }


    fun goTerms(v: View) {
        val intent = Intent(this, TermsActivity::class.java)
        startActivity(intent)
    }

    fun callSignInGoogle(view: View) {
        if (lyTerms.visibility == View.INVISIBLE) {
            lyTerms.visibility = View.VISIBLE
            //showErrorMessage("Aceptar los términos y reintentar")
            Utility.showCustomSnackbar(lyLoginActivity,"Aceptar los términos y reintentar",R.color.orange_strong, 2000)

        } else {  //Si no hay que aceptar los terminos
            if (cbAcept.isChecked) signInGoogle() // Si está marcado entramos con google
        }
    }

    private fun signInGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut() //  Enviamos un cierre de sesion por si hubiera una sesión iniciada de antes
        val singInIntent = googleSignInClient.signInIntent
        startActivityForResult(singInIntent, RESULT_CODE_GOOGLE_SIGN_IN)
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Se ha eliminado el uso del operador "!!" y se ha agregado una comprobación de
     * nulidad en la variable "account" antes de asignar el valor de "email". También se ha utilizado
     * el operador "?:" para proporcionar un valor por defecto en caso de que "email" sea null.
     * **/

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RESULT_CODE_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val email = account.email
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    mAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            db.collection("users").document(email ?: "").set(
                                hashMapOf(
                                    "user" to email,
                                    "dateRegister" to dateRegister,
                                    "provider" to "Google"
                                )
                            )
                            goHome(email ?: "", "Google")
                        } else
                            //showErrorMessage("Error en la conexión con Google")
                            Utility.showCustomSnackbar(
                                lyLoginActivity,
                                "Error en la conexión con Google",
                                R.color.orange_strong, 2000
                            )
                    }
                } else {
                    //showErrorMessage("No se encontró el usuario con este correo")
                    Utility.showCustomSnackbar(
                        lyLoginActivity,
                        "No se encontró el usuario con este correo",
                        R.color.orange_strong, 2000
                    )
                }
            } catch (e: ApiException) {
                //showErrorMessage("Error en la conexión con Google")
                Utility.showCustomSnackbar(
                    lyLoginActivity,
                    "Error en la conexión con Google",
                    R.color.orange_strong, 2000
                )
            }
        }
    }
}


/**Codigo con Toast Funcionando**/
/*
    private fun manageButtonLogin() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        val isValidEmail = ValidateEmail.isEmail(email)
        val isValidPassword = ValidatePassword.isPassword(password)

        if (email.isNotEmpty()) {
            if (!isValidEmail && !hasEmailErrorShown) {
                Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT)
                    .show()
                hasEmailErrorShown = true
            } else if (isValidEmail) {
                hasEmailErrorShown = false
            }
        }

        if (password.isNotEmpty()) {
            if (!isValidPassword && !hasPasswordErrorShown) {
                Toast.makeText(this, "La contraseña no es válida", Toast.LENGTH_SHORT).show()
                hasPasswordErrorShown = true
            } else if (isValidPassword) {
                hasPasswordErrorShown = false
            }
        }

        if (!isValidPassword || !isValidEmail) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_strong))
            tvLogin.isEnabled = true
        }
    }
*/
/*    private fun manageButtonLogin() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        var isValidEmail = ValidateEmail.isEmail(email)
        var isValidPassword = ValidatePassword.isPassword(password)

        if (!isValidPassword) {
            Toast.makeText(this, "La contraseña no es válida", Toast.LENGTH_SHORT).show()
        }

        if (!isValidEmail) {
            Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT).show()
        }

        if (!isValidPassword || !isValidEmail) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_strong))
            tvLogin.isEnabled = true
        }
    }*/
/*    private fun manageButtonLogin() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        if (!ValidatePassword.isPassword((password)) || !ValidateEmail.isEmail(email)) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_strong))
            tvLogin.isEnabled = true
        }
    }*/
/*    private fun manageButtonLogin() {
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        email = etEmail.text.toString()
        password = etPassword.text.toString()
        var isValidEmail = ValidateEmail.isEmail(email)
        var isValidPassword = ValidatePassword.isPassword(password)

        if (!isValidPassword) {
            Toast.makeText(this, "La contraseña no es válida", Toast.LENGTH_SHORT).show()
        }

        if (!isValidEmail) {
            Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT).show()
        }

        if (!isValidPassword || !isValidEmail) {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.gray))
            tvLogin.isEnabled = false
        } else {
            tvLogin.setBackgroundColor(ContextCompat.getColor(this, R.color.orange_strong))
            tvLogin.isEnabled = true
        }
    }*/

