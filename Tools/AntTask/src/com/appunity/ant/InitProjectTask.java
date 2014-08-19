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
package com.appunity.ant;

import com.appunity.ant.pojo.ProjectProfile;
import com.appunity.ant.vcs.SubversionHelper;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class InitProjectTask extends Task {

    String message;
    String text;
    private String profilePath;
    private String workdir;
    private File workDir;
    private boolean isUserTimeWorkDir;

    public void setProfile(String msg) {
        profilePath = msg;
    }

    public void setTimeDir(String msg) {
        if (msg != null && "true".equals(msg)) {
            isUserTimeWorkDir = true;
        }
    }

    public void setWorkdir(String dir) {
        workdir = dir;
    }

    @Override
    public void execute() throws BuildException {
        System.out.println("load profile :" + profilePath);
        baseDir = getProject().getBaseDir();
        System.out.println("baseDir     :" + baseDir.getAbsolutePath());
        workDir = getWorkDir(baseDir, workdir, isUserTimeWorkDir);
        try {
            System.out.println("work dir     :" + workDir.getCanonicalPath());
        } catch (IOException ex) {
        }
//        try {
//            System.out.println(IOUtils.toString(new FileInputStream(profilePath), "UTF-8"));
//        } catch (IOException ex) {
//            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
//        }
        profilePath = obtainValidPath(baseDir, profilePath);
        ProjectProfile profile = getProfile(profilePath);
        System.out.println(profile);
        try {
            getSource(profile.main);
            for (ProjectProfile.Project libProject : profile.libs) {
                getSource(libProject);
            }
        } catch (IOException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected static File getWorkDir(File baseDir, String workdir, boolean isUserTimeWorkDir) throws UnsupportedOperationException {
        File dir = new File(obtainValidPath(baseDir, workdir));
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
            return dir;
        }
    }

    protected static ProjectProfile getProfile(String profilePath) {
        ProjectProfile profile = null;
        try {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(profilePath), "UTF-8");
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

    private void getSource(ProjectProfile.Project project) throws IOException {
        if (project.disk != null) {
            System.out.println("project.disk : " + project.disk);
            FileUtils.copyDirectory(new File(project.disk.url), new File(workDir, project.name), ProjectFileFilter.IgnoreVCSFile);
        } else if (project.git != null) {
            throw new UnsupportedOperationException("尚未实现 GIT");
        } else if (project.hg != null) {
            throw new UnsupportedOperationException("尚未实现 HG");
        } else if (project.svn != null) {
            System.out.println("project.svn : " + project.svn);
            SubversionHelper helper = new SubversionHelper();
            helper.execute(this, workDir, project);
        }
    }

    public static class ProjectFileFilter implements FileFilter {

        public static FileFilter IgnoreVCSFile = new ProjectFileFilter();

        @Override
        public boolean accept(File pathname) {
            String name = pathname.getName();
            if (pathname.isDirectory() && (name.equals(".svn") || name.equals(".hg") || name.equals(".git"))) {
                System.out.println("Ignore  : " + pathname);
                return false;
            } else {
                System.out.println("          " + pathname);
            }
            return true;
        }
    };

    private static String relativePath;
    private File baseDir;

    protected static String obtainValidPath(File baseDir, String path) {
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
    }
}
