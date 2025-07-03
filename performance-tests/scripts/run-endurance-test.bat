@echo off
:: AI智慧课程平台 - 稳定性测试执行脚本
:: 用于执行长时间稳定性测试，检查内存泄漏和性能衰减

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

:: 稳定性测试参数 - 长时间运行
set THREADS=200
set RAMP_UP=300
set DURATION=28800
:: 28800秒 = 8小时

:: 设置日志文件
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "datestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"
set LOG_FILE=%REPORTS_DIR%\endurance-test-%datestamp%.log

echo ======================================== 
echo AI智慧课程平台 - 稳定性测试开始
echo 测试时间: %date% %time%
echo ======================================== 
echo 稳定性测试配置:
echo   服务器地址: %BASE_URL%
echo   API前缀: %API_PREFIX%
echo   并发用户数: %THREADS%
echo   预热时间: %RAMP_UP%秒 (5分钟)
echo   测试持续时间: %DURATION%秒 (8小时)
echo ======================================== 

echo ======================================== >> "%LOG_FILE%"
echo AI智慧课程平台 - 稳定性测试开始 >> "%LOG_FILE%"
echo 测试时间: %date% %time% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"
echo 稳定性测试配置: >> "%LOG_FILE%"
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
    pause
    exit /b 1
)

:: 检查后端服务是否启动
echo 检查后端服务...
echo 检查后端服务... >> "%LOG_FILE%"

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

:: 警告用户稳定性测试的长时间特性
echo.
echo ⚠ 警告: 稳定性测试将持续8小时，请确保:
echo   1. 测试机器有足够的资源和电源
echo   2. 网络连接稳定
echo   3. 服务器有足够的监控
echo   4. 测试期间不要关闭此窗口
echo.
set /p CONFIRM="确认要继续执行8小时稳定性测试吗? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo 测试已取消
    exit /b 0
)

:: 创建系统监控脚本
echo 创建系统监控脚本...
echo 创建系统监控脚本... >> "%LOG_FILE%"

set MONITOR_SCRIPT=%REPORTS_DIR%\system_monitor_%datestamp%.bat
echo @echo off > "%MONITOR_SCRIPT%"
echo :: 系统监控脚本 - 每5分钟记录一次系统状态 >> "%MONITOR_SCRIPT%"
echo set LOG_FILE=%REPORTS_DIR%\system_monitor_%datestamp%.log >> "%MONITOR_SCRIPT%"
echo :monitor_loop >> "%MONITOR_SCRIPT%"
echo echo ======================================== ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo echo 系统监控时间: %%date%% %%time%% ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo echo ======================================== ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo wmic cpu get loadpercentage /value ^| findstr LoadPercentage ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo wmic OS get TotalVisibleMemorySize,FreePhysicalMemory /value ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo netstat -an ^| find "ESTABLISHED" ^| find "8080" ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo echo. ^>^> "%%LOG_FILE%%" >> "%MONITOR_SCRIPT%"
echo timeout /t 300 /nobreak ^>nul >> "%MONITOR_SCRIPT%"
echo goto monitor_loop >> "%MONITOR_SCRIPT%"

:: 启动系统监控
echo 启动系统监控进程...
echo 启动系统监控进程... >> "%LOG_FILE%"
start /min cmd /c "%MONITOR_SCRIPT%"

:: 计算预计结束时间
for /f "tokens=1-4 delims=:.," %%a in ("%time%") do (
    set /a "start_seconds=((%%a*60)+%%b)*60+%%c"
)
set /a "end_seconds=start_seconds+%DURATION%"
set /a "end_hours=end_seconds/3600"
set /a "end_minutes=(end_seconds%%3600)/60"
set /a "end_secs=end_seconds%%60"

if %end_hours% geq 24 (
    set /a "end_hours=end_hours-24"
    echo 预计结束时间: 明天 %end_hours%:%end_minutes%:%end_secs%
    echo 预计结束时间: 明天 %end_hours%:%end_minutes%:%end_secs% >> "%LOG_FILE%"
) else (
    echo 预计结束时间: 今天 %end_hours%:%end_minutes%:%end_secs%
    echo 预计结束时间: 今天 %end_hours%:%end_minutes%:%end_secs% >> "%LOG_FILE%"
)

echo.
echo 开始执行稳定性测试...
echo 开始执行稳定性测试... >> "%LOG_FILE%"

:: 设置结果文件
set RESULT_FILE=%REPORTS_DIR%\csv\endurance-test-results.jtl
set HTML_DIR=%REPORTS_DIR%\html\endurance-test

:: 删除旧的结果文件
if exist "%RESULT_FILE%" del /f "%RESULT_FILE%"
if exist "%HTML_DIR%" rmdir /s /q "%HTML_DIR%"

:: 执行JMeter稳定性测试
echo 正在执行稳定性测试，请耐心等待...
echo 您可以在另一个终端中监控以下文件查看实时进度:
echo   实时结果: %RESULT_FILE%
echo   系统监控: %REPORTS_DIR%\system_monitor_%datestamp%.log

jmeter -n -t "%JMETER_SCRIPTS_DIR%\auth-performance.jmx" -l "%RESULT_FILE%" -e -o "%HTML_DIR%" -JBASE_URL="%BASE_URL%" -JAPI_PREFIX="%API_PREFIX%" -JThreads="%THREADS%" -JRampUp="%RAMP_UP%" -JDuration="%DURATION%" >> "%LOG_FILE%" 2>&1

set TEST_RESULT=%errorlevel%

:: 停止系统监控
echo 停止系统监控...
taskkill /f /im cmd.exe /fi "WINDOWTITLE eq *system_monitor*" >nul 2>nul

if %TEST_RESULT% neq 0 (
    echo ✗ 稳定性测试执行失败或被中断
    echo ✗ 稳定性测试执行失败或被中断 >> "%LOG_FILE%"
) else (
    echo ✓ 稳定性测试执行完成
    echo ✓ 稳定性测试执行完成 >> "%LOG_FILE%"
)

echo.
echo ======================================== 
echo 稳定性测试完成！
echo 测试结束时间: %date% %time%
echo ======================================== 
echo 稳定性测试完成！ >> "%LOG_FILE%"
echo 测试结束时间: %date% %time% >> "%LOG_FILE%"

:: 生成稳定性分析报告
echo.
echo 稳定性测试结果分析:
echo 稳定性测试结果分析: >> "%LOG_FILE%"

if exist "%RESULT_FILE%" (
    echo 正在分析测试结果...
    echo 正在分析测试结果... >> "%LOG_FILE%"
    
    :: 使用PowerShell分析稳定性数据
    powershell -Command "
    $csv = Import-Csv '%RESULT_FILE%'
    $total = $csv.Count
    $success = ($csv | Where-Object {$_.success -eq 'true'}).Count
    $errorRate = [math]::Round((($total - $success) * 100) / $total, 2)
    $avgResponseTime = [math]::Round(($csv | Measure-Object elapsed -Average).Average)
    $maxResponseTime = ($csv | Measure-Object elapsed -Maximum).Maximum
    $minResponseTime = ($csv | Measure-Object elapsed -Minimum).Minimum
    
    # 按小时分组分析性能趋势
    $csv | ForEach-Object { 
        $_.timestamp = [DateTime]::ParseExact($_.timeStamp, 'yyyy-MM-dd HH:mm:ss', $null)
        $_
    } | Group-Object {$_.timestamp.Hour} | ForEach-Object {
        $hour = $_.Name
        $hourData = $_.Group
        $hourAvg = [math]::Round(($hourData | Measure-Object elapsed -Average).Average)
        $hourError = [math]::Round((($hourData.Count - ($hourData | Where-Object {$_.success -eq 'true'}).Count) * 100) / $hourData.Count, 2)
        Write-Host \"  第${hour}小时: 平均响应时间 ${hourAvg}ms, 错误率 ${hourError}%%\"
    }
    
    Write-Host \"\"
    Write-Host \"稳定性测试总结:\"
    Write-Host \"  总请求数: $total\"
    Write-Host \"  成功请求数: $success\"
    Write-Host \"  总体错误率: ${errorRate}%%\"
    Write-Host \"  平均响应时间: ${avgResponseTime}ms\"
    Write-Host \"  最大响应时间: ${maxResponseTime}ms\"
    Write-Host \"  最小响应时间: ${minResponseTime}ms\"
    
    if ($errorRate -lt 1) {
        Write-Host \"✓ 系统稳定性良好，长时间运行无明显性能衰减\"
    } elseif ($errorRate -lt 5) {
        Write-Host \"⚠ 系统稳定性一般，建议优化部分组件\"
    } else {
        Write-Host \"✗ 系统稳定性较差，存在明显的性能问题\"
    }
    " >> "%LOG_FILE%" 2>&1
    
    :: 同时显示在控制台
    powershell -Command "
    $csv = Import-Csv '%RESULT_FILE%'
    $total = $csv.Count
    $success = ($csv | Where-Object {$_.success -eq 'true'}).Count
    $errorRate = [math]::Round((($total - $success) * 100) / $total, 2)
    $avgResponseTime = [math]::Round(($csv | Measure-Object elapsed -Average).Average)
    
    Write-Host \"稳定性测试总结:\"
    Write-Host \"  总请求数: $total\"
    Write-Host \"  成功请求数: $success\"
    Write-Host \"  总体错误率: ${errorRate}%%\"
    Write-Host \"  平均响应时间: ${avgResponseTime}ms\"
    
    if ($errorRate -lt 1) {
        Write-Host \"✓ 系统稳定性良好\"
    } elseif ($errorRate -lt 5) {
        Write-Host \"⚠ 系统稳定性一般\"
    } else {
        Write-Host \"✗ 系统稳定性较差\"
    }
    "
) else (
    echo ⚠ 测试结果文件不存在
    echo ⚠ 测试结果文件不存在 >> "%LOG_FILE%"
)

echo.
echo 报告位置:
echo   测试日志: %LOG_FILE%
echo   CSV结果: %RESULT_FILE%
echo   HTML报告: %HTML_DIR%\index.html
echo   系统监控: %REPORTS_DIR%\system_monitor_%datestamp%.log

:: 询问是否打开报告
echo.
set /p OPEN_REPORT="是否打开HTML测试报告? (Y/N): "
if /i "%OPEN_REPORT%"=="Y" (
    if exist "%HTML_DIR%\index.html" (
        start "" "%HTML_DIR%\index.html"
    )
)

echo.
echo 稳定性测试脚本执行完成!
echo 稳定性测试脚本执行完成! >> "%LOG_FILE%"

if %TEST_RESULT% neq 0 (
    exit /b %TEST_RESULT%
) else (
    exit /b 0
) 