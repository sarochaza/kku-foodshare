$ErrorActionPreference = 'Stop'
$foodshareProjectRoot = Split-Path -Parent $PSScriptRoot
Push-Location $foodshareProjectRoot
try {
    $foodshareReportDir = Join-Path $foodshareProjectRoot 'test\reports\phase12'
    New-Item -ItemType Directory -Force -Path $foodshareReportDir | Out-Null
    foreach ($foodshareTestService in @('java-tests', 'js-tests', 'postgres-tests')) {
        Write-Host "Running $foodshareTestService ..."
        $foodshareTestLog = Join-Path $foodshareReportDir "$foodshareTestService-docker.log"
        $foodshareSavedErrorPreference = $ErrorActionPreference
        $ErrorActionPreference = 'Continue'
        & docker compose -p kku-foodshare-tests -f compose.test.yaml run --rm $foodshareTestService 2>&1 |
            Tee-Object -FilePath $foodshareTestLog
        $foodshareTestExit = $LASTEXITCODE
        $ErrorActionPreference = $foodshareSavedErrorPreference
        if ($foodshareTestExit -ne 0) {
            throw "$foodshareTestService failed. See $foodshareTestLog"
        }
    }
    Write-Host 'All Docker test commands passed. Reports: code\target\surefire-reports and test\reports\phase12'
} finally {
    & docker compose -p kku-foodshare-tests -f compose.test.yaml down
    Pop-Location
}
