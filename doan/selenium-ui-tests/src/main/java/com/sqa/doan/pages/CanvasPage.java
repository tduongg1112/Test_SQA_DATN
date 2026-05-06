package com.sqa.doan.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class CanvasPage extends BasePage {

    // =========== LOCATORS ===========

    private final By BTN_ADD_TABLE = By.cssSelector("[aria-label='Add Table']");
    private final By BTN_EXPORT_JSON = By.cssSelector("[aria-label='Export JSON']");
    private final By BTN_EXPORT_PNG = By.cssSelector("[aria-label='Export Image'], [aria-label='Export PNG']");
    
    private final By ALL_MODEL_NODES = By.cssSelector(".react-flow__node-model");
    private final By TABLE_NAME_DISPLAY = By.cssSelector("[title='Double click to edit']");
    private final By TABLE_NAME_EDITING_INPUT = By.cssSelector(".react-flow__node-model input");
    
    private final By ALL_ADD_ATTR_BTNS = By.cssSelector("[aria-label='Add attribute']");
    private final By BTN_DELETE_TABLE = By.cssSelector("[aria-label='Delete table']");
    
    private final By REACT_FLOW_CANVAS = By.cssSelector(".react-flow__pane");
    private final By ZOOM_CONTROLS = By.cssSelector(".react-flow__controls");

    // Attribute locators (assumed based on standard react-flow/chakra usage, will be generic enough)
    private final By ATTRIBUTE_ROW = By.cssSelector(".attribute-row, [role='group']"); 
    private final By ATTR_DELETE_BTN = By.cssSelector("[aria-label='Delete attribute'], .delete-attr-btn");
    private final By ATTR_PK_ICON = By.cssSelector("[aria-label='Toggle Primary Key'], .pk-icon");
    private final By ATTR_TYPE_DROPDOWN = By.cssSelector("select");
    
    private final By ERROR_MESSAGE_TEXT = By.cssSelector(".chakra-form__error-message, .error-text, [role='alert']");
    private final By TOOLTIP = By.cssSelector("[role='tooltip']");

    private final By EDGE_PATH = By.cssSelector(".react-flow__edge-path");
    private final By SOURCE_HANDLE = By.cssSelector(".react-flow__handle-right");
    private final By TARGET_HANDLE = By.cssSelector(".react-flow__handle-left");

    public CanvasPage(WebDriver driver) {
        super(driver);
    }

    // =========== ACTIONS ===========

    public void clickAddTable() {
        jsClick(BTN_ADD_TABLE);
    }

    public void clickExportJson() {
        jsClick(BTN_EXPORT_JSON);
    }

    public void clickExportPng() {
        List<WebElement> btns = driver.findElements(BTN_EXPORT_PNG);
        if (!btns.isEmpty()) jsClick(btns.get(0));
    }

    public int getModelNodeCount() {
        try { return driver.findElements(ALL_MODEL_NODES).size(); } catch (Exception e) { return 0; }
    }

    public boolean isAddTableButtonVisible() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(BTN_ADD_TABLE)).isDisplayed(); } catch (Exception e) { return false; }
    }

    public boolean isZoomControlsVisible() {
        try { return driver.findElement(ZOOM_CONTROLS).isDisplayed(); } catch (Exception e) { return false; }
    }

    public WebElement doubleClickFirstTableName() {
        List<WebElement> tableNames = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(TABLE_NAME_DISPLAY));
        if (tableNames.isEmpty()) throw new RuntimeException("No table name found!");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", tableNames.get(0));
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        new Actions(driver).doubleClick(tableNames.get(0)).perform();
        return wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE_NAME_EDITING_INPUT));
    }

    public String getFirstTableNameDisplayed() {
        try {
            List<WebElement> tableNames = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(TABLE_NAME_DISPLAY));
            if (tableNames.isEmpty()) return "";
            return tableNames.get(0).getText().trim();
        } catch (Exception e) { return ""; }
    }

    public void renameFirstTable(String newName) {
        List<WebElement> tableNames = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(TABLE_NAME_DISPLAY));
        if (tableNames.isEmpty()) throw new RuntimeException("No table name found!");
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", tableNames.get(0));
        try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        new Actions(driver).doubleClick(tableNames.get(0)).perform();
        
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE_NAME_EDITING_INPUT));
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}

        WebElement input = driver.findElement(TABLE_NAME_EDITING_INPUT);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        input = driver.findElement(TABLE_NAME_EDITING_INPUT);
        input.clear();
        input = driver.findElement(TABLE_NAME_EDITING_INPUT);
        input.sendKeys(newName);
        input.sendKeys(Keys.RETURN);
        try { Thread.sleep(600); } catch (InterruptedException ignored) {}
    }

    // For blur testing
    public void renameAndBlur(String newName) {
        List<WebElement> tableNames = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(TABLE_NAME_DISPLAY));
        new Actions(driver).doubleClick(tableNames.get(0)).perform();
        wait.until(ExpectedConditions.visibilityOfElementLocated(TABLE_NAME_EDITING_INPUT));
        WebElement input = driver.findElement(TABLE_NAME_EDITING_INPUT);
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        input.sendKeys(Keys.BACK_SPACE);
        if(newName != null && !newName.isEmpty()) {
            input.sendKeys(newName);
        }
        // Blur by clicking on canvas
        driver.findElement(REACT_FLOW_CANVAS).click();
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    public void clickAddAttributeOnFirstNode() {
        List<WebElement> addBtns = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(ALL_ADD_ATTR_BTNS));
        if (addBtns.isEmpty()) throw new RuntimeException("No Add Attribute button found!");
        jsClick(addBtns.get(0));
    }

    public void deleteFirstTable() {
        List<WebElement> nodes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(ALL_MODEL_NODES));
        nodes.get(0).click(); // Select
        new Actions(driver).sendKeys(Keys.DELETE).perform(); // Press delete
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
    }

    public void dragFirstNodeBy(int dx, int dy) {
        List<WebElement> nodes = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(ALL_MODEL_NODES));
        if (nodes.isEmpty()) throw new RuntimeException("No nodes found!");
        new Actions(driver).clickAndHold(nodes.get(0)).pause(500).moveByOffset(dx, dy).pause(300).release().perform();
    }

    public boolean isCanvasRendered() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(REACT_FLOW_CANVAS)).isDisplayed(); } catch (Exception e) { return false; }
    }

    public String getErrorMessage() {
        try {
            List<WebElement> errors = driver.findElements(ERROR_MESSAGE_TEXT);
            if (!errors.isEmpty()) return errors.get(0).getText();
        } catch (Exception e) {}
        return "";
    }
    
    public String getTooltipText() {
        try {
            List<WebElement> tooltips = driver.findElements(TOOLTIP);
            if (!tooltips.isEmpty()) return tooltips.get(0).getText();
        } catch (Exception e) {}
        return "";
    }

    public void connectFirstToSecondNode() {
        List<WebElement> sources = driver.findElements(SOURCE_HANDLE);
        List<WebElement> targets = driver.findElements(TARGET_HANDLE);
        if(sources.size() > 0 && targets.size() > 1) {
            new Actions(driver).clickAndHold(sources.get(0)).moveToElement(targets.get(1)).release().perform();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }

    public int getEdgeCount() {
        return driver.findElements(EDGE_PATH).size();
    }

    public void deleteFirstEdge() {
        List<WebElement> edges = driver.findElements(EDGE_PATH);
        if(!edges.isEmpty()) {
            edges.get(0).click();
            new Actions(driver).sendKeys(Keys.DELETE).perform();
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
    }
}
