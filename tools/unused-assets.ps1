param(
    [string]$AssetsRoot = "src/main/resources/assets",
    [string]$UsedList = "",
    [string]$Output = ""
)

if (-not (Test-Path -Path $AssetsRoot)) {
    Write-Error "Assets root not found: $AssetsRoot"
    exit 1
}

if (-not $UsedList) {
    $candidates = @(
        "run/client/resource-usage/used_resources.txt",
        "run/resource-usage/used_resources.txt",
        "run/server/resource-usage/used_resources.txt"
    )
    foreach ($candidate in $candidates) {
        if (Test-Path -Path $candidate) {
            $UsedList = $candidate
            break
        }
    }
}

if (-not $UsedList -or -not (Test-Path -Path $UsedList)) {
    Write-Error "Used list not found. Pass -UsedList or place it under run/client/resource-usage/used_resources.txt."
    exit 1
}

$assetsRootFull = (Resolve-Path -Path $AssetsRoot).Path
$used = New-Object "System.Collections.Generic.HashSet[string]" ([StringComparer]::Ordinal)

Get-Content -Path $UsedList | ForEach-Object {
    $line = $_.Trim()
    if ($line.Length -eq 0) { return }
    if ($line -match '^([^:]+):(.+)$') {
        $domain = $matches[1]
        $path = $matches[2]
        $rel = ($domain + "/" + $path).Replace("\", "/")
        [void]$used.Add($rel)
        if (-not $path.EndsWith(".mcmeta")) {
            [void]$used.Add(($domain + "/" + $path + ".mcmeta").Replace("\", "/"))
        }
    }
}

$unused = New-Object "System.Collections.Generic.List[string]"
Get-ChildItem -Path $assetsRootFull -Recurse -File | ForEach-Object {
    $rel = $_.FullName.Substring($assetsRootFull.Length).TrimStart("\", "/").Replace("\", "/")
    if (-not $used.Contains($rel)) {
        $unused.Add($rel)
    }
}

$unused = $unused | Sort-Object
if (-not $Output) {
    $Output = Join-Path (Split-Path -Parent $UsedList) "unused_assets.txt"
}

$outDir = Split-Path -Parent $Output
if ($outDir -and -not (Test-Path -Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
}

$unused | Set-Content -Path $Output -Encoding UTF8
Write-Host "Wrote $($unused.Count) unused assets to $Output"
