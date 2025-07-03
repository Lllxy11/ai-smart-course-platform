@echo off
:: AI智慧课程平台 - 负载测试执行脚本
:: 用于执行系统的负载测试

setlocal enabledelayedexpansion

:: 设置脚本目录
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..
set JMETER_SCRIPTS_DIR=%PROJECT_ROOT%\jmeter-scripts
set REPORTS_DIR=%PROJECT_ROOT%\reports
set TEST_DATA_DIR=%PROJECT_ROOT%\test-data

:: 确保报告目录存在
if not exist "%REPORTS_DIR%\csv" mkdir "%REPORTS_DIR%\csv"
if not exist "%REPORTS_DIR%\html" mkdir "%REPORTS_DIR%\html"
if not exist "%REPORTS_DIR%\screenshots" mkdir "%REPORTS_DIR%\screenshots"

:: 设置测试参数
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8080
if "%API_PREFIX%"=="" set API_PREFIX=/api
if "%THREADS%"=="" set THREADS=50
if "%RAMP_UP%"=="" set RAMP_UP=60
if "%DURATION%"=="" set DURATION=600

:: 设置日志文件
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "datestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"
set LOG_FILE=%REPORTS_DIR%\load-test-%datestamp%.log

echo ======================================== 
echo AI智慧课程平台 - 负载测试开始
echo 测试时间: %date% %time%
echo ======================================== 
echo 测试配置:
echo   服务器地址: %BASE_URL%
echo   API前缀: %API_PREFIX%
echo   并发用户数: %THREADS%
echo   预热时间: %RAMP_UP%秒
echo   测试持续时间: %DURATION%秒
echo ======================================== 

echo ======================================== >> "%LOG_FILE%"
echo AI智慧课程平台 - 负载测试开始 >> "%LOG_FILE%"
echo 测试时间: %date% %time% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"
echo 测试配置: >> "%LOG_FILE%"
echo   服务器地址: %BASE_URL% >> "%LOG_FILE%"
echo   API前缀: %API_PREFIX% >> "%LOG_FILE%"
echo   并发用户数: %THREADS% >> "%LOG_FILE%"
echo   预热时间: %RAMP_UP%秒 >> "%LOG_FILE%"
echo   测试持续时间: %DURATION%秒 >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"

:: 检查JMeter是否安装
where jmeter >nul 2>nul
if errorlevel 1 (
    echo 错误: JMeter 未安装或不在PATH中
    echo 错误: JMeter 未安装或不在PATH中 >> "%LOG_FILE%"
    echo 请确保 Apache JMeter 已正确安装并配置环境变量
    echo.
    echo 您可以从以下地址下载JMeter:
    echo https://jmeter.apache.org/download_jmeter.cgi
    echo.
    echo 安装后请将JMeter的bin目录添加到系统PATH中
    pause
    exit /b 1
)

:: 检查后端服务是否启动
echo 检查后端服务...
echo 检查后端服务... >> "%LOG_FILE%"

:: 使用PowerShell检查服务状态
powershell -Command "try { $response = Invoke-WebRequest -Uri '%BASE_URL%/actuator/health' -Method GET -TimeoutSec 5; exit 0 } catch { exit 1 }" >nul 2>nul
if errorlevel 1 (
    echo 警告: 无法访问后端服务 %BASE_URL%
    echo 警告: 无法访问后端服务 %BASE_URL% >> "%LOG_FILE%"
    echo 请确保后端服务已启动
    echo.
    set /p CONTINUE="是否继续测试? (Y/N): "
    if /i not "!CONTINUE!"=="Y" (
        exit /b 1
    )
) else (
    echo ✓ 后端服务运行正常
    echo ✓ 后端服务运行正常 >> "%LOG_FILE%"
)

:: 测试函数
goto :main

:run_jmeter_test
set test_name=%1
set jmx_file=%2
set result_file=%3
set html_report_dir=%4

echo.
echo 正在执行 %test_name%...
echo 正在执行 %test_name%... >> "%LOG_FILE%"

:: 删除旧的结果文件
if exist "%result_file%" del /f "%result_file%"
if exist "%html_report_dir%" rmdir /s /q "%html_report_dir%"

:: 执行JMeter测试
jmeter -n -t "%jmx_file%" -l "%result_file%" -e -o "%html_report_dir%" -JBASE_URL="%BASE_URL%" -JAPI_PREFIX="%API_PREFIX%" -JThreads="%THREADS%" -JRampUp="%RAMP_UP%" -JDuration="%DURATION%" >> "%LOG_FILE%" 2>&1

if errorlevel 1 (
    echo ✗ %test_name% 执行失败
    echo ✗ %test_name% 执行失败 >> "%LOG_FILE%"
    exit /b 1
) else (
    echo ✓ %test_name% 执行完成
    echo ✓ %test_name% 执行完成 >> "%LOG_FILE%"
    echo   结果文件: %result_file%
    echo   结果文件: %result_file% >> "%LOG_FILE%"
    echo   HTML报告: %html_report_dir%\index.html
    echo   HTML报告: %html_report_dir%\index.html >> "%LOG_FILE%"
)
exit /b 0

:main

:: 1. 认证接口负载测试
echo.
echo 1. 执行认证接口负载测试...
echo 1. 执行认证接口负载测试... >> "%LOG_FILE%"

call :run_jmeter_test "认证接口负载测试" "%JMETER_SCRIPTS_DIR%\auth-performance.jmx" "%REPORTS_DIR%\csv\auth-load-test-results.jtl" "%REPORTS_DIR%\html\auth-load-test"
if errorlevel 1 goto :error

:: 2. 课程接口负载测试
echo.
echo 2. 执行课程接口负载测试...
echo 2. 执行课程接口负载测试... >> "%LOG_FILE%"

if exist "%JMETER_SCRIPTS_DIR%\course-performance.jmx" (
    call :run_jmeter_test "课程接口负载测试" "%JMETER_SCRIPTS_DIR%\course-performance.jmx" "%REPORTS_DIR%\csv\course-load-test-results.jtl" "%REPORTS_DIR%\html\course-load-test"
    if errorlevel 1 goto :error
) else (
    echo ⚠ 课程接口测试脚本不存在，跳过...
    echo ⚠ 课程接口测试脚本不存在，跳过... >> "%LOG_FILE%"
)

:: 3. 用户管理接口负载测试
echo.
echo 3. 执行用户管理接口负载测试...
echo 3. 执行用户管理接口负载测试... >> "%LOG_FILE%"

if exist "%JMETER_SCRIPTS_DIR%\user-performance.jmx" (
    call :run_jmeter_test "用户管理接口负载测试" "%JMETER_SCRIPTS_DIR%\user-performance.jmx" "%REPORTS_DIR%\csv\user-load-test-results.jtl" "%REPORTS_DIR%\html\user-load-test"
    if errorlevel 1 goto :error
) else (
    echo ⚠ 用户管理接口测试脚本不存在，跳过...
    echo ⚠ 用户管理接口测试脚本不存在，跳过... >> "%LOG_FILE%"
)

:: 生成综合报告
echo.
echo ======================================== 
echo 负载测试完成！
echo 测试结束时间: %date% %time%
echo ======================================== 
echo 报告位置:
echo   测试日志: %LOG_FILE%
echo   CSV结果: %REPORTS_DIR%\csv\
echo   HTML报告: %REPORTS_DIR%\html\

echo ======================================== >> "%LOG_FILE%"
echo 负载测试完成！ >> "%LOG_FILE%"
echo 测试结束时间: %date% %time% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"

:: 显示快速统计
echo.
echo 快速统计信息:
echo 快速统计信息: >> "%LOG_FILE%"

for %%f in ("%REPORTS_DIR%\csv\*.jtl") do (
    if exist "%%f" (
        set filename=%%~nf
        echo   !filename!:
        echo   !filename!: >> "%LOG_FILE%"
        
        :: 使用PowerShell计算统计信息
        powershell -Command "$csv = Import-Csv '%%f'; $total = $csv.Count; $avg = [math]::Round(($csv | Measure-Object elapsed -Average).Average); $success = ($csv | Where-Object {$_.success -eq 'true'}).Count; $errorRate = [math]::Round((($total - $success) * 100) / $total, 2); Write-Host '    总请求数:' $total; Write-Host '    平均响应时间:' $avg'ms'; Write-Host '    成功请求数:' $success; Write-Host '    错误率:' $errorRate'%%'" >> "%LOG_FILE%" 2>&1
        
        powershell -Command "$csv = Import-Csv '%%f'; $total = $csv.Count; $avg = [math]::Round(($csv | Measure-Object elapsed -Average).Average); $success = ($csv | Where-Object {$_.success -eq 'true'}).Count; $errorRate = [math]::Round((($total - $success) * 100) / $total, 2); Write-Host '    总请求数:' $total; Write-Host '    平均响应时间:' $avg'ms'; Write-Host '    成功请求数:' $success; Write-Host '    错误率:' $errorRate'%%'"
    )
)

echo.
echo 使用浏览器打开 HTML 报告查看详细结果:
echo 使用浏览器打开 HTML 报告查看详细结果: >> "%LOG_FILE%"

for /d %%d in ("%REPORTS_DIR%\html\*") do (
    if exist "%%d\index.html" (
        echo   file:///%%d\index.html
        echo   file:///%%d\index.html >> "%LOG_FILE%"
    )
)

:: 询问是否打开报告
echo.
set /p OPEN_REPORTS="是否打开HTML测试报告? (Y/N): "
if /i "%OPEN_REPORTS%"=="Y" (
    for /d %%d in ("%REPORTS_DIR%\html\*") do (
        if exist "%%d\index.html" (
            start "" "%%d\index.html"
        )
    )
)

echo.
echo 负载测试脚本执行完成!
echo 负载测试脚本执行完成! >> "%LOG_FILE%"
exit /b 0

:error
echo.
echo ======================================== 
echo 负载测试执行失败！
echo ======================================== 
echo 负载测试执行失败！ >> "%LOG_FILE%"
echo 请检查日志文件: %LOG_FILE%
pause
exit /b 1 