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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;

/**
 * Tác giả: SQA Team - FR-06 System Administration 
 * Test Suite này sử dụng MockMvc để kiểm tra tầng Network (REST API Controller) 
 * và đảm bảo luồng phân quyền Role + Thống kê hoạt động chính xác.
 */
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@AutoConfigureMockMvc // Công cụ tối thượng để giả lập HTTP Request
@Transactional // Rollback mượt mà sau khi query Database
@ActiveProfiles("test")
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository; // Bắt buộc Inject Repo thật để [CheckDB]

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Arrange: Nhồi trước một lượng Data tĩnh vào DB h2 để làm vốn Thống kê
        Account adminAccount = new Account();
        adminAccount.setUsername("admin1");
        adminAccount.setPassword("123456");
        adminAccount.setEmail("admin1@fpt.edu.vn");
        adminAccount.setCccd("111111111");
        adminAccount.setRole(Role.ADMIN);
        adminAccount.setVisible(1);

        Account studentAccount1 = new Account();
        studentAccount1.setUsername("student1");
        studentAccount1.setPassword("123456");
        studentAccount1.setEmail("student1@fpt.edu.vn");
        studentAccount1.setCccd("222222222");
        studentAccount1.setRole(Role.STUDENT);
        studentAccount1.setVisible(1);

        Account studentAccount2 = new Account();
        studentAccount2.setUsername("student2");
        studentAccount2.setPassword("123456");
        studentAccount2.setEmail("student2@fpt.edu.vn");
        studentAccount2.setCccd("333333333");
        studentAccount2.setRole(Role.STUDENT);
        studentAccount2.setVisible(1);

        accountRepository.save(adminAccount);
        accountRepository.save(studentAccount1);
        accountRepository.save(studentAccount2);
    }

    @Test
    @DisplayName("UT_AS_019: [EP] Verify Admin's ability to view aggregate dashboard accounts without filters.")
    void testSearchAccounts_AsAdmin_NoFilter_ReturnsAll() throws Exception {
        long totalAccounts = accountRepository.count();
        assertEquals(3, totalAccounts, "Phải có đúng 3 tài khoản khởi tạo");

        mockMvc.perform(get("/account/search")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3)) 
                .andExpect(jsonPath("$.content").isArray()); 
    }

    @Test
    @DisplayName("UT_AS_020: [EP] Verify Admin fetching specific role stats (e.g., count total STUDENTs).")
    void testSearchAccounts_AsAdmin_FilterByRoleStudent_ReturnsOnlyStudents() throws Exception {
        mockMvc.perform(get("/account/search")
                .param("role", "STUDENT")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].role").value("STUDENT"));
    }

    @Test
    @DisplayName("UT_AS_021: [BVA] Check authorization walls blocking unauthorized calls to Admin API.")
    void testSearchAccounts_AsStudent_AccessDenied_Throws403() throws Exception {
        mockMvc.perform(get("/account/search")
                .header("X-User-Role", "STUDENT"))
                .andExpect(status().isForbidden());
    }

    // =====================================================
    //  BỔ SUNG TEST ĐỂ NÂNG CAO COVERAGE (UT_AS_022 -> UT_AS_031)
    // =====================================================

    @Test
    @DisplayName("UT_AS_022: Admin lấy thông tin qua username")
    void testGetAccountByUsername() throws Exception {
        mockMvc.perform(get("/account/student1")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student1"));
    }

    @Test
    @DisplayName("UT_AS_023: Lấy thông tin chính mình (me) qua header")
    void testGetAccountByMe() throws Exception {
        mockMvc.perform(get("/account/me")
                .header("X-Username", "student2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student2"));
    }

    @Test
    @DisplayName("UT_AS_024: Admin xóa mềm tài khoản")
    void testDeleteAccount() throws Exception {
        Account temp = new Account();
        temp.setUsername("tobedeleted");
        temp.setCccd("999999999");
        temp.setVisible(1);
        temp = accountRepository.save(temp);

        mockMvc.perform(delete("/account/" + temp.getId())
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("Account Deleted Successfully!"));
        
        Account deleted = accountRepository.findById(temp.getId()).get();
        assertEquals(0, deleted.getVisible());
    }

    @Test
    @DisplayName("UT_AS_025: Admin tạo tài khoản mới")
    void testCreateAccount() throws Exception {
        String json = "{\"firstName\":\"New\",\"lastName\":\"User\",\"cccd\":\"1122334455\"}";
        
        mockMvc.perform(post("/account")
                .header("X-User-Role", "ADMIN")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    @DisplayName("UT_AS_026: Admin cập nhật tài khoản")
    void testUpdateAccount() throws Exception {
        Account acc = accountRepository.findByUsernameAndVisible("student1", 1).get();
        acc.setFirstName("UpdatedName");
        
        mockMvc.perform(put("/account/" + acc.getId())
                .header("X-User-Role", "ADMIN")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(acc)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"));
    }

    @Test
    @DisplayName("UT_AS_027: Lấy bulk tài khoản qua danh sách ID")
    void testGetAccountsByIds() throws Exception {
        Account a1 = accountRepository.findByUsernameAndVisible("student1", 1).get();
        Account a2 = accountRepository.findByUsernameAndVisible("student2", 1).get();
        String ids = a1.getId() + "," + a2.getId();
        
        mockMvc.perform(get("/account/bulk")
                .param("ids", ids)
                .header("X-User-Role", "TEACHER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("UT_AS_028: Bulk lấy danh sách ID rỗng")
    void testGetAccountsByIds_Empty() throws Exception {
        mockMvc.perform(get("/account/bulk")
                .param("ids", "")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("UT_AS_029: Tìm giáo viên qua CCCD")
    void testGetTeacherByCccd() throws Exception {
        Account teacher = new Account();
        teacher.setUsername("teacher1");
        teacher.setCccd("555555555");
        teacher.setRole(Role.TEACHER);
        teacher.setVisible(1);
        accountRepository.save(teacher);

        mockMvc.perform(get("/account/teacher/555555555")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("teacher1"));
    }

    @Test
    @DisplayName("UT_AS_030: Tìm tài khoản qua email")
    void testGetAccountByEmail() throws Exception {
        mockMvc.perform(get("/account/email/student1@fpt.edu.vn")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("student1"));
    }

    @Test
    @DisplayName("UT_AS_031: Tìm tài khoản qua CCCD - Không thấy (404)")
    void testGetAccountByCccd_NotFound() throws Exception {
        mockMvc.perform(get("/account/student/000000000")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isNotFound());
    }
}
