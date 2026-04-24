# PointCheck Backend - Feature-Based Architecture

Este es el backend del ecosistema **PointCheck**, una solución integral para la gestión de citas y servicios entre clientes y especialistas. Está desarrollado con **Kotlin**, **Spring Boot 3.3.1**, y **MySQL**, siguiendo principios de diseño moderno y escalable.

## 🏗️ Arquitectura y Estructura

El proyecto utiliza una **arquitectura basada en características (Feature-based)**, alineada con la estructura del frontend Android para facilitar la mantenibilidad y la cohesión.

### Core vs. Features
- **`core/`**: Contiene configuraciones globales, manejo de excepciones transversales (`GlobalExceptionHandler`), y configuraciones de seguridad/red (CORS, WebClient).
- **`features/`**: Cada subpaquete representa un módulo de negocio independiente que contiene sus propios modelos, repositorios, servicios, DTOs y controladores.
  - `user`: Gestión de perfiles y roles.
  - `auth`: Registro y autenticación.
  - `service`: Catálogo de servicios ofrecidos.
  - `reservation`: Gestión de citas y estados.
  - `attention`: Control de sesiones de atención en tiempo real.
  - `billing`: Registro financiero y estados de pago.
  - `subscription`: Planes para especialistas.
  - `report`: Analytics y dashboards dinámicos.

## 📊 Modelo de Datos
El sistema se apoya en **6 tablas principales** administradas por JPA/Hibernate:
1. `users`: Almacena clientes y especialistas con roles diferenciados.
2. `services`: Catálogo de ofertas con precios y duraciones.
3. `reservations`: Nexo entre cliente, especialista y servicio.
4. `attentions`: Registra el inicio, fin y observaciones de cada sesión.
5. `billing_records`: Trazabilidad financiera de cada atención.
6. `subscriptions`: Estado de acceso para especialistas.

### Reportes y Analytics
A diferencia de arquitecturas tradicionales, los módulos de **Reports**, **Accounting** y **Analytics** no poseen tablas propias. Los datos se calculan dinámicamente mediante el `ReportService` agregando información de reservas, atenciones y facturación. Esto garantiza "una única fuente de verdad" y evita redundancia de datos.

## 🚀 Instalación y Ejecución

### Requisitos previos
- JDK 17 o superior.
- Instancia de MySQL activa.
- Gradle (incluido vía `gradlew`).

### Variables de Entorno
Para ejecutar el proyecto, se deben configurar las siguientes variables de entorno (o definirlas en un archivo `.env` / `application.properties` local):

```properties
DB_USERNAME=tu_usuario
DB_PASSWORD=tu_contraseña
OPENWEATHER_API_KEY=tu_api_key_de_clima
GOOGLE_MAPS_API_KEY=tu_api_key_de_maps
```

### Ejecutar
Desde la carpeta `backend`:
```bash
./gradlew bootRun
```

## 🔌 Endpoints Principales

| Módulo | Endpoint | Método | Descripción |
| :--- | :--- | :--- | :--- |
| **Auth** | `/api/auth/register` | `POST` | Registro de nuevos usuarios. |
| **User** | `/api/users/{id}` | `GET` | Obtener perfil de usuario. |
| **Service** | `/api/services` | `GET` | Listar catálogo de servicios. |
| **Reservation** | `/api/reservations` | `POST` | Crear una nueva reserva. |
| **Attention** | `/api/attentions/start` | `POST` | Iniciar una atención presencial. |
| **Billing** | `/api/billing/{id}/paid` | `PUT` | Marcar registro como pagado. |
| **Report** | `/api/reports/summary/specialist/{id}` | `GET` | Resumen ejecutivo para dashboard. |

## 🧪 Pruebas con Postman
1. Importa los JSON de ejemplo proporcionados en la documentación técnica.
2. Asegúrate de obtener el `userId` tras el registro/login para usarlo en los endpoints de reservas y reportes.
3. El sistema incluye un **Manejador Global de Excepciones** que devuelve errores estandarizados (`ApiErrorResponse`) en caso de argumentos inválidos o recursos no encontrados.

## 📱 Guía de Migración para Android (Próximos Pasos)
Para alinearse con la nueva arquitectura del backend, la aplicación Android deberá actualizar su capa de datos (`ApiService`) siguiendo estos lineamientos:
- **Endpoints de Autenticación**: Migrar de `/api/users/login|register` a `/api/auth/login|register`.
- **Identificadores**: Dejar de usar el `email` como clave primaria en las solicitudes. El `UserResponse` ahora incluye un `id` numérico que debe persistirse localmente (Room) y usarse en `ReservationRequest` (`clientId`, `specialistId`).
- **Fechas**: Las reservas ahora usan `LocalDateTime` en formato ISO-8601. Asegurar el mapeo correcto desde `epochMillis` si es necesario.
- **Nuevas Funcionalidades**: Implementar clientes de API para los módulos de `Attention` (Atención en tiempo real), `Billing` (Pagos) y `Reports` (Dashboard dinámico).

---
**Desarrollado para el portafolio de PointCheck.**
