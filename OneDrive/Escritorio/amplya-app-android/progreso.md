# Progreso de cambios

## gradle/libs.versions.toml
Añadidas versiones y dependencias de Retrofit2, OkHttp, Gson y Glide.

## app/build.gradle.kts
- `namespace` y `applicationId` cambiados a `es.amplya.ay_app_obras`
- Añadidas dependencias: Retrofit2, OkHttp, Gson, Glide
- Añadido soporte 16 KB page size (`useLegacyPackaging = false`)

## app/src/main/AndroidManifest.xml
- Añadidos permisos: INTERNET, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, CAMERA, READ_EXTERNAL_STORAGE
- Actividad principal actualizada a `.ui.MainActivity`

## gradle.properties
Eliminada la propiedad `kotlin.code.style` (no aplica a proyectos Java puros).

## app/src/main/java/com/example/amplya/ (eliminado)
Carpeta del paquete antiguo eliminada.

## app/src/main/java/es/amplya/ay_app_obras/ui/MainActivity.java (nuevo)
MainActivity migrada al nuevo dominio y paquete `ui`.

## app/src/main/java/es/amplya/ay_app_obras/api/ApiClient.java
Dos instancias Retrofit: `getObrasInstance()` → obras.amplya.es, `getInoutInstance()` → inout.amplya.es. OkHttp client compartido.

## app/src/main/java/es/amplya/ay_app_obras/config/AppConfig.java
Dos constantes de base URL: `BASE_URL_OBRAS` y `BASE_URL_INOUT`.

## app/src/main/java/es/amplya/ay_app_obras/utils/PermissionUtils.java (nuevo)
Helpers para solicitar y verificar permisos de ubicación y cámara.

## app/src/main/java/es/amplya/ay_app_obras/model/ (nuevo)
Carpeta creada, lista para las clases de modelo.

## app/src/main/java/es/amplya/ay_app_obras/api/ApiKeyInterceptor.java (nuevo)
Interceptor OkHttp que añade header `X-API-KEY` a cada request.

## app/src/main/java/es/amplya/ay_app_obras/api/ApiClient.java
Añadido `ApiKeyInterceptor` al OkHttpClient compartido.

## app/src/main/java/es/amplya/ay_app_obras/config/AppConfig.java
Añadida constante `API_KEY`.

## app/src/main/java/es/amplya/ay_app_obras/api/SafeCallback.java (nuevo)
Callback genérico con manejo global de errores: timeout, sin conexión, error de red, código HTTP.

## app/src/main/java/es/amplya/ay_app_obras/api/ApiClient.java
Añadidos timeouts de 30s (connect, read, write) al OkHttpClient.

## local.properties
API_KEY guardada de forma segura (no se sube a git).

## app/build.gradle.kts
Lee API_KEY de `local.properties` y la expone vía `BuildConfig.API_KEY`. Activado `buildConfig = true`.

## app/src/main/java/es/amplya/ay_app_obras/config/AppConfig.java
`API_KEY` ahora lee de `BuildConfig` en vez de estar hardcodeada.

## app/src/main/java/es/amplya/ay_app_obras/config/ConfigManager.java (nuevo)
Persiste con SharedPreferences: ipgsbase, puertogsbase, gestgsbase, aplgsbase, ejagsbase.

## app/src/main/java/es/amplya/ay_app_obras/config/SessionManager.java (nuevo)
Persiste usuario, contrasenya (solo si "guardar datos" activo), cod_trabajador. Método `logout()` respeta la opción guardar datos.

## app/src/main/res/layout/activity_login.xml (nuevo)
Layout login: campos Usuario, Contraseña, toggle "Guardar datos", botón ACCEDER.

## app/src/main/res/values/strings.xml
Añadidos strings de login.

## app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java (nuevo)
Pantalla de login con validación, restauración de datos guardados y navegación a MainActivity.

## app/src/main/AndroidManifest.xml
LoginActivity como launcher, MainActivity sin export. Añadida ConfigActivity.

## app/src/main/res/menu/menu_login.xml (nuevo)
Menú con icono de engranaje para abrir configuración.

## app/src/main/res/layout/activity_config.xml (nuevo)
Layout con campos: ipgsbase, puertogsbase, gestgsbase, aplgsbase, ejagsbase y botón guardar.

## app/src/main/java/es/amplya/ay_app_obras/ui/ConfigActivity.java (nuevo)
Pantalla configuración gsBase. Carga y guarda valores con ConfigManager.

## app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java
Añadido menú con engranaje que abre ConfigActivity.

## app/src/main/res/values/strings.xml
Añadidos strings de configuración gsBase.

## app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java
Validación inline con error en TextInputLayout para usuario y contraseña.

## app/src/main/java/es/amplya/ay_app_obras/ui/ConfigActivity.java
Validación inline de campos vacíos antes de guardar configuración gsBase. Fix typo `nopackage`.

## app/src/main/java/es/amplya/ay_app_obras/ui/MainActivity.java
Añadido menú con engranaje que abre ConfigActivity (accesible desde login y menú principal).

## app/src/main/java/es/amplya/ay_app_obras/model/LoginRequest.java (nuevo)
Body del POST login: usuario, contrasenya.

## app/src/main/java/es/amplya/ay_app_obras/model/LoginResponse.java (nuevo)
Respuesta login: usuario_valido, cod_trabajador, mensaje.

## app/src/main/java/es/amplya/ay_app_obras/api/AuthService.java (nuevo)
Interfaz Retrofit: POST login.php.

## app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java
Llamada real a POST inout.amplya.es/login.php con SafeCallback. Guarda cod_trabajador en sesión. Muestra errores descriptivos al usuario.

## app/src/main/java/es/amplya/ay_app_obras/model/KillUserRequest.java (nuevo)
Body de kill-user: cod_trabajador.

## app/src/main/java/es/amplya/ay_app_obras/model/KillUserResponse.java (nuevo)
Respuesta kill-user: mensaje.

## app/src/main/java/es/amplya/ay_app_obras/api/ObrasService.java (nuevo)
Interfaz Retrofit: POST ws-obras/kill-user.php contra obras.amplya.es.

## app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java
Llamada automática a kill-user antes del login si hay sesión previa. Muestra mensaje de retorno del servidor.

## T1: Layout Home con navegación

### app/src/main/res/values/strings.xml
Añadidos strings: nuevo_parte, planning, entrada_salida, historico_fichajes, documentacion, tareas_hoy, cerrar_sesion, entrada, salida, permiso_ubicacion, mes_actual, mes_anterior, sin_fichajes, confirmar_guardar, cancelar, aceptar.

### app/src/main/res/menu/menu_main.xml (nuevo)
Menú overflow con Configuración gsBase y Cerrar sesión.

### app/src/main/res/layout/activity_main.xml
Reemplazado Hello World por RecyclerView (rvHome) en ConstraintLayout.

### app/src/main/res/layout/item_home_card.xml (nuevo)
Card layout para cada celda del grid home.

### app/src/main/java/es/amplya/ay_app_obras/ui/HomeAdapter.java (nuevo)
Adaptador RecyclerView para grid de 6 tarjetas con click listener.

### app/src/main/java/es/amplya/ay_app_obras/ui/MainActivity.java
Grid 2 columnas con 6 tarjetas (Nuevo Parte, Planning, Entrada/Salida, Histórico, Documentación, Tareas). Menú overflow con configuración gsBase y cerrar sesión. Navegación a CheckInActivity y HistoricoActivity.

### app/src/main/java/es/amplya/ay_app_obras/ui/CheckInActivity.java (nuevo)
Activity placeholder para Entrada/Salida con back navigation.

### app/src/main/java/es/amplya/ay_app_obras/ui/HistoricoActivity.java (nuevo)
Activity placeholder para Histórico de Fichajes con back navigation.

### app/src/main/res/layout/activity_checkin.xml (nuevo)
Layout placeholder para CheckInActivity.

### app/src/main/res/layout/activity_historico.xml (nuevo)
Layout placeholder para HistoricoActivity.

### app/src/main/AndroidManifest.xml
Registradas CheckInActivity y HistoricoActivity.

## T1: Enviar parte de horas / ticket en imagen

### app/src/main/java/es/amplya/ay_app_obras/model/SendDocRequest.java (nuevo)
Body POST send_worker_doc: cod_trabajador, fecha, hora, tipo_doc, imagen (Base64), obra, capitulo, partida, importe_horas, observaciones, estado.

### app/src/main/java/es/amplya/ay_app_obras/model/SendDocResponse.java (nuevo)
Respuesta: enviado (boolean), mensaje.

### app/src/main/java/es/amplya/ay_app_obras/api/ObrasService.java
Añadido endpoint POST ws-obras/send_worker_doc.php.

### app/src/main/res/layout/activity_send_doc.xml (nuevo)
Layout completo: selector tipo doc (dropdown), preview imagen, botones cámara/galería, campos distribución (obra, capítulo, partida, importe/horas), observaciones, barra progreso, botón enviar.

### app/src/main/res/xml/file_paths.xml (nuevo)
FileProvider paths para fotos de cámara.

### app/src/main/java/es/amplya/ay_app_obras/ui/SendDocActivity.java (nuevo)
Captura cámara o galería, redimensiona a 1280px max, convierte a Base64 JPEG 80%, valida tipo doc e imagen, envía POST con progreso visual, feedback Toast.

### app/src/main/res/values/strings.xml
Añadidos 16 strings para send doc (tipo_doc, cámara, galería, distribución, observaciones, enviar, errores, permiso cámara).

## T2: Nóminas (pendiente endpoint)

### app/src/main/java/es/amplya/ay_app_obras/ui/NominasActivity.java (nuevo)
Activity placeholder con back navigation. Pendiente definir endpoint con Amplya.

### app/src/main/res/layout/activity_nominas.xml (nuevo)
Layout placeholder con mensaje "Pendiente de definir con Amplya".

## T3: Parte de Incidencia (pendiente endpoint)

### app/src/main/java/es/amplya/ay_app_obras/ui/ParteIncidenciaActivity.java (nuevo)
Activity placeholder con back navigation. Pendiente definir endpoint con Amplya.

### app/src/main/res/layout/activity_parte_incidencia.xml (nuevo)
Layout placeholder.

## T4: Circulares de empresa (pendiente endpoint)

### app/src/main/java/es/amplya/ay_app_obras/ui/CircularesActivity.java (nuevo)
Activity placeholder con back navigation. Pendiente definir endpoint y sistema de notificaciones con Amplya.

### app/src/main/res/layout/activity_circulares.xml (nuevo)
Layout placeholder.

### app/src/main/java/es/amplya/ay_app_obras/ui/MainActivity.java
Grid ampliado a 9 tarjetas: añadidas Nóminas, Parte Incidencia, Circulares. "Nuevo Parte" navega a SendDocActivity.

### app/src/main/AndroidManifest.xml
Registradas SendDocActivity, NominasActivity, ParteIncidenciaActivity, CircularesActivity. Añadido FileProvider para cámara.

## T1: Solicitudes (endpoints pendientes)

### app/src/main/java/es/amplya/ay_app_obras/ui/SolicitudesActivity.java (nuevo)
Menú con 3 tipos de solicitud: Vacaciones, Enfermedad, Permiso. ListView con navegación a cada sub-activity.

### app/src/main/res/layout/activity_solicitudes.xml (nuevo)
Layout con ListView para seleccionar tipo de solicitud.

### app/src/main/java/es/amplya/ay_app_obras/ui/SolicitudVacacionesActivity.java (nuevo)
Activity placeholder solicitud vacaciones. Pendiente endpoint.

### app/src/main/java/es/amplya/ay_app_obras/ui/SolicitudEnfermedadActivity.java (nuevo)
Activity placeholder solicitud enfermedad. Pendiente endpoint.

### app/src/main/java/es/amplya/ay_app_obras/ui/SolicitudPermisoActivity.java (nuevo)
Activity placeholder solicitud permiso. Pendiente endpoint.

### app/src/main/res/layout/activity_solicitud_placeholder.xml (nuevo)
Layout compartido para las 3 solicitudes placeholder.

## T2: Tareas de Hoy

### app/src/main/java/es/amplya/ay_app_obras/model/WorkerTaskRequest.java (nuevo)
Body POST: cod_trabajador.

### app/src/main/java/es/amplya/ay_app_obras/model/WorkerTaskResponse.java (nuevo)
Respuesta: ok, mensaje, lista de Tarea (cod_tarea, titulo, descripcion, hora, estado, obra).

### app/src/main/java/es/amplya/ay_app_obras/api/ObrasService.java
Añadido endpoint POST ws-obras/get_worker_task.php.

### app/src/main/res/layout/activity_tareas_hoy.xml (nuevo)
Layout: progress bar, RecyclerView, texto vacío.

### app/src/main/res/layout/item_tarea.xml (nuevo)
Card con hora, estado, título, obra y descripción.

### app/src/main/java/es/amplya/ay_app_obras/ui/TareasAdapter.java (nuevo)
Adaptador RecyclerView para lista de tareas del día.

### app/src/main/java/es/amplya/ay_app_obras/ui/TareasHoyActivity.java (nuevo)
Carga tareas del trabajador en sesión vía POST get_worker_task.php. Muestra en RecyclerView o mensaje vacío.

## T3: Sistema de código de activación

### app/src/main/java/es/amplya/ay_app_obras/config/ActivationManager.java (nuevo)
Persiste código de activación y estado (activado/no) con SharedPreferences.

## Rediseño visual incremental

### app/src/main/res/layout/activity_login.xml
- Rediseñado según `login_acceso.html`.
- Marca actualizada a `Amplya` y subtítulo `In Out`.
- Corregidos insets con la UI del móvil y separación del texto respecto al label en usuario/contraseña.

### app/src/main/res/layout/activity_main.xml
- Rediseñado según `inicio.html` con hero, tarjetas y navegación inferior integradas en el nuevo lenguaje visual.

### app/src/main/res/layout/activity_config.xml
### app/src/main/java/es/amplya/ay_app_obras/ui/ConfigActivity.java
- Rediseñado según `configuraci_n_gsbase.html`.
- Corregidos insets superior e inferior para respetar la UI del dispositivo.

### app/src/main/res/layout/activity_documentacion.xml
- Rediseñado según `documentaci_n.html` con tarjetas, hero y footer compartido.

### app/src/main/res/layout/activity_send_doc.xml
### app/src/main/java/es/amplya/ay_app_obras/ui/SendDocActivity.java
- Rediseñado según `enviar_documento.html`.
- Añadida zona de subida clicable y visual de fecha/hora conservando la lógica real de cámara/galería/envío.

### app/src/main/res/layout/activity_checkin.xml
### app/src/main/java/es/amplya/ay_app_obras/ui/CheckInActivity.java
- Sustituido el placeholder por una pantalla completa inspirada en `entrada_salida.html`.
- Añadidos reloj vivo, estado local de fichaje y shell visual completa.
- El fichaje local ya se persiste por usuario y alterna Entrada/Salida tomando el último movimiento guardado.
- La cabecera ahora usa volver atrás en lugar del icono de menú sin función.

### app/src/main/res/layout/activity_historico.xml
### app/src/main/java/es/amplya/ay_app_obras/ui/HistoricoActivity.java
- Sustituido el placeholder por una composición editorial basada en `hist_rico_de_fichajes.html`.
- Ahora pinta los fichajes guardados localmente en Entrada/Salida, agrupados por mes y día.

### app/src/main/java/es/amplya/ay_app_obras/model/CheckInRecord.java (nuevo)
### app/src/main/res/layout/item_historico_record.xml (nuevo)
- Nuevo modelo local para persistir movimientos de fichaje y plantilla visual para cada registro del histórico.

### app/src/main/res/layout/activity_main.xml
- Eliminada la barra inferior de pestañas ficticias (`Projects`, `Tasks`, `Files`) de la pantalla de inicio para no mostrar navegación sin uso.

### app/src/main/res/layout/activity_tareas_hoy.xml
### app/src/main/res/layout/item_tarea.xml
### app/src/main/java/es/amplya/ay_app_obras/ui/TareasHoyActivity.java
- Rediseñadas según `tareas_de_hoy.html` manteniendo `RecyclerView` y carga de tareas reales/demo.
- Añadidos encabezado, badge dinámico de tareas activas y tarjetas resumen.

### app/src/main/res/layout/include_footer.xml
### app/src/main/res/values/strings.xml
- Branding unificado a `Amplya`.
- Footer compartido ajustado para soportar insets en pantallas edge-to-edge.

## Validación reciente

- `:app:assembleDebug --no-daemon` validado tras el rediseño de Documentación y Enviar documento.
- `:app:installDebug --no-daemon` ejecutado repetidamente con instalación correcta en el dispositivo `2311DRK48G` tras los ajustes de login, config y el bloque de Fichajes/Tareas.

## Ajuste de login real y ubicación

### app/src/main/java/es/amplya/ay_app_obras/api/AuthService.java
### app/src/main/java/es/amplya/ay_app_obras/model/LoginResponse.java
### app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java
- El login real ya usa `https://inout.amplya.es/ws-inout/login.php`.
- El parser de `usuario_valido` ahora acepta la respuesta real del servidor (`"ok"` / `"ko"`).
- Se mantiene el usuario hardcodeado de pruebas y, si no coincide, se llama al servidor.

### app/src/main/java/es/amplya/ay_app_obras/api/OpenStreetMapService.java (nuevo)
### app/src/main/java/es/amplya/ay_app_obras/model/OpenStreetMapReverseResponse.java (nuevo)
### app/src/main/java/es/amplya/ay_app_obras/config/SessionManager.java
### app/src/main/java/es/amplya/ay_app_obras/ui/CheckInActivity.java
- Añadida resolución de ubicación al iniciar sesión mediante OpenStreetMap (reverse geocoding de Nominatim).
- La ubicación detectada se persiste en sesión y se muestra en Entrada/Salida, también para el usuario hardcodeado.

### Validación
- Petición manual confirmada contra `ws-inout/login.php`: el servidor devuelve `{"usuario_valido":"ko"}` cuando las credenciales no son válidas.
- `:app:installDebug --no-daemon` completado correctamente tras estos cambios e instalado en `2311DRK48G`.

### app/src/main/java/es/amplya/ay_app_obras/model/ActivationRequest.java (nuevo)
Body POST: codigo_activacion.

### app/src/main/java/es/amplya/ay_app_obras/model/ActivationResponse.java (nuevo)
Respuesta: valido (boolean), mensaje.

### app/src/main/java/es/amplya/ay_app_obras/api/ObrasService.java
Añadido endpoint POST ws-obras/verify_activation.php.

### app/src/main/res/layout/activity_activation.xml (nuevo)
Layout: título, descripción, campo código, progreso, botón ACTIVAR.

### app/src/main/java/es/amplya/ay_app_obras/ui/ActivationActivity.java (nuevo)
Verifica código contra BBDD. Si válido, guarda y redirige a Login. Si no, muestra error.

### app/src/main/java/es/amplya/ay_app_obras/api/ApiKeyInterceptor.java
Añadido header X-ACTIVATION-CODE en cada request. Método estático setActivationCode().

### app/src/main/java/es/amplya/ay_app_obras/ui/LoginActivity.java
Verifica activación en onCreate. Si no activado, redirige a ActivationActivity. Setea código activación en interceptor.

### app/src/main/java/es/amplya/ay_app_obras/ui/MainActivity.java
Grid ampliado a 10 tarjetas: añadida Solicitudes. "Tareas de Hoy" navega a TareasHoyActivity.

### app/src/main/res/values/strings.xml
Añadidos 11 strings: solicitudes, solicitud_vacaciones, solicitud_enfermedad, solicitud_permiso, sin_tareas, activacion_titulo, activacion_descripcion, hint_codigo_activacion, activar, codigo_invalido.

### app/src/main/AndroidManifest.xml
Registradas TareasHoyActivity, SolicitudesActivity, SolicitudVacacionesActivity, SolicitudEnfermedadActivity, SolicitudPermisoActivity, ActivationActivity.

## FCM: notificaciones push para trabajadores

Se prepara la app para usar Firebase Cloud Messaging (FCM) y recibir avisos operativos en el teléfono del trabajador.

### Flujo en la app Android
- Al arrancar la app se crea el canal Android `amplya_general_notifications`, necesario desde Android 8 para mostrar notificaciones.
- Desde Android 13 se solicita el permiso `POST_NOTIFICATIONS`. Si el usuario no lo concede, FCM puede recibir datos, pero la app no podrá mostrar notificaciones visibles.
- En el login se pide el token actual a `FirebaseMessaging.getInstance().getToken()` y se guarda en `SessionManager`.
- El POST de login envía un nuevo campo JSON `fcm_token` junto a usuario, contraseña y parámetros gsBase.
- Si Firebase todavía no está configurado porque falta `app/google-services.json`, el login continúa sin bloquearse y enviará el último token guardado, si existe.
- Si Firebase rota el token, `AmplyaFirebaseMessagingService.onNewToken()` lo guarda localmente. En el siguiente login se volverá a enviar a gsBase.
- Cuando la app está abierta y llega un mensaje, `AmplyaFirebaseMessagingService.onMessageReceived()` muestra una notificación local con badge/count.
- Cuando la app está en segundo plano o cerrada, FCM muestra automáticamente los mensajes con bloque `notification`. El contador del icono depende del launcher del dispositivo; Android lo gestiona sobre las notificaciones pendientes.

### Cuándo cambia realmente el token FCM
FCM no cambia el token en cada login. Cambia normalmente cuando:
- El usuario desinstala y vuelve a instalar la app.
- El usuario borra los datos de la aplicación desde Ajustes.
- La app se restaura en otro dispositivo desde una copia de seguridad.
- Firebase rota o expira el token anterior por seguridad.

### Contrato propuesto para gsBase
En `ws-inout/login.php`, gsBase debe leer el campo `fcm_token`:

```json
{
	"usuario": "U882",
	"contrasenya": "***",
	"ipgsbase": "...",
	"puertogsbase": "...",
	"gestgsbase": "...",
	"aplgsbase": "...",
	"ejagsbase": "...",
	"fcm_token": "TOKEN_ACTUAL_DEL_DISPOSITIVO"
}
```

La lógica recomendada en gsBase es:
- Si el trabajador no tiene token guardado, guardar `fcm_token` asociado a `cod_trabajador`.
- Si el token guardado es distinto, sustituirlo por el nuevo.
- Si es igual, no hacer nada.
- Si `fcm_token` llega vacío, no borrar el token existente; puede faltar `google-services.json`, no haber conexión con Firebase o fallar temporalmente la obtención del token.
- En dispositivos compartidos, el token identifica la instalación de la app, no al trabajador. Por eso el backend debe asociarlo siempre al trabajador que ha hecho login correctamente.

### Envío desde gsBase
La app reconoce `click_action = OPEN_DOC_ACTIVITY` para abrir directamente el módulo de Documentación. También recibe `data` como extras del `Intent`.

Ejemplo compatible con el endpoint legacy de FCM:

```json
{
	"to": "TOKEN_DEL_USUARIO_U882",
	"notification": {
		"title": "Documentación Pendiente",
		"body": "El REA de tu subcontrata ha caducado. Súbelo para evitar bloqueos.",
		"click_action": "OPEN_DOC_ACTIVITY"
	},
	"data": {
		"obra_id": "105",
		"tipo_alerta": "documentacion_caducada"
	}
}
```

Para proyectos Firebase nuevos, Google recomienda la API HTTP v1 en lugar del endpoint legacy. El JSON equivalente sería:

```json
{
	"message": {
		"token": "TOKEN_DEL_USUARIO_U882",
		"notification": {
			"title": "Documentación Pendiente",
			"body": "El REA de tu subcontrata ha caducado. Súbelo para evitar bloqueos."
		},
		"data": {
			"obra_id": "105",
			"tipo_alerta": "documentacion_caducada",
			"click_action": "OPEN_DOC_ACTIVITY"
		},
		"android": {
			"notification": {
				"click_action": "OPEN_DOC_ACTIVITY",
				"channel_id": "amplya_general_notifications"
			}
		}
	}
}
```

Pendiente para activar en entorno real: crear el proyecto Firebase, registrar el paquete `es.amplya.ay_inout`, descargar `google-services.json` y colocarlo en `app/google-services.json`.

## gradle/libs.versions.toml
Añadidos Firebase BoM, Firebase Messaging y plugin Google Services.

## build.gradle.kts
Añadido plugin `com.google.gms.google-services` en modo `apply false`.

## app/build.gradle.kts
Añadida dependencia `firebase-messaging`. El plugin Google Services se aplica solo si existe `app/google-services.json` para que el proyecto pueda compilar mientras falta la configuración privada de Firebase.

## app/src/main/AndroidManifest.xml
Añadido permiso `POST_NOTIFICATIONS`, canal por defecto de FCM, icono/color de notificación, `AmplyaApplication`, `AmplyaFirebaseMessagingService` e intent-filter `OPEN_DOC_ACTIVITY` para abrir Documentación desde una notificación.

## app/src/main/java/es/amplya/ay_inout/model/LoginRequest.java
Añadido campo `fcm_token` al body del login.

## app/src/main/java/es/amplya/ay_inout/ui/LoginActivity.java
Antes de ejecutar el login se obtiene el token FCM actual y se envía a `ws-inout/login.php`.

## app/src/main/java/es/amplya/ay_inout/config/SessionManager.java
Persistencia del token FCM y contador local de notificaciones pendientes.

## app/src/main/java/es/amplya/ay_inout/notifications/ (nuevo)
Añadidos `FcmTokenManager`, `NotificationHelper` y `AmplyaFirebaseMessagingService` para obtener tokens, crear canal y mostrar notificaciones recibidas en primer plano.

## app/src/main/java/es/amplya/ay_inout/AmplyaApplication.java (nuevo)
Crea el canal de notificaciones al arrancar la app.
