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
package com.appunity.ant.vcs;

import com.appunity.ant.SvnKitUtil;
import com.appunity.ant.pojo.ProjectProfile;
import com.appunity.ant.pojo.ProjectProfile.Project;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class SubversionHelper {

    public static final String TAG = "SvnTask ";

    Properties svnProperties;
    private File baseDir;
    private String relativePath;
    private ProjectProfile.Subverison subverison;

    public void execute(Task task, File dir, Project project) {
        subverison = project.svn;
        svnProperties = new Properties();
        baseDir = task.getProject().getBaseDir();
        String mSubversionLocal = dir.getAbsolutePath();
        String mSubversionConfig = task.getProject().getProperty("svn.config");
        mSubversionLocal = obtainValidPath(mSubversionLocal);
        mSubversionConfig = obtainValidPath(mSubversionConfig);
        File localDir = new File(mSubversionLocal);
        File configFile = new File(mSubversionConfig);
        svnProperties.setProperty("svn.username", subverison.username);
        svnProperties.setProperty("svn.password", subverison.password);
        svnProperties.setProperty("svn.url", subverison.url);
        try {
            svnProperties.setProperty("svn.local", localDir.getCanonicalPath());
        } catch (IOException ex) {
            throw new BuildException("The svn.properties svn.local parameter configuration is invalid");
        }
        try {
            svnProperties.setProperty("svn.config", configFile.getCanonicalPath());
            svnProperties.setProperty("svn.dirname", project.name);
        } catch (IOException ex) {
            throw new BuildException("The svn.properties svn.config parameter configuration is invalid");
        }
        try {
            syncProjectCode(svnProperties);
        } catch (IOException ex) {
            task.log(ex, LogLevel.ERR.getLevel());
        }
    }

    /**
     * 与SVN同步代码
     *
     * @param properties
     * @return
     */
    public boolean syncProjectCode(Properties properties) throws IOException {
        return SvnKitUtil.syncProjectCode(properties);
    }

    /**
     * 比较代码版本
     *
     * @return
     */
    public boolean compareCodeVersion() {
        return false;
    }

    /**
     * 更新代码版本
     */
    public void updateProjectCode() {
    }

    private String obtainValidPath(String path) {
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
