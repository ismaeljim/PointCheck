# PointCheck - Documentación del Proyecto

PointCheck es una plataforma integral para la gestión de citas y servicios profesionales, diseñada para optimizar la interacción entre especialistas y clientes.

## 🚀 Funcionalidades Recientes (Fase 4: Atención y Facturación)

Se ha completado la auditoría del ciclo operativo y financiero:

1. **Gestión de Atenciones (Workflow Especialista)**:
   - **Ciclo de Vida**: Flujo controlado de `IN_PROGRESS` a `FINISHED` con registro automático de tiempos de ejecución.
   - **Trazabilidad Operativa**: Vinculación mandatoria entre Reserva -> Atención para auditoría de cumplimiento de citas.
   - **Observaciones de Sesión**: Capacidad de registrar notas clínicas o comerciales durante la atención que persisten en el historial del cliente.

2. **Automatización Financiera (Billing)**:
   - **Gatillado Automático**: Al finalizar una atención, el sistema genera inmediatamente un `BillingRecord` (Registro de Cobro) basado en el precio del servicio pactado.
   - **Manejo de Estados de Pago**: Control de cobranza mediante estados `PENDING`, `PAID` y `CANCELLED`.
   - **Conciliación**: Soporte para referencias externas para cruce con transferencias bancarias o vouchers.

3. **Arquitectura de Sincronización**:
   - **Atomicidad Transaccional**: Uso de `@Transactional` en el backend para asegurar que la finalización de una cita y la generación de su cobro sean indivisibles.
   - **UX de Control**: El `AttentionViewModel` en la App permite al especialista gestionar la sesión de forma reactiva con feedback inmediato de éxito.

## 📈 Estado de Implementación y Auditoría

### Autenticación y Seguridad ✅
- Comentado técnico y auditoría de flujo completada.
- *Hallazgo*: Las contraseñas se almacenan en texto plano (Pendiente: BCrypt).

### Perfiles y Especialistas ✅
- Implementada la gestión de perfiles comerciales y horarios.

### Servicios y Reservas ✅
- Validada lógica de colisiones y motor de disponibilidad.
- **Brecha**: Se recomienda implementar `@Transactional` en el proceso de creación de reservas.

### Atenciones y Facturación ✅
- **Auditoría de Backend**: Flujo de estados y generación de cobros validado.
- **Auditoría de App**: Gestión de sesión por parte del especialista auditada.
- **Brecha Financiera**: Falta integración con pasarelas de pago digitales (Webpay/Stripe); actualmente el flujo es de registro manual/lógico.

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
   - **Feedback Visual**: Uso de Skeleton Loading (Shimmer Effect) en el Dashboard para mejorar la percepción de rendimiento.
   - **UX Adaptativa**: Formularios dinámicos que cambian según el tipo de dato (ej: `DayScheduleRow` con selectores de tiempo nativos).

2. **Dashboard Polimórfico**:
   - **Personalización por Rol**: Tres vistas distintas (Admin, Profesional, Cliente) centralizadas en un único punto de entrada con lógica de navegación segregada.
   - **Centro de Notificaciones**: Sistema de alertas in-app con estados de lectura persistentes.

3. **Integraciones Externas**:
   - **Geolocalización Inversa**: Uso de Intents implícitos para delegar la navegación a aplicaciones de mapas (Google Maps) mediante coordenadas o direcciones textuales.
   - **Motor de Clima (Weather Integration)**: Consumo asíncrono de la API de OpenWeather para ofrecer sugerencias contextuales al usuario basadas en las condiciones meteorológicas del día de su cita.
   - **Auditoría de Logs**: Pantalla administrativa para supervisión de cambios críticos en el sistema (cambio de roles, desactivación de usuarios).

4. **Brechas Detectadas**:
   - **Permisos**: Falta solicitud explícita de permisos de ubicación `ACCESS_FINE_LOCATION` si se desea automatizar la detección de la ciudad del usuario.
   - **Caché**: Los datos del clima no se persisten localmente; se recargan en cada apertura del Dashboard.

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