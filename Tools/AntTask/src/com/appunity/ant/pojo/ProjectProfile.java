/*
 * Copyright 2014 ZhangZhenli <zhangzhenli@live.com>.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.appunity.ant.pojo;

import java.util.ArrayList;

import com.google.gson.annotations.SerializedName;

/**
 * 项目配置属性类.
 * <p>
 * 配置示例:
 *
 * <pre>
 * {@code }
 * </pre>
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class ProjectProfile {

    @SerializedName("main")
    public Project main;

    @SerializedName("libs")
    public ArrayList<Project> libs;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProjectProfile [");
        if (main != null) {
            builder.append("\n      main=");
            builder.append(main);
            builder.append(", ");
        }
        if (libs != null) {
            builder.append("\n      libs=");
            builder.append(libs);
        }
        builder.append("]");
        return builder.toString();
    }

    public static class Subverison {

        public String url;
        public String path;
        public String username;
        public String password;
        public String version;
        public String options;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Subverison [");
            if (url != null) {
                builder.append("url=");
                builder.append(url);
                builder.append(", ");
            }
            if (path != null) {
                builder.append("path=");
                builder.append(path);
                builder.append(", ");
            }
            if (username != null) {
                builder.append("username=");
                builder.append(username);
                builder.append(", ");
            }
            if (password != null) {
                builder.append("password=");
                builder.append(password);
                builder.append(", ");
            }
            if (version != null) {
                builder.append("version=");
                builder.append(version);
                builder.append(", ");
            }
            if (options != null) {
                builder.append("options=");
                builder.append(options);
            }
            builder.append("]");
            return builder.toString();
        }

    }

    public static class Git {

        public String url;
        public String path;
        public String username;
        public String password;
        public String options;
        public String branch;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Git [");
            if (url != null) {
                builder.append("url=");
                builder.append(url);
                builder.append(", ");
            }
            if (path != null) {
                builder.append("path=");
                builder.append(path);
                builder.append(", ");
            }
            if (username != null) {
                builder.append("username=");
                builder.append(username);
                builder.append(", ");
            }
            if (password != null) {
                builder.append("password=");
                builder.append(password);
                builder.append(", ");
            }
            if (options != null) {
                builder.append("options=");
                builder.append(options);
                builder.append(", ");
            }
            if (branch != null) {
                builder.append("branch=");
                builder.append(branch);
            }
            builder.append("]");
            return builder.toString();
        }

    }

    public static class Mercurial {

        public String url;
        public String path;
        public String username;
        public String password;
        public String options;
        public String branch;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Mercurial [");
            if (url != null) {
                builder.append("url=");
                builder.append(url);
                builder.append(", ");
            }
            if (path != null) {
                builder.append("path=");
                builder.append(path);
                builder.append(", ");
            }
            if (username != null) {
                builder.append("username=");
                builder.append(username);
                builder.append(", ");
            }
            if (password != null) {
                builder.append("password=");
                builder.append(password);
                builder.append(", ");
            }
            if (options != null) {
                builder.append("options=");
                builder.append(options);
                builder.append(", ");
            }
            if (branch != null) {
                builder.append("branch=");
                builder.append(branch);
            }
            builder.append("]");
            return builder.toString();
        }

    }

    public static class Disk {

        public String url;

        @Override
        public String toString() {
            return "Disk{" + "url=" + url + '}';
        }

    }

    public static class Project {

        public String name;
        public Subverison svn;
        public Git git;
        public Mercurial hg;
        public Disk disk;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("\n          Project [");
            if (name != null) {
                builder.append("\n              name=");
                builder.append(name);
                builder.append(", ");
            }
            if (svn != null) {
                builder.append("\n              svn=");
                builder.append(svn);
                builder.append(", ");
            }
            if (git != null) {
                builder.append("\n              git=");
                builder.append(git);
                builder.append(", ");
            }
            if (hg != null) {
                builder.append("\n              hg=");
                builder.append(hg);
                builder.append(", ");
            }
            if (disk != null) {
                builder.append("\n              disk=");
                builder.append(disk);
            }
            builder.append("]");
            return builder.toString();
        }

    }
}
