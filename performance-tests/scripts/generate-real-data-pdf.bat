@echo off
chcp 65001 > nul
echo.
echo ========================================
echo AI智慧课程平台 - 真实数据PDF报告生成
echo ========================================
echo.

REM 检查Python是否可用
python --version > nul 2>&1
if errorlevel 1 (
    echo ❌ Python未安装或不在PATH中
    echo 请确保Python已正确安装
    pause
    exit /b 1
)

echo ✓ Python环境正常

REM 检查并安装依赖
echo.
echo 正在检查依赖库...
python -c "import reportlab" > nul 2>&1
if errorlevel 1 (
    echo 正在安装reportlab...
    pip install reportlab
    if errorlevel 1 (
        echo ❌ 安装reportlab失败
        pause
        exit /b 1
    )
)

echo ✓ 依赖库已准备就绪

REM 运行PDF生成脚本
echo.
echo 正在生成真实数据PDF报告...
python pdf-generator-real-data.py

if errorlevel 1 (
    echo ❌ PDF生成失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo 真实数据PDF报告生成成功！
echo ========================================
echo.
echo 报告位置: ..\reports\pdf\
echo.
echo 是否打开PDF文件夹? (Y/N):
set /p choice=
if /i "%choice%"=="Y" (
    start ..\reports\pdf\
)

echo.
echo 真实数据PDF生成完成！
pause 