# Backend Analítico Agrotecno

## Configuración

Crear o completar `.env` sin subirlo al repositorio:

```env
MONGODB_URI=mongodb+srv://<usuario>:<password>@<cluster>/?retryWrites=true&w=majority
MONGODB_DATABASE=agrotecno_db
```

El backend utiliza únicamente las colecciones existentes `customer`, `formula` y `supply`.
Al iniciar valida el acceso a la base de datos y no crea colecciones.

La colección `supply` representa los insumos y contiene los campos anidados
`inventory.current_stock` e `inventory.minimum_stock`.

## Ejecución

```powershell
py -3 -m venv .venv
.\.venv\Scripts\Activate.ps1
py -3 -m pip install -r requirements.txt
py -3 -m app.main
```

Swagger UI: `http://localhost:5000/swagger/`

Especificación OpenAPI: `http://localhost:5000/swagger.json`

## Endpoints analíticos

### Maestro Fórmulas (Dashboard Figma - 6 Indicadores)
- `GET /api/analytics/formulas/kpi-total`: Indicador 1 - KPI total de fórmulas registradas y nuevas en el periodo (`?dias=30`).
- `GET /api/analytics/formulas/kpi-activas`: Indicador 2 - KPI cantidad y porcentaje de fórmulas activas (`status: true`).
- `GET /api/analytics/formulas/kpi-en-revision`: Indicador 3 - KPI fórmulas en revisión o inactivas que requieren evaluación.
- `GET /api/analytics/formulas/distribucion-estado`: Indicador 4 - Gráfico de dona con distribución de estado (activas vs en revisión).
- `GET /api/analytics/formulas/evolucion-mensual`: Indicador 5 - Gráfico de barras con evolución histórica mensual de registros (`?meses=6`).
- `GET /api/analytics/formulas/detalle`: Indicador 6 - Tabla paginada de fórmulas con código, tipo, última actualización y estado (`?page=1&limit=10&estado=todas`).
- `GET /api/analytics/formulas/dashboard`: Consolidado de los 6 indicadores para carga reactiva completa.
- `GET /api/analytics/formulas/mas-vendidas`: Indicador Transaccional Híbrido - Ranking de fórmulas más vendidas en un periodo del año (SQL Server `ventas.order_detail` + MongoDB `formula`, filtros: `?anio=2026&limit=5` o `?startDate=&endDate=`).

### Otros endpoints analíticos
- `GET /api/analytics/formulas`: resumen general e indicadores de costo y margen de `formula`.
- `GET /api/analytics/clientes`: total, campos detectados y resumen estadístico de `customer`.
- `GET /api/analytics/insumos-stock-bajo`: compara stock actual y mínimo de `supply`.
- `GET /api/analytics/ventas-formulas`: análisis de órdenes y ventas por fórmula en SQL Server.
- `GET /api/analytics/cobranzas-mora`: cuotas vencidas y cartera en SQL Server.

