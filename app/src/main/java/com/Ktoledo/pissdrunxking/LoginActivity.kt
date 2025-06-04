package com.Ktoledo.pissdrunxking

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnRegister: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializar vistas
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnRegister = findViewById(R.id.btnRegister)

        // Inicializar SharedPreferences para guardar las credenciales
        // MODE_PRIVATE significa que solo esta app puede acceder a estos datos
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // --- Lógica del botón de Registro ---
        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa un nombre de usuario y contraseña.", Toast.LENGTH_SHORT).show()
            } else {
                // Guardar las credenciales usando SharedPreferences.Editor
                val editor = sharedPreferences.edit()
                editor.putString("username", username)
                editor.putString("password", password)
                editor.apply() // Guarda los cambios de forma asíncrona

                Toast.makeText(this, "¡Registro exitoso! Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
            }
        }

        // --- Lógica del botón de Iniciar Sesión ---
        btnLogin.setOnClickListener {
            val inputUsername = etUsername.text.toString().trim()
            val inputPassword = etPassword.text.toString().trim()

            // Obtener las credenciales guardadas
            val savedUsername = sharedPreferences.getString("username", null)
            val savedPassword = sharedPreferences.getString("password", null)

            if (inputUsername == savedUsername && inputPassword == savedPassword && savedUsername != null) {
                Toast.makeText(this, "¡Inicio de sesión exitoso!", Toast.LENGTH_SHORT).show()
                // Si el login es exitoso, navegar a MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Cierra LoginActivity para que el usuario no pueda volver con el botón de atrás
            } else {
                Toast.makeText(this, "Credenciales incorrectas o usuario no registrado.", Toast.LENGTH_LONG).show()
            }
        }

        // Opcional: Si ya hay credenciales guardadas, podrías auto-rellenar los campos
        // val lastUsername = sharedPreferences.getString("username", "")
        // etUsername.setText(lastUsername)
    }
}