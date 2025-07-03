@echo off
:: AI智慧课程平台 - 依赖安装脚本
:: 用于安装PDF生成所需的Python依赖

echo ========================================
echo AI智慧课程平台 - 依赖安装
echo ========================================

:: 检查Python是否已安装
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Python，请先安装Python 3.7+
    echo 下载地址: https://www.python.org/downloads/
    pause
    exit /b 1
)

echo ✓ Python已安装

:: 检查pip是否可用
pip --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: pip不可用，请检查Python安装
    pause
    exit /b 1
)

echo ✓ pip可用

:: 安装reportlab用于PDF生成
echo.
echo 正在安装PDF生成依赖...
pip install reportlab

if %errorlevel% neq 0 (
    echo 错误: reportlab安装失败
    echo 请尝试手动安装: pip install reportlab
    pause
    exit /b 1
)

echo ✓ reportlab安装成功

:: 验证安装
echo.
echo 验证安装...
python -c "import reportlab; print('reportlab版本:', reportlab.Version)"

if %errorlevel% neq 0 (
    echo 警告: reportlab验证失败
) else (
    echo ✓ reportlab验证成功
)

echo.
echo ========================================
echo 依赖安装完成！
echo ========================================
echo.
echo 现在可以运行PDF报告生成功能
pause 