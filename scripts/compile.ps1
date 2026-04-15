param()

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $projectRoot "out"
$classesDir = Join-Path $outputDir "classes"
$testClassesDir = Join-Path $outputDir "test-classes"

Remove-Item -Recurse -Force $classesDir, $testClassesDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classesDir, $testClassesDir | Out-Null

$mainSources = Get-ChildItem -Path (Join-Path $projectRoot "src\main\java") -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
$testSources = Get-ChildItem -Path (Join-Path $projectRoot "src\test\java") -Filter *.java -Recurse | Select-Object -ExpandProperty FullName
$junitJar = Join-Path $projectRoot "lib\junit-platform-console-standalone-1.11.3.jar"

javac -d $classesDir $mainSources
Copy-Item (Join-Path $projectRoot "src\main\resources\*") $classesDir -Recurse -Force

if ($testSources.Count -gt 0) {
    javac -cp "$classesDir;$junitJar" -d $testClassesDir $testSources
}

Write-Host "Compilation complete."
