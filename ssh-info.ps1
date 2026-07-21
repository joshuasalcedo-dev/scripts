$ErrorActionPreference = "SilentlyContinue"

$computerName = $env:COMPUTERNAME
$windowsUser = $env:USERNAME
$fullUser = whoami

$addresses = @(
    Get-NetIPAddress -AddressFamily IPv4 |
        Where-Object {
            $_.AddressState -eq "Preferred" -and
            $_.IPAddress -notlike "127.*" -and
            $_.IPAddress -notlike "169.254.*"
        } |
        Select-Object -ExpandProperty IPAddress -Unique
)

$sshService = Get-Service -Name sshd
$sshListeners = Get-NetTCPConnection -LocalPort 22 -State Listen
$networkProfiles = Get-NetConnectionProfile

$lines = @()

$lines += "========== SSH INFORMATION =========="
$lines += "COMPUTER_NAME=$computerName"
$lines += "WINDOWS_USER=$windowsUser"
$lines += "FULL_USER=$fullUser"
$lines += ""

if ($null -eq $sshService) {
    $lines += "SSH_SERVICE=NOT INSTALLED"
} else {
    $lines += "SSH_SERVICE=$($sshService.Status)"
    $lines += "SSH_START_TYPE=$($sshService.StartType)"
}

if ($sshListeners) {
    $lines += "PORT_22=LISTENING"
} else {
    $lines += "PORT_22=NOT LISTENING"
}

$lines += ""
$lines += "NETWORK_PROFILES:"

foreach ($profile in $networkProfiles) {
    $lines += "  $($profile.InterfaceAlias) | $($profile.NetworkCategory) | $($profile.IPv4Connectivity)"
}

$lines += ""
$lines += "IP_ADDRESSES:"

foreach ($address in $addresses) {
    $lines += "  $address"
}

$lines += ""
$lines += "SSH_COMMANDS:"

foreach ($address in $addresses) {
    $lines += "  ssh $windowsUser@$address"
}

$lines += "====================================="

$lines | ForEach-Object {
    Write-Host $_
}

$desktop = [Environment]::GetFolderPath("Desktop")
$outputFile = Join-Path $desktop "ssh-information.txt"

$lines | Set-Content -LiteralPath $outputFile -Encoding UTF8

Write-Host ""
Write-Host "Saved to: $outputFile" -ForegroundColor Green
