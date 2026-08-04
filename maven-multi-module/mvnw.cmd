@echo off
setlocal

set "MAVEN_VERSION=3.9.9"
set "MAVEN_DISTRIBUTION_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip"
set "MAVEN_DIST_ROOT=%USERPROFILE%\.m2\wrapper\dists"
set "MAVEN_HOME=%MAVEN_DIST_ROOT%\apache-maven-%MAVEN_VERSION%"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
    if not exist "%MAVEN_DIST_ROOT%" mkdir "%MAVEN_DIST_ROOT%"
    echo Downloading Apache Maven %MAVEN_VERSION%...
    powershell -NoProfile -ExecutionPolicy Bypass -Command ^
        "$ErrorActionPreference = 'Stop'; $ProgressPreference = 'SilentlyContinue'; $zip = Join-Path $env:TEMP 'apache-maven-%MAVEN_VERSION%-bin.zip'; Invoke-WebRequest -Uri '%MAVEN_DISTRIBUTION_URL%' -OutFile $zip; Expand-Archive -Path $zip -DestinationPath '%MAVEN_DIST_ROOT%' -Force; Remove-Item $zip"
    if errorlevel 1 exit /b 1
)

call "%MAVEN_HOME%\bin\mvn.cmd" %*
exit /b %errorlevel%
