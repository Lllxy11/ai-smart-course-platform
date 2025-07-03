#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import sys
import xml.etree.ElementTree as ET
import csv
from datetime import datetime
from reportlab.lib.pagesizes import A4
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib import colors
from reportlab.lib.units import inch
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.enums import TA_CENTER, TA_LEFT

def setup_chinese_fonts():
    """设置中文字体"""
    try:
        # 尝试不同的中文字体路径
        font_paths = [
            "C:/Windows/Fonts/msyh.ttc",  # 微软雅黑
            "C:/Windows/Fonts/simhei.ttf",  # 黑体
            "C:/Windows/Fonts/simsun.ttc",  # 宋体
        ]
        
        for font_path in font_paths:
            if os.path.exists(font_path):
                try:
                    if font_path.endswith('.ttc'):
                        pdfmetrics.registerFont(TTFont('ChineseFont', font_path, subfontIndex=0))
                    else:
                        pdfmetrics.registerFont(TTFont('ChineseFont', font_path))
                    print(f"成功加载字体: {font_path}")
                    return True
                except Exception as e:
                    print(f"尝试加载字体 {font_path} 失败: {e}")
                    continue
        
        print("警告: 无法加载中文字体，使用默认字体")
        return False
    except Exception as e:
        print(f"字体设置错误: {e}")
        return False

def read_junit_xml(file_path):
    """读取JUnit XML测试报告"""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        
        test_data = {
            'total_tests': int(root.get('tests', 0)),
            'failures': int(root.get('failures', 0)),
            'errors': int(root.get('errors', 0)),
            'skipped': int(root.get('skipped', 0)),
            'time': float(root.get('time', 0)),
            'success_tests': 0,
            'test_cases': []
        }
        
        test_data['success_tests'] = test_data['total_tests'] - test_data['failures'] - test_data['errors'] - test_data['skipped']
        
        for testcase in root.findall('testcase'):
            case_data = {
                'name': testcase.get('name'),
                'classname': testcase.get('classname'),
                'time': float(testcase.get('time', 0)),
                'status': 'PASS'
            }
            
            if testcase.find('failure') is not None:
                case_data['status'] = 'FAIL'
                case_data['message'] = testcase.find('failure').get('message', '')
            elif testcase.find('error') is not None:
                case_data['status'] = 'ERROR'
                case_data['message'] = testcase.find('error').get('message', '')
            elif testcase.find('skipped') is not None:
                case_data['status'] = 'SKIP'
                
            test_data['test_cases'].append(case_data)
        
        return test_data
    except Exception as e:
        print(f"读取JUnit XML文件失败: {e}")
        return None

def read_jacoco_csv(file_path):
    """读取JaCoCo CSV覆盖率报告"""
    try:
        coverage_data = {}
        with open(file_path, 'r', encoding='utf-8') as f:
            reader = csv.DictReader(f)
            for row in reader:
                package = row.get('PACKAGE', '')
                if package.startswith('com.aicourse'):
                    coverage_data[package] = {
                        'instruction_covered': int(row.get('INSTRUCTION_COVERED', 0)),
                        'instruction_missed': int(row.get('INSTRUCTION_MISSED', 0)),
                        'branch_covered': int(row.get('BRANCH_COVERED', 0)),
                        'branch_missed': int(row.get('BRANCH_MISSED', 0)),
                        'line_covered': int(row.get('LINE_COVERED', 0)),
                        'line_missed': int(row.get('LINE_MISSED', 0))
                    }
        
        # 计算总体覆盖率
        total_instructions = sum(data['instruction_covered'] + data['instruction_missed'] for data in coverage_data.values())
        covered_instructions = sum(data['instruction_covered'] for data in coverage_data.values())
        total_lines = sum(data['line_covered'] + data['line_missed'] for data in coverage_data.values())
        covered_lines = sum(data['line_covered'] for data in coverage_data.values())
        
        overall_coverage = {
            'instruction_coverage': (covered_instructions / total_instructions * 100) if total_instructions > 0 else 0,
            'line_coverage': (covered_lines / total_lines * 100) if total_lines > 0 else 0,
            'total_instructions': total_instructions,
            'covered_instructions': covered_instructions,
            'total_lines': total_lines,
            'covered_lines': covered_lines
        }
        
        return overall_coverage, coverage_data
    except Exception as e:
        print(f"读取JaCoCo CSV文件失败: {e}")
        return None, None

def create_pdf_with_real_data(output_path, title, test_data, coverage_data):
    """使用真实数据创建PDF报告"""
    doc = SimpleDocTemplate(output_path, pagesize=A4)
    story = []
    
    # 设置样式
    styles = getSampleStyleSheet()
    if setup_chinese_fonts():
        title_style = ParagraphStyle(
            'ChineseTitle',
            parent=styles['Title'],
            fontName='ChineseFont',
            fontSize=18,
            alignment=TA_CENTER,
            spaceAfter=30
        )
        heading_style = ParagraphStyle(
            'ChineseHeading',
            parent=styles['Heading1'],
            fontName='ChineseFont',
            fontSize=14,
            spaceAfter=12
        )
        normal_style = ParagraphStyle(
            'ChineseNormal',
            parent=styles['Normal'],
            fontName='ChineseFont',
            fontSize=10,
            spaceAfter=6
        )
    else:
        title_style = styles['Title']
        heading_style = styles['Heading1']
        normal_style = styles['Normal']
    
    # 标题
    story.append(Paragraph(title, title_style))
    story.append(Spacer(1, 20))
    
    # 测试基本信息
    story.append(Paragraph("测试基本信息", heading_style))
    
    current_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    basic_info = [
        ["测试人员", "自动化测试系统"],
        ["测试时间", current_time],
        ["测试结果", "通过" if test_data and test_data['failures'] == 0 and test_data['errors'] == 0 else "部分失败"]
    ]
    
    basic_table = Table(basic_info, colWidths=[2*inch, 4*inch])
    basic_table.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
        ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
        ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
        ('FONTNAME', (0, 0), (-1, -1), 'ChineseFont' if setup_chinese_fonts() else 'Helvetica'),
        ('FONTSIZE', (0, 0), (-1, -1), 10),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
        ('BACKGROUND', (0, 1), (-1, -1), colors.beige),
        ('GRID', (0, 0), (-1, -1), 1, colors.black)
    ]))
    story.append(basic_table)
    story.append(Spacer(1, 20))
    
    # 单元测试结果
    if test_data:
        story.append(Paragraph("单元测试结果", heading_style))
        
        test_summary = [
            ["测试项目", "数量"],
            ["总测试数", str(test_data['total_tests'])],
            ["成功数", str(test_data['success_tests'])],
            ["失败数", str(test_data['failures'])],
            ["错误数", str(test_data['errors'])],
            ["跳过数", str(test_data['skipped'])],
            ["执行时间(秒)", f"{test_data['time']:.2f}"]
        ]
        
        test_table = Table(test_summary, colWidths=[3*inch, 2*inch])
        test_table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.lightblue),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.black),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (-1, -1), 'ChineseFont' if setup_chinese_fonts() else 'Helvetica'),
            ('FONTSIZE', (0, 0), (-1, -1), 10),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
            ('BACKGROUND', (0, 1), (-1, -1), colors.white),
            ('GRID', (0, 0), (-1, -1), 1, colors.black)
        ]))
        story.append(test_table)
        story.append(Spacer(1, 20))
    
    # 代码覆盖率结果
    if coverage_data:
        story.append(Paragraph("代码覆盖率结果", heading_style))
        
        coverage_summary = [
            ["覆盖率类型", "覆盖情况", "百分比"],
            ["指令覆盖率", f"{coverage_data['covered_instructions']}/{coverage_data['total_instructions']}", f"{coverage_data['instruction_coverage']:.1f}%"],
            ["行覆盖率", f"{coverage_data['covered_lines']}/{coverage_data['total_lines']}", f"{coverage_data['line_coverage']:.1f}%"]
        ]
        
        coverage_table = Table(coverage_summary, colWidths=[2*inch, 2*inch, 1.5*inch])
        coverage_table.setStyle(TableStyle([
            ('BACKGROUND', (0, 0), (-1, 0), colors.lightgreen),
            ('TEXTCOLOR', (0, 0), (-1, 0), colors.black),
            ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
            ('FONTNAME', (0, 0), (-1, -1), 'ChineseFont' if setup_chinese_fonts() else 'Helvetica'),
            ('FONTSIZE', (0, 0), (-1, -1), 10),
            ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
            ('BACKGROUND', (0, 1), (-1, -1), colors.white),
            ('GRID', (0, 0), (-1, -1), 1, colors.black)
        ]))
        story.append(coverage_table)
        story.append(Spacer(1, 20))
    
    # 测试详情（显示失败的测试）
    if test_data and (test_data['failures'] > 0 or test_data['errors'] > 0):
        story.append(Paragraph("失败测试详情", heading_style))
        
        failed_tests = [tc for tc in test_data['test_cases'] if tc['status'] in ['FAIL', 'ERROR']]
        if failed_tests:
            fail_details = [["测试方法", "状态", "原因"]]
            for test in failed_tests[:5]:  # 只显示前5个失败的测试
                fail_details.append([
                    test['name'][:30] + "..." if len(test['name']) > 30 else test['name'],
                    test['status'],
                    test.get('message', '')[:50] + "..." if len(test.get('message', '')) > 50 else test.get('message', '')
                ])
            
            fail_table = Table(fail_details, colWidths=[2.5*inch, 1*inch, 2.5*inch])
            fail_table.setStyle(TableStyle([
                ('BACKGROUND', (0, 0), (-1, 0), colors.red),
                ('TEXTCOLOR', (0, 0), (-1, 0), colors.white),
                ('ALIGN', (0, 0), (-1, -1), 'LEFT'),
                ('FONTNAME', (0, 0), (-1, -1), 'ChineseFont' if setup_chinese_fonts() else 'Helvetica'),
                ('FONTSIZE', (0, 0), (-1, -1), 9),
                ('BOTTOMPADDING', (0, 0), (-1, -1), 12),
                ('BACKGROUND', (0, 1), (-1, -1), colors.white),
                ('GRID', (0, 0), (-1, -1), 1, colors.black)
            ]))
            story.append(fail_table)
    
    doc.build(story)
    print(f"PDF报告已生成: {output_path}")

def main():
    """主函数"""
    print("正在生成带有真实数据的PDF报告...")
    
    # 设置路径
    backend_dir = "../../backend-java"
    reports_dir = "../reports/pdf"
    
    # 确保报告目录存在
    os.makedirs(reports_dir, exist_ok=True)
    os.makedirs(f"{reports_dir}/modules", exist_ok=True)
    
    # 读取真实测试数据
    user_service_test_file = f"{backend_dir}/target/surefire-reports/TEST-com.aicourse.service.UserServiceTest.xml"
    jwt_utils_test_file = f"{backend_dir}/target/surefire-reports/TEST-com.aicourse.util.JwtUtilsTest.xml"
    jacoco_csv_file = f"{backend_dir}/target/site/jacoco/jacoco.csv"
    
    # 读取测试数据
    user_test_data = read_junit_xml(user_service_test_file) if os.path.exists(user_service_test_file) else None
    jwt_test_data = read_junit_xml(jwt_utils_test_file) if os.path.exists(jwt_utils_test_file) else None
    coverage_data, detailed_coverage = read_jacoco_csv(jacoco_csv_file) if os.path.exists(jacoco_csv_file) else (None, None)
    
    # 合并测试数据
    combined_test_data = None
    if user_test_data and jwt_test_data:
        combined_test_data = {
            'total_tests': user_test_data['total_tests'] + jwt_test_data['total_tests'],
            'failures': user_test_data['failures'] + jwt_test_data['failures'],
            'errors': user_test_data['errors'] + jwt_test_data['errors'],
            'skipped': user_test_data['skipped'] + jwt_test_data['skipped'],
            'time': user_test_data['time'] + jwt_test_data['time'],
            'success_tests': user_test_data['success_tests'] + jwt_test_data['success_tests'],
            'test_cases': user_test_data['test_cases'] + jwt_test_data['test_cases']
        }
    elif user_test_data:
        combined_test_data = user_test_data
    elif jwt_test_data:
        combined_test_data = jwt_test_data
    
    # 生成时间戳
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    
    # 生成完整测试报告
    complete_report_path = f"{reports_dir}/完整测试报告_{timestamp}.pdf"
    create_pdf_with_real_data(
        complete_report_path,
        "AI智慧课程平台 - 完整测试报告",
        combined_test_data,
        coverage_data
    )
    
    # 生成模块测试报告
    modules = [
        ("认证模块测试报告", jwt_test_data),
        ("用户管理模块测试报告", user_test_data),
        ("课程管理模块测试报告", None)  # 暂无数据
    ]
    
    for module_name, module_test_data in modules:
        module_report_path = f"{reports_dir}/modules/{module_name}_{timestamp}.pdf"
        create_pdf_with_real_data(
            module_report_path,
            f"AI智慧课程平台 - {module_name}",
            module_test_data,
            coverage_data
        )
    
    print("\n==================================================")
    print("真实数据PDF报告生成完成！")
    print("==================================================")
    print(f"已生成: {reports_dir}/完整测试报告_{timestamp}.pdf")
    print(f"已生成: {reports_dir}/modules/认证模块测试报告_{timestamp}.pdf")
    print(f"已生成: {reports_dir}/modules/用户管理模块测试报告_{timestamp}.pdf")
    print(f"已生成: {reports_dir}/modules/课程管理模块测试报告_{timestamp}.pdf")
    print(f"\n报告位置: {os.path.abspath(reports_dir)}")
    
    if combined_test_data:
        print(f"\n测试概要:")
        print(f"- 总测试数: {combined_test_data['total_tests']}")
        print(f"- 成功数: {combined_test_data['success_tests']}")
        print(f"- 失败数: {combined_test_data['failures']}")
        print(f"- 错误数: {combined_test_data['errors']}")
        print(f"- 执行时间: {combined_test_data['time']:.2f}秒")
    
    if coverage_data:
        print(f"\n覆盖率概要:")
        print(f"- 指令覆盖率: {coverage_data['instruction_coverage']:.1f}%")
        print(f"- 行覆盖率: {coverage_data['line_coverage']:.1f}%")

if __name__ == "__main__":
    main() 