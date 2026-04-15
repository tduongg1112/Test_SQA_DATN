# Guideline làm bài Unit Test cho đồ án SQA

## 1. Kết luận ngắn: bài này phải làm gì?

Có. Với yêu cầu giảng viên trong ảnh 1, nhóm không chỉ điền file Excel mô tả test case, mà phải làm đồng thời 3 phần:

1. Viết **unit test script thật** trong source code để chạy automation.
2. Điền **Unit Testing Report bằng Excel** để mô tả phạm vi test, test case, kết quả chạy, coverage.
3. Chụp **evidence** khi chạy test và coverage để đưa vào báo cáo.

Nói ngắn gọn:

- Excel = tài liệu quản lý và báo cáo.
- Test script = code kiểm thử thực tế.
- Screenshot = bằng chứng đã chạy thật.

Nếu chỉ làm Excel mà không có test script chạy được thì bài sẽ thiếu phần `Project Link`, `Execution Report`, `Code Coverage Report`.

---

## 2. Sau khi scan project, cốt lõi của đồ án là gì?

Đây là một đồ án **thiết kế sơ đồ cơ sở dữ liệu trực quan** theo kiến trúc microservices.

### 2.1 Thành phần chính

1. `doan/react-flow`
   Frontend React + TypeScript để người dùng vẽ sơ đồ, thao tác canvas, gọi API, mở WebSocket.

2. `doan/account-service`
   Spring Boot service quản lý tài khoản, xác thực, JWT, logout, tìm kiếm tài khoản, tạo/xóa/cập nhật tài khoản.

3. `doan/react-flow-be`
   Spring Boot service cốt lõi cho diagram: tạo sơ đồ, danh sách sơ đồ, collaboration, model, attribute, connection, migration snapshot, WebSocket real-time.

4. `doan/api-gateway`
   Gateway định tuyến và security. Module này có logic nhưng ít phù hợp hơn cho giai đoạn đầu của unit test vì thiên về cấu hình/filter.

5. `doan/chat-service`
   Flask service proxy AI: nhận request, forward sang Kaggle/Ngrok, đếm token, ghi metrics DB, trả response.

### 2.2 Nhận định cho bài Unit Test

Phần đáng test nhất để lấy điểm:

- `account-service`: logic nghiệp vụ rõ, dễ mock, dễ viết test và dễ giải thích.
- `react-flow-be`: nhiều service có business rule rõ ràng, rất hợp unit test.
- `chat-service`: có nhiều nhánh thành công/thất bại/timeout, rất hợp pytest.
- `react-flow` frontend: chỉ nên test **utils/service logic thuần**, không nên sa đà test UI component nếu nhóm ít thời gian.

---

## 3. Nhóm nên hiểu đúng yêu cầu giảng viên như thế nào?

### 3.1 Yêu cầu ở mục 1.1 Tools and Libraries

Nhóm phải liệt kê:

- Framework test dùng để viết test.
- Library hỗ trợ mock/assert/coverage.

### 3.2 Yêu cầu ở mục 1.2 Scope of Testing

Nhóm phải chia rõ:

- File/class/function **sẽ test**.
- File/class/function **không cần test** và giải thích lý do.

Không được ghi chung chung kiểu "test backend" hay "test frontend".

### 3.3 Yêu cầu ở mục 1.3 Create unit test cases

Mỗi test case trong Excel phải có:

- `Test Case ID`
- `Test Objective`
- `Input`
- `Expected Output`
- `Notes`

Và phải tổ chức theo:

- `File Name / Class Name`
- `Method Name`

Điểm quan trọng:
Mỗi test case trong Excel nên ánh xạ sang một test method thật trong code.

### 3.4 Yêu cầu ở mục 1.4 Project Link

Nhóm phải có link GitHub chứa các file unit test đã viết.

### 3.5 Yêu cầu ở mục 1.5 Execution Report

Phải tổng hợp:

- Bao nhiêu test pass
- Bao nhiêu test fail
- Ảnh chụp terminal hoặc report khi chạy test

### 3.6 Yêu cầu ở mục 1.6 Code Coverage Report

Phải dùng tool coverage, ví dụ:

- JaCoCo cho Java
- pytest-cov cho Python
- Vitest coverage cho TypeScript nếu nhóm làm frontend

### 3.7 Yêu cầu ở mục 2 Requirements for Unit Test Scripts

Script test phải có:

- Comment rõ ràng
- Comment hoặc annotation thể hiện `Test Case ID`
- Tên test dễ hiểu
- Nếu test có DB change thì phải kiểm tra dữ liệu thay đổi đúng
- Nếu test có DB change thì phải rollback hoặc reset dữ liệu sau test

---

## 4. Nhóm có cần làm automation không?

Có, nhưng ở mức **unit test automation**, không phải automation UI kiểu Selenium.

Nhóm nên hiểu như sau:

- Không bắt buộc làm end-to-end automation.
- Không cần test bằng tay toàn bộ rồi chụp màn hình là xong.
- Cần viết test script để chạy bằng lệnh, ví dụ `mvn test`, `pytest`, `npm test`.

Vì vậy, câu trả lời đúng cho câu hỏi của bạn là:

**Đúng, nhóm phải viết các file test script để chạy tự động và bắt lỗi logic.**

Nhưng:

- Chủ yếu là unit test cho service, util, controller logic.
- Không nên biến bài này thành kiểm thử giao diện toàn hệ thống.

---

## 5. Công cụ nên dùng cho project này

## 5.1 Java services

Áp dụng cho:

- `doan/account-service`
- `doan/react-flow-be`
- có thể mở rộng cho `doan/api-gateway` nếu còn thời gian

Khuyến nghị:

- `JUnit 5`
- `Mockito`
- `AssertJ` hoặc assertion mặc định của JUnit
- `spring-boot-starter-test`
- `JaCoCo`

Nguyên tắc:

- Ưu tiên unit test dùng mock repository/service phụ thuộc.
- Hạn chế dùng DB thật.
- Chỉ dùng H2 hoặc `@Transactional` khi thực sự cần test nhánh có persistence.

## 5.2 Python service

Áp dụng cho:

- `doan/chat-service`

Khuyến nghị:

- `pytest`
- `unittest.mock` hoặc `pytest-mock`
- `pytest-cov`

Nguyên tắc:

- Mock `requests.post`
- Mock `db.insert_metrics`
- Mock `TokenCounter`
- Không gọi thật tới URL Kaggle/Ngrok

## 5.3 Frontend React/TypeScript

Áp dụng cho:

- `doan/react-flow`

Khuyến nghị:

- `Vitest`
- `jsdom`
- chỉ test `utils`, `api wrapper`, `service logic` thuần

Nguyên tắc:

- Không ưu tiên test component UI nặng nếu deadline ngắn.
- Tập trung vào các file như `schemaUtils.ts`, `websocket.utils.ts`, `nodeHelpers.ts`, `autoLayout.ts`.

---

## 6. Phạm vi test đề xuất

## 6.1 Nên test

### A. `account-service`

Ưu tiên cao:

- `service/AccountService.java`
- `service/RedisTokenService.java`
- `util/JwtUtil.java`
- `util/ValidateUtil.java`
- `util/UsernameGenerator.java`
- `controller/AuthController.java`

Có thể làm thêm nếu còn thời gian:

- `controller/AccountController.java`
- `specification/AccountSpecification.java`
- `service/OAuth2UserService.java`

### B. `react-flow-be`

Ưu tiên cao:

- `service/DiagramManagementService.java`
- `service/CollaborationService.java`
- `service/DatabaseDiagramService.java`
- `service/DiagramListService.java`
- `service/DatabaseDiagramListService.java`
- `service/ModelService.java`
- `service/ConnectionService.java`
- `service/AttributeService.java`
- `service/SchemaVisualizerService.java`

Ưu tiên trung bình:

- `controller/DiagramListController.java`
- `controller/DiagramCollaborationController.java`
- `controller/SchemaVisualizerController.java`

### C. `chat-service`

Ưu tiên cao:

- `app.py`
  - `health_check`
  - `generate`
  - `generate_stream`
  - `set_kaggle_url`
  - `get_kaggle_url`
  - `get_metrics`
  - `get_metric_detail`
- `database.py`
  - `init_database`
  - `insert_metrics`
- `token_counter.py`
  - `count_tokens`
  - `encode`
  - `decode`

### D. `react-flow` frontend

Ưu tiên vừa phải:

- `src/utils/schemaUtils.ts`
- `src/utils/websocket.utils.ts`
- `src/utils/nodeHelpers.ts`
- `src/utils/autoLayout.ts`
- `src/api/diagramApi.ts`
- `src/services/chatbotService.ts`

## 6.2 Không cần unit test trực tiếp hoặc để sau

Các nhóm sau có thể liệt kê vào cột "Do not need testing" kèm lý do:

1. `entity`, `dto`, `model`
   Lý do: chủ yếu là class chứa dữ liệu, ít hoặc không có business logic.

2. `repository`
   Lý do: đây là interface của Spring Data JPA; logic chính do framework xử lý. Nếu cần thì nên kiểm bằng integration test, không phải ưu tiên của bài unit test.

3. `config` thuần
   Lý do: thiên về wiring framework, khó đem lại giá trị unit test cao.

4. `main application` classes
   Lý do: chỉ là bootstrap ứng dụng.

5. React UI component thuần hiển thị
   Lý do: thiên về render/UI snapshot, không phải trọng tâm tốt nhất khi nhóm cần hoàn thành unit testing report.

6. Gọi thật WebSocket, Redis, MySQL, Kaggle API
   Lý do: đây không còn là unit test mà chuyển sang integration test.

---

## 7. Chiến lược làm bài để vừa đúng yêu cầu vừa khả thi

## 7.1 Nguyên tắc chọn test case

Mỗi method nên có test cho ít nhất các nhóm sau:

1. Happy path
   Input hợp lệ, kết quả đúng.

2. Invalid input
   Input thiếu, null, rỗng, sai định dạng.

3. Not found
   Dữ liệu không tồn tại.

4. Business rule violation
   Vi phạm quy tắc nghiệp vụ.

5. Exception path
   Dependency ném lỗi, service xử lý ra sao.

6. State change
   Khi method làm thay đổi dữ liệu, phải assert trạng thái trước và sau.

## 7.2 Ưu tiên test từ dễ đến khó

Thứ tự khuyến nghị:

1. Util / helper methods
2. Service methods có mock dependency
3. Controller methods đơn giản
4. Frontend utils
5. Python app routes
6. Integration-like unit tests có DB giả

---

## 8. Cách ánh xạ Excel với test script

Nhóm bạn của bạn đang làm đúng một phần ở ảnh 2: mỗi dòng Excel gần như tương ứng một test method.

Nhóm bạn nên dùng quy ước:

- `Test Case ID`: `UT_<MODULE>_<LAYER>_<XXX>`
- `File name`: file test chứa method đó
- `Method name`: tên method test thực tế

Ví dụ:

- `UT_ACC_SVC_001`
- File: `AccountServiceTest`
- Method: `createAccount_shouldGenerateUsernameAndDefaultPassword_whenInputValid`

Trong code test, thêm comment:

```java
// Test Case ID: UT_ACC_SVC_001
@Test
void createAccount_shouldGenerateUsernameAndDefaultPassword_whenInputValid() {
    ...
}
```

Ví dụ Python:

```python
def test_generate_returns_200_and_metrics_when_proxy_success(client, mocker):
    # Test Case ID: UT_CHAT_API_001
    ...
```

Ví dụ TypeScript:

```ts
it("returns null when websocket payload is invalid", () => {
  // Test Case ID: UT_FE_UTL_003
});
```

---

## 9. Bộ khung test case đề xuất theo module

## 9.1 `account-service`

### `AccountService`

Nên có test cho:

- tạo account thành công
- tạo account trùng CCCD
- tạo account trùng email
- update account thành công
- update account trùng CCCD/email
- delete account thành công
- delete account khi không tồn tại
- delete account khi đã bị xóa mềm
- `existsByEmail` với email null/rỗng
- `getAccountByUsername` khi không tìm thấy

### `AuthController`

Nên có test cho:

- logout thành công khi có `X-Username`
- logout trả `400` khi thiếu header
- logout trả `500` khi `redisTokenService` ném exception
- response có cookie `jwt` bị clear

### `ValidateUtil`, `UsernameGenerator`, `JwtUtil`

Nên có test cho:

- validate input hợp lệ/không hợp lệ
- clean list có null/trùng/rỗng
- username generation với tên tiếng Việt/chuỗi rỗng/khoảng trắng
- parse JWT hợp lệ/hết hạn/sai chữ ký nếu code hiện tại hỗ trợ

## 9.2 `react-flow-be`

### `DiagramManagementService`

Nên có test cho:

- soft delete thành công khi đúng owner
- soft delete thất bại khi không phải owner
- restore thành công
- permanent delete thất bại nếu diagram chưa ở trash
- `isOwner` trả đúng
- `getDaysUntilAutoDelete` cho diagram chưa xóa, đã hết hạn, còn hạn

### `CollaborationService`

Nên có test cho:

- lấy danh sách collaborator khi diagram tồn tại
- add collaborator thành công
- add collaborator khi user đã tồn tại
- update permission cho participant
- cấm update permission của owner
- remove collaborator thành công
- cấm remove owner
- `hasAccess`, `getOwner`, `countParticipants`, `deactivateCollaboration`

### `SchemaVisualizerService`, `AttributeService`, `ConnectionService`, `ModelService`

Nên có test cho:

- cập nhật position/name/type thành công
- thêm model/attribute thành công
- xóa model/attribute thành công
- tạo foreign key thành công/thất bại
- xử lý không tìm thấy model/attribute

## 9.3 `chat-service`

### `app.py`

Nên có test cho:

- health check trả đúng JSON
- `generate` thành công khi proxy trả response chuẩn
- `generate` timeout trả `504`
- `generate` exception bất kỳ trả `500`
- `generate` luôn gọi `insert_metrics`
- `set_kaggle_url` cập nhật URL thành công
- `get_metrics` trả dữ liệu đúng

### `database.py`

Nên có test cho:

- khởi tạo DB/tạo bảng
- insert metrics với dữ liệu hợp lệ
- xử lý lỗi kết nối

---

## 10. Structure thư mục test nên có

## 10.1 Java

```text
doan/account-service/src/test/java/com/example/accountservice/
  controller/
    AuthControllerTest.java
  service/
    AccountServiceTest.java
    RedisTokenServiceTest.java
  util/
    JwtUtilTest.java
    UsernameGeneratorTest.java
    ValidateUtilTest.java

doan/react-flow-be/src/test/java/com/example/react_flow_be/
  controller/
    DiagramListControllerTest.java
    DiagramCollaborationControllerTest.java
  service/
    DiagramManagementServiceTest.java
    CollaborationServiceTest.java
    DatabaseDiagramServiceTest.java
    SchemaVisualizerServiceTest.java
    AttributeServiceTest.java
    ConnectionServiceTest.java
    ModelServiceTest.java
```

## 10.2 Python

```text
doan/chat-service/tests/
  test_app.py
  test_database.py
  test_token_counter.py
```

## 10.3 Frontend

```text
doan/react-flow/src/utils/__tests__/
  schemaUtils.test.ts
  websocket.utils.test.ts
  nodeHelpers.test.ts
  autoLayout.test.ts
```

---

## 11. Quy trình làm bài chi tiết cho nhóm

## Bước 1. Chốt scope

Nhóm thống nhất:

- ưu tiên 2 service Java là chính
- thêm chat-service
- frontend utils là phần mở rộng nếu còn thời gian

Không dàn trải toàn bộ tất cả file.

## Bước 2. Lập danh sách file sẽ test và file không test

Tạo 2 bảng:

1. `Tested files`
2. `Not tested files + reason`

Đây là nội dung để điền mục `1.2 Scope of Testing` trong Excel.

## Bước 3. Viết test case trên Excel trước

Làm theo mẫu ảnh 2:

- mỗi dòng là một test case
- nhóm theo module/file/class
- có mã test case rõ ràng

Mẹo:

- chỉ cần viết test case cho những method thực sự sẽ được code test
- không viết dư quá nhiều rồi không kịp code

## Bước 4. Viết test script thật

Nguyên tắc:

- một test case trong Excel phải tìm thấy test method tương ứng trong code
- tên method test mô tả rõ điều kiện và kỳ vọng
- comment `Test Case ID` ngay trên test method

## Bước 5. Chạy test

Ví dụ lệnh:

```bash
cd doan/account-service
./mvnw test
```

```bash
cd doan/react-flow-be
./mvnw test
```

```bash
cd doan/chat-service
pytest -q
```

Nếu nhóm làm frontend:

```bash
cd doan/react-flow
npm test
```

## Bước 6. Chạy coverage

Ví dụ:

- Java: cấu hình JaCoCo rồi chạy Maven test
- Python: `pytest --cov=.`
- Frontend: `vitest --coverage`

## Bước 7. Chụp bằng chứng

Cần chụp:

- terminal test pass/fail
- report coverage
- nếu có HTML report thì chụp màn hình tổng coverage

## Bước 8. Hoàn thiện Excel report

Điền các mục:

- tools/libraries
- scope
- test cases
- project link
- execution report
- coverage report
- prompts used nếu nhóm có dùng AI hỗ trợ

---

## 12. Cách xử lý yêu cầu DB change và rollback

Giảng viên ghi rõ:

- nếu source code làm thay đổi DB thì phải verify DB thay đổi đúng
- sau test DB phải trở lại trạng thái ban đầu

Nhóm nên xử lý như sau:

### Cách 1. Ưu tiên nhất: mock repository

Đây là cách đúng tinh thần unit test nhất:

- không đụng DB thật
- không cần rollback thật
- chỉ verify `save`, `delete`, dữ liệu truyền vào repository

### Cách 2. Nếu cần DB test

Chỉ dùng khi thật sự cần:

- H2 test database
- `@Transactional`
- reset data ở `@BeforeEach` / `@AfterEach`

### Không nên

- test trực tiếp với MySQL production/dev
- dùng Redis/Kafka/Kaggle thật trong unit test

---

## 13. Chiến lược coverage thực tế

Không nên đặt mục tiêu bao phủ toàn bộ source code.

Mục tiêu hợp lý:

- `account-service`: 65-80% cho các class được chọn test
- `react-flow-be`: 55-75% cho các service chính
- `chat-service`: 70%+
- frontend utils: 60%+ nếu làm

Quan trọng hơn coverage tuyệt đối là:

- test đúng logic nghiệp vụ
- có cả pass và nhánh lỗi
- giải thích được vì sao chọn phạm vi đó

---

## 14. Đề xuất format sheet Excel

Nhóm có thể làm theo cấu trúc:

### Sheet 1. `Overview`

- Project name
- Team members
- Tools and libraries
- Scope summary

### Sheet 2. `Test Cases`

Cột đề xuất:

- `Test Case ID`
- `Module`
- `File name`
- `Method name`
- `Purpose`
- `Input`
- `Expected output`
- `Actual output`
- `Test Result`
- `Note`
- `Author`

### Sheet 3. `Execution Report`

- tổng số test
- số pass
- số fail
- số blocked
- ảnh minh chứng

### Sheet 4. `Coverage Report`

- module
- tool coverage
- line coverage
- branch coverage nếu có
- screenshot/link report

---

## 15. Đề xuất naming convention cho nhóm

### Test Case ID

- `UT_ACC_SVC_001`
- `UT_ACC_CTL_001`
- `UT_DGM_SVC_001`
- `UT_COL_SVC_001`
- `UT_CHAT_API_001`
- `UT_FE_UTL_001`

### Tên method test

Mẫu tốt:

- `createAccount_shouldThrowException_whenEmailAlreadyExists`
- `logout_shouldReturnBadRequest_whenUsernameHeaderMissing`
- `softDeleteDiagram_shouldSucceed_whenRequesterIsOwner`
- `generate_shouldReturn504_whenProxyTimeout`

Mẫu kém:

- `test1`
- `testCreate`
- `checkLogic`

---

## 16. Kế hoạch thực hiện trong 5 buổi

### Buổi 1

- scan code
- chốt module test
- chia việc
- tạo Excel skeleton

### Buổi 2

- viết test case trên Excel
- setup framework test còn thiếu

### Buổi 3

- code test cho `account-service`
- code test cho `react-flow-be`

### Buổi 4

- code test cho `chat-service`
- code test frontend utils nếu còn thời gian
- chạy coverage

### Buổi 5

- fix test lỗi
- chụp screenshot
- điền execution report
- rà soát project link và comment `Test Case ID`

---

## 17. Chốt hướng làm bài cho nhóm bạn

Nếu mục tiêu là hoàn thành tốt, đúng yêu cầu và khả thi trong thời gian ngắn, nhóm nên làm theo thứ tự sau:

1. Làm thật chắc `account-service`
2. Làm tiếp `react-flow-be` ở các service chính
3. Làm `chat-service`
4. Chỉ làm frontend utils nếu còn thời gian

Đừng bắt đầu từ UI component, WebSocket end-to-end, Docker integration, hay gọi AI thật. Những phần đó tốn thời gian nhưng không hiệu quả cho bài Unit Test.

---

## 18. Deliverable cuối cùng nhóm phải có

1. File Excel report hoàn chỉnh
2. Source code test scripts trong repo
3. Ảnh execution report
4. Ảnh coverage report
5. Link GitHub repo
6. Danh sách prompts AI đã dùng nếu giảng viên yêu cầu

Nếu một trong các mục trên thiếu, bài làm sẽ bị khuyết phần.
