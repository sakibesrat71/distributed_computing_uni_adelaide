$javaCmd = "java -cp out/production/assi-3_dist_comp CouncilMember"
$logDir = "logs"
if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir }

# Helper: Kill running CouncilMember Java processes (ignore errors if no java processes exist)
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember" } | Stop-Process -Force -ErrorAction SilentlyContinue

# Scenario 1: Ideal Network
Write-Host "Scenario 1: Ideal Network"
for ($i=1; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile reliable" -RedirectStandardOutput "$logDir/${mid}_s1.log"
}
Start-Sleep -Seconds 8  # << Increased from 2 to 8, for TCP readiness
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M4 --profile reliable" -RedirectStandardOutput "$logDir/M4_s1_propose.log"
Start-Sleep -Seconds 8
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember" } | Stop-Process -Force -ErrorAction SilentlyContinue

# Scenario 2: Concurrent Proposals
Write-Host "Scenario 2: Concurrent Proposals"
for ($i=1; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile reliable" -RedirectStandardOutput "$logDir/${mid}_s2.log"
}
Start-Sleep -Seconds 8
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s2_concurrent.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M8 --profile reliable" -RedirectStandardOutput "$logDir/M8_s2_concurrent.log"
Start-Sleep -Seconds 8
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember" } | Stop-Process -Force -ErrorAction SilentlyContinue

# Scenario 3a: Fault-Tolerance, M4 initiates
Write-Host "Scenario 3a: Fault-Tolerance, M4 initiates"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s3a.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3a.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3a.log"
for ($i=4; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile standard" -RedirectStandardOutput "$logDir/${mid}_s3a.log"
}
Start-Sleep -Seconds 8
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M4 --profile standard" -RedirectStandardOutput "$logDir/M4_s3a_propose.log"
Start-Sleep -Seconds 8
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember" } | Stop-Process -Force -ErrorAction SilentlyContinue

# Scenario 3b: Fault-Tolerance, M2 initiates
Write-Host "Scenario 3b: Fault-Tolerance, M2 initiates"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s3b.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3b.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3b.log"
for ($i=4; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile standard" -RedirectStandardOutput "$logDir/${mid}_s3b.log"
}
Start-Sleep -Seconds 8
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3b_propose.log"
Start-Sleep -Seconds 8
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember" } | Stop-Process -Force -ErrorAction SilentlyContinue

# Scenario 3c: Fault-Tolerance, M3 initiates then crashes, M4 recovers
Write-Host "Scenario 3c: Fault-Tolerance, M3 initiates then crashes, M4 recovers"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s3c.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3c.log"
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3c.log"
for ($i=4; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile standard" -RedirectStandardOutput "$logDir/${mid}_s3c.log"
}
Start-Sleep -Seconds 8
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3c_propose.log"
Start-Sleep -Seconds 4
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember M3" } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
Start-Process java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M4 --profile standard" -RedirectStandardOutput "$logDir/M4_s3c_recover.log"
Start-Sleep -Seconds 8
Get-Process java -ErrorAction SilentlyContinue | Where-Object { $_.CommandLine -match "CouncilMember" } | Stop-Process -Force -ErrorAction SilentlyContinue

Write-Host "All scenarios complete. Output logs are in $logDir"
