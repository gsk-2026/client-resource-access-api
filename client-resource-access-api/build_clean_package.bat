@echo off

echo ======================================
echo DreamTech Client Resource Access API
echo ======================================

echo.
echo Cleaning the build...
call mvn clean

echo.
echo Packaging the application...
call mvn package

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
