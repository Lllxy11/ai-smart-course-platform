@echo off
:: AI智慧课程平台 - 压力测试执行脚本
:: 用于执行系统的压力测试，逐步增加负载找出系统极限

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

:: 压力测试参数 - 逐步增加负载
set STRESS_STAGES=100,200,500,800,1000,1500,2000
set STAGE_DURATION=300
set RAMP_UP=60

:: 设置日志文件
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "YY=%dt:~2,2%" & set "YYYY=%dt:~0,4%" & set "MM=%dt:~4,2%" & set "DD=%dt:~6,2%"
set "HH=%dt:~8,2%" & set "Min=%dt:~10,2%" & set "Sec=%dt:~12,2%"
set "datestamp=%YYYY%%MM%%DD%_%HH%%Min%%Sec%"
set LOG_FILE=%REPORTS_DIR%\stress-test-%datestamp%.log

echo ======================================== 
echo AI智慧课程平台 - 压力测试开始
echo 测试时间: %date% %time%
echo ======================================== 
echo 压力测试配置:
echo   服务器地址: %BASE_URL%
echo   API前缀: %API_PREFIX%
echo   压力阶段: %STRESS_STAGES%
echo   每阶段持续时间: %STAGE_DURATION%秒
echo   预热时间: %RAMP_UP%秒
echo ======================================== 

echo ======================================== >> "%LOG_FILE%"
echo AI智慧课程平台 - 压力测试开始 >> "%LOG_FILE%"
echo 测试时间: %date% %time% >> "%LOG_FILE%"
echo ======================================== >> "%LOG_FILE%"
echo 压力测试配置: >> "%LOG_FILE%"
echo   服务器地址: %BASE_URL% >> "%LOG_FILE%"
echo   API前缀: %API_PREFIX% >> "%LOG_FILE%"
echo   压力阶段: %STRESS_STAGES% >> "%LOG_FILE%"
echo   每阶段持续时间: %STAGE_DURATION%秒 >> "%LOG_FILE%"
echo   预热时间: %RAMP_UP%秒 >> "%LOG_FILE%"
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

:: 警告用户压力测试可能的影响
echo.
echo ⚠ 警告: 压力测试将对系统施加高负载，可能影响系统稳定性
echo ⚠ 建议在专用测试环境中执行压力测试
echo.
set /p CONFIRM="确认要继续执行压力测试吗? (Y/N): "
if /i not "%CONFIRM%"=="Y" (
    echo 测试已取消
    exit /b 0
)

echo.
echo 开始执行压力测试...
echo 开始执行压力测试... >> "%LOG_FILE%"

:: 解析压力阶段并逐个执行
set stage_num=1
for %%s in (%STRESS_STAGES%) do (
    set threads=%%s
    echo.
    echo ======================================== 
    echo 压力测试阶段 !stage_num!: !threads! 并发用户
    echo ======================================== 
    echo 压力测试阶段 !stage_num!: !threads! 并发用户 >> "%LOG_FILE%"
    
    :: 设置当前阶段的结果文件
    set stage_result_file=%REPORTS_DIR%\csv\stress-test-stage!stage_num!-!threads!users-results.jtl
    set stage_html_dir=%REPORTS_DIR%\html\stress-test-stage!stage_num!-!threads!users
    
    :: 删除旧的结果文件
    if exist "!stage_result_file!" del /f "!stage_result_file!"
    if exist "!stage_html_dir!" rmdir /s /q "!stage_html_dir!"
    
    echo 执行阶段 !stage_num!/7: !threads! 用户，持续 %STAGE_DURATION% 秒
    echo 执行阶段 !stage_num!/7: !threads! 用户，持续 %STAGE_DURATION% 秒 >> "%LOG_FILE%"
    
    :: 执行JMeter测试
    jmeter -n -t "%JMETER_SCRIPTS_DIR%\auth-performance.jmx" -l "!stage_result_file!" -e -o "!stage_html_dir!" -JBASE_URL="%BASE_URL%" -JAPI_PREFIX="%API_PREFIX%" -JThreads="!threads!" -JRampUp="%RAMP_UP%" -JDuration="%STAGE_DURATION%" >> "%LOG_FILE%" 2>&1
    
    if errorlevel 1 (
        echo ✗ 压力测试阶段 !stage_num! 执行失败 (可能系统已达到极限)
        echo ✗ 压力测试阶段 !stage_num! 执行失败 >> "%LOG_FILE%"
        echo 系统可能已达到性能极限，建议停止测试
        
        set /p CONTINUE_AFTER_FAIL="是否继续执行后续阶段? (Y/N): "
        if /i not "!CONTINUE_AFTER_FAIL!"=="Y" (
            echo 压力测试已停止在阶段 !stage_num!
            echo 压力测试已停止在阶段 !stage_num! >> "%LOG_FILE%"
            goto :generate_report
        )
    ) else (
        echo ✓ 压力测试阶段 !stage_num! 执行完成
        echo ✓ 压力测试阶段 !stage_num! 执行完成 >> "%LOG_FILE%"
        
        :: 快速显示当前阶段结果
        if exist "!stage_result_file!" (
            echo 阶段结果:
            powershell -Command "$csv = Import-Csv '!stage_result_file!'; $total = $csv.Count; $avg = [math]::Round(($csv | Measure-Object elapsed -Average).Average); $success = ($csv | Where-Object {$_.success -eq 'true'}).Count; $errorRate = [math]::Round((($total - $success) * 100) / $total, 2); Write-Host '  总请求数:' $total; Write-Host '  平均响应时间:' $avg'ms'; Write-Host '  错误率:' $errorRate'%%'"
        )
    )
    
    :: 系统恢复时间
    echo 等待系统恢复...
    timeout /t 30 /nobreak >nul
    
    set /a stage_num+=1
)

:generate_report
echo.
echo ======================================== 
echo 压力测试完成！
echo 测试结束时间: %date% %time%
echo ======================================== 
echo 压力测试完成！ >> "%LOG_FILE%"
echo 测试结束时间: %date% %time% >> "%LOG_FILE%"

:: 生成综合分析报告
echo.
echo 压力测试结果分析:
echo 压力测试结果分析: >> "%LOG_FILE%"

set max_successful_stage=0
set max_successful_users=0

for %%f in ("%REPORTS_DIR%\csv\stress-test-stage*.jtl") do (
    if exist "%%f" (
        set filename=%%~nf
        echo   !filename!:
        echo   !filename!: >> "%LOG_FILE%"
        
        :: 提取用户数
        for /f "tokens=4 delims=-" %%u in ("!filename!") do (
            set users_str=%%u
            set users=!users_str:users=!
            
            :: 检查错误率
            for /f "usebackq tokens=*" %%r in (`powershell -Command "$csv = Import-Csv '%%f'; $total = $csv.Count; $success = ($csv | Where-Object {$_.success -eq 'true'}).Count; $errorRate = [math]::Round((($total - $success) * 100) / $total, 2); $errorRate"`) do (
                set error_rate=%%r
                echo     并发用户: !users!, 错误率: !error_rate!%%
                echo     并发用户: !users!, 错误率: !error_rate!%% >> "%LOG_FILE%"
                
                :: 如果错误率小于5%，认为是成功的
                if !error_rate! lss 5 (
                    if !users! gtr !max_successful_users! (
                        set max_successful_users=!users!
                        set max_successful_stage=!stage_num!
                    )
                )
            )
        )
    )
)

echo.
echo ======================================== 
echo 压力测试结论:
echo ======================================== 
echo 压力测试结论: >> "%LOG_FILE%"

if !max_successful_users! gtr 0 (
    echo ✓ 系统最大承载能力: !max_successful_users! 并发用户
    echo ✓ 建议运营负载: !max_successful_users!/2 并发用户 (50%% 安全裕量)
    echo ✓ 系统最大承载能力: !max_successful_users! 并发用户 >> "%LOG_FILE%"
    echo ✓ 建议运营负载: !max_successful_users!/2 并发用户 >> "%LOG_FILE%"
) else (
    echo ⚠ 系统在最低测试负载下即出现问题，需要性能优化
    echo ⚠ 系统在最低测试负载下即出现问题，需要性能优化 >> "%LOG_FILE%"
)

echo.
echo 报告位置:
echo   测试日志: %LOG_FILE%
echo   CSV结果: %REPORTS_DIR%\csv\stress-test-*.jtl
echo   HTML报告: %REPORTS_DIR%\html\stress-test-*\

:: 询问是否打开报告
echo.
set /p OPEN_REPORTS="是否打开HTML测试报告? (Y/N): "
if /i "%OPEN_REPORTS%"=="Y" (
    for /d %%d in ("%REPORTS_DIR%\html\stress-test-*") do (
        if exist "%%d\index.html" (
            start "" "%%d\index.html"
        )
    )
)

echo.
echo 压力测试脚本执行完成!
echo 压力测试脚本执行完成! >> "%LOG_FILE%"
exit /b 0 