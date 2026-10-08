[CmdletBinding()]
param(
  [ValidatePattern('^[A-Za-z0-9][A-Za-z0-9_-]*$')]
  [string]$ProjectName = 'kku-foodshare-phase1',
  [string]$BackupRoot = 'backups'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location $projectRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
  throw 'Docker CLI was not found. Start Docker Desktop and try again.'
}
if (-not (Test-Path -LiteralPath (Join-Path $projectRoot 'docker-compose.yml'))) {
  throw 'Run this script from the provided project; docker-compose.yml is missing.'
}
if (-not (Test-Path -LiteralPath (Join-Path $projectRoot '.env'))) {
  throw 'Copy the exact .env used by the currently running local project into this folder first. Do not add it to the ZIP or GitHub.'
}

if ([IO.Path]::IsPathRooted($BackupRoot)) {
  $backupBase = $BackupRoot
} else {
  $backupBase = Join-Path $projectRoot $BackupRoot
}
$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss-fff'
$backupDirectory = Join-Path $backupBase "foodshare-$timestamp"
$uploadsDirectory = Join-Path $backupDirectory 'uploads'
New-Item -ItemType Directory -Force -Path $uploadsDirectory | Out-Null

$composePrefix = @('compose', '-p', $ProjectName, '-f', 'docker-compose.yml')
function Invoke-Compose {
  param([Parameter(Mandatory)][string[]]$DockerArgs)
  & docker @DockerArgs
  if ($LASTEXITCODE -ne 0) {
    throw "Docker Compose command failed with exit code $LASTEXITCODE."
  }
}

$containerDump = "/tmp/foodshare-$timestamp.dump"
$hostDump = Join-Path $backupDirectory 'foodshare.dump'

Write-Host 'Checking that the current database is ready...'
Invoke-Compose -DockerArgs ($composePrefix + @('exec', '-T', 'db', 'pg_isready', '-U', 'foodshare', '-d', 'foodshare'))

Write-Host 'Exporting PostgreSQL in custom format inside the database container...'
Invoke-Compose -DockerArgs ($composePrefix + @('exec', '-T', 'db', 'pg_dump', '-U', 'foodshare', '-d', 'foodshare', '-Fc', '-f', $containerDump))
Invoke-Compose -DockerArgs ($composePrefix + @('cp', "db:$containerDump", $hostDump))
Invoke-Compose -DockerArgs ($composePrefix + @('exec', '-T', 'db', 'rm', '-f', $containerDump))

Write-Host 'Copying uploaded food images...'
Invoke-Compose -DockerArgs ($composePrefix + @('cp', 'app:/app/uploads/.', $uploadsDirectory))

$manifest = @(
  "Created UTC: $([DateTime]::UtcNow.ToString('o'))"
  "Compose project: $ProjectName"
  'Contents: foodshare.dump and uploads/'
  'Secrets are intentionally not included. Preserve APP_SECRET separately when migrating existing data.'
  'This backup may contain personal data; keep it private and out of GitHub.'
)
$manifest | Set-Content -LiteralPath (Join-Path $backupDirectory 'BACKUP-INFO.txt') -Encoding UTF8

Write-Host "Backup created at: $backupDirectory"
Write-Host 'Do not add this folder or the source .env file to GitHub.'
