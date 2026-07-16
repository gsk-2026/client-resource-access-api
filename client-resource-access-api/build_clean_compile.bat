@echo off

echo ======================================
echo DreamTech Client Resource Access API
echo ======================================

echo.
echo Cleaning the build...
call mvn clean

echo.
echo Compiling the code...
call mvn compile

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
