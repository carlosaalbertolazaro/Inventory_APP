# IT Inventory — paquete documental Unidad 4

**Versión:** 1.0  
**Fecha:** 27 de noviembre de 2026  
**Propósito:** validar alcance, coordinar la implementación y preparar las evidencias del avance y de la entrega final.

> Los documentos describen el **MVP objetivo**. Que una historia o endpoint aparezca aquí no significa que ya esté implementado. El estado real se registra en `05_SEGUIMIENTO_Y_EVIDENCIAS.md`.

## Archivos

| Archivo | Uso | Audiencia |
| --- | --- | --- |
| `01_INGENIERIA_DE_REQUERIMIENTOS.md` | Negocio, roles, reglas, tres procesos, 19 historias con criterios y MoSCoW. | Profesor y equipo |
| `02_WIREFRAMES_Y_NAVEGACION.md` | Wireframes de todas las historias Must, navegación por rol, permisos y estados UI. | Profesor, Android y diseño |
| `03_ARQUITECTURA_Y_CONTRATOS.md` | Estructura Clean/Hilt, contratos de datos, Room, FCM, hardware y criterios técnicos. | Android y backend |
| `04_GUIA_DE_IMPLEMENTACION_SARID.md` | Secuencia concreta para integrar y pulir Hilt sin bloquear las features de hardware. | Sarid |
| `05_SEGUIMIENTO_Y_EVIDENCIAS.md` | Estado actual, checklist del avance, matriz de rúbrica y guion de demostración. | Equipo y profesor |
| `../../../API-PRUEBA-ITINVENTORY/docs/openapi.yaml` | Contrato OpenAPI objetivo de autenticación, recursos, roles y errores. | Android y backend |

La ruta de OpenAPI anterior es relativa a la ubicación lógica de los proyectos hermanos. En este repositorio se puede consultar directamente en:

`C:\Users\Gigan\OneDrive\Documentos\proyecto Ali\API-PRUEBA-ITINVENTORY\docs\openapi.yaml`

## Qué enviar a Sarid

1. `04_GUIA_DE_IMPLEMENTACION_SARID.md` como instrucción de trabajo.
2. `03_ARQUITECTURA_Y_CONTRATOS.md` para conocer las interfaces y límites.
3. `01_INGENIERIA_DE_REQUERIMIENTOS.md` y `02_WIREFRAMES_Y_NAVEGACION.md` para validar que el código corresponde al negocio.
4. `openapi.yaml` para no inventar nombres de campos ni respuestas.

## Qué presentar como avance

- Documento de requerimientos y wireframes.
- OpenAPI como contrato propuesto, identificado como **diseño**, no como backend terminado.
- Aplicación actual compilando y pruebas existentes pasando.
- Demostración de login, registro, sesión y CRUD ya disponibles.
- Tablero de pendientes y responsables de `05_SEGUIMIENTO_Y_EVIDENCIAS.md`.

No se deben mostrar como terminados Hilt, Room, FCM, roles funcionales ni hardware hasta que exista una verificación real.

## Convenciones

- Prioridades: **Must**, **Should**, **Could**, **Won't**.
- Historias: `HU-01`…`HU-19`.
- Procesos: `P-01` inventario, `P-02` asignaciones, `P-03` incidencias.
- Wireframes: `W-01`…`W-13`.
- Reglas de negocio: `RN-01`…`RN-15`.
- Roles técnicos y de API: `ADMIN` y `USER`.
- Fechas de negocio: `AAAA-MM-DD`; fechas de auditoría: ISO 8601 UTC.
- El contrato de API usa nombres de recursos existentes en español y los mínimos literales exigidos: `users/me`, `contact` y `devices`.

## Fuentes

- `Rubrica_Proyecto_Unidad4.pdf`, versión entregada por el profesor, 40 puntos.
- `Proyecto_Unidad4_App_MVP.pdf`, versión 1.0, noviembre de 2026.
- Estructura de referencia: repositorio `kt_demohilt` del profesor.
- Estado verificado del código Android y API al 27 de noviembre de 2026.
