🛹 PissDrunxKing - App de Gestión de Misiones de Skate

📄 Descripción del Proyecto

PissDrunxKing es una aplicación móvil nativa para Android diseñada para la comunidad skater. Su objetivo principal es gamificar la experiencia del skate mediante un sistema de misiones y desafíos. Los usuarios pueden explorar una lista de misiones (trucos específicos en lugares emblemáticos), aceptarlas, y enviar evidencia en video para completarlas.

La aplicación implementa un flujo completo de autenticación, gestión de estado de misiones en tiempo real y persistencia de datos en la nube, ofreciendo una experiencia robusta y escalable.

🚀 Funcionalidades Principales

1. Gestión de Usuarios (Autenticación)

Registro e Inicio de Sesión: Implementado con Firebase Authentication.

Soporte Multi-método: Permite el acceso mediante correo electrónico/contraseña y Google Sign-In.

Gestión de Sesiones: Mantiene la sesión del usuario activa y permite cerrar sesión de forma segura.

Perfiles de Usuario: Almacenamiento automático de la información básica del usuario (UID, email) en Cloud Firestore al registrarse.

2. Sistema de Misiones

Listado Dinámico: Visualización de misiones mediante RecyclerView con un diseño de tarjetas (MaterialCardView) atractivo.

Filtrado por Estado: Uso de TabLayout para organizar las misiones en tres categorías:

Disponibles: Misiones que el usuario aún no ha aceptado.

Aceptadas: Misiones en curso que el usuario ha aceptado.

Completadas: Misiones finalizadas con éxito.

Detalle de Misión: Pantalla de detalle (MissionDetailActivity) que muestra información completa (ubicación, truco, dificultad, premio) e imagen del spot.

3. Flujo de Progreso y Evidencia

Aceptar Misión: Los usuarios pueden aceptar misiones disponibles, lo que actualiza su estado en tiempo real.

Contador Global de Aceptaciones: Sistema de cupos limitados (ej. 15 vacantes) por misión, gestionado globalmente para todos los usuarios.

Cancelar Misión: Posibilidad de abandonar una misión aceptada, liberando el cupo.

Enviar Evidencia: Funcionalidad para que los usuarios envíen un enlace de video (YouTube, Drive, etc.) como prueba.

Abre un diálogo para ingresar el enlace.

Genera un correo electrónico pre-llenado con los detalles para el administrador.

Marca automáticamente la misión como Completada en el perfil del usuario.

🛠️ Tecnologías Utilizadas

Lenguajes:

Kotlin: Lenguaje principal para la lógica de la aplicación y actividades.

Java: Utilizado para la clase core PDKMisionManager y modelos de datos, demostrando interoperabilidad.

Arquitectura & Patrones:

Singleton: Implementado en PDKMisionManager para centralizar el estado y la lógica de negocio.

Adaptadores: RecyclerView.Adapter personalizado para el manejo eficiente de listas.

Backend & Nube (Firebase):

Firebase Authentication: Gestión segura de identidades.

Cloud Firestore: Base de datos NoSQL para persistir:

Perfiles de usuarios.

Estado global de las misiones (contadores).

Progreso individual de cada usuario (misiones aceptadas/completadas).

Interfaz de Usuario (UI):

XML Layouts: Diseño de interfaces nativas.

Material Design: Componentes como TabLayout, MaterialCardView, Toolbar.

Glide: Librería para la carga y caché eficiente de imágenes (preparado para URLs, actualmente usando recursos locales).

📋 Modelo de Datos (Firestore)

El sistema utiliza una estructura NoSQL en Firestore:

Colección globalMissions: Almacena la información estática de cada misión y sus contadores globales.

Colección users:

Documento por uid.

Subcolección userMissions: Almacena el estado específico (ACCEPTED, COMPLETED) de cada misión para ese usuario.

🔧 Instalación y Configuración

Clonar el repositorio:

git clone [https://github.com/tu-usuario/PissDrunxKing.git](https://github.com/tu-usuario/PissDrunxKing.git)


Configurar Firebase:

Crea un proyecto en Firebase Console.

Registra la app con el paquete com.Ktoledo.pissdrunxking.

Habilita Authentication (Email/Password y Google).

Habilita Firestore Database.

Descarga el archivo google-services.json y colócalo en la carpeta app/ del proyecto.

Compilar y Ejecutar:

Abre el proyecto en Android Studio.

Sincroniza los archivos Gradle.

Ejecuta la aplicación en un emulador o dispositivo físico.

👤 Autor

Kleber Toledo A.

LinkedIn

Estudiante de Ingeniería de Ejecución en Informática
