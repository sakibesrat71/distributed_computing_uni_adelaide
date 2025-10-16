# run_tests.ps1

$javaCmd = "java -cp 'out/production/assi-3_dist_comp' CouncilMember"
$logDir = "logs"
if (-Not(Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir }

# Helper function to kill running CouncilMember processes
function Kill-CouncilMember {
    Get-Process java | Where-Object {
        $_.Path -like "*CouncilMember*" -or $_.CommandLine -like "*CouncilMember*"
    } | Stop-Process -Force -ErrorAction SilentlyContinue
}

Kill-CouncilMember

# Scenario 1: Ideal Network
Write-Host "Scenario 1: Ideal Network"
For ($i=1; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile reliable" -RedirectStandardOutput "$logDir/${mid}_s1.log" -NoNewWindow
}
Start-Sleep -Seconds 2
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M4 --profile reliable" -RedirectStandardOutput "$logDir/M4_s1_propose.log" -NoNewWindow
Start-Sleep -Seconds 10
Kill-CouncilMember

# Scenario 2: Concurrent Proposals
Write-Host "Scenario 2: Concurrent Proposals"
For ($i=1; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile reliable" -RedirectStandardOutput "$logDir/${mid}_s2.log" -NoNewWindow
}
Start-Sleep -Seconds 2
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s2_concurrent.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M8 --profile reliable" -RedirectStandardOutput "$logDir/M8_s2_concurrent.log" -NoNewWindow
Start-Sleep -Seconds 10
Kill-CouncilMember

# Scenario 3a: Fault-Tolerance, M4 initiates
Write-Host "Scenario 3a: Fault-Tolerance, M4 initiates"
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s3a.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3a.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3a.log" -NoNewWindow
For ($i=4; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile standard" -RedirectStandardOutput "$logDir/${mid}_s3a.log" -NoNewWindow
}
Start-Sleep -Seconds 2
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M4 --profile standard" -RedirectStandardOutput "$logDir/M4_s3a_propose.log" -NoNewWindow
Start-Sleep -Seconds 10
Kill-CouncilMember

# Scenario 3b: Fault-Tolerance, M2 initiates
Write-Host "Scenario 3b: Fault-Tolerance, M2 initiates"
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s3b.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3b.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3b.log" -NoNewWindow
For ($i=4; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile standard" -RedirectStandardOutput "$logDir/${mid}_s3b.log" -NoNewWindow
}
Start-Sleep -Seconds 2
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3b_propose.log" -NoNewWindow
Start-Sleep -Seconds 10
Kill-CouncilMember

# Scenario 3c: Fault-Tolerance, M3 initiates then crashes, M4 recovers
Write-Host "Scenario 3c: Fault-Tolerance, M3 initiates then crashes, M4 recovers"
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M1 --profile reliable" -RedirectStandardOutput "$logDir/M1_s3c.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M2 --profile latent" -RedirectStandardOutput "$logDir/M2_s3c.log" -NoNewWindow
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3c.log" -NoNewWindow
For ($i=4; $i -le 9; $i++) {
    $mid = "M$i"
    Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember $mid --profile standard" -RedirectStandardOutput "$logDir/${mid}_s3c.log" -NoNewWindow
}
Start-Sleep -Seconds 2
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M3 --profile failure" -RedirectStandardOutput "$logDir/M3_s3c_propose.log" -NoNewWindow
Start-Sleep -Seconds 5
Get-Process java | Where-Object { $_.CommandLine -match "CouncilMember M3" } | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 2
Start-Process -FilePath java -ArgumentList "-cp out/production/assi-3_dist_comp CouncilMember M4 --profile standard" -RedirectStandardOutput "$logDir/M4_s3c_recover.log" -NoNewWindow
Start-Sleep -Seconds 10
Kill-CouncilMember

Write-Host "All scenarios done. Check $logDir for output logs."

