# run-coverage.ps1
# Script chay unit test va hien thi coverage day du
# Su dung: .\run-coverage.ps1

Write-Host ""
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host "  FRONTEND UNIT TEST - COVERAGE REPORT  " -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan
Write-Host ""

# Chay test va coverage, luu ket qua vao file
$output = npx vitest run --coverage --reporter=verbose 2>&1
$outputText = $output | Out-String

# In tat ca output
$output | ForEach-Object { Write-Host $_ }

Write-Host ""
Write-Host "=========================================" -ForegroundColor Yellow
Write-Host "           TOM TAT KET QUA TEST         " -ForegroundColor Yellow
Write-Host "=========================================" -ForegroundColor Yellow

# Dem pass / fail
$passCount = ($outputText | Select-String "✓|v " -AllMatches).Matches.Count
$failLines = $outputText | Select-String "Tests\s+\d+ failed"
if ($failLines) {
    Write-Host $failLines.Line -ForegroundColor Red
} else {
    Write-Host "Tat ca test da PASS!" -ForegroundColor Green
}

# Tim dong tong ket
$summaryLine = $outputText | Select-String "Test Files.*passed"
if ($summaryLine) {
    Write-Host $summaryLine.Line -ForegroundColor Cyan
}

# Kiem tra coverage HTML da duoc tao chua
$htmlPath = ".\coverage\index.html"
if (Test-Path $htmlPath) {
    Write-Host ""
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host "   HTML COVERAGE REPORT DA DUOC TAO!   " -ForegroundColor Green
    Write-Host "=========================================" -ForegroundColor Green
    Write-Host ">> Dang mo trinh duyet..." -ForegroundColor Green
    Start-Process $htmlPath
} else {
    Write-Host ""
    Write-Host "[!] Coverage HTML chua duoc tao. Kiem tra lai config." -ForegroundColor Red
}

Write-Host ""
Write-Host "Done! Xem chi tiet trong: coverage\index.html" -ForegroundColor Cyan
