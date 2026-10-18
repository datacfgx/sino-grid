$p = "$env:APPDATA\npm\node_modules\@crafter\uber-cli\dist\main.js"
if (-not (Test-Path $p)) { Write-Host "NAO ACHEI: $p"; exit 1 }
$c = [IO.File]::ReadAllText($p)
$antes = $c.Length
$c = $c.Replace('j1(K1,["--version"],{encoding:"utf8"})', 'j1(K1,["--version"],{encoding:"utf8",shell:true})')
$c = $c.Replace('{encoding:"utf8",timeout:i,env:v})', '{encoding:"utf8",timeout:i,env:v,shell:true})')
[IO.File]::WriteAllText($p, $c)
Write-Host "patched OK ($antes -> $($c.Length) chars)"
