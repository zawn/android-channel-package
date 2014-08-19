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
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class AndroidCleanTask extends Task {

    String path;
    File buildDir;
    private String android;
    private String channelIDsProFile;
    private File baseDir;

    public void setPath(String msg) {
        path = msg;
    }

    @Override
    public void execute() throws BuildException {
        if (path == null) {
            throw new BuildException("No path set.");
        }
        android = getProject().getProperty("android");
        baseDir = getProject().getBaseDir();
        buildDir = new File(path);
        try {
            ArrayList<String> packageProjects = packageProjects(buildDir.getCanonicalPath());
            System.out.println("Successfully packaged : ");
            for (int i = 0; i < packageProjects.size(); i++) {
                String string = packageProjects.get(i);
                System.out.println(string);
            }
        } catch (IOException ex) {
            throw new BuildException("The path parameter configuration is invalid");
        }
    }

    public ArrayList<String> packageProjects(String path) throws IOException {
        File dir = new File(path);
        File[] listFiles = dir.listFiles();
        ArrayList<String> list = new ArrayList<String>();
        ProjectProfile profile = Utils.getProfile(this, null);
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.getName().equals(profile.main.name)) {
                    System.out.println("Clean Android Project...");
                    if (packageProject(file)) {
                        list.add(file.getAbsolutePath());
                    }
                }
                System.out.println();
            }
            return list;
        } else {
            throw new BuildException(Utils.getCurrentWorkDir(this, null).getAbsolutePath() + profile.main.name);
        }
    }

    private boolean packageProject(File dir) throws IOException {
        if (!dir.isDirectory()) {
            return false;
        }
        String pram = null;
        String distName = null;
        try {
            int isLib = UpdateTask.isLib(dir);
            distName = UpdateTask.UpdateProject(dir, android, null);
            File buildXml = new File(dir, "build.xml");
            StringBuilder pramBuilder = new StringBuilder();
            if (distName != null && isLib == 0 && buildXml.exists()) {
                String ant = Utils.getAntPath(this);
                pramBuilder.append(ant).append(" ");
                pramBuilder.append("-buildfile").append(" ");
                pramBuilder.append(buildXml.getCanonicalPath()).append(" ");
                pramBuilder.append("clean");
                pram = pramBuilder.toString();
                pram = pram.trim();
            }
        } catch (Exception ex) {
        }
        if (pram != null && !"".equals(pram)) {
            System.out.println("清理项目");
            System.out.println(pram);
            Cmd.exec(pram);
            File distDir = new File(dir, "dist");
            File binDir = new File(dir, "bin");
            FileUtils.deleteDirectory(binDir);
            if (!distDir.exists()) {
                distDir.mkdirs();
            } else {
                FileUtils.deleteDirectory(distDir);
            }
            return true;
        }
        return false;
    }


    public static class DistFilter implements FilenameFilter {

        private final String distName;

        public DistFilter(String distName) {
            this.distName = distName;
        }

        @Override
        public boolean accept(File dir, String name) {
            if (name.startsWith(distName) && !name.endsWith("release.apk")) {
                return true;
            }
            return false;
        }
    }
}
