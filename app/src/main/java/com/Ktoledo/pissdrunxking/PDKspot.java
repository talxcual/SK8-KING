// Archivo: PDKspot.java
package com.Ktoledo.pissdrunxking;

import java.io.Serializable;

public class PDKspot implements Serializable {
    private String id;
    private String nombre;
    private String ubicacion;
    private String truco;
    private String dificultad;
    private String recompensa;
    private String drawableName;
    private String misionAceptadaPorUsuarioId;
    private int aceptacionesGlobales;
    private MissionState estado;

    // Enum para el estado de la misión
    public enum MissionState implements Serializable {
        AVAILABLE,
        ACCEPTED,
        COMPLETED
    }

    // CONSTRUCTOR VACÍO REQUERIDO POR FIRESTORE (¡NUEVO!)
    public PDKspot() {
        // Constructor público sin argumentos necesario para Firestore
    }

    // Constructor con argumentos existente
    public PDKspot(String id, String nombre, String ubicacion, String truco,
                   String dificultad, String recompensa, String drawableName,
                   String misionAceptadaPorUsuarioId, int aceptacionesGlobales, MissionState estado) {
        this.id = id;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.truco = truco;
        this.dificultad = dificultad;
        this.recompensa = recompensa;
        this.drawableName = drawableName;
        this.misionAceptadaPorUsuarioId = misionAceptadaPorUsuarioId;
        this.aceptacionesGlobales = aceptacionesGlobales;
        this.estado = estado;
    }

    // Getters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
    public String getTruco() { return truco; }
    public String getDificultad() { return dificultad; }
    public String getRecompensa() { return recompensa; }
    public String getDrawableName() { return drawableName; }
    public String getMisionAceptadaPorUsuarioId() { return misionAceptadaPorUsuarioId; }
    public int getAceptacionesGlobales() { return aceptacionesGlobales; }
    public MissionState getEstado() { return estado; }

    // Setters
    public void setId(String id) { this.id = id; } // Setter para ID (¡NUEVO si no lo tenías!)
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }
    public void setTruco(String truco) { this.truco = truco; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }
    public void setRecompensa(String recompensa) { this.recompensa = recompensa; }
    public void setDrawableName(String drawableName) { this.drawableName = drawableName; }
    public void setMisionAceptadaPorUsuarioId(String misionAceptadaPorUsuarioId) {
        this.misionAceptadaPorUsuarioId = misionAceptadaPorUsuarioId;
    }
    public void setAceptacionesGlobales(int aceptacionesGlobales) {
        this.aceptacionesGlobales = aceptacionesGlobales;
    }
    public void setEstado(MissionState estado) { this.estado = estado; }

    // Otros métodos
    public void incrementarAceptacionesGlobales() {
        this.aceptacionesGlobales++;
    }
}