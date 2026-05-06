$mavenDir = "apache-maven-3.9.6"
if (-not (Test-Path $mavenDir)) {
    Write-Host ">>> Dang tai Maven ve may... (Vui long cho)"
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip" -OutFile "maven.zip"
    Write-Host ">>> Giai nen Maven..."
    Expand-Archive -Path "maven.zip" -DestinationPath "." -Force
    Remove-Item "maven.zip"
}

Write-Host ">>> Bat dau chay Automation Test..."
& ".\$mavenDir\bin\mvn.cmd" clean test
