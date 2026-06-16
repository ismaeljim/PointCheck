# PointCheck - Documentación del Proyecto

PointCheck es una plataforma integral para la gestión de citas y servicios profesionales, diseñada para optimizar la interacción entre especialistas y clientes.

## 🚀 Funcionalidades Recientes (Fases 4-6: Atención, Navegación y Rendimiento)

Se ha completado la auditoría y refactorización del flujo core:

1. **Gestión de Atenciones y Facturación (Fase 4)**:
   - **Ciclo de Vida**: Flujo de `IN_PROGRESS` a `FINISHED` con generación automática de `BillingRecord`.
   - **Trazabilidad**: Vinculación Reserva -> Atención -> Cobro asegurando integridad de datos.

2. **Refactor de Navegación (Fase 5)**:
   - **Simplificación de Rutas**: Las pantallas de `Attention` y `Billing` ahora se acceden únicamente mediante `reservationId`. Se eliminó la redundancia de pasar IDs de cliente/especialista en la URL.
   - **Carga Reactiva**: Los ViewModels ahora son responsables de recuperar el contexto completo (`UserSummaryDto`) a partir del ID de la reserva, garantizando consistencia.

3. **Optimización de Rendimiento (Fase 6)**:
   - **Solución N+1 (Backend)**: Implementación de `@EntityGraph` en Repositorios (`Reservation`, `Attention`, `Billing`) para realizar Fetch Joins y reducir drásticamente el número de consultas SQL.
   - **Optimización de UI (Android)**: Auditoría de recomposiciones en Compose y uso eficiente de `StateFlow` para evitar fugas de memoria y sobrecarga de CPU.

## 📉 Estado de Implementación y Auditoría

### Autenticación y Seguridad ✅
- Comentado técnico y auditoría de flujo completada.

### Atenciones y Facturación ✅
- **Auditoría de Backend**: Optimizada con `@EntityGraph`.
- **Navegación**: Refactorizada para usar rutas minimalistas.

### Arquitectura de Datos (DTOs) ✅
- **UserSummaryDto**: Estandarizado en toda la App para representar usuarios (Cliente/Especialista) con consistencia de campos.

### Módulo de Reportes y BI ✅
- **KPIs Automáticos**: Cálculo de ingresos, carga de trabajo (horas) y promedios de atención por periodo.
- **Filtros Avanzados**: Capacidad de segmentar métricas por tipo de servicio específico para análisis de rentabilidad.
- **Exportación CSV**: Generación de reportes compatibles con Excel mediante delimitadores `;` para localización regional.
- **Comparativa Temporal**: Soporte nativo para comparación de ingresos contra el periodo anterior (semana/mes).
- **Brecha Técnica**: En el backend, los cálculos se realizan en memoria; para escalabilidad se deben migrar a agregaciones SQL. En la App, se requiere implementar `FileProvider` para la persistencia física de los CSVs exportados.

## 🎨 UI Core y Dashboard (Fase 6: Experiencia de Usuario) ✅

Se ha auditado la capa de presentación y las integraciones externas:

1. **Arquitectura de Componentes Reutilizables**:
   - **Atomicidad**: Implementación de `AppComponents.kt` con estilos estandarizados en Material3.
   - **Feedback Visual**: Uso de `PCCard` con soporte para colores semánticos (`containerColor`) para estados de éxito, advertencia y error (ej: "No-Show").
   - **UX Adaptativa**: Formularios dinámicos que cambian según el tipo de dato.

2. **Dashboard Polimórfico**:
   - **Personalización por Rol**: Tres vistas distintas (Admin, Profesional, Cliente) centralizadas en un único punto de entrada.
   - **Centro de Notificaciones**: Implementación base de `ReminderScheduler` para alertas locales (Pendiente integración con flujo de reservas).

3. **Integraciones Externas**:
   - **Geolocalización**: `LocationViewModel` preparado para geocodificación inversa y sugerencias de direcciones (En uso en Perfil Profesional).
   - **Motor de Clima**: Consumo de OpenWeather para contexto en el Dashboard.

## 🛠 Pendientes y Brechas Técnicas (Hacia Producción)

1. **Notificaciones Locales**: El módulo `core.notifications` está funcional pero no se invoca desde los flujos de creación de reservas. Se debe integrar `ReminderScheduler` en `BookingViewModel`.
2. **Identidad Visual**: El `SplashScreen` requiere el logo oficial y una transición animada. Actualmente usa un placeholder de texto.
3. **Limpieza de Código**: 
   - `LocationViewModel`: Eliminar `getLatLngFromAddress` si no se requiere para navegación externa.
   - `UserPreferences`: Unificar los flujos `role` y `userRole` para evitar redundancia.
4. **Persistencia de Reportes**: Implementar `FileProvider` para permitir que el usuario comparta/guarde los CSVs generados en el módulo de BI.
5. **Caché de Clima**: Implementar una política de caché (ej: 30 min) para los datos meteorológicos para reducir consumo de API y datos.

## 📊 Datos y Modelado
- El esquema de base de datos (`schema.sql`) incluye tablas para:
  - Usuarios y Perfiles Profesionales.
  - Servicios y Disponibilidad.
  - Reservas, Atenciones y Facturación.
  - Notificaciones y Suscripciones.

## 🧪 Pruebas
Para ejecutar las pruebas de integración del backend:
```bash
cd backend
./gradlew test --tests "com.duoc.app.ReservationE2ETest"
```