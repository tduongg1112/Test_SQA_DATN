package com.sqa.doan.listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {
    private static ExtentReports extent = ExtentManager.createInstance();
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Test Suite started!");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("Test Suite is ending!");
        extent.flush();
    }

    @Override
    public void onTestStart(ITestResult result) {
        System.out.println("Started test: " + result.getMethod().getMethodName());
        ExtentTest test = extent.createTest(result.getTestClass().getName() + " :: " + result.getMethod().getMethodName());
        extentTest.set(test);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        System.out.println("Passed test: " + result.getMethod().getMethodName());
        extentTest.get().log(Status.PASS, MarkupHelper.createLabel(result.getMethod().getMethodName() + " PASSED", ExtentColor.GREEN));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("Failed test: " + result.getMethod().getMethodName());
        extentTest.get().log(Status.FAIL, MarkupHelper.createLabel(result.getMethod().getMethodName() + " FAILED", ExtentColor.RED));
        extentTest.get().fail(result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        System.out.println("Skipped test: " + result.getMethod().getMethodName());
        extentTest.get().log(Status.SKIP, MarkupHelper.createLabel(result.getMethod().getMethodName() + " SKIPPED", ExtentColor.ORANGE));
        extentTest.get().skip(result.getThrowable());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        // Not implemented
    }
}
