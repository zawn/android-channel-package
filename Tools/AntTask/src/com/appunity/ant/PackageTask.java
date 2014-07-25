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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class PackageTask extends Task {

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
        channelIDsProFile = getProject().getProperty("ChannelIDs");
        List<String> channelIDs = new ArrayList<String>();
        if (channelIDsProFile != null) {
            try {
                File f = new File(baseDir, channelIDsProFile);
                System.out.println("Read the channels list file: " + f.getCanonicalPath());
                if (f.exists()) {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(f));
                    String readLine = bufferedReader.readLine();
                    while (readLine != null) {
                        readLine = readLine.trim();
                        if (!readLine.startsWith("#") && !readLine.equals("")) {
                            System.out.print(readLine + ";");
                            String channelIDName = Utils.toPinYin(readLine);
                            channelIDs.add(channelIDName);
                        }
                        readLine = bufferedReader.readLine();
                    }
                    if (channelIDs.size() > 0) {
                        System.out.println();
                    }
                }
                if (channelIDs.size() > 0) {
                    System.out.println(Arrays.toString(channelIDs.toArray()));
                }else{
                    System.out.println("Did not read to any channel name.");
                }

            } catch (IOException ex) {
                Logger.getLogger(PackageTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("No configuration channels list file");
        }
        try {
            ArrayList<String> packageProjects = packageProjects(buildDir.getCanonicalPath(), channelIDs);
            System.out.println("Successfully packaged : ");
            for (int i = 0; i < packageProjects.size(); i++) {
                String string = packageProjects.get(i);
                System.out.println(string);
            }
        } catch (IOException ex) {
            throw new BuildException("The path parameter configuration is invalid");
        }
    }

    public ArrayList<String> packageProjects(String path, List<String> channelIDs) {
        File dir = new File(path);
        File[] listFiles = dir.listFiles();
        List<String> channelIDsPackageList = new ArrayList<String>();
        ArrayList<String> list = new ArrayList<String>();
        if (listFiles != null) {
            for (File file : listFiles) {
                System.out.println("Making the default package...");
                if (packageProject(file)) {
                    list.add(file.getAbsolutePath());
                }
                System.out.println();
            }
            for (int i = 0; i < list.size(); i++) {
                File file = new File(list.get(i));
                for (int j = 0; j < channelIDs.size(); j++) {
                    System.out.println("Making the ChannelIDs package...");
                    String channelIDName = channelIDs.get(j);
                    boolean isPackage = packageProject(file, channelIDName);
                    if (isPackage) {
                        try {
                            channelIDsPackageList.add(file.getCanonicalPath() + ": " + channelIDName);
                        } catch (IOException ex) {
                            Logger.getLogger(PackageTask.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    System.out.println();
                }
            }
            list.addAll(channelIDsPackageList);
            return list;
        }
        return null;
    }

    private boolean packageProject(File file) {
        return packageProject(file, null);
    }

    private boolean packageProject(File dir, String channelName) {
        if (!dir.isDirectory()) {
            return false;
        }
        String pram = null;
        String distName = null;
        try {
            int isLib = UpdateTask.isLib(dir);
            distName = UpdateTask.UpdateProject(dir, android, channelName);
            File buildXml = new File(dir, "build.xml");
            StringBuilder pramBuilder = new StringBuilder();
            if (distName != null && isLib == 0 && buildXml.exists()) {
                pramBuilder.append("ant").append(" ");
                pramBuilder.append("-buildfile").append(" ");
                pramBuilder.append(buildXml.getCanonicalPath()).append(" ");
                pramBuilder.append("release");
                pram = pramBuilder.toString();
                pram = pram.trim();
            }
        } catch (Exception ex) {
        }
        if (pram != null && !"".equals(pram)) {
            System.out.println(pram);
            Cmd.exec(pram);
            if (channelName != null && !"".equals(pram)) {
                clearBuildDir(dir, distName);
            }
            File distDir = new File(dir, "dist");
            if (!distDir.exists()) {
                distDir.mkdirs();
            }
            File binDir = new File(dir, "bin");
            File distFile = new File(binDir, distName + "-release.apk");
            System.out.println(distFile.getAbsolutePath());
            File distFileCopy;
            if (distFile.exists()) {
                distFileCopy = new File(distDir, distFile.getName());
                if (distFileCopy.exists()) {
                    distFileCopy.delete();
                }
                try {
                    FileUtils.copyFile(distFile, distFileCopy);
                } catch (IOException ex) {
                    Logger.getLogger(PackageTask.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return true;
        }
        return false;
    }

    private boolean clearBuildDir(File dir, String distName) {
        File binDir = new File(dir, "bin");
        if (binDir.exists() && distName != null) {
            File[] listFiles = binDir.listFiles(new DistFilter(distName));
            for (int i = 0; i < listFiles.length; i++) {
                File file = listFiles[i];
                file.deleteOnExit();
            }
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
