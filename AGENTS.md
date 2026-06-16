# PointCheck - Fuente de Verdad (Consolidado)

## 📌 Contexto del Sistema
- **Fase:** Estabilización de Producción (Mock-free) - **COMPLETA**.
- **Backend IP:** `10.24.11.114`.
- **Auth:** Basic Auth vía `AuthInterceptor`.
- **Arquitectura:** MVVM + Clean Architecture + StateFlow.
- **Estado de Mocks:** ELIMINADOS. Los ViewModels ahora manejan estados reales y errores del servidor.

## 📊 Estado del Plan Maestro
1. [DONE] **Sincronización de Sesión**: `UserPreferences` soporta actualizaciones parciales.
2. [DONE] **Notificaciones Locales**: `ReminderScheduler` integrado en `ReservationViewModel`.
3. [DONE] **Integridad de Datos**: Implementado `getAttentionByReservation` y corregido paquete en `ServiceViewModel`.
4. [DONE] **UX & Agenda Profesional**: Implementados estados de `Loading` (esqueletos), panel de detalles de cita y diferenciación visual de Agenda vs. Cobros.
5. [DONE] **Persistencia de Reportes**: Configurado `FileProvider` para exportación de CSVs.
6. [DONE] **Auditoría de Integridad**: Registro de eventos en tiempo real (creación y pagos) vinculado a `AuditLogger`.
7. [DONE] **Validación de "No-Show"**: Gestión visual de citas expiradas y expiración automática en UI.
8. [DONE] **Supervisión Global (Admin)**: Activado acceso administrativo a todas las reservas del sistema y navegación desde el panel maestro.

## 🛠 Notas Técnicas Recientes
- **Agenda Dual & Global**: El sistema ahora distingue entre Mis Atenciones, Mis Reservas y Supervisión Global (Admin) en una sola pantalla adaptativa.
- **Anti-flicker**: El Dashboard ya no parpadea al cargar, manteniendo la UI previa mientras se sincronizan los datos en segundo plano.
- **Flujo Financiero**: Se integró el acceso a "Mis Cobros" y la acción rápida de "Cobrar y Finalizar" en la agenda del profesional.
- **Admin Full**: Se eliminaron todos los diálogos de "Módulo en Desarrollo", conectando métricas reales con sus respectivas pantallas de gestión.
