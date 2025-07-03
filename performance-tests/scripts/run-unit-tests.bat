@echo off
chcp 65001 >nul
:: AI智慧课程平台 - 单元测试执行脚本
:: 用于执行所有单元测试并生成覆盖率报告

setlocal enabledelayedexpansion

:: 设置脚本目录
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..\..\backend-java
set REPORTS_DIR=%SCRIPT_DIR%..\reports

:: 确保报告目录存在
if not exist "%REPORTS_DIR%\unit-tests" mkdir "%REPORTS_DIR%\unit-tests"
if not exist "%REPORTS_DIR%\coverage" mkdir "%REPORTS_DIR%\coverage"

:: 设置日志文件
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "datestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"
set LOG_FILE=%REPORTS_DIR%\unit-tests\unit-test-%datestamp%.log

echo ======================================== >> "%LOG_FILE%"
echo AI智慧课程平台 - 单元测试开始 >> "%LOG_FILE%"
echo 测试时间: %date% %time% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"

echo ======================================== 
echo AI智慧课程平台 - 单元测试开始
echo 测试时间: %date% %time%
echo ========================================

:: 切换到项目目录
cd /d "%PROJECT_ROOT%"

echo 当前目录: %CD%
echo 当前目录: %CD% >> "%LOG_FILE%"

:: 检查Maven是否安装
where mvn >nul 2>nul
if errorlevel 1 (
    echo 错误: Maven 未安装或不在PATH中
    echo 错误: Maven 未安装或不在PATH中 >> "%LOG_FILE%"
    echo 请确保 Apache Maven 已正确安装并配置环境变量
    pause
    exit /b 1
)

echo Maven版本信息:
echo Maven版本信息: >> "%LOG_FILE%"
mvn --version
mvn --version >> "%LOG_FILE%" 2>&1

echo.
echo ======================================== 
echo 1. 清理项目
echo ======================================== 
echo 1. 清理项目 >> "%LOG_FILE%"

mvn clean >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo ✗ 项目清理失败
    echo ✗ 项目清理失败 >> "%LOG_FILE%"
    goto :error
) else (
    echo ✓ 项目清理完成
    echo ✓ 项目清理完成 >> "%LOG_FILE%"
)

echo.
echo ======================================== 
echo 2. 编译项目
echo ======================================== 
echo 2. 编译项目 >> "%LOG_FILE%"

mvn compile >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo ✗ 项目编译失败
    echo ✗ 项目编译失败 >> "%LOG_FILE%"
    goto :error
) else (
    echo ✓ 项目编译完成
    echo ✓ 项目编译完成 >> "%LOG_FILE%"
)

echo.
echo ======================================== 
echo 3. 运行单元测试
echo ======================================== 
echo 3. 运行单元测试 >> "%LOG_FILE%"

mvn test >> "%LOG_FILE%" 2>&1
set TEST_RESULT=%errorlevel%

if %TEST_RESULT% neq 0 (
    echo ⚠ 单元测试执行完成，但有测试失败
    echo ⚠ 单元测试执行完成，但有测试失败 >> "%LOG_FILE%"
) else (
    echo ✓ 单元测试全部通过
    echo ✓ 单元测试全部通过 >> "%LOG_FILE%"
)

echo.
echo ======================================== 
echo 4. 生成测试覆盖率报告
echo ======================================== 
echo 4. 生成测试覆盖率报告 >> "%LOG_FILE%"

mvn jacoco:report >> "%LOG_FILE%" 2>&1
if errorlevel 1 (
    echo ⚠ 覆盖率报告生成失败
    echo ⚠ 覆盖率报告生成失败 >> "%LOG_FILE%"
) else (
    echo ✓ 覆盖率报告生成完成
    echo ✓ 覆盖率报告生成完成 >> "%LOG_FILE%"
)

echo.
echo ======================================== 
echo 5. 复制报告到统一目录
echo ======================================== 
echo 5. 复制报告到统一目录 >> "%LOG_FILE%"

:: 复制JaCoCo覆盖率报告
if exist "target\site\jacoco" (
    xcopy /E /I /Y "target\site\jacoco" "%REPORTS_DIR%\coverage\jacoco" >> "%LOG_FILE%" 2>&1
    echo ✓ JaCoCo覆盖率报告已复制到: %REPORTS_DIR%\coverage\jacoco
    echo ✓ JaCoCo覆盖率报告已复制 >> "%LOG_FILE%"
) else (
    echo ⚠ JaCoCo覆盖率报告未找到
    echo ⚠ JaCoCo覆盖率报告未找到 >> "%LOG_FILE%"
)

:: 复制Surefire测试报告
if exist "target\surefire-reports" (
    xcopy /E /I /Y "target\surefire-reports" "%REPORTS_DIR%\unit-tests\surefire-reports" >> "%LOG_FILE%" 2>&1
    echo ✓ Surefire测试报告已复制到: %REPORTS_DIR%\unit-tests\surefire-reports
    echo ✓ Surefire测试报告已复制 >> "%LOG_FILE%"
) else (
    echo ⚠ Surefire测试报告未找到
    echo ⚠ Surefire测试报告未找到 >> "%LOG_FILE%"
)

echo.
echo ======================================== 
echo 测试完成！
echo ======================================== 
echo 测试完成时间: %date% %time% >> "%LOG_FILE%"
echo 测试完成时间: %date% %time%

echo.
echo 报告位置:
echo   测试日志: %LOG_FILE%
echo   覆盖率报告: %REPORTS_DIR%\coverage\jacoco\index.html
echo   测试报告: %REPORTS_DIR%\unit-tests\surefire-reports\

:: 显示测试结果统计
echo.
echo 测试结果统计:
echo 测试结果统计: >> "%LOG_FILE%"

if exist "target\surefire-reports\TEST-*.xml" (
    for /f "tokens=*" %%i in ('findstr /C:"tests=" target\surefire-reports\TEST-*.xml') do (
        echo %%i | findstr "tests=" >> "%LOG_FILE%"
    )
) else (
    echo 未找到测试结果文件
    echo 未找到测试结果文件 >> "%LOG_FILE%"
)

:: 询问是否打开报告
echo.
set /p OPEN_REPORT="是否打开覆盖率报告? (Y/N): "
if /i "%OPEN_REPORT%"=="Y" (
    if exist "%REPORTS_DIR%\coverage\jacoco\index.html" (
        start "" "%REPORTS_DIR%\coverage\jacoco\index.html"
    ) else (
        echo 覆盖率报告文件不存在
    )
)

echo.
echo 单元测试脚本执行完成!
echo 单元测试脚本执行完成! >> "%LOG_FILE%"

if %TEST_RESULT% neq 0 (
    exit /b %TEST_RESULT%
) else (
    exit /b 0
)

:error
echo.
echo ======================================== 
echo 测试执行失败！
echo ======================================== 
echo 测试执行失败！ >> "%LOG_FILE%"
echo 请检查日志文件: %LOG_FILE%
pause
exit /b 1 