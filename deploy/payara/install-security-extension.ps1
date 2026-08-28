param(
    [string]$PayaraHome = "C:\Payara\payara6",
    [string]$DomainName = "domain1"
)

$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..")
$securityJar = Join-Path $projectRoot "globaltrade-logistics-security\target\globaltrade-logistics-security.jar"
$domainRoot = Join-Path $PayaraHome "glassfish\domains\$DomainName"
$domainLib = Join-Path $domainRoot "lib"
$loginConf = Join-Path $domainRoot "config\login.conf"
$snippet = Get-Content -Path (Join-Path $PSScriptRoot "login.conf.snippet") -Raw

if (-not (Test-Path $securityJar)) {
    throw "Security module JAR was not found. Run Maven package before installing the Payara login module."
}

if (-not (Test-Path $domainLib)) {
    New-Item -ItemType Directory -Path $domainLib | Out-Null
}

Copy-Item -Path $securityJar -Destination (Join-Path $domainLib "globaltrade-logistics-security.jar") -Force

if (-not (Test-Path $loginConf)) {
    New-Item -ItemType File -Path $loginConf | Out-Null
}

$existingLoginConf = Get-Content -Path $loginConf -Raw
if ($existingLoginConf -notmatch "globaltradeSupplyChainRealm") {
    Add-Content -Path $loginConf -Value "`r`n$snippet"
}

Write-Host "Installed GlobalTrade JAAS extension into $domainLib"
Write-Host "Restart Payara before creating or using the globaltradeRealm realm."
