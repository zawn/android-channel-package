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
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    public void setProfile(String msg) {
        profilePath = msg;
    }

    @Override
    public void execute() throws BuildException {
        System.out.println("load profile :" + profilePath);
        try {
            System.out.println(IOUtils.toString(new FileInputStream(profilePath), "UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(InitProjectTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        ProjectProfile profile = getProfile();
        getSource(profile.main);
        for (ProjectProfile.Project libProject : profile.libs) {
            getSource(libProject);
        }
    }

    private ProjectProfile getProfile() {
        ProjectProfile profile = null;
        try {
            Gson gson = new Gson();
            JsonParser parser = new JsonParser();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(profilePath), "UTF-8");
            JsonReader jsonReader = new JsonReader(reader);
            JsonObject asJsonObject = parser.parse(jsonReader).getAsJsonObject();
            profile = gson.fromJson(asJsonObject.get("project"), ProjectProfile.class);
            System.out.println(profile);
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

    private void getSource(ProjectProfile.Project project) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
