# IT Inventory

Aplicación Android para gestionar un inventario de equipos de cómputo. Usa Kotlin, Jetpack Compose, MVVM, Retrofit e inyección manual de dependencias. La autenticación y los datos los proporciona el proyecto hermano `API-PRUEBA-ITINVENTORY`.

## Funcionalidad

- Registro e inicio de sesión con correo y contraseña.
- Confirmación de contraseña y validación de formularios.
- Sesión persistente y cierre de sesión.
- Listado, creación, edición y eliminación de equipos.
- Validación de fechas, estados y número de inventario.
- Mensajes diferenciados para errores de red, HTTP y respuestas inválidas.

## Requisitos

- JDK 17.
- Android SDK con las plataformas requeridas por el proyecto.
- Node.js 18 o superior para ejecutar la API.
- Emulador Android o dispositivo con Android 8.0 (API 26) o superior.

## 1. Iniciar la API

Desde la carpeta `API-PRUEBA-ITINVENTORY`:

```powershell
npm install
npm start
```

La API escucha por defecto en el puerto `3000`. Consulta su README para conocer las cuentas iniciales y el contrato HTTP.

## 2. Configurar la URL en Android

La URL se fija al compilar mediante la propiedad Gradle `API_BASE_URL` y debe terminar en `/` (el proyecto la agrega si falta). El valor predeterminado actual es la API publicada en `https://api-prueba-itinventory.onrender.com/`, por lo que puedes compilar normalmente sin pasar parámetros.

### Emulador Android con API local

Para volver temporalmente a una API ejecutada en tu PC:

```powershell
.\gradlew.bat :app:assembleDebug "-PAPI_BASE_URL=http://10.0.2.2:3000/"
```

`10.0.2.2` apunta desde el emulador al equipo donde se ejecuta la API.

### Teléfono físico con API local

El teléfono y el equipo con la API deben estar en la misma red. Sustituye la IP del ejemplo por la IP local de tu equipo:

```powershell
.\gradlew.bat :app:assembleDebug "-PAPI_BASE_URL=http://192.168.1.20:3000/"
```

La API escucha en `0.0.0.0`; si no responde, revisa que el firewall permita conexiones entrantes al puerto `3000`. Las builds **debug** permiten HTTP para las pruebas locales. Para desplegar fuera de este entorno académico se debe usar HTTPS.

También puedes abrir el proyecto en Android Studio y ejecutar la configuración de la app después de compilar con la propiedad correspondiente.

## 3. Compilar y probar

En Windows, desde esta carpeta:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:assembleDebug
```

El APK debug se genera en:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Las pruebas unitarias cubren validadores, mapeo DTO/dominio, errores de repositorio y estados de los ViewModels. No sustituyen una prueba visual o de integración en un dispositivo.

## Arquitectura

- `data`: DTOs, Retrofit, almacenamiento de sesión, mappers y repositorios.
- `domain`: modelos, contratos de repositorio y validadores.
- `di`: composición e inyección manual de dependencias.
- `ui`: pantallas Compose, navegación y ViewModels.

Retrofit expone cuerpos directamente. La capa `data/remote/ApiCall.kt` centraliza la conversión de excepciones HTTP y de red para evitar duplicar ese manejo en la UI.
