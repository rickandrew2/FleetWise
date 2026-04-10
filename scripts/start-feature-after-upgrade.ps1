param(
    [string]$BaseBranch = "appmod/java-upgrade-20260409135103",
    [string]$FeatureName = "post-upgrade-implementation",
    [string]$JavaHome = "C:\Users\Acer\.jdk\jdk-25"
)

$ErrorActionPreference = "Stop"

Write-Host "== FleetWise post-upgrade preflight ==" -ForegroundColor Cyan

if (-not (Test-Path ".git")) {
    throw "Run this script from the repository root."
}

if (-not (Test-Path ".\\mvnw.cmd")) {
    throw "mvnw.cmd not found. Ensure you're in repository root."
}

if (Test-Path $JavaHome) {
    $env:JAVA_HOME = $JavaHome
    $env:Path = "$env:JAVA_HOME\\bin;$env:Path"
}

Write-Host "\n[1/3] Checking Java toolchain..." -ForegroundColor Yellow
java -version
javac -version

Write-Host "\n[2/3] Running full test suite..." -ForegroundColor Yellow
.\mvnw.cmd clean test

Write-Host "\n[3/3] Creating feature branch from upgraded baseline..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "yyyyMMdd-HHmm"
$branch = "feature/$FeatureName-$timestamp"

$currentBranch = (git rev-parse --abbrev-ref HEAD).Trim()
if ($currentBranch -ne $BaseBranch) {
    git checkout $BaseBranch
}

git checkout -b $branch

Write-Host "\nDone." -ForegroundColor Green
Write-Host "Created and switched to branch: $branch" -ForegroundColor Green
