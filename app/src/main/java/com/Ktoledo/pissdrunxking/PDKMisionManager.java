package com.Ktoledo.pissdrunxking;

import android.content.Context;
import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue; // Para incrementar contadores
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // Para manejar listeners en tiempo real
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions; // Para usar merge al guardar

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; // Para manejar un contador atómico

public class PDKMisionManager {

    private static PDKMisionManager instance;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Context appContext; // Usar applicationContext para evitar fugas de memoria

    // Listeners para las misiones de Firestore (para la UI)
    private ListenerRegistration globalMissionsListener;
    private ListenerRegistration userMissionsListener;

    // Listas en memoria para la UI (se llenarán desde Firestore)
    private List<PDKspot> globalMissionsCache; // Misiones que existen en la base de datos (disponibles para todos)
    private List<PDKspot> userAcceptedMissionsCache; // Misiones aceptadas y/o completadas por el usuario actual

    // Callback para notificar a la UI cuando las misiones cambian
    public interface MisionUpdateListener {
        void onMisionsLoaded(List<PDKspot> available, List<PDKspot> accepted, List<PDKspot> completed);
        void onError(String message);
    }
    private MisionUpdateListener listener;

    private PDKMisionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
        this.globalMissionsCache = new ArrayList<>();
        this.userAcceptedMissionsCache = new ArrayList<>();

        // Inicializa las misiones por defecto si la colección global está vacía
        // Esto solo debería ejecutarse una vez en el ciclo de vida de la app si no hay misiones preexistentes
        initializeGlobalMissions();
    }

    public static synchronized PDKMisionManager getInstance(Context context) {
        if (instance == null) {
            instance = new PDKMisionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void setMisionUpdateListener(MisionUpdateListener listener) {
        this.listener = listener;
        // Si ya hay datos cargados, notifica al listener inmediatamente
        notifyListener();
    }

    public void removeMisionUpdateListener() {
        this.listener = null;
        stopListeningForMissions();
    }

    private String getCurrentUserId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    // --- Inicialización de Misiones Globales por defecto (solo si no existen) ---
    private void initializeGlobalMissions() {
        db.collection("globalMissions")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // La colección está vacía, crea misiones por defecto
                            createDefaultGlobalMisiones();
                        } else {
                            Log.d("PDKMisionManager", "Misiones globales ya existen en Firestore.");
                            startListeningForMissions(); // Ya que existen, empieza a escuchar
                        }
                    } else {
                        Log.e("PDKMisionManager", "Error al verificar misiones globales: " + task.getException());
                        if (listener != null) listener.onError("Error al verificar misiones globales: " + task.getException().getMessage());
                    }
                });
    }

    private void createDefaultGlobalMisiones() {
        List<PDKspot> defaultMisiones = new ArrayList<>();
        defaultMisiones.add(new PDKspot(
                "mision1", "Plaza de Armas", "Pudahuel", "Flip 360 en 3 escalones",
                "Media", "Gorra de marca", "foto_plaza_armas",
                null, 0, PDKspot.MissionState.AVAILABLE));
        defaultMisiones.add(new PDKspot(
                "mision2", "Parque Los Reyes", "Santiago", "Boardslide en baranda",
                "Alta", "Descuento en tienda de skate", "foto_parque_reyes",
                null, 0, PDKspot.MissionState.AVAILABLE));
        defaultMisiones.add(new PDKspot(
                "mision3", "Skatepark Quilicura", "Quilicura", "Ollie largo sobre rampa",
                "Fácil", "Stickers exclusivos", "foto_quilicura",
                null, 0, PDKspot.MissionState.AVAILABLE));
        defaultMisiones.add(new PDKspot(
                "mision4", "Cerro San Cristobal", "Santiago", "Grind en rampa",
                "Media", "Polera PissDrunxKing", "foto_cerro_san_cristobal",
                null, 0, PDKspot.MissionState.AVAILABLE));
        defaultMisiones.add(new PDKspot(
                "mision5", "Parque O'Higgins", "Santiago", "Manual largo",
                "Baja", "Calcetas PissDrunxKing", "foto_parque_ohiggins",
                null, 0, PDKspot.MissionState.AVAILABLE));

        // Contador atómico para saber cuándo todas las misiones por defecto han sido añadidas
        AtomicInteger missionsAdded = new AtomicInteger(0);

        for (PDKspot mision : defaultMisiones) {
            db.collection("globalMissions").document(mision.getId())
                    .set(mision)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("PDKMisionManager", "Misión global '" + mision.getNombre() + "' añadida.");
                        if (missionsAdded.incrementAndGet() == defaultMisiones.size()) {
                            // Todas las misiones por defecto se han añadido, ahora empieza a escuchar
                            startListeningForMissions();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("PDKMisionManager", "Error al añadir misión global '" + mision.getNombre() + "': " + e.getMessage());
                        if (listener != null) listener.onError("Error al añadir misión global: " + e.getMessage());
                    });
        }
    }


    // --- Métodos de escucha en tiempo real de Firestore ---
    public void startListeningForMissions() {
        stopListeningForMissions(); // Asegurarse de que no haya listeners duplicados
        String userId = getCurrentUserId();
        if (userId == null) {
            Log.w("PDKMisionManager", "No hay usuario autenticado, no se pueden escuchar misiones del usuario.");
            // Si no hay usuario, solo podemos escuchar misiones globales si lo deseas
            // Pero las listas específicas del usuario estarán vacías.
            if (listener != null) listener.onMisionsLoaded(new ArrayList<>(globalMissionsCache), new ArrayList<>(), new ArrayList<>());
            return;
        }

        // Listener para misiones globales
        globalMissionsListener = db.collection("globalMissions")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("PDKMisionManager", "Listen failed for global missions.", e);
                        if (listener != null) listener.onError("Error al cargar misiones globales: " + e.getMessage());
                        return;
                    }

                    globalMissionsCache.clear();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                PDKspot mision = doc.toObject(PDKspot.class);
                                globalMissionsCache.add(mision);
                            } catch (Exception ex) {
                                Log.e("PDKMisionManager", "Error al convertir documento de misión global: " + ex.getMessage());
                            }
                        }
                        Log.d("PDKMisionManager", "Misiones globales actualizadas. Total: " + globalMissionsCache.size());
                        notifyListener();
                    }
                });

        // Listener para misiones aceptadas/completadas por el usuario actual
        userMissionsListener = db.collection("users").document(userId).collection("userMissions")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w("PDKMisionManager", "Listen failed for user missions.", e);
                        if (listener != null) listener.onError("Error al cargar misiones del usuario: " + e.getMessage());
                        return;
                    }

                    userAcceptedMissionsCache.clear();
                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            try {
                                PDKspot mision = doc.toObject(PDKspot.class); // Firestore puede convertirlo si el constructor vacío existe
                                userAcceptedMissionsCache.add(mision);
                            } catch (Exception ex) {
                                Log.e("PDKMisionManager", "Error al convertir documento de misión de usuario: " + ex.getMessage());
                            }
                        }
                        Log.d("PDKMisionManager", "Misiones de usuario actualizadas. Total: " + userAcceptedMissionsCache.size());
                        notifyListener();
                    }
                });
    }

    public void stopListeningForMissions() {
        if (globalMissionsListener != null) {
            globalMissionsListener.remove();
            globalMissionsListener = null;
            Log.d("PDKMisionManager", "Global missions listener stopped.");
        }
        if (userMissionsListener != null) {
            userMissionsListener.remove();
            userMissionsListener = null;
            Log.d("PDKMisionManager", "User missions listener stopped.");
        }
    }

    // --- Notificar a la UI ---
    private void notifyListener() {
        if (listener != null) {
            List<PDKspot> available = pdkGetMisionesDisponibles();
            List<PDKspot> accepted = pdkGetMisionesAceptadasPorUsuario();
            List<PDKspot> completed = pdkGetMisionesCompletadasPorUsuario();
            listener.onMisionsLoaded(available, accepted, completed);
        }
    }

    // --- Métodos para obtener listas de misiones (ahora usan los cachés de Firestore) ---
    // NOTA: Estas funciones ahora operan sobre los datos YA CARGADOS de Firestore en los cachés.
    // La carga en sí la hacen los listeners de Firestore.
    public List<PDKspot> pdkGetMisionesDisponibles() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return new ArrayList<>(); // Si no hay usuario, no hay misiones disponibles para él

        List<PDKspot> disponibles = new ArrayList<>();
        // Recorre todas las misiones globales
        for (PDKspot globalMision : globalMissionsCache) {
            boolean isAcceptedOrCompletedByUser = false;
            // Verifica si esta misión global ya ha sido aceptada o completada por el usuario actual
            for (PDKspot userMision : userAcceptedMissionsCache) {
                if (userMision.getId().equals(globalMision.getId()) &&
                        (userMision.getEstado() == PDKspot.MissionState.ACCEPTED || userMision.getEstado() == PDKspot.MissionState.COMPLETED)) {
                    isAcceptedOrCompletedByUser = true;
                    break;
                }
            }
            if (!isAcceptedOrCompletedByUser) {
                disponibles.add(globalMision);
            }
        }
        Collections.sort(disponibles, Comparator.comparing(PDKspot::getNombre));
        return disponibles;
    }

    public List<PDKspot> pdkGetMisionesAceptadasPorUsuario() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return new ArrayList<>();

        List<PDKspot> aceptadas = new ArrayList<>();
        for (PDKspot userMision : userAcceptedMissionsCache) {
            if (userMision.getEstado() == PDKspot.MissionState.ACCEPTED) {
                aceptadas.add(userMision);
            }
        }
        Collections.sort(aceptadas, Comparator.comparing(PDKspot::getNombre));
        return aceptadas;
    }

    public List<PDKspot> pdkGetMisionesCompletadasPorUsuario() {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) return new ArrayList<>();

        List<PDKspot> completadas = new ArrayList<>();
        for (PDKspot userMision : userAcceptedMissionsCache) {
            if (userMision.getEstado() == PDKspot.MissionState.COMPLETED) {
                completadas.add(userMision);
            }
        }
        Collections.sort(completadas, Comparator.comparing(PDKspot::getNombre));
        return completadas;
    }

    // --- Métodos de acción que escriben en Firestore ---
    public void pdkAceptarMision(PDKspot mision) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            Log.e("PDKMisionManager", "No hay usuario logueado para aceptar la misión.");
            if (listener != null) listener.onError("Debes iniciar sesión para aceptar misiones.");
            return;
        }

        // 1. Añadir la misión a la subcolección del usuario
        PDKspot misionParaUsuario = new PDKspot(mision.getId(), mision.getNombre(), mision.getUbicacion(),
                mision.getTruco(), mision.getDificultad(), mision.getRecompensa(), mision.getDrawableName(),
                currentUserId, mision.getAceptacionesGlobales(), PDKspot.MissionState.ACCEPTED);

        db.collection("users").document(currentUserId).collection("userMissions")
                .document(mision.getId())
                .set(misionParaUsuario)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PDKMisionManager", "Misión '" + mision.getNombre() + "' aceptada por " + currentUserId + " en Firestore.");
                    // No es necesario llamar a notifyListener aquí porque el listener de Firestore ya lo hará
                })
                .addOnFailureListener(e -> {
                    Log.e("PDKMisionManager", "Error al aceptar misión en Firestore: " + e.getMessage());
                    if (listener != null) listener.onError("Error al aceptar misión: " + e.getMessage());
                });

        // 2. Incrementar las aceptaciones globales en la misión global
        db.collection("globalMissions").document(mision.getId())
                .update("aceptacionesGlobales", FieldValue.increment(1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("PDKMisionManager", "Contador de aceptaciones globales incrementado para '" + mision.getNombre() + "'.");
                })
                .addOnFailureListener(e -> {
                    Log.e("PDKMisionManager", "Error al incrementar aceptaciones globales: " + e.getMessage());
                });
    }

    public void pdkCancelarMision(PDKspot mision) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            Log.e("PDKMisionManager", "No hay usuario logueado para cancelar la misión.");
            if (listener != null) listener.onError("Debes iniciar sesión para cancelar misiones.");
            return;
        }

        // Eliminar el documento de la misión de la subcolección del usuario
        db.collection("users").document(currentUserId).collection("userMissions")
                .document(mision.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("PDKMisionManager", "Misión '" + mision.getNombre() + "' cancelada por " + currentUserId + " en Firestore.");
                    // No es necesario llamar a notifyListener aquí porque el listener de Firestore ya lo hará
                })
                .addOnFailureListener(e -> {
                    Log.e("PDKMisionManager", "Error al cancelar misión en Firestore: " + e.getMessage());
                    if (listener != null) listener.onError("Error al cancelar misión: " + e.getMessage());
                });

        // Decrementar las aceptaciones globales (opcional, dependiendo de si quieres que se decremente)
        db.collection("globalMissions").document(mision.getId())
                .update("aceptacionesGlobales", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> {
                    Log.d("PDKMisionManager", "Contador de aceptaciones globales decrementado para '" + mision.getNombre() + "'.");
                })
                .addOnFailureListener(e -> {
                    Log.e("PDKMisionManager", "Error al decrementar aceptaciones globales: " + e.getMessage());
                });
    }

    public void pdkMarcarMisionCompletada(PDKspot mision) {
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            Log.e("PDKMisionManager", "No hay usuario logueado para marcar misión como completada.");
            if (listener != null) listener.onError("Debes iniciar sesión para completar misiones.");
            return;
        }

        // Actualizar el estado de la misión en la subcolección del usuario
        // Podemos usar un Map para actualizar solo campos específicos
        Map<String, Object> updates = new HashMap<>();
        updates.put("estado", PDKspot.MissionState.COMPLETED.name()); // Guardar enum como String
        updates.put("completedAt", System.currentTimeMillis()); // Registrar cuándo se completó

        db.collection("users").document(currentUserId).collection("userMissions")
                .document(mision.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PDKMisionManager", "Misión '" + mision.getNombre() + "' marcada como completada en Firestore.");
                    // No es necesario llamar a notifyListener aquí porque el listener de Firestore ya lo hará
                })
                .addOnFailureListener(e -> {
                    Log.e("PDKMisionManager", "Error al marcar misión como completada en Firestore: " + e.getMessage());
                    if (listener != null) listener.onError("Error al completar misión: " + e.getMessage());
                });
    }

    // Método para añadir una nueva misión global (desde un panel de administración, por ejemplo)
    public void pdkAddMissionToGlobalPool(PDKspot newMission) {
        db.collection("globalMissions").document(newMission.getId())
                .set(newMission)
                .addOnSuccessListener(aVoid -> {
                    Log.d("PDKMisionManager", "Nueva misión añadida al pool global en Firestore: " + newMission.getNombre());
                    // No es necesario llamar a notifyListener aquí porque el listener de Firestore ya lo hará
                })
                .addOnFailureListener(e -> {
                    Log.e("PDKMisionManager", "Error al añadir nueva misión global a Firestore: " + e.getMessage());
                    if (listener != null) listener.onError("Error al añadir misión: " + e.getMessage());
                });
    }

    // Este método es para limpiar TODOS los datos de misión de Firestore (¡USAR CON PRECAUCIÓN!)
    // Esto es solo para propósitos de desarrollo/prueba.
    public void clearAllMisionesAndFirestoreData() {
        // Eliminar misiones globales
        db.collection("globalMissions").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot doc : task.getResult()) {
                    doc.getReference().delete();
                }
                Log.d("PDKMisionManager", "Todas las misiones globales eliminadas de Firestore.");
            } else {
                Log.e("PDKMisionManager", "Error al eliminar misiones globales: " + task.getException());
            }
        });

        // Eliminar misiones de usuario (solo para el usuario actual)
        String userId = getCurrentUserId();
        if (userId != null) {
            db.collection("users").document(userId).collection("userMissions").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot doc : task.getResult()) {
                        doc.getReference().delete();
                    }
                    Log.d("PDKMisionManager", "Misiones del usuario " + userId + " eliminadas de Firestore.");
                } else {
                    Log.e("PDKMisionManager", "Error al eliminar misiones de usuario: " + task.getException());
                }
            });
        }
        // Después de borrar en Firestore, también limpiamos los cachés locales
        globalMissionsCache.clear();
        userAcceptedMissionsCache.clear();
        // Volvemos a inicializar las misiones por defecto en Firestore
        createDefaultGlobalMisiones();
        Log.d("PDKMisionManager", "Todas las misiones han sido borradas y reiniciadas en Firestore.");
    }
}