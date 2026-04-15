# Kế hoạch Unit Test tổng thể cho nhóm 4 người

## 1. Mục tiêu chung

Mục tiêu của nhóm là hoàn thành bài Unit Test theo đúng yêu cầu giảng viên:

1. Có file báo cáo test case theo format Excel/CSV.
2. Có test script automation chạy thật trên source code.
3. Có evidence cho execution report và coverage report.
4. Mỗi thành viên phụ trách rõ một module cốt lõi của hệ thống.

Kế hoạch này được cập nhật theo đúng phân công mới của nhóm:

- `Bảo`: Account Service
- `Đức Anh`: Frontend Visual Engine
- `Tùng Dương`: Chatbot Service
- `NMD`: Diagram Service và Collaboration Mode

---

## 2. Bức tranh tổng thể hệ thống

Hệ thống của đồ án hiện tại có 4 vùng chức năng lớn phù hợp với cách chia việc của nhóm:

### A. Account Service

Chịu trách nhiệm:

- xác thực
- JWT / logout
- quản lý thông tin tài khoản
- validate dữ liệu account

Tech stack chính:

- Java
- Spring Boot
- JUnit
- Mockito

### B. Frontend Visual Engine

Chịu trách nhiệm:

- render UI
- xử lý interaction phía React
- hiển thị danh sách, canvas, dialog, components điều hướng
- tương tác API và hiển thị dữ liệu sơ đồ

Tech stack chính:

- JavaScript / TypeScript
- React
- Jest
- React Testing Library

### C. Chatbot Service

Chịu trách nhiệm:

- gọi AI upstream
- xử lý request/response của chatbot
- đếm token
- ghi metrics
- trả dữ liệu AI cho frontend

Tech stack chính:

- Python
- Flask
- PyTest

### D. Diagram Service và Collaboration Mode

Chịu trách nhiệm:

- danh sách sơ đồ
- tạo/sửa/xóa sơ đồ
- quyền truy cập
- collaboration
- logic model/attribute/connection phía backend

Tech stack chính:

- Java
- Spring Boot
- JUnit
- Mockito

---

## 3. Phân công chính thức của nhóm

## Người 1: Bảo

### Vai trò

Chuyên trách `Account Service`

### Phạm vi module

- `doan/account-service`

### Trọng tâm test

- service logic
- controller logic quan trọng
- util class
- validation / exception path

### File test dự kiến

- `AccountServiceTest`
- `AuthControllerTest`
- `RedisTokenServiceTest`
- `JwtUtilTest`
- `ValidateUtilTest`
- `UsernameGeneratorTest`

### Deliverable tối thiểu

- test case cho module account
- test script JUnit chạy được
- ảnh execution report
- ảnh coverage report

---

## Người 2: Đức Anh

### Vai trò

Chuyên trách `Frontend Visual Engine`

### Phạm vi module

- `doan/react-flow/src/components`
- `doan/react-flow/src/pages`
- `doan/react-flow/src/hooks`
- các phần UI logic và view behavior phù hợp với Jest/RTL

### Trọng tâm test

- render component quan trọng
- interaction cơ bản
- state update ở mức UI
- các case lỗi hiển thị / empty state / loading state

### File test dự kiến

- `*.test.tsx` cho component/page quan trọng
- `*.test.ts` cho hook/UI helper nếu phù hợp

### Deliverable tối thiểu

- test case cho phần frontend visual
- test script Jest/RTL chạy được
- ảnh execution report
- ảnh coverage report

---

## Người 3: Tùng Dương

### Vai trò

Chuyên trách `Chatbot Service`

### Phạm vi module

- `doan/chat-service/app.py`
- `doan/chat-service/database.py`
- `doan/chat-service/token_counter.py`

### Trọng tâm test

- API chatbot
- timeout/error path
- metrics logging
- tokenizer / token count
- DB helper của chatbot

### File test dự kiến

- `tests/test_app.py`
- `tests/test_database.py`
- `tests/test_token_counter.py`

### Deliverable tối thiểu

- test case cho chatbot service
- pytest script chạy được
- ảnh execution report
- ảnh coverage report

---

## Người 4: NMD

### Vai trò

Chuyên trách `Diagram Service và Collaboration Mode`

### Phạm vi module

- `doan/react-flow-be`

### Trọng tâm test

- diagram management
- collaboration service
- permission/access logic
- model/attribute/connection service cần thiết

### File test dự kiến

- `DiagramManagementServiceTest`
- `CollaborationServiceTest`
- `DatabaseDiagramServiceTest`
- `SchemaVisualizerServiceTest`
- các test service/controller phù hợp khác

### Deliverable tối thiểu

- test case cho diagram/collaboration
- test script JUnit/Mockito chạy được
- ảnh execution report
- ảnh coverage report

---

## 4. Cấu trúc kế hoạch tổng thể ở mức hệ thống

Nhóm sẽ làm việc theo 3 lớp:

### Lớp 1. Kế hoạch tổng thể

Nội dung:

- chia module theo người
- thống nhất format test case
- thống nhất naming convention
- thống nhất cách chụp evidence

### Lớp 2. Kế hoạch theo module

Mỗi người phải có:

- danh sách file được test
- danh sách method được test
- test case CSV/Excel tương ứng
- file test script tương ứng

### Lớp 3. Tổng hợp báo cáo cuối

Bao gồm:

- merge test case của cả nhóm
- đếm pass/fail
- tổng hợp coverage
- điền project link

---

## 5. Quy tắc chung cho cả nhóm

## 5.1 Quy tắc về scope

Mỗi người chỉ tập trung vào đúng module mình phụ trách.

Không dàn trải sang phần của người khác trừ khi:

- cần review chéo
- cần thống nhất format
- cần hỗ trợ merge/fix môi trường

## 5.2 Quy tắc về test case

Mỗi test case phải ánh xạ được sang test script thật.

Không viết test case quá rộng kiểu:

- test chatbot
- test account
- test diagram

Mà phải cụ thể đến:

- file test
- method test
- input
- expected output

## 5.3 Quy tắc về kết quả

Ban đầu `Test Result` có thể để `Pending`.

Sau khi chạy automation thật mới cập nhật thành:

- `Pass`
- `Fail`

## 5.4 Quy tắc về bug thật

Nhóm nên giữ lại một số case `[INTENDED FAIL]` hoặc case fail thực tế nếu có lý do rõ ràng.

Điều này giúp:

- bài có tính thực tế
- phần kết luận mạnh hơn
- dễ nêu improvement/future fix

---

## 6. Kế hoạch triển khai theo giai đoạn

## Giai đoạn 1. Chốt scope và test case

Tất cả thành viên cần hoàn thành:

1. xác định file/class/module phụ trách
2. lập test case theo format chung
3. review chéo để tránh test case vô lý hoặc trùng lặp

Đầu ra:

- file CSV/Excel cho từng người

## Giai đoạn 2. Setup môi trường test

Mỗi người setup environment theo stack của mình:

- Java: JUnit / Mockito / Maven test
- Frontend: Jest / RTL
- Python: PyTest

Đầu ra:

- test runner chạy được
- repo sẵn sàng để thêm test file

## Giai đoạn 3. Viết test script

Mỗi người code test script tương ứng với test case đã chốt.

Đầu ra:

- file test được tạo
- test script chạy được bước đầu

## Giai đoạn 4. Chạy automation và sửa lỗi

Mỗi người:

- chạy test
- fix test fail do mock/setup
- xác nhận intentional fail nếu có

Đầu ra:

- kết quả pass/fail thật

## Giai đoạn 5. Coverage và báo cáo cuối

Mỗi người:

- chạy coverage
- chụp evidence
- bàn giao cho nhóm tổng hợp

Đầu ra:

- execution report
- coverage report
- file báo cáo tổng hợp

---

## 7. Kế hoạch theo mốc thời gian đề xuất

## Buổi 1

- chốt scope từng người
- hoàn thành test case thô

## Buổi 2

- setup môi trường test
- tạo skeleton file test

## Buổi 3

- viết test script cho case ưu tiên cao

## Buổi 4

- chạy automation
- sửa lỗi test
- bổ sung case thiếu

## Buổi 5

- chạy coverage
- chụp ảnh
- merge báo cáo cuối

---

## 8. Dependencies giữa các thành viên

## 8.1 Bảo và NMD

Liên quan với nhau ở lane Java:

- có thể thống nhất chung cách viết JUnit/Mockito
- có thể dùng chung style naming/test annotation

## 8.2 Đức Anh và Tùng Dương

Liên quan với nhau ở luồng chatbot:

- Tùng Dương phụ trách backend chatbot
- Đức Anh phụ trách UI visual engine

Nhưng ở kế hoạch hiện tại:

- phần Chatbot Service backend là của Tùng Dương
- frontend render/interaction là của Đức Anh

## 8.3 Toàn nhóm

Phải thống nhất:

- format file test case
- convention `Test Case ID`
- cách điền pass/fail
- cách ghi note intentional fail

---

## 9. Danh sách đầu ra cuối cùng của toàn nhóm

Toàn nhóm phải có:

1. Kế hoạch tổng thể nhóm
2. File test case của từng người
3. File test script thật của từng module
4. Kết quả execution report
5. Kết quả coverage report
6. Link GitHub

---

## 10. Định nghĩa hoàn thành cho từng thành viên

Một thành viên được xem là hoàn thành khi có đủ:

1. file test case module của mình
2. file test script module của mình
3. kết quả chạy test thật
4. ảnh execution report
5. ảnh coverage report

Nếu thiếu một trong các mục trên thì chưa được xem là xong module.

---

## 11. Chốt hướng làm việc của nhóm

Kế hoạch tổng thể sau cập nhật là:

1. `Bảo` làm `Account Service`
2. `Đức Anh` làm `Frontend Visual Engine`
3. `Tùng Dương` làm `Chatbot Service`
4. `NMD` làm `Diagram Service và Collaboration Mode`

Đây là cách chia hợp lý nhất theo kiến trúc hiện tại của đồ án vì:

- khớp đúng module hệ thống
- khớp đúng tech stack từng người
- giảm chồng chéo
- dễ tổng hợp báo cáo cuối

Từ kế hoạch tổng thể này, mỗi người sẽ có một guideline module riêng. Phần tiếp theo của bạn là guideline riêng cho `Chatbot Service`.
