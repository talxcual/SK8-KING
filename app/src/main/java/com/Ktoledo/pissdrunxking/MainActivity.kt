// Archivo: MainActivity.kt
package com.Ktoledo.pissdrunxking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.Ktoledo.pissdrunxking.PDKspot.MissionState // Importar el enum MissionState

class MainActivity : AppCompatActivity() {

    private lateinit var pdkGestor: PDKMisionManager // Declarar el gestor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar el gestor de misiones utilizando el método Singleton getInstance()
        pdkGestor = PDKMisionManager.getInstance(this) //

        // --- IMPORTANTE: Bloque para añadir misiones por defecto o iniciales ---
        // Tu PDKMisionManager ya tiene una lógica robusta en `createDefaultMisiones()`
        // que se ejecuta si `allMisiones` está vacío al cargar.
        // Por lo tanto, no necesitas agregar las misiones por defecto aquí de nuevo.
        // Si tu intención es agregar MISIONES NUEVAS y que no sean parte de los defaults,
        // entonces el siguiente bloque sería el lugar adecuado, pero NO para las que ya están en createDefaultMisiones().

        // Ejemplo de cómo añadir una NUEVA misión (si la misión no existe ya como default)
        // Asegúrate de que el ID sea único para evitar problemas.
        /*
        val newMissionId = "mision_extra_01" // ID único para esta misión
        val existingMissions = pdkGestor.pdkGetMisionesDisponibles() +
                               pdkGestor.pdkGetMisionesAceptadasPorUsuario() +
                               pdkGestor.pdkGetMisionesCompletadasPorUsuario()

        // Solo añade si el ID de esta misión no existe ya en ninguna de las listas
        if (existingMissions.none { it.id == newMissionId }) {
            val nuevaMisionPersonalizada = PDKspot(
                newMissionId, // **ID ÚNICO y requerido**
                "Spot Secreto del Parque",
                "Las Condes",
                "Kickflip al bajón de 5 escalones",
                "Experto",
                "Tabla de skate profesional",
                "https://ejemplo.com/spot_secreto.jpg", // Asegúrate de que sea una URL real o manejada
                null, // null si aún no ha sido aceptada por nadie
                0,    // 0 aceptaciones globales al inicio
                MissionState.AVAILABLE // Estado inicial
            )
            // Llama al método correcto del gestor para añadir una misión al pool global
            pdkGestor.pdkAddMissionToGlobalPool(nuevaMisionPersonalizada) //
            Log.d("MainActivity", "Nueva misión 'Spot Secreto del Parque' añadida al pool global.")
        }
        */

        // Referencias a los botones de la interfaz (asegúrate que existan en activity_main.xml)
        val btnLogin: Button = findViewById(R.id.btnLogin)
        val btnViewAvailableMissions: Button = findViewById(R.id.btnViewAvailableMissions)
        val btnViewAcceptedMissions: Button = findViewById(R.id.btnViewAcceptedMissions)
        val btnClearData: Button = findViewById(R.id.btnClearData) // Asegúrate que este botón exista o coméntalo.

        // Listener para el botón de Login (si esta MainActivity es una pantalla de "menú" después del login)
        btnLogin.setOnClickListener {
            // Este botón debería redirigir a AuthActivity
            Toast.makeText(this, "Redirigiendo a pantalla de autenticación...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish() // Finaliza MainActivity para que no quede en la pila
        }

        // Listener para ver misiones disponibles (redirige a MissionsListActivity)
        btnViewAvailableMissions.setOnClickListener {
            val intent = Intent(this, MissionsListActivity::class.java)
            startActivity(intent)
        }

        // Listener para ver misiones aceptadas (redirige a MissionsListActivity)
        btnViewAcceptedMissions.setOnClickListener {
            val intent = Intent(this, MissionsListActivity::class.java)
            startActivity(intent)
        }

        // Listener para borrar datos (Útil para pruebas y desarrollo)
        btnClearData.setOnClickListener {
            // Llama al método correcto para borrar todos los datos guardados
            pdkGestor.clearAllMisionesAndFirestoreData() //
            Toast.makeText(this, "Todos los datos de misiones han sido borrados y reiniciados.", Toast.LENGTH_LONG).show()
            recreate() // Recarga la actividad para reflejar los cambios
        }
    }

    override fun onResume() {
        super.onResume()
        // Logs para depuración, usando los métodos GETTERS correctos del gestor
        val totalMissions = pdkGestor.pdkGetMisionesDisponibles().size + //
                pdkGestor.pdkGetMisionesAceptadasPorUsuario().size + //
                pdkGestor.pdkGetMisionesCompletadasPorUsuario().size //
        Log.d("MainActivity", "onResume - Total de misiones en pool global: $totalMissions")
        Log.d("MainActivity", "onResume - Misiones disponibles: ${pdkGestor.pdkGetMisionesDisponibles().size}") //
        Log.d("MainActivity", "onResume - Misiones aceptadas por este usuario: ${pdkGestor.pdkGetMisionesAceptadasPorUsuario().size}") //
        Log.d("MainActivity", "onResume - Misiones completadas por este usuario: ${pdkGestor.pdkGetMisionesCompletadasPorUsuario().size}") //
    }
}