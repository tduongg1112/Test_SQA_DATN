package com.example.accountservice.controller;

import com.example.accountservice.enums.Role;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tác giả: SQA Team - FR-06 System Administration 
 * Test Suite này sử dụng MockMvc để kiểm tra tầng Network (REST API Controller) 
 * và đảm bảo luồng phân quyền Role + Thống kê hoạt động chính xác.
 */
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc // Công cụ tối thượng để giả lập HTTP Request
@Transactional        // Rollback mượt mà sau khi query Database
@ActiveProfiles("test")
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository; // Bắt buộc Inject Repo thật để [CheckDB]

    @BeforeEach
    void setUp() {
        // Arrange: Nhồi trước một lượng Data tĩnh vào DB h2 để làm vốn Thống kê
        Account adminAccount = new Account();
        adminAccount.setUsername("admin1");
        adminAccount.setPassword("123456");
        adminAccount.setEmail("admin1@fpt.edu.vn");
        adminAccount.setRole(Role.ADMIN);
        adminAccount.setVisible(1);
        
        Account studentAccount1 = new Account();
        studentAccount1.setUsername("student1");
        studentAccount1.setPassword("123456");
        studentAccount1.setEmail("student1@fpt.edu.vn");
        studentAccount1.setRole(Role.STUDENT);
        studentAccount1.setVisible(1);

        Account studentAccount2 = new Account();
        studentAccount2.setUsername("student2");
        studentAccount2.setPassword("123456");
        studentAccount2.setEmail("student2@fpt.edu.vn");
        studentAccount2.setRole(Role.STUDENT);
        studentAccount2.setVisible(1);

        accountRepository.save(adminAccount);
        accountRepository.save(studentAccount1);
        accountRepository.save(studentAccount2);
    }

    @Test
    @DisplayName("UT_FR06_008: Admin truy xuất Dashboard tổng quan - Không Filter")
    void testSearchAccounts_AsAdmin_NoFilter_ReturnsAll() throws Exception {
        long totalAccounts = accountRepository.count();
        assertEquals(3, totalAccounts, "Phải có đúng 3 tài khoản khởi tạo");

        // Act & Assert Http API Request
        // Giả lập Admin Header: X-Role chứa danh phận, token tuỳ thuộc kiến trúc auth thực tế
        mockMvc.perform(get("/account/search")
                .header("X-User-Role", "ADMIN"))
                
                // Kỳ vọng hệ thống trả về 200 OK và mang JSON Data về Dashboard
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3)) // Trang báo tổng 3 Object
                .andExpect(jsonPath("$.content").isArray());     // Trả về tập danh sách
    }

    @Test
    @DisplayName("UT_FR06_009: Admin truy xuất Dashboard - Thống kê khoanh vùng Tỷ lệ Sinh Viên")
    void testSearchAccounts_AsAdmin_FilterByRoleStudent_ReturnsOnlyStudents() throws Exception {
        // Act & Assert : Bắn API ghép Param '?role=STUDENT'
        mockMvc.perform(get("/account/search")
                .param("role", "STUDENT")
                .header("X-User-Role", "ADMIN"))
                
                .andExpect(status().isOk())
                // DB có 2 Học Sinh đã khởi tạo -> Kết quả thống kê (totalElements) phải bằng đúng 2.
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].role").value("STUDENT"));
    }

    @Test
    @DisplayName("UT_FR06_010: Student cố tình gọi API chặn phân quyền của Dashboard System")
    void testSearchAccounts_AsStudent_AccessDenied_Throws403() throws Exception {
        // Act & Assert (Vét Nhánh Role Cấm)
        mockMvc.perform(get("/account/search")
                // Gắn mác mình là STUDENT đang đòi vào trang của Admin
                .header("X-User-Role", "STUDENT"))
                
                // Kì vọng: Trả bề đúng lỗi 403 Forbidden AccessDenied (Thay vì Error 500 nổ Server).
                .andExpect(status().isForbidden());
    }
}
