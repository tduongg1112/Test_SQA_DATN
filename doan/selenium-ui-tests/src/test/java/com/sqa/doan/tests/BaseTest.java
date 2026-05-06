package com.sqa.doan.tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.time.Duration;

public class BaseTest {
    protected WebDriver driver;
    // Điền URL ứng dụng ReactJS chạy dưới local của bạn vào đây
    protected final String BASE_URL = "http://localhost:5173";

    @BeforeClass
    public void setUp() {
        // Tự động setup ChromeDriver tương thích với Chrome hiện tại
        WebDriverManager.chromedriver().setup();
        
        ChromeOptions options = new ChromeOptions();
        // Bỏ comment nếu muốn chạy không hiện giao diện
        // options.addArguments("--headless"); 
        options.addArguments("--start-maximized");
        options.addArguments("--remote-allow-origins=*");
        
        // --- BYPASS GOOGLE BOT DETECTION ---
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--disable-blink-features=AutomationControlled");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        driver.get(BASE_URL);
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
