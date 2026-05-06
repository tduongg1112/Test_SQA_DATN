import pytest
import requests
from unittest.mock import patch, MagicMock
from app import app 

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

class TestChatbotMetricsService:

    @patch('app.db.get_connection')
    def test_get_metrics_success_AD_001(self, mock_get_connection, client):
        """UT_AD_001: Assess happy-path calculation of Token Usage and Time averages."""
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = [
            {"id": 1, "input_tokens": 100, "status": "success"},
            {"id": 2, "input_tokens": 200, "status": "error"}
        ]
        
        mock_cursor.fetchone.return_value = {
            "total_requests": 2,
            "successful_requests": 1,
            "avg_ttft_ms": 150.0,
            "avg_total_time_ms": 400.0,
            "avg_input_tokens": 150.0,
            "avg_output_tokens": 50.0,
            "total_tokens_processed": 400
        }

        response = client.get('/metrics?limit=10')
        data = response.get_json()

        assert response.status_code == 200
        assert "statistics" in data
        assert data["statistics"]["total_requests"] == 2
        assert data["statistics"]["total_tokens_processed"] == 400
        assert data["statistics"]["avg_total_time_ms"] == 400.0

    @patch('app.db.get_connection')
    def test_get_metrics_empty_db_bug_AD_002(self, mock_get_connection, client):
        """UT_AD_002: [BUG] Evaluate empty dataset (Divide by Zero / Null handling)."""
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = []
        mock_cursor.fetchone.return_value = {
            "total_requests": 0,
            "successful_requests": None,
            "avg_ttft_ms": None,
            "avg_total_time_ms": None,
            "total_tokens_processed": None
        }

        response = client.get('/metrics')
        data = response.get_json()
        
        # Intentional Fail check - DB Returns NULL which crashes frontend
        # The test passes at the python level, but conceptually logs the Bug for SQA.
        if data["statistics"]["avg_total_time_ms"] is None:
            pytest.fail("[BUG CRITICAL] In MySQL, SUM and AVG on zero rows return NULL. Backend returns JSON null, which crashes frontend. COALESCE(SUM, 0) is needed.")

    @patch('app.db.get_connection')
    def test_get_metrics_error_stats_AD_003(self, mock_get_connection, client):
        """UT_AD_003: Error metric statistics."""
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = []
        # 2 success, 3 timeout
        mock_cursor.fetchone.return_value = {
            "total_requests": 5,
            "successful_requests": 2,
            "avg_ttft_ms": 100.0,
            "avg_total_time_ms": 200.0,
            "total_tokens_processed": 500
        }

        response = client.get('/metrics?limit=5')
        data = response.get_json()

        assert response.status_code == 200
        assert data["statistics"]["successful_requests"] == 2

    @patch('app.db.get_connection')
    def test_get_metrics_limit_zero_AD_004(self, mock_get_connection, client):
        """UT_AD_004: Query Param bounds check for metrics pagination."""
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = [] # Empty array for limit 0
        mock_cursor.fetchone.return_value = {
            "total_requests": 100,
            "successful_requests": 95,
            "avg_total_time_ms": 500.0,
            "total_tokens_processed": 1000
        }

        response = client.get('/metrics?limit=0')
        data = response.get_json()

        assert response.status_code == 200
        assert len(data["metrics"]) == 0
        assert data["statistics"]["total_requests"] == 100

    def test_get_metrics_limit_type_error_bug_AD_005(self, client):
        """UT_AD_005: [BUG] Type coercion exception handling on limit query parameter."""
        response = client.get('/metrics?limit=five')
        
        # We expect the server to gracefully handle the bad type and return 400 Bad Request.
        # BUT current code crashes with a ValueError inside int() causing a 500 Server Error.
        if response.status_code == 500:
            pytest.fail("[BUG CRITICAL] Current code type=int inherently triggers a strict 500 Server Error if ValueError is thrown when parsing raw strings.")
        
        assert response.status_code == 400

    @patch('app.db.get_connection')
    def test_get_metric_detail_not_found_AD_006(self, mock_get_conn, client):
        """UT_AD_006: Evaluate ID bounds checking for detailed metric view."""
        mock_cursor = MagicMock()
        mock_get_conn.return_value.cursor.return_value = mock_cursor
        mock_cursor.fetchone.return_value = None
        
        response = client.get('/metrics/99999')
        assert response.status_code == 404
        assert response.get_json()["error"] == "Metric not found"

    @patch('app.db.get_connection')
    def test_get_metrics_extreme_tokens_AD_007(self, mock_get_connection, client):
        """UT_AD_007: Validate extreme token integers handling."""
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        mock_cursor.fetchall.return_value = []
        mock_cursor.fetchone.return_value = {
            "total_requests": 1,
            "successful_requests": 1,
            "avg_total_time_ms": 100.0,
            "total_tokens_processed": 100000000 # 100 Million
        }

        response = client.get('/metrics?limit=1')
        data = response.get_json()

        assert response.status_code == 200
        assert data["statistics"]["total_tokens_processed"] == 100000000


    # =====================================================
    #  BỔ SUNG TEST ĐỂ NÂNG CAO COVERAGE (AD_008 -> AD_012)
    # =====================================================

    def test_health_check_AD_008(self, client):
        """UT_AD_008: Kiểm tra trạng thái hệ thống (Health Check)"""
        response = client.get('/')
        assert response.status_code == 200
        assert response.get_json()["status"] == "ok"

    def test_set_get_kaggle_url_AD_009(self, client):
        """UT_AD_009: Kiểm tra cấu hình URL hệ thống Admin"""
        new_url = "https://new-api.ngrok.io"
        response = client.post('/set-kaggle-url', json={"url": new_url})
        assert response.status_code == 200
        
        response = client.get('/get-kaggle-url')
        assert response.get_json()["kaggle_url"] == new_url

    @patch('app.db.get_connection')
    def test_get_metric_detail_success_AD_010(self, mock_get_conn, client):
        """UT_AD_010: Kiểm tra lấy chi tiết Metric thành công"""
        mock_cursor = MagicMock()
        mock_get_conn.return_value.cursor.return_value = mock_cursor
        mock_cursor.fetchone.return_value = {"id": 1, "status": "success"}
        
        response = client.get('/metrics/1')
        assert response.status_code == 200
        assert response.get_json()["id"] == 1

    def test_set_kaggle_url_error_AD_011(self, client):
        """UT_AD_011: Kiểm tra lỗi khi cập nhật URL thiếu tham số"""
        response = client.post('/set-kaggle-url', json={})
        assert response.status_code == 400

    @patch('app.db.get_connection')
    def test_get_metrics_database_connection_fail_AD_012(self, mock_get_connection, client):
        """UT_AD_012: Kiểm tra độ chịu lỗi (Fault tolerance) khi DB offline"""
        mock_get_connection.return_value = None 
        response = client.get('/metrics')
        assert response.status_code == 500
        assert response.get_json() == {"error": "Database connection failed"}
