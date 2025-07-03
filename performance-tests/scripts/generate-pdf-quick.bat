@echo off
chcp 65001 >nul
:: AI智慧课程平台 - 快速PDF报告生成脚本
:: 一键生成所需的4份PDF测试报告

setlocal enabledelayedexpansion

:: 设置脚本目录
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

echo ========================================
echo AI智慧课程平台 - 快速PDF报告生成
echo ========================================
echo.

:: 检查Python是否可用
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Python，请先安装Python 3.7+
    echo 下载地址: https://www.python.org/downloads/
    echo.
    echo 安装Python后，请运行: install-dependencies.bat
    pause
    exit /b 1
)

echo ✓ Python已安装

:: 检查reportlab是否已安装
python -c "import reportlab" >nul 2>&1
if %errorlevel% neq 0 (
    echo.
    echo 警告: 未安装reportlab库，正在自动安装...
    pip install reportlab
    if errorlevel 1 (
        echo 错误: reportlab安装失败
        echo 请手动执行: pip install reportlab
        pause
        exit /b 1
    )
    echo ✓ reportlab安装成功
)

echo ✓ 依赖库已准备就绪
echo.

:: 确保项目根目录路径正确
set PDF_PROJECT_ROOT=%PROJECT_ROOT%
if "%PDF_PROJECT_ROOT:~-1%"=="\" set PDF_PROJECT_ROOT=%PDF_PROJECT_ROOT:~0,-1%

echo 正在生成PDF测试报告...
echo.

:: 执行Python PDF生成器
python "%SCRIPT_DIR%pdf-generator.py" "%PDF_PROJECT_ROOT%" --complete --modules

if %errorlevel% neq 0 (
    echo.
    echo 错误: PDF报告生成失败
    echo.
    echo 可能的原因：
    echo 1. 测试结果不存在，请先运行测试
    echo 2. Python脚本执行错误
    echo 3. 权限不足
    echo.
    echo 建议操作：
    echo 1. 运行 run-unit-tests.bat 生成单元测试结果
    echo 2. 运行 run-load-test.bat 生成性能测试结果
    echo 3. 检查 %PROJECT_ROOT%\reports\ 目录是否存在测试结果
    pause
    exit /b 1
)

echo.
echo ========================================
echo PDF报告生成成功！
echo ========================================
echo.
echo 已生成以下4份PDF文件：
echo 1. 完整测试报告.pdf
echo 2. 认证模块测试报告.pdf  
echo 3. 用户管理模块测试报告.pdf
echo 4. 课程管理模块测试报告.pdf
echo.
echo 报告位置: %PROJECT_ROOT%\reports\pdf\
echo.

:: 询问是否打开文件夹
set /p open_folder="是否打开PDF报告文件夹? (Y/N): "
if /i "%open_folder%"=="Y" (
    start "" "%PROJECT_ROOT%\reports\pdf"
)

echo.
echo 提示：这些PDF文件符合实验结果记录表格式要求
echo 可直接用于提交测试报告
echo.
pause 