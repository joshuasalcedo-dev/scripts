$password = Read-Host "Enter a password for the SSH account" -AsSecureString

New-LocalUser `
    -Name "swa" `
    -Password $password `
    -FullName "Swaswa SSH Account" `
    -Description "Local account for remote development"

Add-LocalGroupMember `
    -Group "Administrators" `
    -Member "swa"
