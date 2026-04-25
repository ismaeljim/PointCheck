# PointCheck Backend - Feature-Based Architecture

Este es el backend del ecosistema **PointCheck**, una solución integral para la gestión de citas y servicios entre clientes y especialistas. Está desarrollado con **Kotlin**, **Spring Boot 3.3.1**, y **MySQL**, siguiendo principios de diseño moderno y escalable.

## 🏗️ Arquitectura y Estructura

El proyecto utiliza una **arquitectura basada en características (Feature-based)**, alineada con la estructura del frontend Android para facilitar la mantenibilidad y la cohesión.

### Core vs. Features
- **`core/`**: Contiene configuraciones globales, manejo de excepciones transversales (`GlobalExceptionHandler`), y configuraciones de seguridad/red (CORS, WebClient).
- **`features/`**: Cada subpaquete representa un módulo de negocio independiente.

## 📊 Modelo de Datos (7 Tablas Principales)
El sistema se apoya en una arquitectura de **7 tablas principales** para mantener la integridad y escalabilidad:

1. **`users`**: Almacena los datos básicos de autenticación y mantiene el rol (`UserRole.CLIENT` o `UserRole.SPECIALIST`).
2. **`professional_profiles`**: Almacena los datos extendidos del especialista (nombre comercial, especialidad, dirección, etc.). Es obligatorio para ofrecer servicios.
3. **`services`**: Catálogo de ofertas vinculado directamente a un `professionalProfileId`.
4. **`reservations`**: Nexo entre el cliente y el especialista. Mantiene `clientId` y `specialistId` para la gestión de la agenda.
5. **`attentions`**: Registra la ejecución real de la sesión de atención (inicio, fin, observaciones).
6. **`billing_records`**: Registra el cobro externo asociado a una atención.
7. **`subscriptions`**: Controla el estado de acceso y plan activo de un perfil profesional.

### 📈 Reportes y Analytics
Los módulos de **Reports**, **Accounting** y **Analytics** no poseen tablas propias. Todos los valores se calculan dinámicamente mediante el `ReportService` agregando información de reservas, atenciones y facturación en tiempo real.

## 🚀 Instalación y Ejecución

### Requisitos previos
- JDK 17 o superior.
- MySQL 8.0+.

### Ejecutar
Desde la carpeta `backend`:
```bash
./gradlew bootRun
```

## 🔌 Endpoints Principales

| Módulo | Endpoint | Método | Descripción |
| :--- | :--- | :--- | :--- |
| **Auth** | `/api/auth/register` | `POST` | Registro de nuevos usuarios. |
| **Prof. Profile** | `/api/professional-profiles` | `POST` | Crear perfil para especialista. |
| **Service** | `/api/services/professional-profile/{id}` | `GET` | Listar servicios de un perfil. |
| **Reservation** | `/api/reservations` | `POST` | Crear una nueva reserva. |
| **Attention** | `/api/attentions/start` | `POST` | Iniciar una atención. |
| **Billing** | `/api/billing/{id}/paid` | `PUT` | Marcar registro como pagado. |
| **Subscription** | `/api/subscriptions` | `POST` | Contratar plan para el perfil. |

## 🧪 Flujo de Prueba Postman (Happy Path)
Para validar el sistema completo, siga este orden de peticiones:

1.  **Registrar Especialista**: `POST /api/auth/register` (con `role: SPECIALIST`).
2.  **Crear Professional Profile**: `POST /api/professional-profiles` usando el `userId` obtenido.
3.  **Registrar Cliente**: `POST /api/auth/register` (con `role: CLIENT`).
4.  **Crear Servicio**: `POST /api/services` vinculándolo al `professionalProfileId`.
5.  **Crear Reserva**: `POST /api/reservations` asociando cliente, especialista y servicio.
6.  **Iniciar Atención**: `POST /api/attentions/start` enviando el `reservationId`.
7.  **Finalizar Atención**: `POST /api/attentions/{id}/finish`.
8.  **Registrar Cobro**: `POST /api/billing` (se genera automáticamente al finalizar o manualmente).
9.  **Marcar como Pagado**: `PUT /api/billing/{id}/paid`.
10. **Consultar Reporte**: `GET /api/reports/specialist/{specialistId}` para ver métricas actualizadas.

---
**Desarrollado para el ecosistema PointCheck.**
