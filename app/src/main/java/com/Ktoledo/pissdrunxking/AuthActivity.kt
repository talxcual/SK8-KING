package com.Ktoledo.pissdrunxking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore 
import com.google.firebase.firestore.ktx.firestore 
import com.google.firebase.auth.FirebaseUser 

class AuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore 

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleSignIn: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = Firebase.auth
        db = Firebase.firestore 

        val ivSk8KingGif = findViewById<ImageView>(R.id.ivSk8KingGif)
        Glide.with(this)
            .asGif()
            .load(R.drawable.sk8king)
            .into(ivSk8KingGif)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    Log.d("AuthActivity", "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("AuthActivity", "Google sign in failed", e)
                    val errorMessage = when (e.statusCode) {
                        GoogleSignInStatusCodes.CANCELED -> getString(R.string.google_signin_cancelled)
                        GoogleSignInStatusCodes.NETWORK_ERROR -> getString(R.string.google_signin_network_error)
                        else -> getString(R.string.google_signin_failed_generic, e.message)
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    tvStatus.text = errorMessage
                }
            } else {
                Toast.makeText(this, getString(R.string.google_signin_cancelled), Toast.LENGTH_SHORT).show()
                tvStatus.text = getString(R.string.google_signin_cancelled)
            }
        }

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        tvStatus = findViewById(R.id.tvStatus)

        btnRegister.setOnClickListener {
            registerUser()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserDataToFirestore(user, user.displayName ?: user.email ?: "Usuario Google") // Usar displayName de Google si existe
                        Toast.makeText(this, getString(R.string.google_auth_success), Toast.LENGTH_SHORT).show()
                        tvStatus.text = getString(R.string.google_auth_success_email, user.email)
                        navigateToMissionsList()
                    } else {
                        Toast.makeText(this, "Usuario Google nulo después de la autenticación.", Toast.LENGTH_SHORT).show()
                        tvStatus.text = "Error: Usuario Google nulo."
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthUserCollisionException -> getString(R.string.google_auth_collision_error)
                        else -> getString(R.string.google_auth_firebase_failed_generic, exception?.message)
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    tvStatus.text = errorMessage
                }
            }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        etEmail.error = null
        etPassword.error = null

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.error_email_empty)
            etEmail.requestFocus()
            tvStatus.text = getString(R.string.error_email_empty_status)
            return
        }
        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_empty)
            etPassword.requestFocus()
            tvStatus.text = getString(R.string.error_password_empty_status)
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserDataToFirestore(user, email.substringBefore("@")) // Usar la parte del email antes del @ como nombre inicial
                        Toast.makeText(this, getString(R.string.register_success, user.email), Toast.LENGTH_SHORT).show()
                        tvStatus.text = getString(R.string.register_success_status, user.email)
                        navigateToMissionsList()
                    } else {
                        Toast.makeText(this, "Usuario registrado nulo.", Toast.LENGTH_SHORT).show()
                        tvStatus.text = "Error: Usuario registrado nulo."
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthWeakPasswordException -> {
                            etPassword.error = getString(R.string.error_password_weak_et)
                            etPassword.requestFocus()
                            getString(R.string.error_password_weak_toast)
                        }
                        is FirebaseAuthInvalidCredentialsException -> {
                            etEmail.error = getString(R.string.error_email_invalid_et)
                            etEmail.requestFocus()
                            getString(R.string.error_credentials_invalid_toast)
                        }
                        is FirebaseAuthUserCollisionException -> {
                            etEmail.error = getString(R.string.error_email_in_use_et)
                            etEmail.requestFocus()
                            getString(R.string.error_email_in_use_toast)
                        }
                        else -> getString(R.string.register_failed_generic, exception?.message)
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    tvStatus.text = getString(R.string.register_failed_status, errorMessage)
                }
            }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        etEmail.error = null
        etPassword.error = null

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.error_email_empty)
            etEmail.requestFocus()
            tvStatus.text = getString(R.string.error_email_empty_status)
            return
        }
        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_password_empty)
            etPassword.requestFocus()
            tvStatus.text = getString(R.string.error_password_empty_status)
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // En el login, podrías guardar datos si no existen o actualizarlos.
                        // Por ahora, solo navegamos, asumiendo que los datos ya se guardaron en el registro.
                        // Si quieres guardar/actualizar en login, deberíamos verificar si el documento ya existe.
                        Toast.makeText(this, getString(R.string.login_success, user.email), Toast.LENGTH_SHORT).show()
                        tvStatus.text = getString(R.string.login_success_status, user.email)
                        navigateToMissionsList()
                    } else {
                        Toast.makeText(this, "Usuario de inicio de sesión nulo.", Toast.LENGTH_SHORT).show()
                        tvStatus.text = "Error: Usuario de inicio de sesión nulo."
                    }
                } else {
                    val exception = task.exception
                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            getString(R.string.error_credentials_invalid_toast)
                        }
                        else -> getString(R.string.login_failed_generic, exception?.message)
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    tvStatus.text = getString(R.string.login_failed_status, errorMessage)
                }
            }
    }

    // <-- ¡NUEVO MÉTODO! Para guardar los datos del usuario en Firestore
    private fun saveUserDataToFirestore(user: FirebaseUser, userName: String) {
        val userId = user.uid
        val userEmail = user.email
        val userPhotoUrl = user.photoUrl?.toString() // Obtener URL de foto de perfil (principalmente para Google)

        val userData = hashMapOf(
            "uid" to userId,
            "email" to userEmail,
            "name" to userName,
            "photoUrl" to userPhotoUrl, // Puede ser nulo
            "createdAt" to System.currentTimeMillis() // Marca de tiempo de creación
            // Puedes añadir más campos aquí, como rol, fecha de nacimiento, etc.
        )

        // Referencia a la colección "users" y al documento con el UID del usuario
        db.collection("users").document(userId)
            .set(userData) // set() sobrescribe si existe, o crea si no existe
            .addOnSuccessListener {
                Log.d("AuthActivity", "Datos de usuario guardados en Firestore para UID: $userId")
                // No mostramos Toast aquí para no saturar al usuario, ya se muestra el de registro/login
            }
            .addOnFailureListener { e ->
                Log.w("AuthActivity", "Error al guardar datos de usuario en Firestore", e)
                Toast.makeText(this, "Error al guardar datos de usuario: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToMissionsList() {
        val intent = Intent(this, MissionsListActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Cuando el usuario ya está autenticado, podemos cargar sus datos de Firestore si los necesitamos.
            // Por ahora, solo navegamos, ya que los datos se guardan en registro/primer login.
            navigateToMissionsList()
        }
    }
}