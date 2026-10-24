from flask import Blueprint, jsonify
from flask_swagger_ui import get_swaggerui_blueprint

SWAGGER_SPEC = {
    "openapi": "3.0.3",
    "info": {
        "title": "Backend Analítico Agrotecno",
        "version": "1.0.0",
        "description": "API analítica conectada a MongoDB (agrotecno_db) y SQL Server (agroTecno_db).",
    },
    "paths": {
        "/health": {
            "get": {
                "summary": "Verifica estado de salud y conectividad a MongoDB y SQL Server",
                "responses": {
                    "200": {"description": "Ambas bases de datos operativas"},
                    "207": {"description": "Una o más bases de datos desconectadas o en alerta"},
                },
            }
        },
        "/api/analytics/clientes": {
            "get": {
                "summary": "Analiza clientes registrados en MongoDB (colección 'customer')",
                "responses": {
                    "200": {"description": "Resumen estadístico de los clientes"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Analiza fórmulas registradas en MongoDB (colección 'formula')",
                "responses": {
                    "200": {"description": "Resumen e indicadores de costo y margen de fórmulas"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/kpi-total": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Indicador 1: KPI de Fórmulas Registradas",
                "description": "Calcula el total de fórmulas registradas y el conteo de nuevas fórmulas registradas en el periodo seleccionado (ej. últimos 30 días).",
                "parameters": [
                    {
                        "name": "dias",
                        "in": "query",
                        "description": "Ventana de días para evaluar nuevas incorporaciones (por defecto 30)",
                        "required": False,
                        "schema": {"type": "integer", "default": 30},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Métricas del KPI de fórmulas registradas",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "Fórmulas registradas",
                                    "total_registradas": 32,
                                    "nuevas_en_periodo": 4,
                                    "periodo_dias": 30,
                                    "comparativa_texto": "+ 4 nuevas en el periodo",
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/kpi-activas": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Indicador 2: KPI de Fórmulas Activas",
                "description": "Calcula la cantidad total y el porcentaje de fórmulas que se encuentran activas (status: true).",
                "responses": {
                    "200": {
                        "description": "Métricas del KPI de fórmulas activas",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "Fórmulas activas",
                                    "total_activas": 28,
                                    "total_registradas": 32,
                                    "porcentaje_activas": 87.5,
                                    "porcentaje_texto": "87.5% del total",
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/kpi-en-revision": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Indicador 3: KPI de Fórmulas en Revisión",
                "description": "Calcula la cantidad y proporción de fórmulas inactivas o pendientes de evaluación técnica (status: false).",
                "responses": {
                    "200": {
                        "description": "Métricas del KPI de fórmulas en revisión",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "En revisión",
                                    "total_en_revision": 4,
                                    "total_registradas": 32,
                                    "porcentaje_en_revision": 12.5,
                                    "mensaje": "Requieren evaluación",
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/distribucion-estado": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Indicador 4: Gráfico de Estado de Fórmulas (Donut Chart)",
                "description": "Proporciona la distribución proporcional entre fórmulas activas y en revisión para renderizar en gráficos de dona.",
                "responses": {
                    "200": {
                        "description": "Distribución porcentual y conteos por estado",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "Estado de fórmulas",
                                    "descripcion": "Distribución según el estado del registro",
                                    "total_formulas": 32,
                                    "activas": {"cantidad": 28, "porcentaje": 87.5, "etiqueta": "28 activas", "porcentaje_texto": "87.5% del total"},
                                    "en_revision": {"cantidad": 4, "porcentaje": 12.5, "etiqueta": "4 en revisión", "porcentaje_texto": "12.5% del total"},
                                    "serie_grafico": [
                                        {"estado": "Activas", "cantidad": 28, "porcentaje": 87.5, "color": "#1b4d3e"},
                                        {"estado": "En revisión", "cantidad": 4, "porcentaje": 12.5, "color": "#d9822b"},
                                    ],
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/evolucion-mensual": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Indicador 5: Gráfico de Fórmulas Registradas por Mes (Bar Chart)",
                "description": "Agrupa cronológicamente los nuevos registros de fórmulas en los últimos N meses (por defecto 6 meses).",
                "parameters": [
                    {
                        "name": "meses",
                        "in": "query",
                        "description": "Número de meses a evaluar hacia atrás",
                        "required": False,
                        "schema": {"type": "integer", "default": 6},
                    }
                ],
                "responses": {
                    "200": {
                        "description": "Serie cronológica mensual de fórmulas registradas",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "Fórmulas registradas por mes",
                                    "descripcion": "Cantidad de nuevos registros por mes en orden cronológico",
                                    "total_meses_evaluados": 6,
                                    "total_registros_periodo": 21,
                                    "items": [
                                        {"anio": 2026, "mes": 5, "mes_nombre": "May", "mes_completo": "Mayo", "cantidad_registros": 2},
                                        {"anio": 2026, "mes": 6, "mes_nombre": "Jun", "mes_completo": "Junio", "cantidad_registros": 3},
                                        {"anio": 2026, "mes": 7, "mes_nombre": "Jul", "mes_completo": "Julio", "cantidad_registros": 3},
                                        {"anio": 2026, "mes": 8, "mes_nombre": "Ago", "mes_completo": "Agosto", "cantidad_registros": 4},
                                        {"anio": 2026, "mes": 9, "mes_nombre": "Sep", "mes_completo": "Septiembre", "cantidad_registros": 5},
                                        {"anio": 2026, "mes": 10, "mes_nombre": "Oct", "mes_completo": "Octubre", "cantidad_registros": 4},
                                    ],
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/detalle": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Indicador 6: Tabla de Detalle y Auditoría de Fórmulas",
                "description": "Retorna el listado paginado y filtrable de fórmulas con código, nombre, tipo, última actualización y estado.",
                "parameters": [
                    {
                        "name": "page",
                        "in": "query",
                        "description": "Número de página (1-based)",
                        "required": False,
                        "schema": {"type": "integer", "default": 1},
                    },
                    {
                        "name": "limit",
                        "in": "query",
                        "description": "Cantidad de registros por página",
                        "required": False,
                        "schema": {"type": "integer", "default": 10},
                    },
                    {
                        "name": "estado",
                        "in": "query",
                        "description": "Filtro por estado ('todas', 'activas', 'en_revision')",
                        "required": False,
                        "schema": {"type": "string", "enum": ["todas", "activas", "en_revision"], "default": "todas"},
                    },
                    {
                        "name": "search",
                        "in": "query",
                        "description": "Búsqueda por texto en código, nombre o descripción",
                        "required": False,
                        "schema": {"type": "string"},
                    },
                ],
                "responses": {
                    "200": {
                        "description": "Listado de detalle de fórmulas con metadatos de paginación",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "Detalle de fórmulas",
                                    "descripcion": "Listado de fórmulas para revisar los registros y su estado",
                                    "items": [
                                        {
                                            "codigo": "FOR001",
                                            "nombre": "Bioestimulante foliar",
                                            "descripcion": "Aplicación foliar",
                                            "iniciales": "BF",
                                            "tipo": "Foliar",
                                            "ultima_actualizacion": "15/10/2026",
                                            "estado": True,
                                            "estado_texto": "Activa",
                                        }
                                    ],
                                    "paginacion": {
                                        "total_registros": 32,
                                        "pagina_actual": 1,
                                        "limite_por_pagina": 10,
                                        "total_paginas": 4,
                                        "mostrando_mensaje": "Mostrando 10 de 32 fórmulas",
                                    },
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/dashboard": {
            "get": {
                "tags": ["Fórmulas (Maestro)"],
                "summary": "Dashboard Completo: Consolidado de los 6 Indicadores de Fórmulas",
                "description": "Retorna en una sola respuesta reactiva todos los KPIs, gráficos y tabla requeridos por el diseño Figma.",
                "parameters": [
                    {
                        "name": "dias",
                        "in": "query",
                        "description": "Ventana de días para el KPI de nuevos registros (default 30)",
                        "required": False,
                        "schema": {"type": "integer", "default": 30},
                    },
                    {
                        "name": "meses",
                        "in": "query",
                        "description": "Meses para la evolución mensual (default 6)",
                        "required": False,
                        "schema": {"type": "integer", "default": 6},
                    },
                    {
                        "name": "limit_detalle",
                        "in": "query",
                        "description": "Límite de registros para la tabla previa en el dashboard (default 4)",
                        "required": False,
                        "schema": {"type": "integer", "default": 4},
                    },
                ],
                "responses": {
                    "200": {"description": "Objeto consolidado con los 6 indicadores"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/formulas/mas-vendidas": {
            "get": {
                "tags": ["Fórmulas (Maestro)", "Transaccional Híbrido"],
                "summary": "Fórmulas Más Vendidas en un Periodo (SQL Server + MongoDB)",
                "description": "Ranking transaccional que agrupa las órdenes en SQL Server (ventas.order_detail y ventas.sales_order) por unidades vendidas, excluyendo anuladas, y enriquece los resultados con el nombre, tipo y catálogo de MongoDB (colección 'formula').",
                "parameters": [
                    {
                        "name": "anio",
                        "in": "query",
                        "description": "Año a consultar (ej. 2026). Si no se envía, evalúa todo el histórico o el rango de fechas.",
                        "required": False,
                        "schema": {"type": "integer", "example": 2026},
                    },
                    {
                        "name": "startDate",
                        "in": "query",
                        "description": "Fecha inicial en formato YYYY-MM-DD",
                        "required": False,
                        "schema": {"type": "string", "format": "date", "example": "2026-01-01"},
                    },
                    {
                        "name": "endDate",
                        "in": "query",
                        "description": "Fecha final en formato YYYY-MM-DD",
                        "required": False,
                        "schema": {"type": "string", "format": "date", "example": "2026-12-31"},
                    },
                    {
                        "name": "limit",
                        "in": "query",
                        "description": "Cantidad máxima de fórmulas en el ranking (default 5)",
                        "required": False,
                        "schema": {"type": "integer", "default": 5},
                    },
                ],
                "responses": {
                    "200": {
                        "description": "Ranking de fórmulas más vendidas con datos transaccionales y de catálogo",
                        "content": {
                            "application/json": {
                                "example": {
                                    "indicador": "Fórmulas más vendidas",
                                    "descripcion": "Ranking de fórmulas por unidades vendidas en el periodo seleccionado (SQL Server + MongoDB)",
                                    "periodo": {
                                        "anio": 2026,
                                        "fecha_inicio": None,
                                        "fecha_fin": None,
                                        "filtro_aplicado": "Año 2026",
                                    },
                                    "fuente_datos": {
                                        "transaccional": "SQL Server (ventas.order_detail, ventas.sales_order)",
                                        "catalogo": "MongoDB (colección 'formula')",
                                    },
                                    "total_formulas_en_ranking": 5,
                                    "total_unidades_vendidas": 117.0,
                                    "monto_total_recaudado": 57860.0,
                                    "items": [
                                        {
                                            "posicion": 1,
                                            "formula_id": "FOR001",
                                            "formula_name": "NutriBio Crecimiento Foliar",
                                            "descripcion": "Formula enriquecida en Nitrogeno y Magnesio para fase vegetativa",
                                            "tipo": "Foliar",
                                            "iniciales": "NC",
                                            "unidades_vendidas": 40.0,
                                            "etiqueta_unidades": "40 unidades",
                                            "total_ordenes": 2,
                                            "monto_total_recaudado": 16800.0,
                                            "precio_promedio_venta": 420.0,
                                            "precio_sugerido_catalogo": 420.0,
                                            "costo_preparacion_catalogo": 250.0,
                                            "unidad": "litros",
                                            "estado_formula": True,
                                            "primera_venta": "08/10/2026",
                                            "ultima_venta": "15/10/2026",
                                        }
                                    ],
                                }
                            }
                        },
                    },
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos-stock-bajo": {
            "get": {
                "summary": "Identifica insumos con stock por debajo del mínimo en MongoDB (colección 'supply')",
                "responses": {
                    "200": {"description": "Insumos en situación de reposición urgente"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/ventas-formulas": {
            "get": {
                "summary": "Analiza ventas de fórmulas registradas en SQL Server (esquema 'ventas')",
                "responses": {
                    "200": {"description": "Resumen de órdenes y recaudación por fórmula"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/cobranzas-mora": {
            "get": {
                "summary": "Analiza cartera vencida y estados de cuenta en SQL Server (esquema 'cobranzas')",
                "responses": {
                    "200": {"description": "Cuotas vencidas y deudas por cliente"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/kpi-total": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Indicador 1: KPI - Insumos registrados",
                "description": "Total de insumos en la coleccion 'supply' y cuantos son nuevos en el periodo seleccionado.",
                "parameters": [
                    {
                        "name": "dias",
                        "in": "query",
                        "schema": {"type": "integer", "default": 30},
                        "description": "Ventana de tiempo en dias para contar nuevos insumos",
                    }
                ],
                "responses": {
                    "200": {"description": "Total de insumos y nuevos en el periodo"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/kpi-stock-bajo": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Indicador 2: KPI - Insumos con stock bajo",
                "description": "Cantidad de insumos cuyo stock actual es menor al stock minimo configurado.",
                "responses": {
                    "200": {"description": "Conteo y porcentaje de insumos con stock bajo"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/kpi-disponibles": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Indicador 3: KPI - Insumos disponibles",
                "description": "Cantidad de insumos con stock igual o superior al minimo requerido.",
                "responses": {
                    "200": {"description": "Conteo y porcentaje de insumos disponibles"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/estado-inventario": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Indicador 4: Grafico circular - Estado del inventario",
                "description": "Distribucion de disponibles vs stock bajo para grafico de dona.",
                "responses": {
                    "200": {"description": "Serie para grafico circular con disponibles y stock bajo"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/por-categoria": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Indicador 5: Grafico de barras - Insumos por categoria",
                "description": "Conteo de insumos agrupados por categoria para grafico de barras.",
                "responses": {
                    "200": {"description": "Serie para grafico de barras por categoria"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/detalle-inventario": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Indicador 6: Tabla - Detalle del inventario",
                "description": "Tabla paginada con stock actual vs minimo y estado de cada insumo. Prioriza stock bajo.",
                "parameters": [
                    {"name": "page", "in": "query", "schema": {"type": "integer", "default": 1}},
                    {"name": "limit", "in": "query", "schema": {"type": "integer", "default": 10}},
                    {
                        "name": "estado",
                        "in": "query",
                        "schema": {"type": "string", "enum": ["todos", "disponible", "stock_bajo"], "default": "todos"},
                        "description": "Filtrar por estado del insumo",
                    },
                    {
                        "name": "search",
                        "in": "query",
                        "schema": {"type": "string"},
                        "description": "Buscar por nombre o codigo del insumo",
                    },
                ],
                "responses": {
                    "200": {"description": "Lista paginada de insumos con estado de stock"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/ordenes-recientes": {
            "get": {
                "tags": ["Insumos (Maestro)", "Transaccional Híbrido"],
                "summary": "Órdenes de Venta Recientes (SQL Server + MongoDB)",
                "description": "Lista transaccional de órdenes de venta desde SQL Server (ventas.vw_resumen_ordenes), enriquecida con datos del cliente desde MongoDB (colección 'customer').",
                "parameters": [
                    {"name": "limit", "in": "query", "schema": {"type": "integer", "default": 10}, "description": "Cantidad máxima de órdenes a retornar"},
                    {"name": "estado", "in": "query", "schema": {"type": "string", "enum": ["C", "P", "A"]}, "description": "Filtro por estado: C (Completado), P (Pendiente), A (Anulado)"},
                    {"name": "dias", "in": "query", "schema": {"type": "integer"}, "description": "Filtrar por órdenes de los últimos N días"},
                ],
                "responses": {
                    "200": {"description": "Lista de órdenes de venta recientes con cliente y estado"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
        "/api/analytics/insumos/dashboard": {
            "get": {
                "tags": ["Insumos (Maestro)"],
                "summary": "Dashboard consolidado de Insumos (indicadores maestros y transaccional)",
                "description": "Retorna todos los indicadores del dashboard de insumos en una sola llamada.",
                "parameters": [
                    {"name": "dias", "in": "query", "schema": {"type": "integer", "default": 30}},
                    {"name": "limit_detalle", "in": "query", "schema": {"type": "integer", "default": 4}},
                    {"name": "limit_ordenes", "in": "query", "schema": {"type": "integer", "default": 5}},
                ],
                "responses": {
                    "200": {"description": "Todos los indicadores del dashboard de insumos"},
                    "404": {"$ref": "#/components/responses/CollectionNotFound"},
                    "503": {"$ref": "#/components/responses/DbUnavailable"},
                },
            }
        },
    },

    "components": {
        "responses": {
            "CollectionNotFound": {
                "description": "La colección requerida no existe",
                "content": {"application/json": {"example": {
                    "error": "coleccion_inexistente",
                    "mensaje": "La colección no existe en la base de datos configurada",
                }}},
            },
            "DbUnavailable": {
                "description": "Servicio de base de datos no disponible",
                "content": {"application/json": {"example": {
                    "error": "base_datos_no_disponible",
                    "mensaje": "Error de conexión o consulta con la base de datos",
                }}},
            },
        }
    },
}

swagger_bp = Blueprint("swagger_spec", __name__)


@swagger_bp.get("/swagger.json")
def swagger_json():
    return jsonify(SWAGGER_SPEC)


swagger_ui_bp = get_swaggerui_blueprint(
    "/swagger",
    "/swagger.json",
    config={"app_name": "Backend Analítico Agrotecno"},
)