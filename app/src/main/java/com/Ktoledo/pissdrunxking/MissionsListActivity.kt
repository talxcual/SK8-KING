// Archivo: MissionsListActivity.kt
package com.Ktoledo.pissdrunxking

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
// import java.util.Collections // No se necesita importar aquí si ya lo maneja PDKMisionManager

class MissionsListActivity : AppCompatActivity(), PDKMisionManager.MisionUpdateListener { // Implementar la interfaz

    private lateinit var recyclerView: RecyclerView
    private lateinit var missionsAdapter: MissionsAdapter
    private lateinit var pdkGestor: PDKMisionManager
    private lateinit var tabLayoutMissionTypes: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Lista de Misiones"

        // *** CAMBIO CLAVE: Inicializar las vistas ANTES de interactuar con el gestor de misiones ***
        // Esto asegura que 'tabLayoutMissionTypes' y 'recyclerView' estén listos
        // antes de que se dispare cualquier callback de misiones que los use.
        tabLayoutMissionTypes = findViewById(R.id.tabLayoutMissionTypes)
        recyclerView = findViewById(R.id.rvMissions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inicializa el adaptador con una lista mutable vacía
        missionsAdapter = MissionsAdapter(
            this,
            mutableListOf(), // Inicialmente vacía, pero mutable
            onAcceptMissionClick = { mission ->
                pdkGestor.pdkAceptarMision(mission)
            },
            onSubmitEvidenceClick = { mission ->
                showSubmitEvidenceDialog(mission)
            },
            onCancelMissionClick = { mission ->
                pdkGestor.pdkCancelarMision(mission)
            }
        )
        recyclerView.adapter = missionsAdapter

        setupTabLayout() // Configura los listeners de las pestañas
        // tabLayoutMissionTypes.selectTab(tabLayoutMissionTypes.getTabAt(0)) // Esta línea se puede quitar si el onResume() ya maneja la carga inicial.
        // Si onMisionsLoaded se dispara inmediatamente al setear el listener,
        // la primera pestaña se actualizará con los datos.

        // *** Inicializar PDKMisionManager y establecer el listener DESPUÉS de que las vistas estén listas ***
        pdkGestor = PDKMisionManager.getInstance(this)
        pdkGestor.setMisionUpdateListener(this) // Ahora, cuando onMisionsLoaded se llame, tabLayoutMissionTypes ya existirá.
        pdkGestor.startListeningForMissions() // Comienza a escuchar actualizaciones de misiones
    }

    override fun onResume() {
        super.onResume()
        pdkGestor.setMisionUpdateListener(this) // Reestablecer el listener por si la actividad se reanuda
        pdkGestor.startListeningForMissions() // Asegurar que se sigue escuchando las misiones
    }

    override fun onPause() {
        super.onPause()
        pdkGestor.removeMisionUpdateListener() // Detener el listener para evitar fugas de memoria
    }

    override fun onDestroy() {
        super.onDestroy()
        pdkGestor.removeMisionUpdateListener() // Detener el listener al destruir la actividad
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_missions_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                performSignOut()
                true
            }
            R.id.action_clear_data -> { // Asegúrate de que este ID exista en tu res/menu/menu_missions_list.xml
                pdkGestor.clearAllMisionesAndFirestoreData();
                Toast.makeText(this, "Datos de prueba borrados y reiniciados.", Toast.LENGTH_SHORT).show();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performSignOut() {
        FirebaseAuth.getInstance().signOut()
        pdkGestor.removeMisionUpdateListener() // Es buena práctica remover el listener antes de cambiar de actividad
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show()
    }

    private fun setupTabLayout() {
        tabLayoutMissionTypes.addTab(tabLayoutMissionTypes.newTab().setText("Disponibles"))
        tabLayoutMissionTypes.addTab(tabLayoutMissionTypes.newTab().setText("Aceptadas"))
        tabLayoutMissionTypes.addTab(tabLayoutMissionTypes.newTab().setText("Completadas"))

        tabLayoutMissionTypes.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateAdapterWithCurrentData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                updateAdapterWithCurrentData()
            }
        })
    }

    // Implementación de la interfaz MisionUpdateListener
    override fun onMisionsLoaded(available: List<PDKspot>, accepted: List<PDKspot>, completed: List<PDKspot>) {
        Log.d("MissionsListActivity", "onMisionsLoaded - Disponibles: ${available.size}, Aceptadas: ${accepted.size}, Completadas: ${completed.size}")
        // Aquí es donde necesitamos pasar MutableList al adaptador
        updateAdapterWithCurrentData(available, accepted, completed)
    }

    override fun onError(message: String) {
        Toast.makeText(this, "Error en misiones: $message", Toast.LENGTH_LONG).show()
        Log.e("MissionsListActivity", "Error: $message")
    }

    // Método para actualizar el adaptador con los datos correctos de la pestaña seleccionada
    private fun updateAdapterWithCurrentData(available: List<PDKspot>? = null, accepted: List<PDKspot>? = null, completed: List<PDKspot>? = null) {
        // Asegúrate de que las listas que pasas sean MutableList
        // Usamos .toMutableList() para convertir List<PDKspot> a MutableList<PDKspot>
        val currentAvailable = (available ?: pdkGestor.pdkGetMisionesDisponibles()).toMutableList()
        val currentAccepted = (accepted ?: pdkGestor.pdkGetMisionesAceptadasPorUsuario()).toMutableList()
        val currentCompleted = (completed ?: pdkGestor.pdkGetMisionesCompletadasPorUsuario()).toMutableList()

        when (tabLayoutMissionTypes.selectedTabPosition) {
            0 -> missionsAdapter.updateMissions(currentAvailable, MissionType.AVAILABLE)
            1 -> missionsAdapter.updateMissions(currentAccepted, MissionType.ACCEPTED)
            2 -> missionsAdapter.updateMissions(currentCompleted, MissionType.COMPLETED)
        }
    }

    private fun showSubmitEvidenceDialog(mission: PDKspot) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.dialog_submit_evidence_title))
        builder.setMessage(getString(R.string.dialog_submit_evidence_message, mission.getNombre()))

        val input = EditText(this)
        input.inputType = InputType.TYPE_TEXT_VARIATION_URI
        builder.setView(input)

        builder.setPositiveButton(getString(R.string.dialog_submit_evidence_positive_button)) { dialog, which ->
            val videoLink = input.text.toString().trim()
            if (videoLink.isNotBlank()) {
                sendEmailWithEvidence(mission.getNombre(), videoLink)
                pdkGestor.pdkMarcarMisionCompletada(mission)
                Toast.makeText(this, getString(R.string.toast_evidence_sent_and_completed, mission.getNombre()), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, getString(R.string.toast_empty_link_error), Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(getString(R.string.dialog_cancel_button)) { dialog, which ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendEmailWithEvidence(missionName: String, videoLink: String) {
        val emailRecipient = getString(R.string.admin_email)
        val subject = getString(R.string.email_subject_evidence, missionName)
        val body = getString(R.string.email_body_evidence, missionName, videoLink)

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(emailRecipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, getString(R.string.toast_no_email_app), Toast.LENGTH_LONG).show()
        }
    }

    enum class MissionType {
        AVAILABLE, ACCEPTED, COMPLETED
    }
}