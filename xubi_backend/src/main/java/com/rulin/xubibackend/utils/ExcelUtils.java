package com.rulin.xubibackend.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
public class ExcelUtils {
    /**
     * 将Excel文件转换为CSV格式字符串
     *
     * @param multipartFile 上传的Excel文件(MultipartFile格式)
     * @return 转换后的CSV格式字符串，如果处理失败或数据为空则返回空字符串
     */
    public static String excelToCsv(MultipartFile multipartFile) {
        //读取数据
        List<Map<Integer, String>> list = null;

        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            log.error("表格处理错误", e);
        }
        //如果数据为空
        if (CollUtil.isEmpty(list)) {
            return "";
        }

        //转换为 csv
        StringBuilder stringBuilder = new StringBuilder();
        //读取表头（第一行）
        LinkedHashMap<Integer, String> headerMap = (LinkedHashMap<Integer, String>) list.get(0);
        List<String> headerList = headerMap.values().stream().filter(StrUtil::isNotEmpty).collect(Collectors.toList());
        stringBuilder.append(StrUtil.join(",", headerList)).append("\n");

        //读取数据（读取完表头之后，从第一行开始读取
        for (int i = 1; i < list.size(); i++) {
            LinkedHashMap<Integer, String> dataMap = (LinkedHashMap) list.get(i);
            List<String> dataList = dataMap.values().stream().filter(StrUtil::isNotEmpty).collect(Collectors.toList());
            stringBuilder.append(StrUtil.join(",", dataList)).append("\n");
        }

        return stringBuilder.toString();
    }

    /**
     * 程序的主入口方法
     *
     * @param args 命令行参数，此处未使用
     */
    public static void main(String[] args) {
        // 调用excelToCsv方法，传入null参数
        excelToCsv(null);
    }
}
