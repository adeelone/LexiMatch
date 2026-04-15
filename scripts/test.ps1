param()

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$classesDir = Join-Path $projectRoot "out\classes"
$testClassesDir = Join-Path $projectRoot "out\test-classes"
$junitJar = Join-Path $projectRoot "lib\junit-platform-console-standalone-1.11.3.jar"

& (Join-Path $PSScriptRoot "compile.ps1")

java -jar $junitJar `
  execute `
  --class-path "$classesDir;$testClassesDir" `
  --scan-class-path `
  --details summary `
  --disable-banner
