# 🛹 PissDrunxKing - App de Gestión de Misiones de Skate

![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=flat&logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin)
![Java](https://img.shields.io/badge/Language-Java-ED8B00?style=flat&logo=java)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat&logo=firebase)

## 📄 Descripción del Proyecto

**PissDrunxKing** es una aplicación móvil nativa diseñada para la comunidad skater, cuyo objetivo es **gamificar la experiencia del skate** mediante un sistema de misiones y desafíos reales.

El proyecto destaca por su arquitectura híbrida (**Kotlin + Java**) y una gestión robusta de datos en la nube. Los usuarios exploran spots, aceptan misiones con cupos limitados y validan sus trucos mediante evidencia en video, todo sincronizado en tiempo real.

## 🚀 Funcionalidades Principales

### 1. Gestión de Usuarios (Auth)
* **Acceso Multi-plataforma:** Autenticación robusta vía **Correo/Contraseña** y **Google Sign-In**.
* **Persistencia de Sesión:** Mantiene al usuario conectado y gestiona el ciclo de vida de la sesión de forma segura.
* **Perfiles en Nube:** Creación automática de documentos de usuario en **Firestore** vinculados al UID único.

### 2. Sistema de Misiones (Gamificación)
* **Interfaz Dinámica:** Listado visual de misiones utilizando `RecyclerView` y tarjetas `MaterialCardView`.
* **Filtrado por Estados:** Organización inteligente mediante `TabLayout` en tres categorías:
    * **Disponibles:** Misiones abiertas para aceptar.
    * **Aceptadas:** Desafíos en curso del usuario.
    * **Completadas:** Historial de logros desbloqueados.
* **Detalle Inmersivo:** Vista completa con ubicación, dificultad del truco y referencia visual del spot.

### 3. Flujo de Progreso y Evidencia
* **Control de Cupos Globales:** Sistema de vacantes limitadas (ej. 15 cupos) sincronizado entre todos los usuarios en tiempo real.
* **Validación de Logros:**
    * Input para enlaces de evidencia (YouTube/Drive).
    * Generación automática de correos para revisión administrativa.
    * Actualización inmediata del estado a "Completado" tras el envío.

## 🛠️ Tecnologías Utilizadas

* **Lenguajes:**
    * **Kotlin:** Lógica de UI, Actividades y gestión de eventos.
    * **Java:** Lógica Core (`PDKMisionManager`) y Modelos de datos (Interoperabilidad).
* **Arquitectura:** Patrón Singleton para gestión centralizada del estado.
* **Backend (BaaS):**
    * **Firebase Authentication:** Gestión de identidad.
    * **Cloud Firestore:** Base de datos NoSQL para persistencia en tiempo real.
* **UI/UX:** Material Design, Glide (Carga de imágenes), XML Layouts.

## 📋 Modelo de Datos (NoSQL)

El sistema utiliza **Cloud Firestore** con dos colecciones principales que separan la lógica estática de la dinámica:

* **`globalMissions`:** Contiene la definición de las misiones y los contadores globales de cupos.
* **`users`:** Almacena la información del perfil y una subcolección `userMissions` con el estado individual (ACCEPTED, COMPLETED) de cada usuario.

## 🔧 Instalación y Configuración

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/tu-usuario/PissDrunxKing.git](https://github.com/tu-usuario/PissDrunxKing.git)
    ```

2.  **Configurar Firebase:**
    * Crea un proyecto en [Firebase Console](https://console.firebase.google.com/).
    * Registra la app con el paquete: `com.Ktoledo.pissdrunxking`.
    * Descarga el archivo `google-services.json` y colócalo en la carpeta `/app` del proyecto.
    * Habilita **Authentication** y **Firestore Database**.

3.  **Compilar y Ejecutar:**
    * Abre el proyecto en **Android Studio**.
    * Espera a que Gradle sincronice las dependencias.
    * Ejecuta la app en un emulador o dispositivo físico conectado.

## 👤 Autor

**Kleber Toledo A.**
* [LinkedIn](https://www.linkedin.com/in/kleber-toledo-amaro-51aa23313/)
* Estudiante de Ingeniería de Ejecución en Informática - Universidad de las Américas

---
*Este proyecto demuestra competencias en desarrollo móvil nativo, integración de servicios en la nube y lógica de negocio compleja.*
