# Guía de estudio: núcleo de hardware

## 1. Qué se implementó

Se creó la base de cámara, ubicación y grabación de audio dentro de `core/hardware`, sin modificar pantallas, ViewModels, navegación, DTOs ni el CRUD existente.

La rama de trabajo es `feat/hardware-core`.

| Capacidad | Implementación |
| --- | --- |
| Cámara | Archivo `.jpg` temporal, URI `content://` mediante `FileProvider`, borrado individual y limpieza de huérfanos. |
| Ubicación | Consulta puntual con `FusedLocationProviderClient` y actualizaciones continuas con `callbackFlow`. |
| Audio | Grabación `.m4a` con `MediaRecorder`, detención, cancelación y liberación segura. |
| Arquitectura | DataSource → RepositoryImpl → Repository → UseCase. |
| DI | Constructores públicos con `@Inject`; módulos documentados para activarlos cuando Sarid habilite Hilt. |
| Pruebas | 34 pruebas totales: 24 anteriores + 10 de hardware; 0 fallos, 0 errores y 0 omitidas. |

### Archivos creados

```text
app/src/main/java/com/example/appcrud/core/hardware/
├── camera/
│   ├── data/CameraFileDataSource.kt
│   ├── data/CameraRepositoryImpl.kt
│   ├── di/CameraModule.kt
│   └── domain/
│       ├── model/CapturedPhoto.kt
│       ├── repository/CameraRepository.kt
│       └── usecase/
│           ├── CreatePhotoUriUseCase.kt
│           └── DeleteTempPhotoUseCase.kt
├── location/
│   ├── data/LocationDataSource.kt
│   ├── data/LocationRepositoryImpl.kt
│   ├── di/LocationModule.kt
│   └── domain/
│       ├── model/DeviceLocation.kt
│       ├── repository/LocationRepository.kt
│       └── usecase/
│           ├── GetCurrentLocationUseCase.kt
│           └── ObserveLocationUseCase.kt
└── audio/
    ├── data/AudioRecorderDataSource.kt
    ├── data/AudioRepositoryImpl.kt
    ├── di/AudioModule.kt
    └── domain/
        ├── model/AudioRecording.kt
        ├── repository/AudioRepository.kt
        └── usecase/
            ├── StartAudioRecordingUseCase.kt
            ├── StopAudioRecordingUseCase.kt
            └── ReleaseAudioRecorderUseCase.kt
```

También se crearon:

- `app/src/main/res/xml/file_paths.xml`.
- `app/src/test/java/com/example/appcrud/core/hardware/camera/CameraRepositoryImplTest.kt`.
- `app/src/test/java/com/example/appcrud/core/hardware/location/LocationRepositoryImplTest.kt`.
- `app/src/test/java/com/example/appcrud/core/hardware/audio/AudioRepositoryImplTest.kt`.
- Esta guía: `docs/HARDWARE_STUDY_GUIDE.md`.

### Archivos modificados

- `gradle/libs.versions.toml`: versiones y librerías de Play Services Location, `javax.inject` y MockK.
- `app/build.gradle.kts`: dependencias anteriores.
- `app/src/main/AndroidManifest.xml`: permisos y registro del `FileProvider`.

## 2. La arquitectura y el motivo de cada capa

El recorrido completo será:

```text
API de Android
    ↓
DataSource
    ↓
RepositoryImpl (data)
    ↓
Repository (interfaz de domain)
    ↓
UseCase
    ↓
ViewModel → StateFlow → Compose
```

### `domain/model`

Contiene los modelos que entiende la aplicación:

- `CapturedPhoto`: URI segura y ruta absoluta del archivo.
- `DeviceLocation`: latitud y longitud.
- `AudioRecording`: ruta del audio.

Así, el ViewModel no necesita conocer `Location`, `MediaRecorder` ni `FileProvider` directamente.

### `domain/repository`

Declara **qué operaciones ofrece el negocio**, no cómo funcionan las API de Android.

Ejemplo conceptual:

```kotlin
interface LocationRepository {
    suspend fun getCurrentLocation(): Result<DeviceLocation>
    fun observeLocation(): Flow<Result<DeviceLocation>>
}
```

El dominio depende de este contrato. La implementación concreta puede cambiar sin reescribir el ViewModel.

### `data/*DataSource`

Es la clase que sí habla con Android:

- Crea archivos y URI con `FileProvider`.
- Consulta Google Play Services para ubicación.
- Configura y controla `MediaRecorder`.

También es el lugar donde se comprueban permisos, se convierten objetos Android a modelos propios y se liberan recursos.

### ¿Por qué DataSource y RepositoryImpl no son lo mismo?

El DataSource responde: **“¿cómo obtengo o controlo este recurso?”**.

El RepositoryImpl responde: **“¿cómo cumplo el contrato del dominio?”**.

En este núcleo algunos repositorios parecen simples delegaciones porque todavía no hay más fuentes de datos. Esa separación permite agregar después, sin tocar domain:

- Subida de fotografías o audios a la API.
- Persistencia de metadatos en Room.
- Caché o sincronización.
- Combinación de datos locales y remotos.
- Políticas de reintento.

### `domain/usecase`

Cada caso de uso expresa una acción concreta y evita que el ViewModel dependa de un repositorio completo.

```kotlin
class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository,
) {
    suspend operator fun invoke(): Result<DeviceLocation> =
        repository.getCurrentLocation()
}
```

`operator fun invoke` permite llamar a la instancia como si fuera una función:

```kotlin
val result = getCurrentLocationUseCase()
```

No es una función `suspend` por usar `operator`; lo es porque la consulta puntual debe esperar de forma asíncrona el resultado del GPS. `ObserveLocationUseCase` no es `suspend`: devuelve inmediatamente un `Flow` que se recolecta después.

### `Result<T>`

Una operación puede devolver datos o un error sin lanzar una excepción hacia Compose:

```kotlin
result.fold(
    onSuccess = { value -> /* actualizar estado */ },
    onFailure = { error -> /* publicar mensaje */ },
)
```

`Result.failure` no significa que la app deba cerrarse. El ViewModel debe transformar ese error en un estado visible y accionable.

## 3. Cámara: archivo real y URI segura

### 3.1 Por qué no se usa una miniatura

La captura de miniaturas devuelve un `Bitmap` pequeño y acopla fácilmente la cámara a Compose. Para inventario se necesita una fotografía de mejor calidad, una ruta temporal y una URI que pueda entregarse a otra app de cámara.

Aquí el núcleo crea primero un archivo `.jpg`. La futura UI utilizará el contrato `ActivityResultContracts.TakePicture`, pasándole esa URI. La cámara escribirá la fotografía completa directamente en el archivo.

El DataSource no recibe una `Activity`: únicamente conserva `applicationContext`. Esto evita retener una pantalla destruida después de una rotación.

### 3.2 Creación del archivo, bloque por bloque

`CameraFileDataSource.createPhotoUri()` hace lo siguiente:

1. Obtiene o crea `cacheDir/images`.
2. Usa `File.createTempFile(...)` para generar un nombre único con prefijo `IMG_` y extensión `.jpg`.
3. Construye la autoridad `${applicationId}.fileprovider`.
4. Convierte el archivo en una URI con `FileProvider.getUriForFile(...)`.
5. Devuelve `CapturedPhoto(uri, filePath)` dentro de `Result.success`.

Si el directorio no puede crearse, se produce un `IOException`; `runCatching` lo convierte en `Result.failure`.

La ruta absoluta sirve para administrar o borrar el archivo dentro de nuestra app. La URI `content://` sirve para compartir acceso controlado con la aplicación de cámara.

### 3.3 Qué hacen `file_paths.xml` y el manifiesto

`file_paths.xml` limita el contenido compartido:

```xml
<cache-path
    name="temp_images"
    path="images/" />
```

Esto no expone todo el almacenamiento: solo el directorio de imágenes temporales.

El provider se registra con:

- La misma autoridad `${applicationId}.fileprovider` usada por el DataSource.
- `android:exported="false"`: otras apps no pueden abrir el provider arbitrariamente.
- `android:grantUriPermissions="true"`: sí se puede conceder acceso temporal a una URI concreta.
- Metadatos que apuntan a `@xml/file_paths`.

Nunca debe compartirse una URI `file://` con otra app; en Android moderno puede provocar `FileUriExposedException`.

### 3.4 Borrado y huérfanos

`deleteTempPhoto` compara rutas canónicas y exige que el archivo sea hijo directo de `cacheDir/images`. Así evita que una ruta arbitraria termine borrando otro archivo de la aplicación.

`deleteOrphanedTempPhotos` elimina archivos `.jpg` que no aparecen en el conjunto de rutas conservadas. La futura capa de negocio debe pasar las rutas que siguen asociadas a equipos o incidencias.

Importante: el caché puede ser eliminado por Android. Estas fotografías todavía no constituyen almacenamiento permanente ni una URL de servidor.

## 4. Ubicación: consulta puntual y flujo continuo

### 4.1 Permisos primero

Antes de llamar al GPS se acepta `ACCESS_FINE_LOCATION` **o** `ACCESS_COARSE_LOCATION`.

Si ninguno está concedido, ambas operaciones devuelven:

```kotlin
Result.failure(SecurityException("Permiso de ubicación no concedido"))
```

Esto evita invocar una API protegida sin permiso. Declarar permisos en el manifiesto no equivale a que el usuario los haya concedido en tiempo de ejecución.

### 4.2 Consulta puntual con `suspend`

`getCurrentLocation()` combina:

- `FusedLocationProviderClient`: el proveedor de ubicación de Google Play Services.
- `Priority.PRIORITY_BALANCED_POWER_ACCURACY`: equilibrio entre precisión y batería.
- `suspendCancellableCoroutine`: adapta los listeners de un `Task` a una función `suspend`.
- `CancellationTokenSource`: cancela también la petición de Play Services si se cancela la corrutina.

El listener de éxito puede entregar `null`. En ese caso se devuelve un fallo con el mensaje `No hay una ubicación actual disponible`, no un objeto con coordenadas inventadas.

Cuando sí existe una `Location`, se transforma a `DeviceLocation(latitude, longitude)`.

Antes de reanudar la corrutina se comprueba `continuation.isActive`, evitando reanudar una operación que ya fue cancelada.

### 4.3 Flujo continuo con `callbackFlow`

`observeLocation()` crea un flujo frío: el registro de actualizaciones ocurre cuando un consumidor recolecta el flujo.

Dentro del `callbackFlow`:

1. Se vuelven a comprobar los permisos.
2. Se crea un `LocationCallback`.
3. `onLocationResult` recibe las ubicaciones.
4. `trySend(...)` publica cada valor en el canal del flujo.
5. `LocationRequest` define intervalos de 10 segundos y un mínimo de 5 segundos.
6. Los errores de registro se emiten como `Result.failure` y cierran el flujo.

El bloque más importante es:

```kotlin
awaitClose {
    fusedLocationClient.removeLocationUpdates(locationCallback)
}
```

`awaitClose` se ejecuta cuando el flujo se cierra o su recolector se cancela. Ahí se elimina exactamente el mismo `LocationCallback` registrado. Sin esta llamada, el GPS podría seguir enviando actualizaciones y reteniendo recursos después de salir de la pantalla.

Una consulta puntual es adecuada para guardar una verificación. Un flujo continuo consume más batería y solo debe mantenerse activo mientras la funcionalidad realmente lo necesite.

## 5. Audio: estados y liberación de `MediaRecorder`

### 5.1 Creación compatible con cada Android

La fábrica utiliza:

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    MediaRecorder(context)
} else {
    MediaRecorder()
}
```

El constructor con `Context` corresponde a API 31 o superior. El constructor sin argumentos se conserva para versiones anteriores.

La fábrica también permite sustituir el recorder por un mock en pruebas JVM, sin cambiar la lógica de estados del DataSource.

### 5.2 Configuración de la grabación

Al iniciar:

1. Se crea `cacheDir/audio`.
2. `File.createTempFile(...)` genera un archivo único `.m4a`.
3. Se selecciona el micrófono con `AudioSource.MIC`.
4. Se configura contenedor `MPEG_4` y codificador `AAC`.
5. Se establecen 128 kbps y 44 100 Hz.
6. Se asigna la ruta de salida.
7. Se ejecuta `prepare()`.
8. Se ejecuta `start()`.

El orden importa: no se debe llamar a `start()` antes de configurar y preparar el recorder.

### 5.3 Estados principales

| Situación | Comportamiento |
| --- | --- |
| Inicio correcto | Existen `mediaRecorder` y `currentFile`; el audio está grabando. |
| Segundo inicio mientras ya graba | Devuelve estado inválido, abandona el temporal y libera la instancia anterior. |
| `stop()` correcto | Devuelve la ruta del audio conservado y libera el recorder. |
| `stop()` sin grabación | Devuelve `IllegalStateException`, sin intentar detener un objeto inexistente. |
| Error al preparar, iniciar o detener | Devuelve `Result.failure`, elimina el archivo incompleto y libera. |
| `cancelRecording()` | Intenta detener, borra el temporal y siempre libera. |
| `release()` | Libera si existe una instancia y deja las referencias en `null`. |

`MediaRecorder.stop()` puede fallar si la grabación es demasiado corta o el estado interno no es válido. Por eso nunca se da por hecho que terminará correctamente.

### 5.4 Por qué se usa `try/finally`

Al detener, el patrón es conceptualmente:

```kotlin
try {
    recorder.stop()
    Result.success(AudioRecording(file.absolutePath))
} catch (error: Exception) {
    file.delete()
    Result.failure(error)
} finally {
    release()
}
```

Tanto si `stop()` funciona como si lanza una excepción, se ejecuta `release()`.

A su vez, `release()` pone `mediaRecorder = null` y `currentFile = null` en su propio `finally`. Esto evita conservar el micrófono, reutilizar una instancia en estado inválido o retener una ruta que ya no representa una grabación activa.

La liberación es idempotente: llamarla de nuevo cuando ya no existe recorder no produce un error. `AudioRepositoryImpl.stopRecording()` también libera en `finally` como segunda barrera de seguridad.

Para abandonar una pantalla durante una grabación, la integración futura debe cancelar explícitamente si no desea conservar el archivo; `onCleared()` del ViewModel debe liberar siempre.

## 6. Pruebas unitarias

Comando ejecutado:

```powershell
$env:JAVA_HOME="C:\Users\Gigan\AppData\Local\Programs\Android Studio\jbr"
./gradlew testDebugUnitTest
```

La ruta indicada inicialmente para Android Studio (`C:\Program Files\Android\Android Studio\jbr`) no existe en este equipo. Se utilizó el JBR real instalado en el perfil del usuario.

Resultado:

```text
BUILD SUCCESSFUL
34 pruebas
0 fallos
0 errores
0 omitidas
```

### Cámara: 3 pruebas

- Se crea físicamente un `.jpg` en el directorio temporal y se devuelve la URI segura.
- Se elimina el archivo asociado.
- Se eliminan huérfanos respetando las rutas conservadas.

Se usa un directorio real de `TemporaryFolder`; solamente se simulan `Context`, `Uri` y la llamada estática a `FileProvider`, porque las clases de Android no están disponibles como implementación real en una prueba JVM local.

### Ubicación: 2 pruebas

- La consulta puntual sin permisos devuelve `SecurityException` con el mensaje requerido.
- El flujo sin permisos devuelve el mismo fallo.

Las pruebas terminan antes de invocar Play Services, por lo que no necesitan un dispositivo GPS.

### Audio: 5 pruebas

- Ciclo `start → stop → release`, creación `.m4a` y una sola liberación real del mock.
- Detención sin grabación activa.
- Error durante `prepare()`.
- Error durante `stop()` y borrado del archivo incompleto.
- Cancelación, borrado y liberación.

Estas pruebas verifican la lógica JVM y las interacciones con `MediaRecorder`. La calidad del audio, la cámara física, los permisos reales y la precisión GPS necesitan después una prueba en dispositivo/emulador.

## 7. Integración futura con ViewModel y Compose

Esta sección es una guía para la siguiente fase; esas pantallas todavía no se han modificado.

### 7.1 Estado del ViewModel

```kotlin
data class HardwareUiState(
    val photo: CapturedPhoto? = null,
    val location: DeviceLocation? = null,
    val recording: AudioRecording? = null,
    val isRecording: Boolean = false,
    val isLoadingLocation: Boolean = false,
    val error: String? = null,
)
```

El ViewModel recibirá casos de uso, no DataSources:

```kotlin
class HardwareViewModel @Inject constructor(
    private val createPhotoUri: CreatePhotoUriUseCase,
    private val getCurrentLocation: GetCurrentLocationUseCase,
    private val startAudioRecording: StartAudioRecordingUseCase,
    private val stopAudioRecording: StopAudioRecordingUseCase,
    private val releaseAudioRecorder: ReleaseAudioRecorderUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HardwareUiState())
    val uiState = _uiState.asStateFlow()

    fun preparePhoto() {
        createPhotoUri().fold(
            onSuccess = { photo -> _uiState.update { it.copy(photo = photo) } },
            onFailure = { error -> _uiState.update { it.copy(error = error.message) } },
        )
    }

    fun loadLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocation = true) }
            val result = getCurrentLocation()
            _uiState.update { state ->
                state.copy(
                    location = result.getOrNull(),
                    isLoadingLocation = false,
                    error = result.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun toggleRecording() {
        val result = if (_uiState.value.isRecording) {
            stopAudioRecording()
        } else {
            startAudioRecording()
        }
        _uiState.update { state ->
            state.copy(
                recording = result.getOrNull() ?: state.recording,
                isRecording = result.isSuccess && !state.isRecording,
                error = result.exceptionOrNull()?.message,
            )
        }
    }

    override fun onCleared() {
        releaseAudioRecorder()
        super.onCleared()
    }
}
```

Las operaciones de archivo pueden trasladarse a `Dispatchers.IO` si la integración real muestra trabajo considerable. No se debe bloquear el hilo principal con subidas o procesamiento pesado.

Para actualizaciones continuas se guarda el `Job` de recolección y se cancela al dejar de necesitar el GPS. Cancelar el `Job` cierra el `callbackFlow` y ejecuta `awaitClose`.

### 7.2 Estado con ciclo de vida en Compose

Cuando se agregue la dependencia `androidx.lifecycle:lifecycle-runtime-compose`, la pantalla observará:

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

Es preferible a recolectar sin considerar el ciclo de vida porque evita trabajo de UI cuando esta no está activa.

Esta recolección no sustituye la cancelación de un Job GPS independiente lanzado dentro del ViewModel. El ViewModel sigue siendo responsable de detener ese flujo.

### 7.3 Conexión con la cámara

La UI prepara el archivo mediante el caso de uso y después lanza `ActivityResultContracts.TakePicture` con `uiState.photo?.uri`.

Al terminar la captura:

- Si fue satisfactoria, se conserva la referencia y posteriormente se sube o asocia al equipo/incidencia.
- Si el usuario cancela, se ejecuta `DeleteTempPhotoUseCase` para no dejar un archivo vacío.
- Si el usuario abandona el flujo, se aplica la política de huérfanos.

El DataSource sigue sin recibir `Activity`, launcher ni Composable.

## 8. Permisos, rechazo permanente y Ajustes

La solicitud runtime pertenece a la capa de UI. El DataSource vuelve a comprobar el permiso como barrera de seguridad.

| Hardware | Permiso runtime |
| --- | --- |
| Cámara | `CAMERA` |
| Ubicación | `ACCESS_FINE_LOCATION` y `ACCESS_COARSE_LOCATION` |
| Audio | `RECORD_AUDIO` |

Flujo recomendado:

1. La persona pulsa una acción con contexto, por ejemplo “Tomar evidencia”.
2. Se explica para qué se necesita el permiso.
3. Se solicita con `rememberLauncherForActivityResult`.
4. Si se concede, se ejecuta el caso de uso.
5. Si se rechaza una vez, se muestra una explicación y una opción para reintentar.
6. Si el rechazo es permanente, se muestra un botón “Abrir Ajustes”; no se insiste con un diálogo que Android ya no presentará.
7. Al volver de Ajustes, se comprueba nuevamente el permiso.

Para distinguir el rechazo permanente se usa `shouldShowRequestPermissionRationale(activity, permission)` **después de haber solicitado el permiso al menos una vez**:

- Denegado + rationale `true`: normalmente se puede explicar y volver a solicitar.
- Denegado + rationale `false` + solicitud ya realizada: puede ser denegación permanente o “No volver a preguntar”; se ofrece Ajustes.
- Antes de la primera solicitud no se debe etiquetar como permanente solo porque rationale sea `false`.

Apertura de Ajustes desde UI:

```kotlin
fun openApplicationSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null),
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
```

La pantalla puede obtener temporalmente la `Activity` para rationale; esa dependencia no debe pasarse a DataSource ni Repository.

## 9. Activación de Hilt coordinada con Sarid

Actualmente no existen el plugin ni las dependencias de Hilt. Por eso `CameraModule`, `LocationModule` y `AudioModule` son marcadores con el código de activación documentado, no clases anotadas que romperían la compilación.

Cuando Hilt esté habilitado:

1. Mantener los `@Inject constructor` de DataSources, RepositoryImpl y UseCases.
2. Convertir cada marcador en `@Module` con `@InstallIn(SingletonComponent::class)`.
3. Usar `@Binds` para asociar cada RepositoryImpl con su interfaz.
4. Usar `@Provides` con `@ApplicationContext` para DataSources y para el `FusedLocationProviderClient`.
5. Decidir el alcance de audio deliberadamente: un singleton obliga a que todas las pantallas compartan una única grabación; si se requieren grabaciones independientes, conviene un alcance más corto.
6. No registrar una segunda fábrica manual de los mismos repositorios en `AppContainer`.

La futura UI podrá usar `@HiltViewModel` solamente después de completar esa integración.

## 10. Qué falta fuera de este núcleo

- Conectar los casos de uso a ViewModels y Compose.
- Implementar launchers, rationale, rechazo permanente y regreso desde Ajustes.
- Subir y asociar fotografías/audios a equipos o incidencias.
- Sustituir los temporales por almacenamiento persistente cuando el negocio lo requiera.
- Probar hardware y permisos reales en dispositivo.
- Integrar los módulos Hilt cuando Sarid termine la base de DI.

El hecho de que el núcleo y sus pruebas estén terminados no significa que las tres funciones ya sean visibles o estén conectadas a los procesos de negocio.
