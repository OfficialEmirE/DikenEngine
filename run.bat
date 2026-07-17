@echo off
title DikenEngine
setlocal enabledelayedexpansion

set PROJ_DIR=%~dp0DikenEngine
set SRC_DIR=%PROJ_DIR%\src
set OUT_DIR=%PROJ_DIR%\bin
set LIB_DIR=%PROJ_DIR%\libs
set RES_DIR=%PROJ_DIR%\res
set BUILD_DIR=%~dp0_FinalJarBuild

:: =============================================
:: Clean and prepare output directory
:: =============================================
if exist "%BUILD_DIR%" rmdir /S /Q "%BUILD_DIR%"
mkdir "%BUILD_DIR%"
mkdir "%BUILD_DIR%\libs"

:: =============================================
:: Build classpath for compilation
:: =============================================
set CP=%RES_DIR%
for %%j in ("%LIB_DIR%\*.jar") do set CP=!CP!;%%j

:: =============================================
:: Compile Java sources
:: =============================================
echo Building DikenEngine...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"
dir /B /S "%SRC_DIR%\*.java" > "%TEMP%\diken_sources.txt" 2>nul
javac -d "%OUT_DIR%" -cp "%CP%" @"%TEMP%\diken_sources.txt"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo BUILD SUCCESSFUL
echo.

:: =============================================
:: Copy libraries to build directory
:: =============================================
echo Copying libraries...
xcopy /E /I /Y "%LIB_DIR%\*" "%BUILD_DIR%\libs\" >nul

:: =============================================
:: Build manifest Class-Path (relative to JAR)
:: =============================================
set MANIFEST_CP=
for %%j in ("%LIB_DIR%\*.jar") do (
    set MANIFEST_CP=!MANIFEST_CP! libs/%%~nxj
)

echo Main-Class: me.ramazanenescik04.diken.DikenEngine > "%TEMP%\diken_manifest.txt"
echo Class-Path:%MANIFEST_CP% >> "%TEMP%\diken_manifest.txt"

:: =============================================
:: Create JAR (classes only first)
:: =============================================
echo Creating DikenEngine.jar...
jar cfm "%BUILD_DIR%\DikenEngine.jar" "%TEMP%\diken_manifest.txt" -C "%OUT_DIR%" .

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo JAR CREATION FAILED
    pause
    exit /b 1
)

:: =============================================
:: Add resources into JAR
:: =============================================
jar uf "%BUILD_DIR%\DikenEngine.jar" -C "%RES_DIR%" .

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo JAR RESOURCE UPDATE FAILED
    pause
    exit /b 1
)

echo JAR created: _FinalJarBuild\DikenEngine.jar
echo.

:: =============================================
:: Run the JAR
:: =============================================
if "%1"=="--studio" (
    echo Starting Studio...
    java -jar "%BUILD_DIR%\DikenEngine.jar" --studio
) else if "%1"=="" (
    echo Starting Game...
    java -jar "%BUILD_DIR%\DikenEngine.jar"
) else (
    java -jar "%BUILD_DIR%\DikenEngine.jar" %*
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Press any key to exit...
    pause >nul
)
