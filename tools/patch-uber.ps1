$alvos = @()
$root = (npm root -g).Trim()
$alvos += "$root\@crafter\uber-cli\dist\main.js"
$cmd = (Get-Command uber -ErrorAction SilentlyContinue).Source
if ($cmd) {
  $binDir = Split-Path $cmd
  $alvos += "$binDir\node_modules\@crafter\uber-cli\dist\main.js"
  $alvos += (Split-Path $binDir) + "\node_modules\@crafter\uber-cli\dist\main.js"
}
$p = $alvos | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $p) { Write-Host "NAO ACHEI em nenhum candidato"; exit 1 }
$c = [IO.File]::ReadAllText($p)
$antes = $c.Length
$c = $c.Replace('j1(K1,["--version"],{encoding:"utf8"})', 'j1(K1,["--version"],{encoding:"utf8",shell:true})')
$c = $c.Replace('{encoding:"utf8",timeout:i,env:v})', '{encoding:"utf8",timeout:i,env:v,shell:true})')
[IO.File]::WriteAllText($p, $c)
Write-Host "patched OK em $p ($antes -> $($c.Length))"
