# Guideline riêng cho Chatbot Service

## 1. Phạm vi đúng của phần bạn

File này chỉ nói về đúng phần bạn đang phụ trách:

- `doan/chat-service/app.py`
- `doan/chat-service/database.py`
- `doan/chat-service/token_counter.py`

Không bao gồm:

- frontend visual engine
- React component
- Jest / React Testing Library
- Java service
- account service
- diagram service
- collaboration service

Nếu cần một câu ngắn gọn để tự nhắc phạm vi:

**Phần của bạn = backend chatbot + AI request/response + token + metrics + DB helper của chatbot.**

---

## 2. Mục tiêu kiểm thử của phần Chatbot Service

Mục tiêu không phải là kiểm thử bản thân mô hình AI có “thông minh” hay không.

Mục tiêu thực sự là kiểm tra 5 thứ:

1. API chatbot nhận request đúng.
2. Service gọi upstream AI đúng cách.
3. Service xử lý đúng khi upstream trả thành công, timeout, hoặc lỗi.
4. Token và metrics được tính/lưu đúng.
5. Chatbot service không crash khi upstream hoặc tokenizer lỗi.

Nói cách khác, bạn đang kiểm thử:

- reliability
- error handling
- metrics logging
- response contract
- DB side-effect

Bạn **không** kiểm thử:

- chất lượng ngôn ngữ của LLM
- độ đúng nội dung học thuật do AI sinh ra
- UI hiển thị phía frontend

---

## 3. Các file test case đang thuộc phần của bạn

Trong file [CS_Chatbot_AI_TestCases.csv](/Users/tungduong1112/Tài liệu học tập%20PTIT/Semester%208/Đảm%20bảo%20chất%20lượng%20Phần%20mềm%20(SQA)/Test%20Đồ%20án/CS_Chatbot_AI_TestCases.csv:1), phần đúng scope của bạn chỉ là:

- `UT_CS_APP_*`
- `UT_CS_DB_*`
- `UT_CS_TKN_*`

Các prefix này tương ứng với:

- `APP`: route/API logic trong `app.py`
- `DB`: database helper trong `database.py`
- `TKN`: tokenizer helper trong `token_counter.py`

Phần hiện tại không thuộc scope của bạn:

- `UT_FE_CBT_*`
- `UT_FE_STA_*`

Nếu cần tách riêng cho báo cáo cá nhân, bạn chỉ nên lọc giữ các dòng:

- `UT_CS_APP_001` tới `UT_CS_APP_015`
- `UT_CS_DB_001` tới `UT_CS_DB_004`
- `UT_CS_TKN_001` tới `UT_CS_TKN_004`

---

## 4. Kết quả cuối mà phần của bạn phải tạo ra

Bạn cần có đủ 5 đầu ra:

1. File test case chatbot service
2. File test script PyTest thật
3. Kết quả chạy test automation thật
4. Coverage report cho chatbot service
5. Evidence để chèn vào báo cáo

Nếu thiếu test script chạy thật thì mới chỉ là kế hoạch, chưa phải bài làm hoàn chỉnh.

---

## 5. Cấu trúc file test sẽ tạo

Khuyến nghị tạo:

```text
doan/chat-service/tests/
  test_app.py
  test_database.py
  test_token_counter.py
  conftest.py
```

### Vai trò từng file

`test_app.py`

- test cho `health_check`
- test cho `generate`
- test cho `generate_stream`
- test cho `set_kaggle_url`
- test cho `get_metrics`
- test cho `get_metric_detail`

`test_database.py`

- test `get_connection`
- test `init_database`
- test `insert_metrics`

`test_token_counter.py`

- test `__init__`
- test `count_tokens`
- test `count_tokens_from_ids`
- test `encode`
- test `decode`

`conftest.py`

- fixture cho Flask app client
- fixture mock DB
- fixture mock tokenizer

---

## 6. Framework và tool chỉ dành cho phần của bạn

## 6.1 Tooling chính

Bạn chỉ cần:

- `PyTest`
- `pytest-cov`
- `pytest-mock`

## 6.2 Không cần cho lane của bạn

Ở giai đoạn này bạn không cần:

- Jest
- React Testing Library
- JUnit
- Mockito
- JaCoCo

Các tool đó là của người khác hoặc của phần hệ thống chung.

---

## 7. Môi trường test chuẩn cho Chatbot Service

## 7.1 Môi trường chạy chuẩn

Khuyến nghị:

- `Python 3.10` hoặc `3.11`
- virtual environment riêng

## 7.2 Nguyên tắc môi trường unit test chuẩn

Unit test của bạn phải:

- không gọi thật tới ngrok/Kaggle
- không gọi MySQL thật
- không tải tokenizer thật từ HuggingFace
- không phụ thuộc internet
- chạy lặp lại nhiều lần vẫn ổn định

Nếu test chỉ chạy được khi:

- mạng đang tốt
- ngrok còn sống
- MySQL local đang bật

thì đó chưa phải unit test chuẩn.

---

## 8. Các dependency ngoài phải mock

Trong phần chatbot service, bạn phải mock các dependency sau:

### Với `app.py`

- `requests.post`
- `db.insert_metrics`
- `db.get_connection`
- `token_counter.count_tokens`

### Với `database.py`

- `mysql.connector.connect`
- connection object
- cursor object

### Với `token_counter.py`

- `AutoTokenizer.from_pretrained`
- `self.tokenizer.tokenize`
- `self.tokenizer.encode`
- `self.tokenizer.decode`

---

## 9. Những gì cần được test trước

## 9.1 Nhóm ưu tiên 1: API chính

Đây là phần cần làm trước vì giá trị cao nhất:

- `health_check`
- `generate`
- `generate_stream`

Tương ứng trong CSV:

- `UT_CS_APP_001` đến `UT_CS_APP_012`

## 9.2 Nhóm ưu tiên 2: DB helper

- `get_connection`
- `init_database`
- `insert_metrics`

Tương ứng:

- `UT_CS_DB_001` đến `UT_CS_DB_004`

## 9.3 Nhóm ưu tiên 3: token helper

- `count_tokens`
- `fallback estimate`
- init không có tokenizer

Tương ứng:

- `UT_CS_TKN_001` đến `UT_CS_TKN_004`

---

## 10. Cách viết test script từ CSV sang PyTest

Trong CSV:

- `File name = ChatbotAppTest`
- `Method name = generate_withValidResponseKey_returns200AndMetrics`

Trong source thật, bạn nên viết theo chuẩn pytest:

```python
def test_generate_with_valid_response_key_returns_200_and_metrics(client, mocker):
    # Test Case ID: UT_CS_APP_003
    ...
```

### Mapping khuyến nghị

`ChatbotAppTest`

- file thật: `test_app.py`

`ChatbotDatabaseTest`

- file thật: `test_database.py`

`TokenCounterTest`

- file thật: `test_token_counter.py`

---

## 11. Cách tổ chức fixture

## 11.1 Fixture cho Flask app client

Bạn cần fixture kiểu:

```python
@pytest.fixture
def client():
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client
```

Mục đích:

- gọi `GET /`
- gọi `POST /generate`
- gọi `POST /generate-stream`

## 11.2 Fixture mock DB

Mục đích:

- tránh gọi MySQL thật
- kiểm soát return value của `insert_metrics`
- kiểm soát `get_connection`

## 11.3 Fixture mock tokenizer

Mục đích:

- tránh tải tokenizer thật
- chủ động điều khiển số token

---

## 12. Lộ trình triển khai thực tế cho phần của bạn

## Bước 1. Setup môi trường Python

```bash
cd doan/chat-service
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pip install pytest pytest-cov pytest-mock
```

Definition of Done:

- pytest chạy được
- import được app/database/token_counter

## Bước 2. Tạo skeleton file test

Tạo các file:

- `tests/test_app.py`
- `tests/test_database.py`
- `tests/test_token_counter.py`
- `tests/conftest.py`

Definition of Done:

- pytest nhận diện được file test
- không còn lỗi `module not found`

## Bước 3. Code nhóm case ưu tiên cao

Thứ tự nên làm:

1. `UT_CS_APP_001`
2. `UT_CS_APP_003`
3. `UT_CS_APP_005`
4. `UT_CS_APP_006`
5. `UT_CS_APP_010`
6. `UT_CS_DB_003`
7. `UT_CS_TKN_001`
8. `UT_CS_TKN_003`

Lý do:

- đây là các case chứng minh rõ nhất service chạy đúng và chống lỗi tốt

## Bước 4. Chạy automation lần đầu

```bash
pytest -q
```

Mục tiêu:

- xem test pass/fail thực tế
- fix lỗi do setup fixture/mock

## Bước 5. Chạy coverage

```bash
pytest --cov=. --cov-report=term-missing --cov-report=html
```

Mục tiêu:

- lấy coverage cho báo cáo
- xác định nhánh nào còn chưa được test

## Bước 6. Cập nhật lại file test case

Sau khi chạy thật:

- đổi `Pending` thành `Pass` hoặc `Fail`
- giữ lại một số case `Intentional Fail` nếu muốn phản ánh bug thật

---

## 13. Chiến lược cho các case fail chủ đích

Trong CSV của bạn hiện có một số case có giá trị rất tốt cho báo cáo cuối:

### `UT_CS_APP_008`

Ý nghĩa:

- chỉ ra `generate()` chưa validate request body tốt

### `UT_CS_APP_009`

Ý nghĩa:

- chỉ ra code đang bỏ qua `response.status_code` của upstream

### `UT_CS_TKN_004`

Ý nghĩa:

- chỉ ra `TokenCounter` có khả năng vào trạng thái thiếu `self.tokenizer`

Bạn không nên xóa các case này.

Bạn nên dùng chúng để:

- giải thích limitation hiện tại
- nêu bug thực tế
- đề xuất hướng fix trong conclusion

---

## 14. Coverage mục tiêu cho riêng phần bạn

Mục tiêu thực tế và hợp lý:

- `app.py`: 70%+
- `database.py`: 80%+
- `token_counter.py`: 70%+

Tổng thể lane chatbot:

- coverage khoảng `70% - 80%` là đẹp và khả thi

Không cần cố đạt 100% nếu phải test những nhánh không đáng giá.

---

## 15. Evidence bạn cần chụp cho báo cáo

Bạn cần chụp ít nhất:

1. terminal chạy `pytest -q`
2. terminal chạy coverage
3. nếu có `htmlcov`, chụp trang tổng coverage
4. nếu có intentional fail, chụp đúng dòng test fail đó

Đây sẽ là input trực tiếp cho:

- Execution Report
- Code Coverage Report

---

## 16. Định nghĩa hoàn thành cho riêng phần Chatbot Service

Phần của bạn được xem là hoàn thành khi có đủ:

1. File test case chatbot service đã chốt
2. Test script PyTest thật
3. Automation chạy được
4. Có pass/fail thực tế
5. Có coverage report
6. Có note bug/intentional fail để đưa vào kết luận

Nếu chỉ có CSV mà chưa có pytest chạy thật thì chưa hoàn thành.

---

## 17. Việc tiếp theo sau khi bạn duyệt file này

Nếu bạn xác nhận guideline này đúng scope của mình, bước tiếp theo nên là:

1. setup test tooling Python
2. tạo skeleton `tests/`
3. implement `test_app.py` trước
4. chạy pytest lần đầu
5. mở rộng sang `database.py` và `token_counter.py`

Đây là đường đi ngắn nhất và đúng nhất cho phần riêng của bạn.
