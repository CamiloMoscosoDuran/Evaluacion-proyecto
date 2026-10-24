import unittest
from unittest.mock import patch

from app.main import app
from app.config import settings
from app.database import mongodb, sqlserver
from app.models.formula import Formula
from app.repositories.mongodb.client_repository import get_clients
from app.schemas.formula_schema import format_formula_response
from app.services.analytics.formula_service import analyze_low_stock


class BackendSmokeTestCase(unittest.TestCase):
    def setUp(self):
        self.app = app.test_client()

    def test_settings_collections(self):
        self.assertEqual(settings.MONGODB_COLLECTIONS, ("customer", "formula", "supply"))
        self.assertEqual(settings.SQLSERVER_DATABASE, "agroTecno_db")

    @patch("app.repositories.mongodb.client_repository.find_all")
    def test_clients_use_customer_collection(self, find_all):
        find_all.return_value = [{"_id": "CLI001"}]

        clients = get_clients()

        self.assertEqual(clients, [{"_id": "CLI001"}])
        find_all.assert_called_once_with("customer", limit=1000)

    @patch("app.services.analytics.formula_service.get_insumos")
    def test_low_stock_uses_nested_inventory_fields(self, get_insumos):
        get_insumos.return_value = [
            {"_id": "SUP001", "inventory": {"current_stock": 4, "minimum_stock": 5}},
            {"_id": "SUP002", "inventory": {"current_stock": 8, "minimum_stock": 5}},
        ]

        result = analyze_low_stock()

        self.assertEqual(result["total_stock_bajo"], 1)
        self.assertEqual(result["insumos_stock_bajo"][0]["_id"], "SUP001")
        self.assertEqual(
            result["campos_utilizados"],
            {
                "stock_actual": "inventory.current_stock",
                "stock_minimo": "inventory.minimum_stock",
            },
        )

    def test_root_endpoint(self):
        response = self.app.get("/")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertEqual(data.get("estado"), "Operativo")

    def test_swagger_json(self):
        response = self.app.get("/swagger.json")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("/health", data.get("paths", {}))
        self.assertIn("/api/analytics/clientes", data.get("paths", {}))
        self.assertIn("/api/analytics/formulas", data.get("paths", {}))
        self.assertIn("/api/analytics/insumos-stock-bajo", data.get("paths", {}))
        self.assertIn("/api/analytics/ventas-formulas", data.get("paths", {}))
        self.assertNotIn("/api/analytics/reuniones", data.get("paths", {}))

    def test_health_endpoint_structure(self):
        response = self.app.get("/health")
        # May be 200 or 207 depending on if database containers are currently active
        self.assertIn(response.status_code, [200, 207])
        data = response.get_json()
        self.assertIn("mongodb", data)
        self.assertIn("sqlserver", data)

    def test_formula_model(self):
        f = Formula(id="FOR001", name="Test Formula", preparation_cost=100.0, suggested_price=150.0)
        self.assertEqual(f.id, "FOR001")
        self.assertEqual(f.name, "Test Formula")

    def test_schema_formatting(self):
        doc = {
            "_id": "FOR001",
            "name": "Formula 1",
            "preparation_cost": 50.0,
            "recipe": {"version": "1.0", "ingredients": []},
        }
        res = format_formula_response(doc)
        self.assertEqual(res["id"], "FOR001")
        self.assertEqual(res["version"], "1.0")

    def test_formula_kpi_total(self):
        response = self.app.get("/api/analytics/formulas/kpi-total?dias=30")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("total_registradas", data)
        self.assertIn("nuevas_en_periodo", data)
        self.assertIn("comparativa_texto", data)

    def test_formula_kpi_activas(self):
        response = self.app.get("/api/analytics/formulas/kpi-activas")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("total_activas", data)
        self.assertIn("porcentaje_activas", data)

    def test_formula_kpi_en_revision(self):
        response = self.app.get("/api/analytics/formulas/kpi-en-revision")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("total_en_revision", data)
        self.assertIn("mensaje", data)

    def test_formula_distribucion_estado(self):
        response = self.app.get("/api/analytics/formulas/distribucion-estado")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("activas", data)
        self.assertIn("en_revision", data)
        self.assertIn("serie_grafico", data)

    def test_formula_evolucion_mensual(self):
        response = self.app.get("/api/analytics/formulas/evolucion-mensual?meses=6")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("items", data)
        self.assertEqual(len(data["items"]), 6)
        self.assertIn("mes_nombre", data["items"][0])

    def test_formula_detalle(self):
        response = self.app.get("/api/analytics/formulas/detalle?limit=5")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("items", data)
        self.assertIn("paginacion", data)
        if data["items"]:
            first = data["items"][0]
            self.assertIn("codigo", first)
            self.assertIn("nombre", first)
            self.assertIn("tipo", first)
            self.assertIn("estado_texto", first)

    def test_formula_dashboard(self):
        response = self.app.get("/api/analytics/formulas/dashboard")
        self.assertEqual(response.status_code, 200)
        data = response.get_json()
        self.assertIn("indicadores", data)
        self.assertIn("indicador_1_kpi_total", data["indicadores"])
        self.assertIn("indicador_2_kpi_activas", data["indicadores"])
        self.assertIn("indicador_3_kpi_en_revision", data["indicadores"])
        self.assertIn("indicador_4_distribucion_estado", data["indicadores"])
        self.assertIn("indicador_5_evolucion_mensual", data["indicadores"])
        self.assertIn("indicador_6_detalle_reciente", data["indicadores"])

    def test_swagger_includes_all_formula_indicators(self):
        response = self.app.get("/swagger.json")
        self.assertEqual(response.status_code, 200)
        paths = response.get_json().get("paths", {})
        self.assertIn("/api/analytics/formulas/kpi-total", paths)
        self.assertIn("/api/analytics/formulas/kpi-activas", paths)
        self.assertIn("/api/analytics/formulas/kpi-en-revision", paths)
        self.assertIn("/api/analytics/formulas/distribucion-estado", paths)
        self.assertIn("/api/analytics/formulas/evolucion-mensual", paths)
        self.assertIn("/api/analytics/formulas/detalle", paths)
        self.assertIn("/api/analytics/formulas/dashboard", paths)
        self.assertIn("/api/analytics/formulas/mas-vendidas", paths)

    def test_top_selling_formulas(self):
        response = self.app.get("/api/analytics/formulas/mas-vendidas?anio=2026&limit=3")
        # SQL server may be accessible or return 503 if down in CI
        if response.status_code == 200:
            data = response.get_json()
            self.assertIn("items", data)
            self.assertIn("total_formulas_en_ranking", data)
            self.assertIn("fuente_datos", data)
            if data["items"]:
                first = data["items"][0]
                self.assertIn("formula_id", first)
                self.assertIn("formula_name", first)
                self.assertIn("unidades_vendidas", first)


if __name__ == "__main__":
    unittest.main()


