@echo off
chcp 65001 >nul
:: AI智慧课程平台 - 主测试执行脚本
:: 提供菜单选择执行不同类型的测试

setlocal enabledelayedexpansion

:: 设置脚本目录
set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..

:: 设置颜色输出
for /F %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"

:main_menu
cls
echo.
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m    AI智慧课程平台 - 测试管理中心    %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[96m请选择要执行的测试类型:%ESC%[0m
echo.
echo %ESC%[93m  1. 单元测试 (Unit Tests)%ESC%[0m
echo     - 执行所有单元测试
echo     - 生成测试覆盖率报告
echo     - 预计耗时: 5-10分钟
echo.
echo %ESC%[93m  2. 负载测试 (Load Tests)%ESC%[0m
echo     - 验证系统在预期负载下的性能
echo     - 50并发用户，持续10分钟
echo     - 预计耗时: 15分钟
echo.
echo %ESC%[93m  3. 压力测试 (Stress Tests)%ESC%[0m
echo     - 逐步增加负载找出系统极限
echo     - 100-2000并发用户，7个阶段
echo     - 预计耗时: 40-60分钟
echo.
echo %ESC%[93m  4. 稳定性测试 (Endurance Tests)%ESC%[0m
echo     - 长时间运行检查内存泄漏
echo     - 200并发用户，持续8小时
echo     - 预计耗时: 8小时
echo.
echo %ESC%[93m  5. 生成PDF测试报告 (Generate PDF Reports)%ESC%[0m
echo     - 生成4份标准PDF测试报告
echo     - 1份完整测试报告 + 3份模块报告
echo     - 预计耗时: 2-5分钟
echo.
echo %ESC%[93m  6. 完整测试套件 (Full Test Suite)%ESC%[0m
echo     - 依次执行单元测试和性能测试
echo     - 不包含稳定性测试
echo     - 预计耗时: 1-2小时
echo.
echo %ESC%[91m  7. 清理测试报告 (Clean Reports)%ESC%[0m
echo     - 删除所有测试报告和临时文件
echo.
echo %ESC%[94m  8. 查看测试报告 (View Reports)%ESC%[0m
echo     - 打开现有的测试报告
echo.
echo %ESC%[95m  9. 安装PDF生成依赖 (Install Dependencies)%ESC%[0m
echo     - 安装Python reportlab库
echo.
echo %ESC%[90m  0. 退出 (Exit)%ESC%[0m
echo.
echo %ESC%[92m========================================%ESC%[0m

set /p choice=%ESC%[96m请输入选项 (0-9): %ESC%[0m

if "%choice%"=="1" goto unit_tests
if "%choice%"=="2" goto load_tests
if "%choice%"=="3" goto stress_tests
if "%choice%"=="4" goto endurance_tests
if "%choice%"=="5" goto generate_pdf
if "%choice%"=="6" goto full_suite
if "%choice%"=="7" goto clean_reports
if "%choice%"=="8" goto view_reports
if "%choice%"=="9" goto install_dependencies
if "%choice%"=="0" goto exit

echo %ESC%[91m无效选项，请重新选择！%ESC%[0m
timeout /t 2 >nul
goto main_menu

:unit_tests
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m           执行单元测试              %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[96m正在启动单元测试...%ESC%[0m
cd /d "%SCRIPT_DIR%"
call run-unit-tests.bat
echo.
echo %ESC%[96m单元测试执行完成！%ESC%[0m
goto return_to_menu

:load_tests
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m           执行负载测试              %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[96m正在启动负载测试...%ESC%[0m
cd /d "%SCRIPT_DIR%"
call run-load-test.bat
echo.
echo %ESC%[96m负载测试执行完成！%ESC%[0m
goto return_to_menu

:stress_tests
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m           执行压力测试              %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[91m警告: 压力测试将对系统施加高负载！%ESC%[0m
echo %ESC%[96m正在启动压力测试...%ESC%[0m
cd /d "%SCRIPT_DIR%"
call run-stress-test.bat
echo.
echo %ESC%[96m压力测试执行完成！%ESC%[0m
goto return_to_menu

:endurance_tests
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m           执行稳定性测试            %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[91m警告: 稳定性测试将持续8小时！%ESC%[0m
echo %ESC%[96m正在启动稳定性测试...%ESC%[0m
cd /d "%SCRIPT_DIR%"
call run-endurance-test.bat
echo.
echo %ESC%[96m稳定性测试执行完成！%ESC%[0m
goto return_to_menu

:generate_pdf
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m         生成PDF测试报告            %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.

:: 检查Python是否可用
python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo %ESC%[91m错误: 未检测到Python，请先安装Python 3.7+%ESC%[0m
    echo %ESC%[96m或选择选项9安装依赖%ESC%[0m
    goto return_to_menu
)

:: 检查reportlab是否已安装
python -c "import reportlab" >nul 2>&1
if %errorlevel% neq 0 (
    echo %ESC%[93m警告: 未安装reportlab库%ESC%[0m
    set /p install_choice=%ESC%[96m是否现在安装? (Y/N): %ESC%[0m
    if /i "!install_choice!"=="Y" (
        echo %ESC%[96m正在安装reportlab...%ESC%[0m
        pip install reportlab
        if errorlevel 1 (
            echo %ESC%[91m安装失败，请手动执行: pip install reportlab%ESC%[0m
            goto return_to_menu
        )
        echo %ESC%[92m✓ reportlab安装成功%ESC%[0m
    ) else (
        echo %ESC%[96m操作已取消%ESC%[0m
        goto return_to_menu
    )
)

echo %ESC%[96m正在生成PDF测试报告...%ESC%[0m
echo.

:: 确保项目根目录路径正确
set PDF_PROJECT_ROOT=%PROJECT_ROOT%
if "%PDF_PROJECT_ROOT:~-1%"=="\" set PDF_PROJECT_ROOT=%PDF_PROJECT_ROOT:~0,-1%

:: 生成完整测试报告和模块报告
python "%SCRIPT_DIR%pdf-generator.py" "%PDF_PROJECT_ROOT%" --complete --modules

if %errorlevel% neq 0 (
    echo %ESC%[91m PDF报告生成失败%ESC%[0m
    echo %ESC%[96m请检查测试结果是否存在，或手动执行Python脚本%ESC%[0m
) else (
    echo.
    echo %ESC%[92m========================================%ESC%[0m
    echo %ESC%[92m       PDF报告生成成功！             %ESC%[0m
    echo %ESC%[92m========================================%ESC%[0m
    echo.
    echo %ESC%[96m生成的PDF文件:%ESC%[0m
    echo %ESC%[93m  1. 完整测试报告.pdf%ESC%[0m
    echo %ESC%[93m  2. 认证模块测试报告.pdf%ESC%[0m
    echo %ESC%[93m  3. 用户管理模块测试报告.pdf%ESC%[0m
    echo %ESC%[93m  4. 课程管理模块测试报告.pdf%ESC%[0m
    echo.
    echo %ESC%[96m报告位置: %PROJECT_ROOT%\reports\pdf\%ESC%[0m
    echo.
    set /p open_folder=%ESC%[96m是否打开PDF报告文件夹? (Y/N): %ESC%[0m
    if /i "!open_folder!"=="Y" (
        start "" "%PROJECT_ROOT%\reports\pdf"
    )
)

goto return_to_menu

:install_dependencies
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m         安装PDF生成依赖            %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[96m正在执行依赖安装...%ESC%[0m
cd /d "%SCRIPT_DIR%"
call install-dependencies.bat
echo.
echo %ESC%[96m依赖安装完成！%ESC%[0m
goto return_to_menu

:full_suite
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m         执行完整测试套件            %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[96m开始执行完整测试套件...%ESC%[0m
echo.

:: 1. 单元测试
echo %ESC%[93m步骤 1/3: 执行单元测试%ESC%[0m
cd /d "%SCRIPT_DIR%"
call run-unit-tests.bat
if errorlevel 1 (
    echo %ESC%[91m单元测试失败，停止后续测试%ESC%[0m
    goto return_to_menu
)

echo.
echo %ESC%[93m步骤 2/3: 执行负载测试%ESC%[0m
call run-load-test.bat
if errorlevel 1 (
    echo %ESC%[91m负载测试失败，继续执行压力测试%ESC%[0m
)

echo.
echo %ESC%[93m步骤 3/3: 执行压力测试%ESC%[0m
call run-stress-test.bat

echo.
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m         完整测试套件执行完成        %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
goto return_to_menu

:clean_reports
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m           清理测试报告              %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[91m警告: 此操作将删除所有测试报告和日志文件！%ESC%[0m
set /p confirm=%ESC%[96m确认要继续吗? (Y/N): %ESC%[0m

if /i not "%confirm%"=="Y" (
    echo %ESC%[96m操作已取消%ESC%[0m
    goto return_to_menu
)

echo.
echo %ESC%[96m正在清理测试报告...%ESC%[0m

cd /d "%PROJECT_ROOT%"
if exist "reports" (
    rmdir /s /q "reports"
    echo %ESC%[92m✓ 已删除测试报告目录%ESC%[0m
) else (
    echo %ESC%[93m⚠ 测试报告目录不存在%ESC%[0m
)

cd /d "backend-java"
if exist "target\site\jacoco" (
    rmdir /s /q "target\site\jacoco"
    echo %ESC%[92m✓ 已删除覆盖率报告%ESC%[0m
)

if exist "target\surefire-reports" (
    rmdir /s /q "target\surefire-reports"
    echo %ESC%[92m✓ 已删除单元测试报告%ESC%[0m
)

echo.
echo %ESC%[92m清理完成！%ESC%[0m
goto return_to_menu

:view_reports
cls
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m           查看测试报告              %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.

set found_reports=0

:: 检查单元测试覆盖率报告
if exist "%PROJECT_ROOT%\reports\coverage\jacoco\index.html" (
    set /a found_reports+=1
    echo %ESC%[93m%found_reports%. 单元测试覆盖率报告%ESC%[0m
    echo   %PROJECT_ROOT%\reports\coverage\jacoco\index.html
    echo.
)

:: 检查性能测试报告
for /d %%d in ("%PROJECT_ROOT%\reports\html\*") do (
    if exist "%%d\index.html" (
        set /a found_reports+=1
        for %%f in ("%%d") do set "report_name=%%~nf"
        echo %ESC%[93m!found_reports!. 性能测试报告 - !report_name!%ESC%[0m
        echo   %%d\index.html
        echo.
    )
)

:: 检查PDF报告
if exist "%PROJECT_ROOT%\reports\pdf\*.pdf" (
    set /a found_reports+=1
    echo %ESC%[93m!found_reports!. PDF测试报告文件夹%ESC%[0m
    echo   %PROJECT_ROOT%\reports\pdf\
    echo.
)

if %found_reports%==0 (
    echo %ESC%[91m未找到任何测试报告%ESC%[0m
    echo %ESC%[96m请先执行测试生成报告%ESC%[0m
    goto return_to_menu
)

echo %ESC%[94m0. 返回主菜单%ESC%[0m
echo.
set /p report_choice=%ESC%[96m请选择要查看的报告 (0-%found_reports%): %ESC%[0m

if "%report_choice%"=="0" goto main_menu

:: 打开对应的报告
set current_num=0

if exist "%PROJECT_ROOT%\reports\coverage\jacoco\index.html" (
    set /a current_num+=1
    if "%report_choice%"=="!current_num!" (
        start "" "%PROJECT_ROOT%\reports\coverage\jacoco\index.html"
        echo %ESC%[92m已打开单元测试覆盖率报告%ESC%[0m
        goto return_to_menu
    )
)

for /d %%d in ("%PROJECT_ROOT%\reports\html\*") do (
    if exist "%%d\index.html" (
        set /a current_num+=1
        if "%report_choice%"=="!current_num!" (
            start "" "%%d\index.html"
            for %%f in ("%%d") do set "report_name=%%~nf"
            echo %ESC%[92m已打开性能测试报告 - !report_name!%ESC%[0m
            goto return_to_menu
        )
    )
)

if exist "%PROJECT_ROOT%\reports\pdf\*.pdf" (
    set /a current_num+=1
    if "%report_choice%"=="!current_num!" (
        start "" "%PROJECT_ROOT%\reports\pdf"
        echo %ESC%[92m已打开PDF报告文件夹%ESC%[0m
        goto return_to_menu
    )
)

echo %ESC%[91m无效选项！%ESC%[0m
timeout /t 2 >nul
goto view_reports

:return_to_menu
echo.
set /p return_choice=%ESC%[96m按任意键返回主菜单...%ESC%[0m
goto main_menu

:exit
cls
echo.
echo %ESC%[92m========================================%ESC%[0m
echo %ESC%[92m     感谢使用AI智慧课程平台测试工具    %ESC%[0m
echo %ESC%[92m========================================%ESC%[0m
echo.
echo %ESC%[96m测试报告位置:%ESC%[0m
echo   %PROJECT_ROOT%\reports\
echo.
echo %ESC%[96m如有问题，请查看日志文件或联系开发团队%ESC%[0m
echo.
exit /b 0 