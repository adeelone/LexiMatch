param(
    [int]$Port = 8080
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

Write-Host "Serving LexiMatch at http://localhost:$Port/web/"
python -m http.server $Port
