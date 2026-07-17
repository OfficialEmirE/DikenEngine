@echo off
title DikenEngine
setlocal enabledelayedexpansion

set PROJ_DIR=%~dp0DikenEngine
set SRC_DIR=%PROJ_DIR%\src
set OUT_DIR=%PROJ_DIR%\bin
set LIB_DIR=%PROJ_DIR%\libs

:: Build
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

set CP=%PROJ_DIR%\res
for %%j in ("%LIB_DIR%\*.jar") do set CP=!CP!;%%j

echo Building DikenEngine...
dir /B /S "%SRC_DIR%\*.java" > "%TEMP%\sources.txt" 2>nul
javac -d "%OUT_DIR%" -cp "%CP%" @"%TEMP%\sources.txt"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo BUILD FAILED
    pause
    exit /b 1
)

echo BUILD SUCCESSFUL
echo.

:: Run
set CP=%CP%;%OUT_DIR%

if "%1"=="--studio" (
    echo Starting Studio...
    java -cp "%CP%" me.ramazanenescik04.diken.DikenEngine --studio
) else if "%1"=="" (
    echo Starting Game...
    java -cp "%CP%" me.ramazanenescik04.diken.DikenEngine
) else (
    java -cp "%CP%" me.ramazanenescik04.diken.DikenEngine %*
)

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Press any key to exit...
    pause >nul
)
