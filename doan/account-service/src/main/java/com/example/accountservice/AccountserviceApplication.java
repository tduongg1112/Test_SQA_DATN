package com.example.accountservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.annotation.Order;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableAsync;

import com.example.accountservice.dto.AccountDTO;
import com.example.accountservice.enums.Role;
import com.example.accountservice.model.Account;
import com.example.accountservice.service.AccountService;

import lombok.extern.slf4j.Slf4j;

@Order(1)
@SpringBootApplication
@Slf4j
@EnableAsync
public class AccountserviceApplication implements CommandLineRunner {

	@Autowired
	private AccountService accountService;

	// @Autowired
	// private KafkaTemplate<String, AccountDTO> kafkaTemplate;

	public static void main(String[] args) {
		SpringApplication.run(AccountserviceApplication.class, args);
	}

	@Autowired
	private org.springframework.core.env.Environment env;

	@Override
	public void run(String... args) throws Exception {
		// Bỏ qua việc tạo Admin ảo nếu đang chạy ở môi trường Test
		if (java.util.Arrays.asList(env.getActiveProfiles()).contains("test")) {
			return;
		}

		System.out.println("first");
		// ✅ Tạo account admin mặc định
		if (!accountService.existsByCccd("123456789012")) {
			Account admin = new Account();
			admin.setFirstName("Hiệp");
			admin.setLastName("Nguyễn Hoàng");
			admin.setCccd("123456789012");
			admin.setRole(Role.ADMIN);

			Account saved = accountService.createAccount(admin);
			log.info("Admin created: {} - password: 123456Aa@", saved.getUsername());
		}
	}
}
