@echo off
:: AI Smart Course Platform - PDF Report Generator (English Version)
:: Generate 4 PDF test reports

echo ========================================
echo AI Smart Course Platform - PDF Generator
echo ========================================
echo.

:: Check Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Python not found
    echo Please install Python 3.7+
    pause
    exit /b 1
)
echo [OK] Python installed

:: Check reportlab
python -c "import reportlab" >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] Installing reportlab...
    pip install reportlab
    if errorlevel 1 (
        echo [ERROR] Failed to install reportlab
        pause
        exit /b 1
    )
)
echo [OK] reportlab ready

echo.
echo Generating PDF reports...
echo.

:: Set paths
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

:: Generate PDFs (English version)
python "%SCRIPT_DIR%pdf-generator-en.py" "%PROJECT_ROOT%" --complete --modules

if %errorlevel% neq 0 (
    echo [ERROR] PDF generation failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo PDF Reports Generated Successfully!
echo ========================================
echo.
echo Generated 4 PDF files:
echo 1. Complete Test Report.pdf
echo 2. Authentication Module Report.pdf  
echo 3. User Management Module Report.pdf
echo 4. Course Management Module Report.pdf
echo.
echo Location: %PROJECT_ROOT%\reports\pdf\
echo.

set /p open_folder="Open PDF folder? (Y/N): "
if /i "%open_folder%"=="Y" (
    start "" "%PROJECT_ROOT%\reports\pdf"
)

echo.
echo PDF generation completed!
pause 