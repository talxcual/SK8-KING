package com.Ktoledo.pissdrunxking

import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
// Ya no necesitamos Glide si las imágenes son locales de drawable.
// import com.bumptech.glide.Glide

class MissionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mission_detail)

        val tvDetailMissionName: TextView = findViewById(R.id.tvDetailMissionName)
        val tvDetailMissionLocation: TextView = findViewById(R.id.tvDetailMissionLocation)
        val tvDetailMissionTrick: TextView = findViewById(R.id.tvDetailMissionTrick)
        val tvDetailMissionDifficulty: TextView = findViewById(R.id.tvDetailMissionDifficulty)
        val tvDetailMissionPrize: TextView = findViewById(R.id.tvDetailMissionPrize)
        val ivMissionImage: ImageView = findViewById(R.id.ivMissionImage)

        val mission: PDKspot? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("MISSION_DETAIL", PDKspot::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("MISSION_DETAIL") as PDKspot?
        }

        mission?.let {
            title = it.nombre
            tvDetailMissionName.text = it.nombre
            tvDetailMissionLocation.text = "Ubicación: ${it.ubicacion}"
            tvDetailMissionTrick.text = "Truco: ${it.getTruco()}"
            tvDetailMissionDifficulty.text = "Dificultad: ${it.dificultad}"
            tvDetailMissionPrize.text = "Recompensa: ${it.getRecompensa()}"

            // CAMBIO CLAVE: Cargar imagen desde drawable usando el nombre del recurso
            val drawableName = it.getDrawableName() // Obtener el nombre del drawable desde la misión
            if (!drawableName.isNullOrEmpty()) {
                val imageResId = resources.getIdentifier(drawableName, "drawable", packageName)
                if (imageResId != 0) { // Si el recurso se encontró
                    ivMissionImage.setImageResource(imageResId)
                } else {
                    // Si no se encuentra el recurso drawable por el nombre, usar una imagen de error
                    ivMissionImage.setImageResource(R.drawable.error_image) //
                    Toast.makeText(this, "Error: No se encontró la imagen para ${it.nombre}.", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Si el nombre del drawable está vacío o nulo
                ivMissionImage.setImageResource(R.drawable.placeholder_image) //
            }

        } ?: run {
            Toast.makeText(this, "No se pudo cargar la información de la misión.", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}