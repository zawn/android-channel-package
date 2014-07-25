package com.appunity.ant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 处理源码
 *
 * @author Zawn
 */
public class SrcTask extends Task {

    Properties svnProperties;
    String message;
    String path;
    File baseDir;
    private String android;
    private File toDir;
    private final JavaFilter javaFilter = new JavaFilter();
    private final DirFilter dirFilter = new DirFilter();

    public void setPath(String msg) {
        path = msg;
    }

    @Override
    public void execute() {
        if (path == null) {
            throw new BuildException("No path set.");
        }
        baseDir = new File(path);
        String buildDir = getProject().getProperty("build.dir");
        buildDir = (buildDir == null || "".equals(buildDir)) ? "Build" : buildDir;
        toDir = new File(baseDir.getParentFile(), buildDir);
        try {
            log("Copying files from:");
            log("           " + baseDir.getCanonicalPath());
            log("       to work dir:");
            log("           " + toDir.getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(SrcTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (toDir.exists()) {
            try {
                FileUtils.deleteDirectory(toDir);
            } catch (IOException ex) {
                Logger.getLogger(SrcTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        toDir.mkdirs();
        try {
            FileUtils.copyDirectory(baseDir, toDir);
        } catch (IOException ex) {
            Logger.getLogger(SrcTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        File[] listFiles = toDir.listFiles();
        String property = getProject().getProperty("project.sourceProcess");
        if ("true".equals(property)) {
            for (int i = 0; i < listFiles.length; i++) {
                File file = listFiles[i];
                if (isProject(file)) {
                    try {
                        processProject(file);
                    } catch (Exception ex) {
                        Logger.getLogger(SrcTask.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    /**
     * 更新给定目录下面的所有Android项目
     *
     * @return 成功更新的项目的路径
     */
    public String[] UpdateProjects(String dir) {
        return null;
    }

    /**
     * 更新给定目录下面的Android项目
     *
     * @return 项目更新成功返回true,其他情况下返回false.
     */
    public boolean UpdateProject(String dir) {
        return false;
    }

    /**
     * 根据提供的Android Project target 查找本地Android SDK 对应的 TargetId
     *
     * @return
     */
    private int getAndroidTargetId(String target) {
        return 0;
    }

    private void processProject(File toDir) throws Exception {
        ArrayList<File> srcDir = getSrcDir(toDir);
        for (int i = 0; i < srcDir.size(); i++) {
            File file = srcDir.get(i);
            if (file.isFile()) {
                processJavaFile(file);
            } else {
                processSrcDir(file);
            }
        }
    }

    private void processSrcDir(File file) {
        File[] listFiles = file.listFiles(javaFilter);
        for (int i = 0; i < listFiles.length; i++) {
            File f = listFiles[i];
            processJavaFile(f);
        }
        listFiles = file.listFiles(dirFilter);
        for (int i = 0; i < listFiles.length; i++) {
            File f = listFiles[i];
            processSrcDir(f);
        }
    }

    public class JavaFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            System.out.println(pathname.getAbsolutePath());
            if (pathname.isFile() && pathname.getName().endsWith(".java")) {
                return true;
            }
            return false;
        }
    }

    public class DirFilter implements FileFilter {

        @Override
        public boolean accept(File pathname) {
            System.out.println(pathname.getAbsolutePath());
            if (pathname.isDirectory()) {
                return true;
            }
            return false;
        }
    }

    private void processJavaFile(File javaFile) {
        PrintWriter out = null;
        File dest = new File(javaFile.getParentFile(), javaFile.getName() + ".back");
        javaFile.renameTo(dest);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(dest), "UTF-8"));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(javaFile), "UTF-8")));
            String readLine = in.readLine();
            while (readLine != null) {
                if (readLine.contains("android.util.Log") && !javaFile.getName().equals("Log.java")) {
                    System.out.println(readLine);
                    readLine = readLine.replace("android.util.Log", "cn.mimessage.util.Log");
                    System.out.println("   " + readLine);
                }
                out.println(readLine);
                readLine = in.readLine();
            }
            in.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SrcTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SrcTask.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
        dest.delete();
    }

    private boolean isProject(File file) {
        if (!file.isDirectory()) {
            return false;
        }
        if (!(new File(file, ".project")).exists()) {
            return false;
        }
        return true;
    }

    private ArrayList<File> getSrcDir(File projectDir) throws Exception {
        ArrayList<File> list = new ArrayList<File>();
        try {
            File classpath = new File(projectDir, ".classpath");
            DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domfac.newDocumentBuilder();

            NodeList childNodes = builder.parse(classpath).getElementsByTagName("classpathentry");
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node item1 = childNodes.item(j);
                if (item1.getAttributes().getNamedItem("kind").getNodeValue().equals("src")) {
                    String srcPath = item1.getAttributes().getNamedItem("path").getNodeValue();
                    if (!srcPath.equals("gen")) {
                        File f = new File(projectDir, srcPath);
                        System.out.println(f.getAbsoluteFile());
                        if (f.exists()) {
                            list.add(f);
                        } else {
                            list.add(processLinkSrc(f));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception();
        }
        return list;
    }

    private File processLinkSrc(File f) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static String getClassPath() {
        String value = System.getProperties().getProperty("java.class.path");
        String[] jar = value.split(";");
        StringBuilder sb = new StringBuilder();
        String sp = ";\n";
        sb.append("\"");
        sb.append(".;\n");
        for (int i = 1; i < jar.length; i++) {
            File file = new File(jar[i]);
            try {
                sb.append(file.getCanonicalPath()).append(sp);
            } catch (IOException ex) {
            }
        }
        sb.append("\"");
        System.out.println("ClassPath: ");
        System.out.println(sb.toString());
        return sb.toString();
    }
}
