# Script de pruebas HTTP para Proyecto5
# Requisitos: PowerShell (Windows) y servidor arrancado en http://localhost:8586

$baseUrl = 'http://localhost:8586'
$adminUser = 'admin@barberia.com'
$adminPass = 'admin123'

Write-Host "Iniciando sesión como admin ($adminUser)" -ForegroundColor Cyan
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
try {
    $login = Invoke-WebRequest -Uri "$baseUrl/login" -Method Post -Body @{ username = $adminUser; password = $adminPass } -WebSession $session -UseBasicParsing -ErrorAction Stop
    Write-Host "Login HTTP status: $($login.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Login falló: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Crear barbero de prueba
$testEmail = "barbero.test@example.com"
Write-Host "Creando barbero de prueba: $testEmail" -ForegroundColor Cyan
$barberoForm = @{
    nombre = 'Test'
    apellido = 'Barbero'
    email = $testEmail
    telefono = '3000000000'
    password = 'barbero123'
    rol = 'ROLE_BARBERO'
}
# POST form
try {
    $resp = Invoke-WebRequest -Uri "$baseUrl/admin/barberos/guardar" -Method Post -Body $barberoForm -WebSession $session -UseBasicParsing -ErrorAction Stop
    Write-Host "Crear barbero: status $($resp.StatusCode)" -ForegroundColor Green
} catch {
    Write-Host "Error creando barbero: $($_.Exception.Message)" -ForegroundColor Red
    # continuar para intentar extraer lista
}

# Obtener lista de barberos para extraer ID
Write-Host "Obteniendo lista de barberos..." -ForegroundColor Cyan
$listResp = Invoke-WebRequest -Uri "$baseUrl/admin/barberos" -Method Get -WebSession $session -UseBasicParsing
$html = $listResp.Content
# Buscar pattern /admin/barberos/editar/{id} para el email
$pattern = "/admin/barberos/editar/(\d+)"
$barberoId = $null
foreach ($m in [regex]::Matches($html, $pattern)) {
    # for each match, try to see if the same snippet contains the email
    $id = $m.Groups[1].Value
    if ($html -match ("/admin/barberos/editar/" + $id + ".*?" + [regex]::Escape($testEmail))) {
        $barberoId = $id
        break
    }
}
# Fallback: buscar fila que contenga el email y luego extraer nearby editar link
if (-not $barberoId) {
    if ($html -match $testEmail) {
        # attempt to find nearest editar id using context
        $idx = $html.IndexOf($testEmail)
        $chunk = $html.Substring( [Math]::Max(0, $idx-400), [Math]::Min(800, $html.Length-$idx+400))
        $m2 = [regex]::Match($chunk, "/admin/barberos/editar/(\d+)")
        if ($m2.Success) { $barberoId = $m2.Groups[1].Value }
    }
}

if (-not $barberoId) {
    Write-Host "No se pudo determinar el ID del barbero creado." -ForegroundColor Yellow
    Write-Host "Contenido parcial de /admin/barberos (primeros 1000 chars):" -ForegroundColor DarkCyan
    Write-Host $html.Substring(0, [Math]::Min(1000, $html.Length))
    exit 1
}

Write-Host "Barbero creado/identificado con ID: $barberoId" -ForegroundColor Green

# Generar turnos para los próximos 7 días (API REST)
$today = (Get-Date).ToString('yyyy-MM-dd')
$end = (Get-Date).AddDays(7).ToString('yyyy-MM-dd')
Write-Host "Generando turnos para barbero $barberoId desde $today hasta $end" -ForegroundColor Cyan
$body = @{ barberoId = [int]$barberoId; fechaInicio = $today; fechaFin = $end } | ConvertTo-Json
try {
    $gen = Invoke-RestMethod -Uri "$baseUrl/api/turnos/generar" -Method Post -Body $body -ContentType 'application/json' -WebSession $session -ErrorAction Stop
    Write-Host "Respuesta generar turnos: " -ForegroundColor Green
    $gen
} catch {
    Write-Host "Error al generar turnos: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Obtener turnos disponibles
Write-Host "Consultando turnos disponibles..." -ForegroundColor Cyan
try {
    $disponibles = Invoke-RestMethod -Uri "$baseUrl/api/turnos/disponibles/$barberoId" -Method Get -WebSession $session -ErrorAction Stop
    Write-Host "Turnos disponibles encontrados: $($disponibles.Count)" -ForegroundColor Green
    if ($disponibles.Count -gt 0) { $disponibles | Select-Object -First 5 | Format-List }
} catch {
    Write-Host "Error al obtener turnos disponibles: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Reservar el primer turno si existe
if ($disponibles.Count -gt 0) {
    $turnoId = $disponibles[0].idTurno
    Write-Host "Reservando turno ID: $turnoId" -ForegroundColor Cyan
    $resBody = @{ turnoId = $turnoId } | ConvertTo-Json
    try {
        $res = Invoke-RestMethod -Uri "$baseUrl/api/turnos/reservar" -Method Post -Body $resBody -ContentType 'application/json' -WebSession $session -ErrorAction Stop
        Write-Host "Reserva respuesta:" -ForegroundColor Green
        $res | Format-List
    } catch {
        Write-Host "Error al reservar turno: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "No hay turnos disponibles para reservar." -ForegroundColor Yellow
}

Write-Host "Pruebas finalizadas." -ForegroundColor Cyan

# Fin del script
