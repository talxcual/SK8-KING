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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MissionsListActivity : AppCompatActivity(), PDKMisionManager.MisionUpdateListener {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: MissionsPagerAdapter
    private lateinit var pdkGestor: PDKMisionManager
    private lateinit var tabLayoutMissionTypes: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions_list)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Let the custom TextView handle the title

        tabLayoutMissionTypes = findViewById(R.id.tabLayoutMissionTypes)
        viewPager = findViewById(R.id.viewPagerMissions)

        pagerAdapter = MissionsPagerAdapter(
            this,
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
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayoutMissionTypes, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Disponibles"
                1 -> "Aceptadas"
                2 -> "Completadas"
                3 -> "Kings"
                else -> ""
            }
        }.attach()

        pdkGestor = PDKMisionManager.getInstance(this)
        pdkGestor.setMisionUpdateListener(this)
        pdkGestor.startListeningForMissions()
    }

    override fun onResume() {
        super.onResume()
        pdkGestor.setMisionUpdateListener(this)
        pdkGestor.startListeningForMissions()
    }

    override fun onPause() {
        super.onPause()
        pdkGestor.removeMisionUpdateListener()
    }

    override fun onDestroy() {
        super.onDestroy()
        pdkGestor.removeMisionUpdateListener()
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
            R.id.action_clear_data -> {
                pdkGestor.clearAllMisionesAndFirestoreData();
                Toast.makeText(this, "Datos de prueba borrados y reiniciados.", Toast.LENGTH_SHORT).show();
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performSignOut() {
        FirebaseAuth.getInstance().signOut()
        pdkGestor.removeMisionUpdateListener()
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        Toast.makeText(this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show()
    }

    override fun onMisionsLoaded(available: List<PDKspot>, accepted: List<PDKspot>, completed: List<PDKspot>) {
        Log.d("MissionsListActivity", "onMisionsLoaded - Disponibles: ${available.size}, Aceptadas: ${accepted.size}, Completadas: ${completed.size}")
        pagerAdapter.updateData(
            available ?: pdkGestor.pdkGetMisionesDisponibles(),
            accepted ?: pdkGestor.pdkGetMisionesAceptadasPorUsuario(),
            completed ?: pdkGestor.pdkGetMisionesCompletadasPorUsuario()
        )
    }

    override fun onError(message: String) {
        Toast.makeText(this, "Error en misiones: $message", Toast.LENGTH_LONG).show()
        Log.e("MissionsListActivity", "Error: $message")
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