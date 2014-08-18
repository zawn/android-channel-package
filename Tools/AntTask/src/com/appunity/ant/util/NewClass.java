/*
 * Copyright 2014 ZhangZhenli <zhangzhenli@live.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appunity.ant.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.FileUtils;

/**
 * 批量转换Java类文件编码
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class NewClass {

    /**
     * @author niewj
     * @since 2012-6-1
     */
    static File srcDir = new File("E:\\android-channel-package\\Build\\HouseCoreLib\\src");  // 待转码的GBK格式文件夹
    static File destDir = new File("E:\\android-channel-package\\Build\\HouseCoreLib\\src2");  // 转码成UTF8的目标文件夹

    public static void main(String[] args) throws FileNotFoundException, IOException {

        Collection<File> listFiles = FileUtils.listFiles(srcDir, new String[]{"java"}, true);
        int size = 0;
        System.out.println("listFiles = " + listFiles.size());
        for (final File file : listFiles) {
            CharsetDetector charsetDetector = new CharsetDetector();
            String[] detectAllCharset = charsetDetector.detectCharset(file);
            if (detectAllCharset != null && detectAllCharset.length == 1 && "GB2312".equals(detectAllCharset[0])) {
                System.out.println(Arrays.toString(detectAllCharset) + file.getAbsolutePath());
            } else {
                System.out.println(Arrays.toString(detectAllCharset) + file.getAbsolutePath());
            }
        }
    }

    /**
     */
    private void parse2UTF_8(File file, File destFile) throws IOException {
        StringBuffer msg = new StringBuffer();
        //读写对象  
        PrintWriter ps = new PrintWriter(new OutputStreamWriter(new FileOutputStream(destFile, false), "utf8"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));

        //读写动作  
        String line = br.readLine();
        while (line != null) {
            msg.append(line).append("\r\n");
            line = br.readLine();
        }
        ps.write(msg.toString());
        br.close();
        ps.flush();
        ps.close();
    }
}
