# IT Inventory

Aplicación Android para gestionar un inventario de equipos de cómputo. Usa Kotlin, Jetpack Compose, MVVM, Retrofit e inyección manual de dependencias. La autenticación y los datos los proporciona el proyecto hermano `API-PRUEBA-ITINVENTORY`.

## Funcionalidad

- Registro e inicio de sesión con correo y contraseña.
- Confirmación de contraseña y validación de formularios.
- Sesión persistente y cierre de sesión.
- Listado, creación, edición y eliminación de equipos.
- Validación de fechas, estados y número de inventario.
- Mensajes diferenciados para errores de red, HTTP y respuestas inválidas.

## 2. Configurar la URL en Android

La URL se fija al compilar mediante la propiedad Gradle `API_BASE_URL` y debe terminar en `/` (el proyecto la agrega si falta). El valor predeterminado actual es la API publicada en `https://api-prueba-itinventory.onrender.com/`, por lo que puedes compilar normalmente sin pasar parámetros.

## Arquitectura

- `data`: DTOs, Retrofit, almacenamiento de sesión, mappers y repositorios.
- `domain`: modelos, contratos de repositorio y validadores.
- `di`: composición e inyección manual de dependencias.
- `ui`: pantallas Compose, navegación y ViewModels.

Retrofit expone cuerpos directamente. La capa `data/remote/ApiCall.kt` centraliza la conversión de excepciones HTTP y de red para evitar duplicar ese manejo en la UI.
