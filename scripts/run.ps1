param()

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$classesDir = Join-Path $projectRoot "out\classes"

& (Join-Path $PSScriptRoot "compile.ps1")

java -cp $classesDir com.leximatch.App
