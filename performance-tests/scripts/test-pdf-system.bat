@echo off
chcp 65001 >nul
:: AI智慧课程平台 - PDF生成系统测试脚本
:: 用于验证PDF生成功能是否正常工作

echo ========================================
echo AI智慧课程平台 - PDF系统测试
echo ========================================
echo.

:: 设置脚本目录
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

echo 1. 检查Python环境...
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Python未安装或不在PATH中
    echo 请安装Python 3.7+: https://www.python.org/downloads/
    goto :end_test
) else (
    python --version
    echo ✅ Python环境正常
)

echo.
echo 2. 检查reportlab库...
python -c "import reportlab; print('reportlab版本:', reportlab.Version)" >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ reportlab库未安装
    echo 正在尝试自动安装...
    pip install reportlab
    if errorlevel 1 (
        echo ❌ reportlab安装失败
        echo 请手动执行: pip install reportlab
        goto :end_test
    ) else (
        echo ✅ reportlab安装成功
    )
) else (
    python -c "import reportlab; print('✅ reportlab版本:', reportlab.Version)"
)

echo.
echo 3. 检查项目结构...
if not exist "%PROJECT_ROOT%\backend-java" (
    echo ❌ 未找到backend-java目录
    goto :end_test
) else (
    echo ✅ backend-java目录存在
)

if not exist "%SCRIPT_DIR%pdf-generator.py" (
    echo ❌ 未找到pdf-generator.py文件
    goto :end_test
) else (
    echo ✅ PDF生成器脚本存在
)

echo.
echo 4. 创建测试报告目录...
if not exist "%PROJECT_ROOT%\reports" mkdir "%PROJECT_ROOT%\reports"
if not exist "%PROJECT_ROOT%\reports\pdf" mkdir "%PROJECT_ROOT%\reports\pdf"
echo ✅ 报告目录已创建

echo.
echo 5. 测试PDF生成功能...
echo 正在生成测试PDF报告...

python "%SCRIPT_DIR%pdf-generator.py" "%PROJECT_ROOT%" --complete --modules

if %errorlevel% neq 0 (
    echo ❌ PDF生成测试失败
    echo 可能原因：
    echo - 测试数据不存在（请先运行单元测试）
    echo - Python权限不足
    echo - 路径包含中文字符
    echo.
    echo 建议操作：
    echo 1. 先运行: run-unit-tests.bat
    echo 2. 再运行: generate-pdf-quick.bat
) else (
    echo ✅ PDF生成测试成功
    echo.
    echo 检查生成的文件...
    if exist "%PROJECT_ROOT%\reports\pdf\*.pdf" (
        echo ✅ 找到PDF文件
        for %%f in ("%PROJECT_ROOT%\reports\pdf\*.pdf") do echo   - %%~nf.pdf
        for %%f in ("%PROJECT_ROOT%\reports\pdf\modules\*.pdf") do echo   - modules\%%~nf.pdf
    ) else (
        echo ⚠️ 未找到PDF文件，但未报告错误
    )
)

:end_test
echo.
echo ========================================
echo 测试完成
echo ========================================
echo.
echo 如果所有检查都通过，您可以：
echo 1. 运行 run-all-tests.bat 使用完整功能
echo 2. 运行 generate-pdf-quick.bat 快速生成PDF
echo 3. 查看 PDF报告生成说明.md 了解详细信息
echo.
pause 