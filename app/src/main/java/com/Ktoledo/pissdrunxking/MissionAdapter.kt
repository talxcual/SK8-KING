// Archivo: MissionsAdapter.kt

package com.Ktoledo.pissdrunxking

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// Ya no necesitamos Glide si las imágenes son locales de drawable.
// import com.bumptech.glide.Glide
import com.Ktoledo.pissdrunxking.MissionsListActivity.MissionType // Importa el enum de la actividad
import com.Ktoledo.pissdrunxking.PDKspot.MissionState // Importa el enum de PDKspot

class MissionsAdapter(
    private val context: Context,
    private var missions: MutableList<PDKspot>, // Aseguramos que sea MutableList
    private val onAcceptMissionClick: (PDKspot) -> Unit,
    private val onSubmitEvidenceClick: (PDKspot) -> Unit,
    private val onCancelMissionClick: (PDKspot) -> Unit // Listener para cancelar misión
) : RecyclerView.Adapter<MissionsAdapter.MissionViewHolder>() {

    private var currentMissionType: MissionType = MissionType.AVAILABLE // Para saber qué tipo de misión se muestra

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val mission = missions[position]
        holder.bind(mission, currentMissionType) // Pasa el tipo de misión al bind

        // Configurar listeners
        holder.btnAcceptMission.setOnClickListener { onAcceptMissionClick(mission) }
        holder.btnSubmitEvidence.setOnClickListener { onSubmitEvidenceClick(mission) }
        holder.btnCancelMission.setOnClickListener { onCancelMissionClick(mission) }
    }

    override fun getItemCount(): Int = missions.size

    // Método para actualizar la lista de misiones y el tipo actual
    // Aquí el tipo 'newMissions' ya debe ser MutableList
    fun updateMissions(newMissions: MutableList<PDKspot>, type: MissionType) {
        this.missions = newMissions
        this.currentMissionType = type // Actualiza el tipo de misión que se está mostrando
        notifyDataSetChanged()
    }

    inner class MissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgMission: ImageView = itemView.findViewById(R.id.imgMission)
        private val tvMissionName: TextView = itemView.findViewById(R.id.tvMissionName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvTrick: TextView = itemView.findViewById(R.id.tvTrick)
        private val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
        private val tvReward: TextView = itemView.findViewById(R.id.tvReward)
        private val tvGlobalAcceptances: TextView = itemView.findViewById(R.id.tvGlobalAcceptances)

        val btnAcceptMission: Button = itemView.findViewById(R.id.btnAcceptMission)
        val btnSubmitEvidence: Button = itemView.findViewById(R.id.btnSubmitEvidence)
        val btnCancelMission: Button = itemView.findViewById(R.id.btnCancelMission) // Botón de cancelar

        fun bind(mission: PDKspot, type: MissionType) {
            tvMissionName.text = mission.getNombre()
            tvLocation.text = "Ubicación: ${mission.getUbicacion()}"
            tvTrick.text = "Truco: ${mission.getTruco()}"
            tvDifficulty.text = "Dificultad: ${mission.getDificultad()}"
            tvReward.text = "Recompensa: ${mission.getRecompensa()}"
            tvGlobalAcceptances.text = "Aceptaciones globales: ${mission.getAceptacionesGlobales()}"

            // CAMBIO CLAVE: Cargar imagen desde drawable usando el nombre del recurso
            val drawableName = mission.getDrawableName() // Obtener el nombre del drawable desde la misión
            if (!drawableName.isNullOrEmpty()) {
                val imageResId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                if (imageResId != 0) { // Si el recurso se encontró
                    imgMission.setImageResource(imageResId)
                } else {
                    // Si no se encuentra el recurso drawable por el nombre, usar una imagen de error
                    imgMission.setImageResource(R.drawable.error_image) // Asegúrate de tener R.drawable.error_image
                }
            } else {
                // Si el nombre del drawable está vacío o nulo
                imgMission.setImageResource(R.drawable.placeholder_image) // Asegúrate de tener R.drawable.placeholder_image
            }

            // Lógica de visibilidad de botones basada en el tipo de misión (pestaña actual)
            when (type) {
                MissionType.AVAILABLE -> {
                    btnAcceptMission.visibility = View.VISIBLE
                    btnSubmitEvidence.visibility = View.GONE
                    btnCancelMission.visibility = View.GONE
                }
                MissionType.ACCEPTED -> {
                    btnAcceptMission.visibility = View.GONE
                    // Mostrar Enviar Evidencia y Cancelar solo si la misión está ACCEPTED por el usuario
                    // El estado ahora viene del documento de usuario, que debería ser ACCEPTED si está en esta lista
                    if (mission.getEstado() == MissionState.ACCEPTED) {
                        btnSubmitEvidence.visibility = View.VISIBLE
                        btnCancelMission.visibility = View.VISIBLE
                    } else {
                        // Esto podría ocurrir si por alguna razón la misión en la lista de aceptadas no tiene el estado ACCEPTED
                        // Debería ser raro si la lógica de carga de PDKMisionManager es correcta, pero es un fallback
                        btnSubmitEvidence.visibility = View.GONE
                        btnCancelMission.visibility = View.GONE
                    }
                }
                MissionType.COMPLETED -> {
                    btnAcceptMission.visibility = View.GONE
                    btnSubmitEvidence.visibility = View.GONE
                    btnCancelMission.visibility = View.GONE
                    // Opcional: Cambiar el estilo de la tarjeta para misiones completadas
                    // Por ejemplo, cambiar el color de fondo, añadir un icono de "check", etc.
                }
            }
        }
    }
}