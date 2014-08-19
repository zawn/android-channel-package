/*
 * Copyright 2013 ZhangZhenli <zhangzhenli@live.com>.
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
package com.appunity.ant;

import com.appunity.ant.pojo.ProjectProfile;
import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class Utils {

    private static boolean isUserTimeWorkDir;
    private static File currentWorkDir;

    public static String toPinYin(String hzString) {
        /**
         * 设置输出格式
         */
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] hzChars = new char[hzString.length()];
        for (int i = 0; i < hzString.length(); i++) {
            hzChars[i] = hzString.charAt(i);
        }

        int t0 = hzChars.length;
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < t0; i++) {
                char c = hzChars[i];
                String[] result = PinyinHelper.toHanyuPinyinStringArray(c, format);
                if (result != null && result.length > 0) {
                    sb.append(CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, result[0]));
                } else {
                    sb.append(c);
                }
            }
        } catch (BadHanyuPinyinOutputFormatCombination e1) {
            if (sb.length() == 0) {
                sb.append("Anonymous");
            }
        }
        return sb.toString();
    }

    private static String relativePath;
    private static File baseDir;

    protected static String obtainValidPath(Task task, String path, String name) {
        baseDir = task.getProject().getBaseDir();
        if (path == null || "".equals(path)) {
            path = task.getProject().getProperty(name);
        }
        if (path == null || "".equals(path)) {
            throw new BuildException("Can not find the path " + name + ":" + path);
        }
        try {
            File tempFile = new File(path);
            try {
                if (relativePath == null) {
                    relativePath = new File("").getCanonicalPath();
                }
                boolean contains = tempFile.getCanonicalPath().contains(relativePath);
                if (contains || path.startsWith(".")) {
                    tempFile = new File(baseDir, path);
                }
                return tempFile.getCanonicalPath();
            } catch (Exception e) {
            }
            return path;
        } catch (Exception e) {
            throw new BuildException("Can not find the path " + name + ":" + path);
        }
    }

    protected static ProjectProfile getProfile(Task task, String profilePath) {
        ProjectProfile profile = null;
        try {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(obtainValidPath(task, profilePath, "project.profile")), "UTF-8");
            JsonReader jsonReader = new JsonReader(reader);
            JsonObject asJsonObject = parser.parse(jsonReader).getAsJsonObject();
            profile = gson.fromJson(asJsonObject.get("project"), ProjectProfile.class);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JsonIOException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JsonSyntaxException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        return profile;
    }

    protected static File getCurrentWorkDir(Task task, String workdir) throws UnsupportedOperationException {
        if (currentWorkDir == null) {
            return getNewWorkDir(task, workdir, false);
        } else {
            return currentWorkDir;
        }
    }

    protected static File getNewWorkDir(Task task, String workdir, boolean... isClean) throws UnsupportedOperationException {
        String timedir = task.getProject().getProperty(" work.dir.timestamp");
        if (timedir != null && "true".equals(timedir)) {
            isUserTimeWorkDir = true;
        }
        File dir = new File(Utils.obtainValidPath(task, workdir, "work.dir"));
        if (!dir.exists()) {
            System.out.println("make dir     :" + workdir);
            dir.mkdirs();
        } else {
            if (!dir.isDirectory()) {
                File oldfile = new File(workdir + ".bak");
                for (int i = 0; oldfile.exists(); i++) {
                    oldfile = new File(workdir + "." + i + ".bak");
                }
                dir.renameTo(oldfile);
                System.out.println("make dir     :" + workdir);
                dir.mkdirs();
                System.out.println("work dir " + workdir + " is not a dir, will rename old file to " + oldfile.getName());
            }
        }
        if (!dir.canWrite()) {
            throw new UnsupportedOperationException("工作目录不可写: " + workdir);
        }
        if (isUserTimeWorkDir) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            File file = new File(dir, df.format(new Date()));
            while (file.exists()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
                file = new File(dir, df.format(new Date()));
            }
            file.mkdirs();
            return file;
        } else {
            if (isClean != null && isClean[0]) {
                for (File object : dir.listFiles()) {
                    if (object.isDirectory()) {
                        try {
                            FileUtils.deleteDirectory(object);
                            System.out.println("delete old dir: " + object);
                        } catch (IOException ex) {
                            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {
                        object.delete();
                        System.out.println("delete old file: " + object);
                    }
                }
            }
            currentWorkDir = dir;
            return dir;
        }
    }

    public static String getAntPath(Task task) {
        String ant = task.getProject().getProperty("ant");
        if (ant == null || "".equals(ant)) {
            ant = "ant";
        }
        return ant;
    }
}
