# Script de pruebas usando curl.exe para Proyecto5
# Requisitos: servidor en http://localhost:8586 y curl en C:\Windows\System32\curl.exe

$curl = 'C:\\Windows\\System32\\curl.exe'
$baseUrl = 'http://localhost:8586'
$cookieAdmin = 'scripts/cookies_admin.txt'

Write-Host "Usando curl: $curl" -ForegroundColor Cyan

if (-not (Test-Path $curl)) {
    Write-Host "curl.exe no encontrado en $curl" -ForegroundColor Red
    exit 1
}

Write-Host "1) Login como admin (admin@barberia.com)" -ForegroundColor Cyan
$loginOut = & $curl '-s' '-i' '-c' $cookieAdmin '-d' 'username=admin@barberia.com&password=admin123' "$baseUrl/login"
Write-Host $loginOut

Write-Host "2) Crear / actualizar barbero de prueba" -ForegroundColor Cyan
$createOut = & $curl '-s' '-i' '-b' $cookieAdmin '-L' '-d' 'nombre=Test&apellido=Barbero&email=barbero.test@example.com&telefono=3000000000&password=barbero123&rol=ROLE_BARBERO' "$baseUrl/admin/barberos/guardar"
Write-Host $createOut

Write-Host "3) Obtener lista de barberos y extraer ID del test" -ForegroundColor Cyan
$listHtml = & $curl '-s' '-b' $cookieAdmin "$baseUrl/admin/barberos"
Write-Host "Lista recibida (primeros 800 chars):" -ForegroundColor DarkCyan
Write-Host ($listHtml.Substring(0, [Math]::Min(800, $listHtml.Length)))

$barberoId = $null
$m = [regex]::Match($listHtml, '/admin/barberos/editar/(\d+).*?barbero.test@example.com', 'Singleline')
if ($m.Success) { $barberoId = $m.Groups[1].Value }
if (-not $barberoId) {
    $m2 = [regex]::Match($listHtml, '/admin/barberos/editar/(\d+)')
    if ($m2.Success) { $barberoId = $m2.Groups[1].Value }
}

if (-not $barberoId) {
    Write-Host "No se pudo determinar ID del barbero de prueba." -ForegroundColor Yellow
    exit 1
}

Write-Host "Barbero ID detectado: $barberoId" -ForegroundColor Green

Write-Host "4) Generar turnos (próximos 7 días)" -ForegroundColor Cyan
$today = (Get-Date).ToString('yyyy-MM-dd')
$end = (Get-Date).AddDays(7).ToString('yyyy-MM-dd')
$genJson = "{`"barberoId`":$barberoId,`"fechaInicio`":`"$today`",`"fechaFin`":`"$end`"}"
$genOut = & $curl '-s' '-i' '-b' $cookieAdmin '-H' 'Content-Type: application/json' '-d' $genJson "$baseUrl/api/turnos/generar"
Write-Host $genOut

Write-Host "5) Obtener turnos disponibles (primeros 1000 chars)" -ForegroundColor Cyan
$disp = & $curl '-s' '-b' $cookieAdmin "$baseUrl/api/turnos/disponibles/$barberoId"
if ($disp) {
    Write-Host ($disp.Substring(0, [Math]::Min(1000, $disp.Length)))
} else {
    Write-Host "Respuesta vacía al pedir turnos disponibles" -ForegroundColor Yellow
}

Write-Host "6) Reservar primer turno disponible (si existe)" -ForegroundColor Cyan
$m3 = [regex]::Match($disp, '"idTurno"\s*:\s*(\d+)')
if ($m3.Success) { $turnoId = $m3.Groups[1].Value } else { $turnoId = $null }

if ($turnoId) {
    Write-Host "Reservando turno id: $turnoId" -ForegroundColor Green
    $resJson = "{`"turnoId`":$turnoId}"
    $resOut = & $curl '-s' '-i' '-b' $cookieAdmin '-H' 'Content-Type: application/json' '-d' $resJson "$baseUrl/api/turnos/reservar"
    Write-Host $resOut
} else {
    Write-Host "No se encontró id de turno en la respuesta." -ForegroundColor Yellow
}

Write-Host "Pruebas finalizadas." -ForegroundColor Cyan
