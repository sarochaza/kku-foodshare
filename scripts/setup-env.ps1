$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (Test-Path '.env') { Write-Host '.env exists; leaving it unchanged.'; exit 0 }
function New-Secret {
  $bytes = New-Object byte[] 32
  $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
  try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
  return [BitConverter]::ToString($bytes).Replace('-', '').ToLowerInvariant()
}
$content = Get-Content '.env.example' -Raw
$content = $content -replace '(?m)^DATABASE_PASSWORD=\r?$', ('DATABASE_PASSWORD=' + (New-Secret))
$content = $content -replace '(?m)^APP_SECRET=\r?$', ('APP_SECRET=' + (New-Secret))
[IO.File]::WriteAllText((Join-Path (Get-Location) '.env'), $content, (New-Object Text.UTF8Encoding $false))
Write-Host 'Created .env with random secrets. Run: docker compose -p kku-foodshare-phase1 -f docker-compose.yml up --build -d'
