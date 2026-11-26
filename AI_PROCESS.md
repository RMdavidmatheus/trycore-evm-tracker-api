# AI_PROCESS.md — Proceso de Desarrollo con IA

Este documento detalla de forma transparente cómo utilicé herramientas de Inteligencia Artificial durante el desarrollo de **Trycore EVM Tracker API**, qué decisiones tomé de manera independiente, dónde me aparté de las sugerencias de la IA y cómo validé matemáticamente el dominio de **Valor Ganado (EVM)**.

---

## 1. Herramientas de IA Utilizadas y Por Qué las Elegí

1. **Gemini (Web):**
   * **Uso:** Diseño inicial de la Arquitectura Hexagonal, modelado de dominio, entidades JPA, migración inicial de Flyway, DTOs, mappers con MapStruct, servicios de cálculo EVM, controladores REST, resolución de *code smells* detectados por SonarQube y generación de casos de prueba en JSON para Postman.
   * **Por qué:** Permite iterar rápidamente sobre decisiones de arquitectura, contrastar enfoques (por ejemplo, manejo de precisión financiera con `BigDecimal` y casos borde de EVM) y mantener contexto continuo sobre todo el backend.

2. **IntelliJ IDEA + Claude Agent:**
   * **Uso:** Generación y refinamiento de **pruebas unitarias** (cobertura de la capa de negocio y casos borde de EVM), configuración e integración de la **documentación interactiva con Scalar** en `/api-docs` y apoyo en la consolidación del **`README.md`**.
   * **Por qué:** Al estar integrado directamente en el IDE (IntelliJ IDEA), Claude Agent tiene visibilidad completa del árbol de archivos, dependencias de Gradle (`build.gradle`) y resultados de compilación/ejecución de JUnit en tiempo real, lo que reduce errores de contexto y acelera el ciclo de escritura y verificación de tests.

---

## 2. Cómo Aprendí EVM y Validé las Fórmulas

Antes de escribir la lógica en `EvmCalculatorService`, analicé el estándar del PMI para **Earned Value Management (EVM)** centrándome en la relación entre tres ejes: alcance planificado, alcance realmente ejecutado y costo real incurrido.

### Conceptos clave asimilados y validados:
* **$PV$ (Planned Value):** Cuánto trabajo *debería* haberse completado a la fecha de corte en términos de presupuesto ($PV = \% \text{Planificado} \times BAC$).
* **$EV$ (Earned Value):** Cuánto vale el trabajo *realmente* completado según el presupuesto original ($EV = \% \text{Completado} \times BAC$).
* **$CV$ y $SV$:** Variaciones absolutas en dinero. Un valor negativo indica sobrecosto ($CV = EV - AC < 0$) o retraso ($SV = EV - PV < 0$).
* **$CPI$ y $SPI$:** Índices de eficiencia relativa. Valores mayores a $1$ indican eficiencia favorable (bajo presupuesto o adelantado); menores a $1$ indican alerta (sobre presupuesto o atrasado).
* **Consolidación a nivel proyecto (regla crítica):** Validé que **nunca se deben promediar los índices $CPI$ o $SPI$ de las actividades**. El consolidado del proyecto siempre suma primero los montos base ($BAC_{\text{total}} = \sum BAC_i$, $PV_{\text{total}} = \sum PV_i$, $EV_{\text{total}} = \sum EV_i$, $AC_{\text{total}} = \sum AC_i$) y sobre esos totales recalcula $CPI = \frac{EV_{\text{total}}}{AC_{\text{total}}}$ y $SPI = \frac{EV_{\text{total}}}{PV_{\text{total}}}$.
* **Casos borde:** Validé qué ocurre cuando $AC = 0$ (división por cero en $CPI$), cuando $PV = 0$ (división por cero en $SPI$) y cuando $EV = 0$ con $AC > 0$ ($CPI = 0$, lo que provocaría división por cero en $EAC = \frac{BAC}{CPI}$). En todos esos escenarios el sistema retorna `null` en el índice afectado y un estado semántico explícito (`NO_COST_DATA` / `NO_SCHEDULE_DATA`).

---

## 3. Verificación Manual de los Cálculos

Para asegurar que los números tienen sentido financiero y no solo que "el código compila", realicé una verificación manual con un proyecto de 3 actividades activas y 1 actividad en caso borde:

| Actividad | $BAC$ | $\%$ Plan. | $\%$ Comp. | $AC$ | $PV$ | $EV$ | $CV$ | $SV$ | $CPI$ | $SPI$ |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| 1. Diseño Arquitectura | $10{,}000$ | $50\%$ | $40\%$ | $6{,}000$ | $5{,}000$ | $4{,}000$ | $-2{,}000$ | $-1{,}000$ | $0.6667$ | $0.8000$ |
| 2. Microservicios Core | $20{,}000$ | $60\%$ | $80\%$ | $12{,}000$ | $12{,}000$ | $16{,}000$ | $+4{,}000$ | $+4{,}000$ | $1.3333$ | $1.3333$ |
| 3. Pasarela de Pagos | $8{,}000$ | $50\%$ | $50\%$ | $4{,}000$ | $4{,}000$ | $4{,}000$ | $0$ | $0$ | $1.0000$ | $1.0000$ |
| 4. Pruebas QA (Borde) | $5{,}000$ | $0\%$ | $0\%$ | $0$ | $0$ | $0$ | $0$ | $0$ | `null` | `null` |
| **CONSOLIDADO** | **$43{,}000$** | — | — | **$22{,}000$** | **$21{,}000$** | **$24{,}000$** | **$+2{,}000$** | **$+3{,}000$** | **$1.0909$** | **$1.1429$** |

**Validación de proyección consolidada:**
* $CPI_{\text{proyecto}} = \frac{24{,}000}{22{,}000} \approx 1.0909 \rightarrow$ `UNDER_BUDGET` (por cada dólar gastado se generan $\$1.09$ de valor).
* $SPI_{\text{proyecto}} = \frac{24{,}000}{21{,}000} \approx 1.1429 \rightarrow$ `AHEAD_OF_SCHEDULE`.
* $EAC_{\text{proyecto}} = \frac{43{,}000}{1.0909} \approx 39{,}416.9951$ y $VAC_{\text{proyecto}} = 43{,}000 - 39{,}416.9951 \approx +3{,}583.0049$ (ahorro proyectado al finalizar el proyecto).

Estos valores fueron contrastados contra las respuestas reales de la API en Postman/Scalar y las aserciones de JUnit.

---

## 4. Dos Decisiones Donde NO Seguí la Sugerencia de la IA

### Decisión 1: Estrategia de borrado lógico (`isActive`) y manejo de fechas (`LocalDateTime`)
* **Qué propuso la IA:** Inicialmente sugirió implementar borrado lógico implícito mediante `@SQLRestriction("deleted_at IS NULL")` de Hibernate y usar `OffsetDateTime` con `@UpdateTimestamp` en las entidades JPA.
* **Qué decidí hacer:** Opté por agregar una columna explícita `is_active` (`boolean isActive = true`) en las entidades, modelos de dominio y migración de Flyway, junto con `LocalDateTime` para los campos de auditoría, filtrando explícitamente en los repositorios (`findByIdAndIsActiveTrue`, `findAllByIsActiveTrue...`).
* **Por qué:** Tener un indicador booleano `is_active` hace que el estado del registro sea más claro, auditable y fácil de indexar en PostgreSQL (`WHERE is_active = TRUE`), evitando la "magia oculta" de filtros globales de Hibernate que a veces complican consultas administrativas o pruebas de persistencia.

### Decisión 2: Documentación con Scalar en `/api-docs` y uso de Gradle + MapStruct
* **Qué propuso la IA:** Inicialmente propuso exponer la documentación con **Swagger UI** clásico en `/swagger-ui.html` y, ante el error inicial de importación de MapStruct, sugirió configuración de Maven (`pom.xml`) o reemplazar MapStruct por mappers manuales en Java puro.
* **Qué decidí hacer:** Mantuve **Gradle** con **MapStruct** (`lombok-mapstruct-binding`) y elegí integrar **Scalar** en la ruta `/api-docs` (separando el JSON crudo en `/v3/api-docs`), apoyándome en Claude Agent dentro de IntelliJ IDEA para su configuración.
* **Por qué:** MapStruct genera código en tiempo de compilación sin *boilerplate* ni reflexión en tiempo de ejecución, y Scalar ofrece una experiencia de prueba de endpoints mucho más limpia y moderna cumpliendo con el requisito de la prueba de exponer la documentación en `/api-docs`.

---

## 5. Decisión de Arquitectura Tomada de Forma Independiente

Decidí estructurar el backend bajo **Arquitectura Hexagonal (Ports & Adapters)** combinada con **Java `record`s** inmutables en la capa de dominio y **Flyway** para el versionamiento del esquema de base de datos:
* El paquete `domain` (`model`, `port.in`, `port.out`, `exception`) no tiene ninguna dependencia de Spring ni de JPA.
* El cálculo EVM vive aislado en `EvmCalculatorService` operando sobre `BigDecimal` con escala controlada (`RoundingMode.HALF_UP`), lo que permite probar toda la matemática del negocio con pruebas unitarias rápidas sin levantar contexto de Spring ni base de datos.
* Los adaptadores de entrada (`infrastructure.adapter.in.rest`) y salida (`infrastructure.adapter.out.persistence`) están completamente desacoplados mediante puertos e interfaces, facilitando el mantenimiento y cumplimiento de reglas estrictas de calidad en SonarQube (como la corrección de `java:S8688` especificando `ZoneId` en `.now()` y `java:S1710` eliminando wrappers redundantes `@ApiResponses`).

---

## 6. Historial Cronológico de Prompts (Textuales)

### A. Prompts enviados a Gemini (Diseño e implementación del backend)

1. > `Listame 4 jsons de prueba para postman tanto de actividades como de proyectos`
2. > `Perfecto, ahora generame un README.MD que indique el proceso de inicializacion desde local y ya añadi el script de inicializacion de la db`
3. > `Los pre requisitos metelos tambien en el readme.md y da una mini guia de como lanzarlo en local`
4. > `espera en el readme.md añade la ruta /api-docs para que sea redirigido a Scalar y alli probar los endpoints`
5. > `Ahora genera otro MD llamado AI_PROCESS.md donde diremos que usamos Intellij Idea y claude agent para la generación de los test unitarios y la documentacion de scalar y el readme.MD`

### B. Prompts enviados a Claude Agent en IntelliJ IDEA (Tests unitarios, Scalar y README)

> **[IMPORTANTE — PEGAR AQUÍ TUS PROMPTS TEXTUALES]:** Copia y pega aquí exactamente, en orden cronológico y sin resumir, los prompts que le enviaste a Claude Agent dentro de IntelliJ IDEA para generar los tests unitarios, configurar Scalar y ajustar el `README.md`.

1. > `Puedes ayudarme a generar los test unitarios cubriendo minimamente el 85% de cobertura del código?`
2. > `Optimiza el consumo de memoria`
3. > `Genera una suite de pruebas`

---

## 7. Reflexión Honesta: ¿Qué Haría Diferente si Repitiera el Ejercicio?

1. **Definir desde el primer prompt el stack exacto y convenciones del proyecto:** Al iniciar sin especificar que el gestor de dependencias era Gradle, el paquete base era `com.business.trycoreevmtrackerapi` y que quería borrado lógico con `isActive`, perdí tiempo ajustando imports, constructores de `record`s y configuración de MapStruct. Dar un contexto técnico completo al inicio evita retrabajo.
2. **Inyectar `Clock` en lugar de usar `LocalDateTime.now(ZoneId)`:** Aunque solucioné el *code smell* `java:S8688` de SonarQube especificando `ZoneId.of("America/Bogota")`, inyectar un bean de `java.time.Clock` en los adaptadores y en el `GlobalExceptionHandler` haría que las pruebas unitarias basadas en fechas fueran 100% deterministas.
3. **Configurar el linter (Checkstyle / Spotless) y JaCoCo desde el commit inicial:** Integrar el análisis estático y el umbral de cobertura desde la primera rama `feature/*` garantiza que ningún código llegue a `develop` con advertencias de calidad.