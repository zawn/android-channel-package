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
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class TestRunCmd {

    public static void main(String[] args) {
        String[] cmd = new String[]{"cmd.exe", "/C", "E:\\android-channel-package\\Tools\\apache-ant-1.8.4\\bin\\ant -buildfile E:\\android-channel-package\\build\\house365-rent\\build.xml clean"};
        try {
            Process process = Runtime.getRuntime().exec(cmd);

            new Thread(new StreamDrainer(process.getInputStream())).start();
            new Thread(new StreamDrainer(process.getErrorStream())).start();

            process.getOutputStream().close();

            int exitValue = process.waitFor();
            System.out.println("返回值：" + exitValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class StreamDrainer implements Runnable {

        private InputStream ins;

        public StreamDrainer(InputStream ins) {
            this.ins = ins;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(ins, "GBK"));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
