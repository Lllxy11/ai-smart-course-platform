@echo off
:: AI智慧课程平台 - 中文PDF报告生成脚本
:: 按照实验结果记录表格式生成中文PDF

echo ========================================
echo AI智慧课程平台 - 中文PDF报告生成
echo ========================================
echo.

:: 检查Python
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Python
    echo 请安装Python 3.7+
    pause
    exit /b 1
)
echo [OK] Python已安装

:: 检查reportlab
python -c "import reportlab" >nul 2>&1
if %errorlevel% neq 0 (
    echo [INFO] 正在安装reportlab...
    pip install reportlab
    if errorlevel 1 (
        echo [ERROR] reportlab安装失败
        pause
        exit /b 1
    )
)
echo [OK] reportlab已准备

echo.
echo 正在生成中文PDF报告...
echo.

:: 设置路径
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

:: 生成中文版PDF
python "%SCRIPT_DIR%pdf-generator-zh.py" "%PROJECT_ROOT%" --complete --modules

if %errorlevel% neq 0 (
    echo [ERROR] PDF生成失败
    pause
    exit /b 1
)

echo.
echo ========================================
echo 中文PDF报告生成成功！
echo ========================================
echo.
echo 已生成4份PDF文件:
echo 1. 完整测试报告.pdf
echo 2. 认证模块测试报告.pdf  
echo 3. 用户管理模块测试报告.pdf
echo 4. 课程管理模块测试报告.pdf
echo.
echo 位置: %PROJECT_ROOT%\reports\pdf\
echo.

set /p open_folder="是否打开PDF文件夹? (Y/N): "
if /i "%open_folder%"=="Y" (
    start "" "%PROJECT_ROOT%\reports\pdf"
)

echo.
echo 中文PDF生成完成！
pause 