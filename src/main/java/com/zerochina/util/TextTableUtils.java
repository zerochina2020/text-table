package com.zerochina.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zerochina
 * @description 文本表格工具
 * @date 2022-05-25
 **/
public class TextTableUtils {
    private static final Pattern pattern = Pattern.compile("[\u4e00-\u9fa5|。|，]");
    /**
     * 中文对齐填充字符(中文全角空格，一个中文宽度)
     */
    private static final String CN_BLANK = "\u3000";
    /**
     * 英文对齐填充字符
     */
    private static final String EN_BLANK = " ";

    /**
     * 以文本表格格式化数据
     *
     * @param data 待输出数据
     * @return
     */
    public static List<String> print(String[][] data) {
        return print(data, null, null);
    }

    /**
     * 以文本表格格式化数据
     *
     * @param data         待输出数据(二维数组数据)
     * @param colMinLength 单元格内容最小长度
     * @param colMaxLength 单元格内容最大长度(小于最小长度时，取最小长度)
     * @return
     */
    public static List<String> print(String[][] data, Integer colMinLength, Integer colMaxLength) {
        List<String> result = new ArrayList<>();
        if (Objects.nonNull(data)) {
            if (Objects.isNull(colMinLength) || colMinLength <= 0) {
                colMinLength = 0;
            }
            if (Objects.isNull(colMaxLength) || colMaxLength <= 0) {
                colMaxLength = Integer.MAX_VALUE;
            }
            if (colMaxLength < colMinLength) {
                colMaxLength = colMinLength;
            }

            // 处理待打印表格数据，处理超长单元格，去除超长部分
            String[][] printData = new String[data.length][data[0].length];
            for (int i = 0; i < data.length; i++) {
                for (int j = 0; j < data[i].length; j++) {
                    String content = data[i][j];
                    if (Objects.nonNull(content) && content.length() > colMaxLength) {
                        content = content.substring(0, colMaxLength);
                    }
                    printData[i][j] = content;
                }
            }

            // 计算每列最大宽度并将宽度不足单元格以空格补齐
            Integer[][] colsMaxLength = maxColsLength(data, colMinLength);
            if (Objects.nonNull(colsMaxLength)) {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < colsMaxLength.length; i++) {
                    int maxCnCount = colsMaxLength[i][1];
                    int maxEnCount = colsMaxLength[i][0] - colsMaxLength[i][1];
                    if (maxEnCount < maxCnCount) {
                        colsMaxLength[i][0] = 2 * maxCnCount + 1;
                    }
                    if (colsMaxLength[i][0] % 2 == 0) {
                        colsMaxLength[i][0] = colsMaxLength[i][0] + 1;
                    }
                    builder.append("+" + genTableSplit(colsMaxLength[i][0] + 2, colsMaxLength[i][1]));
                }
                builder.append("+");
                String split = builder.toString();
                result.add(split);

                for (int i = 0; i < printData.length; i++) {
                    if (i == 1) {
                        result.add(split);
                    }
                    builder = new StringBuilder();
                    for (int j = 0; j < printData[i].length; j++) {
                        String content = printData[i][j];
                        if (Objects.isNull(content)) {
                            content = "";
                        }
                        int chineseCount = getChineseCount(content);
                        int cnPadding = colsMaxLength[j][1] - chineseCount;
                        int enPadding = colsMaxLength[j][0] + chineseCount - colsMaxLength[j][1] - content.length();
                        if (i == 0) {
                            int cnLeft = cnPadding / 2;
                            int cnRight = cnPadding - cnLeft;
                            int enLeft = enPadding / 2;
                            int enRight = enPadding - enLeft;
                            content = "| " + repeat(EN_BLANK, enLeft) + repeat(CN_BLANK, cnLeft) + content + repeat(CN_BLANK, cnRight) + repeat(EN_BLANK, enRight) + " ";
                        } else {
                            content = "| " + content + repeat(EN_BLANK, enPadding) + repeat(CN_BLANK, cnPadding) + " ";
                        }
                        builder.append(content);
                    }
                    builder.append("|");
                    result.add(builder.toString());
                }
                result.add(split);
            }
        }
        return result;
    }

    /**
     * 计算表格每列最大长度和每列中文字符最大数
     *
     * @param data         表格数据
     * @param colMinLength 每列最小长度
     * @return
     */
    private static Integer[][] maxColsLength(String[][] data, int colMinLength) {
        if (Objects.isNull(data)) {
            return null;
        }
        Integer[][] maxColsLength = new Integer[data[0].length][2];
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                String content = data[i][j];
                // 计算列中最大中文数
                int chineseCount = getChineseCount(content);
                if (Objects.isNull(maxColsLength[j][1])) {
                    maxColsLength[j][1] = chineseCount;
                } else if (chineseCount > maxColsLength[j][1]) {
                    maxColsLength[j][1] = chineseCount;
                }
                // 计算列最大长度
                int colLength = Objects.isNull(content) ? colMinLength : content.length();
                if (chineseCount == 0) {
                    colLength = colLength + maxColsLength[j][1];
                }
                if (Objects.isNull(maxColsLength[j][0])) {
                    maxColsLength[j][0] = colMinLength;
                } else if (colLength > maxColsLength[j][0]) {
                    maxColsLength[j][0] = colLength;
                }
            }
        }
        return maxColsLength;
    }

    /**
     * 获取字符串中汉字个数
     *
     * @param content 字符串
     * @return
     */
    private static int getChineseCount(String content) {
        if (Objects.isNull(content) || content.length() == 0) {
            return 0;
        }
        Matcher matcher = pattern.matcher(content);
        int chineseCount = 0;
        while (matcher.find()) {
            chineseCount++;
        }
        return chineseCount;
    }

    /**
     * 复制指定字符串
     *
     * @param s     待复制字符串
     * @param count 复制次数
     * @return
     */
    public static String repeat(String s, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(s);
        }
        return builder.toString();
    }

    /**
     * 生成减号与空格组成的表格分割线
     *
     * @param length
     * @param cnBlankCount
     * @return
     */
    public static String genTableSplit(int length, int cnBlankCount) {
        if (length % 2 == 0) {
            length++;
        }
        StringBuilder builder = new StringBuilder();
        int cnBlankEnd = 2 * cnBlankCount;
        for (int i = 0; i < length; i++) {
            if (i % 2 == 0) {
                builder.append("-");
            } else {
                if (i < cnBlankEnd) {
                    builder.append(CN_BLANK);
                } else {
                    builder.append(EN_BLANK);
                }
            }
        }
        return builder.toString();
    }
}
