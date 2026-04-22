import pytest
import requests
from unittest.mock import patch, MagicMock
# Import trực tiếp app từ chat-service
from app import app 

@pytest.fixture
def client():
    # Setup môi trường giả lập (Test Client) của Flask mà không cần bật server
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

class TestChatbotMetricsService:

    @patch('app.db.get_connection')
    def test_get_metrics_success(self, mock_get_connection, client):
        """
        UT_FR06_001: MOCK Data chuẩn - Đảm bảo API trả về số liệu Token và Thời gian trung bình chính xác.
        """
        # Arrange: Giả lập Cursor của MySQL
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        # MOCK Data trả về từ câu query lấy List (fetchall)
        mock_cursor.fetchall.return_value = [
            {"id": 1, "input_tokens": 100, "status": "success"},
            {"id": 2, "input_tokens": 200, "status": "error"}
        ]
        
        # MOCK Data trả về từ câu query tính Thống Kê (fetchone)
        mock_cursor.fetchone.return_value = {
            "total_requests": 2,
            "successful_requests": 1,
            "avg_ttft_ms": 150.0,
            "avg_total_time_ms": 400.0,
            "avg_input_tokens": 150.0,
            "avg_output_tokens": 50.0,
            "total_tokens_processed": 400
        }

        # Act: Gọi GET API Metrics
        response = client.get('/metrics?limit=10')
        data = response.get_json()

        # Assert: Xác thực kết quả JSON từ API
        assert response.status_code == 200
        assert "statistics" in data
        assert data["statistics"]["total_requests"] == 2
        assert data["statistics"]["total_tokens_processed"] == 400
        assert data["statistics"]["avg_total_time_ms"] == 400.0


    @patch('app.db.get_connection')
    def test_get_metrics_empty_db_bug(self, mock_get_connection, client):
        """
        UT_FR06_002: [BUG] Xử lý lỗ hổng Exception/Null khi Database còn trống. 
        Nếu không có ai dùng Chat, MySQL AVG/SUM trả về NULL, vậy API phải tự rào lỗi này.
        """
        # Arrange
        mock_cursor = MagicMock()
        mock_get_connection.return_value.cursor.return_value = mock_cursor
        
        # Khi SQL Table trống, COUNT(*) = 0, nhưng SUM và AVG trả về NULL (None trong Python)
        mock_cursor.fetchall.return_value = []
        mock_cursor.fetchone.return_value = {
            "total_requests": 0,
            "successful_requests": None,
            "avg_ttft_ms": None,
            "avg_total_time_ms": None,
            "total_tokens_processed": None
        }

        # Act
        response = client.get('/metrics')
        data = response.get_json()
        
        # Assert (Intentional Fail cho Report SQA)
        # Nếu frontend ReactJS sử dụng JSON 'null' này để vẽ đồ thị (Ví dụ gọi hàm: null.toFixed(2)), 
        # React sẽ bị crash trắng xóa ứng dụng!
        
    # =====================================================
    #  BỔ SUNG TEST ĐỂ NÂNG CAO COVERAGE (FR-06)
    # =====================================================

    def test_health_check(self, client):
        """UT_FR06_003: Kiểm tra trạng thái hệ thống (Health Check)"""
        response = client.get('/')
        assert response.status_code == 200
        assert response.get_json()["status"] == "ok"

    @patch('app.requests.post')
    @patch('app.db.insert_metrics')
    def test_generate_success(self, mock_insert, mock_post, client):
        """UT_FR06_004: Kiểm tra luồng gọi Chatbot thành công"""
        # Mock Kaggle API response
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"response": "Hello from AI"}
        mock_post.return_value = mock_response

        payload = {
            "question": "Xin chào",
            "diagram": {"nodes": []},
            "history": "None"
        }
        
        response = client.post('/generate', json=payload)
        assert response.status_code == 200
        assert "metrics" in response.get_json()
        assert mock_insert.called

    def test_set_get_kaggle_url(self, client):
        """UT_FR06_005: Kiểm tra cấu hình URL hệ thống Admin"""
        # Set URL mới
        new_url = "https://new-api.ngrok.io"
        response = client.post('/set-kaggle-url', json={"url": new_url})
        assert response.status_code == 200
        
        # Get lại để kiểm chứng
        response = client.get('/get-kaggle-url')
        assert response.get_json()["kaggle_url"] == new_url

    @patch('app.db.get_connection')
    def test_get_metric_detail_not_found(self, mock_get_conn, client):
        """UT_FR06_006: Kiểm tra xử lý lỗi khi tìm Metric không tồn tại"""
        mock_cursor = MagicMock()
        mock_get_conn.return_value.cursor.return_value = mock_cursor
        mock_cursor.fetchone.return_value = None
        
        response = client.get('/metrics/999')
        assert response.status_code == 404
        assert response.get_json()["error"] == "Metric not found"

    @patch('app.db.get_connection')
    def test_get_metric_detail_success(self, mock_get_conn, client):
        """UT_FR06_007: Kiểm tra lấy chi tiết Metric thành công"""
        mock_cursor = MagicMock()
        mock_get_conn.return_value.cursor.return_value = mock_cursor
        mock_cursor.fetchone.return_value = {"id": 1, "status": "success"}
        
        response = client.get('/metrics/1')
        assert response.status_code == 200
        assert response.get_json()["id"] == 1

    @patch('app.requests.post')
    def test_generate_timeout(self, mock_post, client):
        """UT_FR06_008: Kiểm tra xử lý lỗi Timeout khi gọi Kaggle"""
        mock_post.side_effect = requests.Timeout()
        
        response = client.post('/generate', json={"question": "test"})
        assert response.status_code == 504
        assert response.get_json()["error"] == "Request timeout"

    def test_set_kaggle_url_error(self, client):
        """UT_FR06_009: Kiểm tra lỗi khi cập nhật URL thiếu tham số"""
        response = client.post('/set-kaggle-url', json={})
        assert response.status_code == 400

    @patch('app.requests.post')
    @patch('app.db.insert_metrics')
    def test_generate_stream_success(self, mock_insert, mock_post, client):
        """UT_FR06_010: Kiểm tra luồng Streaming Chatbot"""
        # Mock streaming response
        mock_response = MagicMock()
        mock_response.__enter__.return_value.iter_lines.return_value = [
            b'data: {"text": "Hello "}',
            b'data: {"text": "World"}'
        ]
        mock_post.return_value = mock_response

        response = client.post('/generate-stream', json={"prompt": "test"})
        assert response.status_code == 200
        # Trigger the generator to execute the code inside
        content = b"".join(response.response)
        assert b"Hello" in content



    @patch('app.db.get_connection')
    def test_get_metrics_database_connection_fail(self, mock_get_connection, client):
        """
        Kiểm tra độ chịu lỗi (Fault tolerance): Khi mất kết nối DB Database, API báo 500 an toàn gọn gàng.
        """
        # Arrange
        mock_get_connection.return_value = None  # Simulate DB offline

        # Act
        response = client.get('/metrics')

        # Assert
        assert response.status_code == 500
        assert response.get_json() == {"error": "Database connection failed"}
