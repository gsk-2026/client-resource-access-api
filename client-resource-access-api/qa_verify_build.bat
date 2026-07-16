@echo off

echo =========================
echo Java Version
echo =========================
java -version

echo.
echo =========================
echo JAVA_HOME
echo =========================
echo %JAVA_HOME%

echo.
echo =========================
echo Maven Version
echo =========================
mvn -version

echo ======================================
echo DreamTech Client Resource Access API
echo ======================================

echo.
echo Cleaning project...
call mvn clean

echo.
echo Running Verification project...
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
