[CmdletBinding()]
param()
$ErrorActionPreference = 'Stop'
$seedRoot = Split-Path -Parent $PSScriptRoot
Push-Location $seedRoot
try {
    $seedJdk = $env:JAVA_HOME
    if (-not $seedJdk -or -not (Test-Path (Join-Path $seedJdk 'bin\javac.exe'))) {
        $seedCandidates = Get-ChildItem (Join-Path $env:USERPROFILE '.jdks') -Directory -ErrorAction SilentlyContinue |
            Where-Object { Test-Path (Join-Path $_.FullName 'bin\javac.exe') } | Sort-Object Name -Descending
        $seedJdk = $seedCandidates | Select-Object -First 1 -ExpandProperty FullName
    }
    if (-not $seedJdk) { throw 'Configure JAVA_HOME para um JDK 21 ou superior.' }
    $seedJar = Join-Path $seedRoot 'target\gestao-eventos-0.0.1-SNAPSHOT.jar'
    if (Test-Path $seedJar) {
        $seedClasspath = $seedJar
    } else {
        $seedRepo = Join-Path $env:USERPROFILE '.m2\repository'
        $seedArtifacts = @('org\postgresql\postgresql\42.7.8\postgresql-42.7.8.jar',
            'at\favre\lib\bcrypt\0.10.2\bcrypt-0.10.2.jar', 'at\favre\lib\bytes\1.5.0\bytes-1.5.0.jar')
        $seedJars = foreach ($seedArtifact in $seedArtifacts) {
            $seedFile = Join-Path $seedRepo $seedArtifact
            if (-not (Test-Path $seedFile)) { throw 'Faltam dependencias Java. Execute mvn package antes de popular os dados.' }
            $seedFile
        }
        $seedClasspath = $seedJars -join [IO.Path]::PathSeparator
    }
    $seedClasses = Join-Path $seedRoot 'target\demo-seed'
    New-Item -ItemType Directory -Force -Path $seedClasses | Out-Null
    & (Join-Path $seedJdk 'bin\javac.exe') --release 21 -encoding UTF-8 -cp $seedClasspath -d $seedClasses `
        'src\main\java\br\com\eventsbymc\eventsapi\infrastructure\seed\DemoDataSeeder.java'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar o utilitario de demonstracao.' }
    & (Join-Path $seedJdk 'bin\java.exe') -cp ($seedClasses + [IO.Path]::PathSeparator + $seedClasspath) `
        br.com.eventsbymc.eventsapi.infrastructure.seed.DemoDataSeeder
    if ($LASTEXITCODE -ne 0) { throw 'Carga de demonstracao nao concluida. Verifique a mensagem acima.' }
} finally { Pop-Location }
