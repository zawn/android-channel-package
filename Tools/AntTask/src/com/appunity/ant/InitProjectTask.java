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
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
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
    private String workDirString;
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
        workDirString = dir;
    }

    @Override
    public void execute() throws BuildException {
        System.out.println("load profile  :" + profilePath);
        workDir = Utils.getNewWorkDir(this, workDirString, true);
        try {
            System.out.println("source dir  :" + workDir.getCanonicalPath());
        } catch (IOException ex) {
            throw new BuildException("source dir error");
        }
        profilePath = Utils.obtainValidPath(this, profilePath, "project.profile");
        ProjectProfile profile = Utils.getProfile(this, profilePath);
        if (profile == null) {
            throw new BuildException("Package configuration json file not found");
        }
        System.out.println(profile);
        try {
            getSource(profile.main);
            if (profile.libs != null) {
                for (ProjectProfile.Project libProject : profile.libs) {
                    getSource(libProject);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        }
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
}
