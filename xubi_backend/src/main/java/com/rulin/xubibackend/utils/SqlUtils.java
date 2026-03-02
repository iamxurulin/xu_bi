package com.rulin.xubibackend.utils;

import org.apache.commons.lang3.StringUtils;

public class SqlUtils {

    /**
     * 校验排序字段是否合法（防止 SQL 注入）
     * 该方法通过检查字符串中是否包含非法字符来判断排序字段是否合法

     * 非法字符包括：等号、左括号、右括号和空格
     *
     * @param sortField 需要校验的排序字段字符串
     * @return 如果字段合法返回true，否则返回false
     */
    public static boolean validSortField(String sortField) {
        // 首先检查字符串是否为空或只包含空白字符
        if (StringUtils.isBlank(sortField)) {
            return false;
        }
        // 检查字符串中是否包含任何非法字符
        // 如果包含任何非法字符，则返回false
        return !StringUtils.containsAny(sortField, "=", "(", ")", " ");
    }
}
