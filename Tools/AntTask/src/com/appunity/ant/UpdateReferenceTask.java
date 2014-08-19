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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class UpdateReferenceTask extends Task {

    private String relativePath;
    private File baseDir;
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
        workDir = Utils.getCurrentWorkDir(this, workdir);
        try {
            System.out.println("work dir     :" + workDir.getCanonicalPath());
        } catch (IOException ex) {
        }
        ProjectProfile profile = Utils.getProfile(this, profilePath);
        try {
            updateReference(profile);
        } catch (IOException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 更新项目间引用
     */
    private void updateReference(ProjectProfile profile) throws IOException {
        HashMap<String, String> map = new HashMap<String, String>();
        if (profile.libs != null) {
            for (ProjectProfile.Project libProject : profile.libs) {
                map.put(libProject.name, ".." + File.separator + libProject.name);
            }
        }
        System.out.println("原有的依赖项:");
        getReference(profile.main, map);
        for (ProjectProfile.Project libProject : profile.libs) {
            getReference(libProject, map);
        }
        System.out.println("更新后依赖项:");
        getReference(profile.main, map);
        for (ProjectProfile.Project libProject : profile.libs) {
            getReference(libProject, map);
        }
    }

    private void getReference(ProjectProfile.Project main, HashMap<String, String> map) throws IOException {
        Properties properties = new Properties();
        File propertiesFile = new File(new File(workDir, main.name), "project.properties");
        System.out.println(main.name);
        if (propertiesFile.exists()) {
            properties.load(new FileInputStream(propertiesFile));
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                Set<String> keySet = map.keySet();
                if (key.contains("android.library.reference")) {
                    System.out.println(key + "=" + value);
                    boolean find = false;
                    for (String string : keySet) {
                        if (value.contains(string)) {
                            find = true;
                            properties.put(key, map.get(string));
                            break;
                        }
                    }
                    if (!find) {
                        throw new UnsupportedOperationException(main.name + "中发现未知依赖项:" + value);
                    }
                }
            }
            properties.store(new FileOutputStream(propertiesFile), new Date().toString());
        }
    }
}
