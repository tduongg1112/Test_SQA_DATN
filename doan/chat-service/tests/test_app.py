import pytest
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
        
        if data["statistics"]["total_tokens_processed"] is None:
            pytest.fail(
                "[BUG CRITICAL] Developer Python tại 'app.py' CHƯA SỬ DỤNG HÀM TẦNG DB NHƯ: "
                "`COALESCE(SUM(total_tokens), 0)` trong câu SQL dòng 331. "
                "Hệ quả: Nếu hệ thống chưa ai gọi Chatbot, API trả về NULL gây sập biểu đồ Dashboard React!"
            )
        
        # Nếu developer fix code trả về 0, dòng assert này mới được thực hiện an toàn.
        assert data["statistics"]["total_tokens_processed"] == 0


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
