# Download and setup Gradle wrapper
$gradleVersion = "8.11"
$gradleUrl = "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip"
$tempDir = $env:TEMP
$zipFile = "$tempDir\gradle-$gradleVersion-bin.zip"
$gradleDir = "$tempDir\gradle-$gradleVersion"

Write-Host "Setting up Gradle wrapper for PrintScript..."

# Download Gradle if not already present
if (-not (Test-Path $gradleDir)) {
    Write-Host "Downloading Gradle $gradleVersion..."
    $ProgressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $gradleUrl -OutFile $zipFile
    Write-Host "Extracting Gradle..."
    Expand-Archive -Path $zipFile -DestinationPath $tempDir -Force
}

$gradleExe = "$gradleDir\bin\gradle.bat"
Write-Host "Using Gradle from: $gradleExe"

# Generate wrapper
Write-Host "Generating Gradle wrapper..."
& $gradleExe wrapper --gradle-version $gradleVersion

Write-Host "Gradle wrapper setup complete!"
Write-Host "You can now use: .\gradlew.bat build"
