# Tổng quan Dự án

Tài liệu này cung cấp một cái nhìn tổng quan, chuyên sâu về toàn bộ kiến trúc, công nghệ và luồng hoạt động của Đồ án Tốt nghiệp (sử dụng để phục vụ môn Kiểm thử chất lượng Phần mềm - SQA).

---

## 1. Giới thiệu chung
Dự án là một hệ thống thiết kế cơ sở dữ liệu trực quan (Visual Database Design Tool) tích hợp Chatbot AI (thông qua mô hình triển khai trên Kaggle/Ngrok proxy) để hỗ trợ người dùng. Hệ thống cho phép:
- Quản lý tài khoản và định danh (OAuth2/JWT).
- Vẽ và thiết kế sơ đồ cơ sở dữ liệu trực quan (React Flow).
- Thao tác real-time với WebSocket.
- Tương tác với AI Chatbot để phân tích, tạo lịch sử và hỗ trợ truy vấn cấu trúc Database.

Hệ thống được thiết kế theo kiến trúc **Microservices**, đóng gói toàn bộ qua **Docker** và **Docker Compose**, giúp dễ dàng triển khai (one-click deployment), độc lập môi trường.

---

## 2. Kiến trúc Hệ thống (Microservices Architecture)
Hệ thống bao gồm các thành phần chính:
1. **Frontend (react-flow)**: Cung cấp giao diện mạng lưới đồ họa cho người dùng thao tác.
2. **API Gateway (api-gateway)**: Cổng giao tiếp duy nhất cho toàn bộ hệ thống từ external client. Reverse Proxy, định tuyến request và tiền xử lý Security/CORS.
3. **Service Registry (eureka-server)**: Quản lý định tuyến nội bộ, phát hiện các service đang chạy (Service Discovery).
4. **Account Service (account-service)**: Quản lý nghiệp vụ người dùng, xác thực (Authentication) và phân quyền (Authorization).
5. **Visualizer Backend (react-flow-be)**: Dịch vụ Core quản lý đồ án vẽ diagram, cung cấp API và quản lý kết nối WebSocket cho real-time collaboration.
6. **Chat Service (chat-service)**: Dịch vụ AI Proxy (Python) kết nối mô hình LLM bên ngoài (thông qua Kaggle/Ngrok), phục vụ hỏi-đáp cấu trúc cơ sở dữ liệu.

---

## 3. Technology Stack (Công nghệ sử dụng)

### Frontend
- **Framework**: React 18, Vite, TypeScript
- **UI & Graphics**: React Flow (quản lý canvas vẽ), Chakra UI (Components)
- **State & Connection**: StompJS/SockJS (cho WebSockets), Axios/Fetch
- **Khác**: Framer Motion (Animations), Recharts

### Backend (Java Services)
- **Framework Core**: Java 21, Spring Boot 3.4.7, Spring Cloud 2024.0.1
- **Gateway**: Spring Cloud Gateway, WebFlux
- **Discovery**: Netflix Eureka Client/Server
- **Security**: Spring Security, OAuth2, JWT (io.jsonwebtoken)
- **Database Access**: Spring Data JPA

### Backend (Python Service - Chatbot Proxy)
- **Framework**: Python 3.10, Flask
- **Tính năng**: Tracking log, tính toán Token (Token counter), Proxy forward request tới host LLM.

### Infrastructure & Database
- **RDBMS Database**: MySQL 8.0
- **Cache / Message Broker**: Redis 7 (Alpine)
- **Message Queue**: Apache Kafka (trong cấu hình account-service)
- **Containerization**: Docker, Docker Compose

---

## 4. Chi tiết cấu hình Ports & Network

Dự án sử dụng Docker Bridge Network `diagram-network` cho các services giao tiếp nội bộ thông qua container hostnames. Dưới đây là bảng ánh xạ Port từ Container ra Host:

| Service Tên | Loại | Container Host | Cổng ngoài (Host) | Cổng trong (Container) | Ghi chú |
| :--- | :--- | :--- | :---: | :---: | :--- |
| **API Gateway** | Cổng giao tiếp | `api-gateway` | **8080** | 8080 | Entrypoint chính cho UI gọi vào BE |
| **Frontend** | UI Web | `react-flow` | **5173** | 5173 | Giao diện hiển thị với người dùng |
| **Eureka Server** | Registry | `eureka-server` | **8761** | 8761 | UI xem trạng thái các Microservice |
| **Account Service** | Backend | `account-service` | **8082** | 8082 | Quản lý User |
| **React Flow BE** | Backend | `react-flow-be` | **8085** | 8085 | Thao tác Diagram + WebSocket |
| **Chat Service** | Proxy AI | `chat-service` | **8000** | 8000 | Xử lý request NLP và tính Tokens |
| **Redis** | Cache | `redis-shared` | **6379** | 6379 | |
| **MySQL** | RDBMS | `mysql-diagram` | **3308** | 3306 | Kết nối từ ngoài host cần dùng 3308 |

---

## 5. Chi tiết Databases & Schemas
Mặc dù hệ thống dùng 1 container `mysql-diagram`, nhưng dữ liệu được phân chia theo schema logic riêng rẽ, bám sát tính chất Microservices (Data Decentralization):

1. **`schema_visualizer`**: Được quản lý bởi `react-flow-be`. Chứa các bảng quản lý diagrams, project graph, nodes, edges mà người dùng lưu trữ.
2. **`schema_account`**: Được quản lý bởi `account-service`. Cung cấp kho trữ dữ liệu user, roles, thông tin xác thực OAuth2/JWT.
3. **`schema_chatbot`** (hoặc tạo log metrics thông qua chat-service): Chứa bảng `chat_metrics` lưu trữ logs tương tác giữa user và AI, số token đầu vào/đầu ra, thời gian response.

---

## 6. Luồng định tuyến (Routing Flow) tại API Gateway
Gateway (port 8080) quy định cách request từ Frontend được chuyển hướng đến đúng Backend.
- Request Path có dạng `/oauth2/**`, `/login/oauth2/**`, hoặc `/account/**` ➔ Forward tới **Account Service** (lb://account-service).
- Request Path có dạng `/ws/**` (WebSocket) ➔ Forward tới **React Flow BE** (lb:ws://react-flow-be).
- Request Path có dạng `/api/**` ➔ Forward tới **React Flow BE** (lb://react-flow-be).
- Request Docs (`/v3/api-docs`...) ➔ Forward nội bộ tương ứng.
- **Tất cả các requests không thuộc các chuẩn trên (`/**`)** ➔ Forward mặc định tới **Chat Service** Proxy (http://chat-service:8000).

---

## 7. Luồng hoạt động (Operational Flow) tiêu biểu

### A. Luồng Đăng nhập (Authentication)
1. Frontend gọi `/account/login` (hoặc OAuth2).
2. Request chạm API Gateway (8080), Gateway dựa vào routing `Path=/account/**` đẩy dữ liệu xuống Account Service (8082).
3. Account Service kiểm tra `schema_account` trong MySQL. Nếu hợp lệ, dịch vụ sinh ra JWT Token, có thể lưu session vào Redis.
4. Token được trả qua Gateway về Frontend để sử dụng cho các request sau.

### B. Luồng Thiết kế Diagram & Collaboration (Real-time)
1. Frontend load canvas của dự án.
2. Mở kết nối WebSocket (StompJS) tới `/ws/**`. Gateway chuyển proxy là `lb:ws://react-flow-be:8085`.
3. Khi người dùng di chuyển node, frontend emit event qua WebSocket. 
4. `react-flow-be` nhận event, cập nhật trạng thái xuống `schema_visualizer` và broadcast sự thay đổi tới các client đang cùng xem chung dự án, đảm bảo Real-time Collaboration.

### C. Luồng Chatbot (Hỏi đáp Diagram với AI)
1. Người dùng gõ text hỏi AI về sơ đồ trong giao diện. 
2. Payload gồm toàn bộ cấu trúc Graph hiện tại (JSON) và câu hỏi được gửi tới API Gateway.
3. Vì path của AI không nằm trong các path khai báo (/api, /account) nên Gateway catch-all (`/**`) và đẩy sang Chat Service (Python).
4. Chat Service (`app.py`):
   - Nhận payload, gom lại thành Prompts.
   - Thống kê (Tokenize) số input tokens.
   - Forward HTTP requests sang external host ở ngoài (Kaggle URL expose qua Ngrok).
   - Đón kết quả, thống kê số output tokens và đo thời gian xử lý (Total time, TTFT).
   - Ghi lại lịch sử, metrics vào database (metric tracking table).
   - Proxy trả kết quả cuối cùng lại Frontend.

---

## 8. Hướng dẫn khởi động dự án
Vì toàn bộ hệ thống đã được container hóa hoàn chỉnh:
1. Yêu cầu duy nhất trên máy host là cài đặt **Docker** và **Docker Compose**.
2. **Build và Khởi chạy**:
   - Di chuyển vào thư mục `doan/infrastructure/`
   - Run commands:
     ```bash
     docker-compose build
     docker-compose up -d
     ```
3. Sau khi tất cả dịch vụ báo `Up` (đặc biệt sau khi mysql-diagram khởi tạo xong):
   - Truy cập giao diện chính: http://localhost:5173
   - Truy cập trạng thái Eureka: http://localhost:8761/
   - API Docs có thể truy cập qua Gateway (Swagger): http://localhost:8080/swagger-ui.html

---
