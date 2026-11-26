# Trycore EVM Tracker API

API REST desarrollada en **Java 21** y **Spring Boot** bajo **Arquitectura Hexagonal (Ports & Adapters)** para la gestión de proyectos, actividades y el cálculo automático en tiempo real de indicadores de **Valor Ganado (Earned Value Management - EVM)** según el estándar del PMI.

---

## Prerrequisitos

Antes de ejecutar el proyecto en tu entorno local, asegúrate de contar con las siguientes herramientas instaladas:

* **Java Development Kit (JDK) 21** o superior (verifica con `java -version`).
* **PostgreSQL 15+** ejecutándose localmente en el puerto `5432`.
* **Git** para clonar el repositorio.
* **Gradle** *(opcional: el repositorio incluye el wrapper `./gradlew` listo para usar)*.
* **Navegador web** para acceder a la documentación interactiva en **Scalar**.

---

## Guía Rápida: Cómo lanzarlo en local

Sigue estos 4 pasos para inicializar la base de datos y levantar la API en tu máquina local:

### Paso 1: Clonar el repositorio
```bash
git clone <https://github.com/RMdavidmatheus/trycore-evm-tracker-api.git>
cd trycore-evm-tracker-api
```

### Paso 2: Crear la base de datos en PostgreSQL
El repositorio ya incluye el script de inicialización de la base de datos (por ejemplo, en `/db_script.sql`). Conéctate a tu instancia local de PostgreSQL como superusuario (`postgres`) usando `psql`, pgAdmin o DBeaver, y ejecuta dicho script para crear la base de datos vacía:

```sql
CREATE DATABASE trycore_db
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;
```

> **Nota:** No necesitas crear las tablas manualmente. Al iniciar la aplicación, **Flyway** detectará la base de datos vacía y ejecutará automáticamente las migraciones ubicadas en `src/main/resources/db/migration`.

### Paso 3: Configurar variables de entorno o credenciales
Verifica los parámetros de conexión en `src/main/resources/application.yml` (o `application.properties`), o configura las siguientes variables de entorno en tu terminal con la contraseña de tu PostgreSQL local:

**En Linux / macOS / Git Bash:**
```bash
export DB_URL=jdbc:postgresql://localhost:5432/trycore_evm_db
export DB_USERNAME=postgres
export DB_PASSWORD=tu_password
```

**En Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/trycore_evm_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="tu_password"
```

Tambien puedes generar un .env en la raiz del proyecto, con las llaves y valores, el servicio detectara este .env y subira las variables de entorno.

```powershell
DB_URL="jdbc:postgresql://localhost:5432/trycore_evm_db"
DB_USERNAME="postgres"
DB_PASSWORD="tu_password"
```

### Paso 4: Levantar el servidor local
Ejecuta el proyecto usando el wrapper de Gradle desde la raíz del repositorio:

**En Linux / macOS / Git Bash:**
```bash
./gradlew bootRun
```

**En Windows (PowerShell / CMD):**
```powershell
.\gradlew.bat bootRun
```

Cuando aparezca en consola el mensaje `Started TrycoreEvmTrackerApiApplication`, la API quedará disponible en **`http://localhost:3000`**.

---

## Documentación Interactiva y Pruebas (OpenAPI / Scalar)

Una vez que la aplicación esté corriendo localmente, ingresa a la ruta `/api-docs` en tu navegador para ser redirigido a **Scalar**, donde podrás consultar los esquemas de entrada/salida, códigos de error y probar todos los endpoints directamente:

* **Scalar API Reference:** [http://localhost:3000/api-docs](http://localhost:8080/api-docs)
* **Especificación OpenAPI (JSON):** [http://localhost:3000/v3/api-docs](http://localhost:8080/v3/api-docs)

### Configuración en `application.yml`
Para asegurar que `/api-docs` redirija correctamente a la interfaz interactiva de Scalar sin colisionar con el contrato JSON de OpenAPI:

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
scalar:
  path: /api-docs
```

---

## Arquitectura del Proyecto

El backend implementa **Arquitectura Hexagonal (Ports & Adapters)**, manteniendo el dominio de negocio libre de dependencias de frameworks:

```text
src/main/java/com/business/trycoreevmtrackerapi
├── domain
│   ├── exception           # Excepciones de negocio (ResourceNotFoundException)
│   ├── model               # Modelos puros, Value Objects y Enums (Project, Activity, EvmMetrics, CostStatus, ScheduleStatus)
│   └── port
│       ├── in              # Casos de uso (ProjectUseCase, ActivityUseCase)
│       └── out             # Contratos de persistencia (ProjectRepositoryPort, ActivityRepositoryPort)
├── application
│   └── service             # Motor de cálculo EVM y servicios (EvmCalculatorService, ProjectService, ActivityService)
└── infrastructure
    ├── adapter
    │   ├── in/rest         # Controladores REST, DTOs, Mappers MapStruct y GlobalExceptionHandler
    │   └── out/persistence # Entidades JPA, Repositorios Spring Data y Adaptadores de persistencia
    └── config              # Configuración de OpenAPI/Scalar y CORS
```

---

## Endpoints Principales

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `POST` | `/api/v1/projects` | Crea un nuevo proyecto |
| `GET` | `/api/v1/projects` | Lista todos los proyectos activos con sus métricas EVM consolidadas |
| `GET` | `/api/v1/projects/{id}` | Obtiene el detalle de un proyecto, sus actividades e indicadores EVM |
| `PUT` | `/api/v1/projects/{id}` | Actualiza el nombre y descripción de un proyecto |
| `DELETE` | `/api/v1/projects/{id}` | Realiza borrado lógico (`is_active = false`) del proyecto y sus actividades |
| `POST` | `/api/v1/projects/{projectId}/activities` | Registra una actividad en el proyecto y calcula sus métricas EVM |
| `GET` | `/api/v1/projects/{projectId}/activities` | Lista las actividades activas de un proyecto |
| `PUT` | `/api/v1/projects/{projectId}/activities/{activityId}` | Actualiza avance/costos de una actividad y recalcula sus indicadores |
| `DELETE` | `/api/v1/projects/{projectId}/activities/{activityId}` | Realiza borrado lógico (`is_active = false`) de una actividad |

---

## Ejecución de Pruebas Unitarias y de Integración

Para ejecutar toda la suite de pruebas automatizadas (cálculo EVM, casos borde y contratos de endpoints):

```bash
./gradlew test
```

Si cuentas con **JaCoCo** configurado en `build.gradle`, puedes generar el reporte de cobertura con:

```bash
./gradlew test jacocoTestReport
```

---

## Fórmulas de Valor Ganado (EVM) Implementadas

Todas las operaciones financieras utilizan `BigDecimal` con redondeo `HALF_UP` a 4 decimales para garantizar precisión y evitar errores de punto flotante:

| Indicador | Fórmula | Regla / Caso Borde |
| :--- | :--- | :--- |
| **PV (Planned Value)** | $\$PV = \% \text{Planificado} \times BAC\$ | Calculado por actividad y sumado en el consolidado |
| **EV (Earned Value)** | $\$EV = \% \text{Completado} \times BAC\$ | Calculado por actividad y sumado en el consolidado |
| **CV (Cost Variance)** | $\$CV = EV - AC\$ | Valor negativo indica sobrecosto |
| **SV (Schedule Variance)** | $\$SV = EV - PV\$ | Valor negativo indica atraso en cronograma |
| **CPI (Cost Performance Index)** | $\$CPI = \frac{EV}{AC}\$ | Retorna `null` y estado `NO_COST_DATA` si $AC = 0$ |
| **SPI (Schedule Performance Index)** | $\$SPI = \frac{EV}{PV}\$ | Retorna `null` y estado `NO_SCHEDULE_DATA` si $PV = 0$ |
| **EAC (Estimate at Completion)** | $\$EAC = \frac{BAC}{CPI}\$ | Retorna `null` si $CPI$ es `null` o $0$ |
| **VAC (Variance at Completion)** | $\$VAC = BAC - EAC\$ | Retorna `null` si $EAC$ es `null` |

### Interpretación de Estados
* **Costos (`costStatus`):** $CPI > 1 \rightarrow$ `UNDER_BUDGET` | $CPI = 1 \rightarrow$ `ON_BUDGET` | $CPI < 1 \rightarrow$ `OVER_BUDGET` | $AC = 0 \rightarrow$ `NO_COST_DATA`
* **Cronograma (`scheduleStatus`):** $SPI > 1 \rightarrow$ `AHEAD_OF_SCHEDULE` | $SPI = 1 \rightarrow$ `ON_SCHEDULE` | $SPI < 1 \rightarrow$ `BEHIND_SCHEDULE` | $PV = 0 \rightarrow$ `NO_SCHEDULE_DATA`