# AGENTS.md — PointCheck Architectural Baseline

## 🏛️ Estado y Filosofía del Proyecto
PointCheck es una plataforma SaaS transaccional donde la correctitud del modelo de dominio y la robustez de la UI frente a condiciones de carrera tienen prioridad absoluta.

---

# 🧭 Principios Arquitectónicos Obligatorios

## 1. Arquitectura de Memoria Caliente (Hot State)
**ADR-007 IMPLEMENTADO.** Se prohíbe el acceso directo y asíncrono a DataStore/EncryptedPrefs desde la UI para decisiones de navegación. `UserPreferences` mantiene una caché en RAM sincronizada en el `warmUp` inicial. La app debe suspenderse en el Splash hasta que `isInitialized` sea verdadero.

## 2. Inicialización de Sesión Atómica
Ningún ViewModel o componente de UI puede asumir un rol por defecto (ej. "CLIENT"). Se debe respetar el estado de incertidumbre controlada `""` (vacío) mientras la sesión se sincroniza desde el disco a la RAM.

## 3. Navegación Segura y Robusta
* **Prohibición de popUpTo(0):** Queda estrictamente prohibido el uso de `0` como destino de limpieza de stack. Se debe usar `navController.graph.findStartDestination().id` para mantener la integridad del `NavHost`.
* **Software Debounce:** Toda acción de navegación global (NavigationBar) debe implementar un cerrojo de **500ms** para prevenir el "Process Trashing" por clics rápidos.

## 4. Separación de Identidad y Actividad Comercial (DDD)
**User** representa autenticación y roles. **ProfessionalProfile** representa la actividad comercial.

## 5. Reactive Error Events
Uso de `Channel` (`errorEvents`) en ViewModels para comunicar errores de negocio "one-shot" vía Snackbars.

## 6. Identity Fail-Safe & Atomic Purge
Ante `401/403`, los ViewModels abortan silenciosamente. El `AuthInterceptor` usa un `AtomicBoolean` para garantizar una única purga de sesión ante ráfagas de errores concurrentes.

## 7. Routing & Contract Governance (API Safety)
**ADR-008 IMPLEMENTADO.** Para evitar colisiones con el manejador de recursos estáticos de Spring Boot (`NoResourceFoundException`), todos los endpoints de API deben residir bajo prefijos claros (`/api/...`) y contar con controladores dedicados para rutas raíz de gobernanza (ej. `/api/audit`).

---

# 🔒 Architectural Decision Records

## ADR-001 al ADR-004: [CONSULTAR VERSIONES PREVIAS PARA DETALLES]
(ProfessionalProfile como eje, Desacoplamiento de Entidades, Renombre de IDs, Propagación Reactiva).

## ADR-005: Trazabilidad Global y Gobernanza (Admin Module)
Estado: **IMPLEMENTADO**. Registro obligatorio de `target_id` y `target_name` en auditoría. Sincronización de métricas reales desde `AdminService` eliminando mocks de negocio.

## ADR-006: Estandarización de Capa de Presentación (M3 Atomic Design)
Estado: **IMPLEMENTADO**. Uso obligatorio de `PointCheckCard`, `PointCheckTextField` y `PointCheckButton`.

## ADR-007: Arquitectura de Persistencia de Baja Latencia
Estado: **IMPLEMENTADO**. Implementación de RAM Cache en `UserPreferences` para eliminar el "Estado Espectro" y redirecciones erróneas al Login durante el arranque o cambios de pestaña.

## ADR-008: Aislamiento de Rutas de Auditoría y Administración
Estado: **IMPLEMENTADO**. Separación de `AuditController` de `AdminController` para cumplir con el contrato estricto `/api/audit` exigido por el frontend, garantizando respuestas JSON consistentes y evitando fallbacks a recursos estáticos.

---

# 🚨 Regla de Continuidad para IA
1. Leer AGENTS.md y AI_HANDOFF.md antes de proponer cambios.
2. Prohibido eliminar el debounce de 500ms en `MainActivity.kt`.
3. Prohibido cambiar el valor inicial de `cachedRole` de `""` a cualquier rol por defecto.
4. Las listas de categorías en el Dashboard deben mantenerse como `LazyRow` (Carrusel) para evitar ítems huérfanos.
