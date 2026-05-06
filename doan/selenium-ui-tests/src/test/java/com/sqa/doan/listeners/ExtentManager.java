package com.sqa.doan.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {
    private static ExtentReports extent;

    public static ExtentReports createInstance() {
        if (extent == null) {
            String fileName = System.getProperty("user.dir") + "/test-output/ExtentReport.html";
            ExtentSparkReporter htmlReporter = new ExtentSparkReporter(fileName);
            htmlReporter.config().setTheme(Theme.STANDARD);
            htmlReporter.config().setDocumentTitle("ReactFlow UI Automation Report");
            htmlReporter.config().setEncoding("utf-8");
            htmlReporter.config().setReportName("Selenium UI Test Results");

            extent = new ExtentReports();
            extent.attachReporter(htmlReporter);
            extent.setSystemInfo("Automation Tester", "Duc Anh");
            extent.setSystemInfo("Organization", "Test_SQA_DATN");
            extent.setSystemInfo("Build no", "1.0");
        }
        return extent;
    }
}
