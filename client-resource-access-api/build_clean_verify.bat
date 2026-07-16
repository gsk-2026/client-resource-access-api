@echo off

echo ======================================
echo DreamTech Client Resource Access API
echo ======================================

echo.
echo Cleaning the buildt...
call mvn clean

echo.
echo Verifying the build...
call mvn verify -Pqatest

if errorlevel 1 (
    echo.
    echo ***********************************
    echo BUILD FAILED
    echo ***********************************
    exit /b 1
)

echo.
echo ***********************************
echo BUILD SUCCESS
echo ***********************************
