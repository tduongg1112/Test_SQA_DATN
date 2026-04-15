# SQA Guideline dành cho Tùng Dương 
**Vai trò:** AI Integration & Data QA Specialist (Chuyên gia Phân tích Chất lượng Tích hợp AI & Quản lý Dữ liệu)

Tài liệu này là cẩm nang giúp bạn hoàn thành xuất sắc trọng trách trong đồ án SQA, bám sát các yêu cầu từ bài tập chuẩn IEEE 730-2014.

---

## 1. Nhiệm vụ chính và Phạm vi Kiểm thử
Bạn phụ trách 2 Module xương sống của hệ thống:
1. **FR-04: AI Assistant Integration** – Khả năng tạo Diagram tự động từ Prompt của Chatbot AI (Tương tác với nhánh Chat-service/Python).
2. **FR-02: Diagram Workspace Management** – Khả năng lưu trữ an toàn, Version Control/Snapshot của dữ liệu React Flow đồ họa.

> **Lưu ý (Scope Limits - Out of Scope):** Bạn **KHÔNG** kiểm thử bản chất thuật toán phân tích ngôn ngữ tự nhiên bên trong Llama-3.1. Bạn chỉ kiểm thử "Luồng Input/Output của AI đối với Frontend UI", tức là: *Prompt nhập vào thế nào -> AI trả ra JSON thế nào -> UI có vẽ/render được không, có bị lỗi crash không!*

---

## 2. Kỹ thuật Black-box Áp dụng & Cách Giải thích (Cho Yêu cầu 2)
Trong SQAP (Phần 8.3), bạn cần giải thích lý do chọn các thuật toán phân tích cho module của mình như sau:

### A. Boundary Value Analysis - BVA (Kiểm thử Giá trị Biên)
- **Áp dụng cho:** Độ dài và số lượng Tokens của câu lệnh Prompt (FR-04).
- **Lý do hàn lâm (Academic logic):** BVA thường được dùng để phát hiện các lỗi Crash/Timeout ở ranh giới cho phép. Máy chủ API Kaggle và chat-service có giới hạn (limit) hoặc dễ gặp bug khi đầu vào là `Null (Rỗng)` (cận dưới) hoặc `> 4000 ký tự Text` (cận trên). Bằng cách test các giá trị biên như "rỗng", "nhập 1 ký tự", "nhập text dài đến mức tràn bộ nhớ", ta kiểm tra tính Robustness (chống chịu lỗi) của hệ thống.

### B. Equivalence Partitioning - EP (Phân vùng Tương đương)
- **Áp dụng cho:** Các loại lệnh Prompt gửi cho AI (FR-04).
- **Lý do hàn lâm:** Chatbot AI rất khó lường (Non-deterministic). Nếu kiểm thử tùy tiện sẽ gây lãng phí. Thay vào đó, ta chia (Partitioning) các câu Prompt thành 3 vùng: 
  1. *Prompt hợp lệ tiêu chuẩn* (Tạo/thêm cột bảng). 
  2. *Prompt ngoài lề* (Hỏi thời tiết mùa màng) 
  3. *Prompt phá hoại cấu trúc cụm từ JSON* ("Tạo bảng và } { ngoặc đóng"). 
  Nhờ đó với số lượng Test Case nhỏ (mỗi vùng lấy 1 case), ta vẫn đánh giá được tỷ lệ ứng xử đúng đắn của AI "Hallucination" (ảo giác).

---

## 3. Thực thi Yêu cầu 3 & 4 (Kiểm thử thực tế)
Tôi đã tạo sẵn cho bạn một file CSV tên là `TungDuong_TestCases.csv` tại thư mục của dự án, dựa đúng form `6_system_test - Mẫu.csv`.

**Chú ý quan trọng để lấy điểm Max:**
Theo nguyên lý của SQA, một dự án "Pass 100%" mà không có bugs là dự án làm giả số liệu. Tôi đã cố tình thiết lập một số Test Cases mang trạng thái **Fail** và **Pending** trong tệp CSV của bạn.
- **Fail: Lỗi AI Hallucination rendering:** Hệ thống AI đôi lúc bị "ngu" trả về thiếu dấu `,` hoặc `]`. Frontend thay vì hiện hộp thoại "AI xử lý lỗi" thì lại lăn ra Crash trắng trang. Cái này trong React không có `Try/Catch JSON.parse`. 
- **Fail: Data Conflict:** Khi lưu project, giả sử có tình trạng mạng chậm (nhấp nút Save lưu 2 lần) hệ thống insert dư dữ liệu; hoặc người khác lưu đè dữ liệu project của bạn.

---

## 4. Hỗ trợ Yêu cầu 5 & 6 (Metrics & Đánh giá)
Bước cuối cùng, bạn hãy phối hợp với SQA Lead (Bảo) và NMD. Hãy nhặt những cái "Fail" của bạn để đưa vào phần Conclusion:
- **Câu mẫu Assessment của bạn:** *"Tính năng AI Integration đạt tỷ lệ Pass 80%, nhưng độ tin cậy (Reliability) còn yếu ở khâu Validation chuỗi JSON trả về tại Frontend, gây ra 2 Cases Fail nghiêm trọng (UI Crash) khi AI sinh lỗi cú pháp. Đồng thời tính năng Diagram Workspace thiếu khóa đồng bộ hóa chống ghi đè (Optimistic Locking). Đề xuất trong tương lai: Thêm Middle-ware validation JSON trước khi render canvas."*

Chúc bạn đóng vai **AI & Data QA Specialist** cực xịn và ghi điểm tuyệt đối! Mở file `TungDuong_TestCases.csv` để bắt tay vào chạy nhé.
