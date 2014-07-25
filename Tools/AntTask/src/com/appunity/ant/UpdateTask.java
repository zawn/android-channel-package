package com.appunity.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 处理源码
 *
 * @author Zawn
 */
public class UpdateTask extends Task {

    String path;
    File baseDir;
    private String android;

    public void setPath(String msg) {
        path = msg;
    }

    @Override
    public void execute() {
        if (path == null) {
            throw new BuildException("No path set.");
        }
        baseDir = new File(path);
        try {
            UpdateProjects(baseDir.getCanonicalPath());
        } catch (IOException ex) {
            throw new BuildException("The path parameter configuration is invalid");
        }
    }

    /**
     * 更新给定目录下面的所有Android项目
     *
     * @return 成功更新的项目的路径
     */
    public ArrayList<String> UpdateProjects(String dirString) {
        android = getProject().getProperty("android");
        File dir = new File(dirString);
        File[] listFiles = dir.listFiles();
        ArrayList<String> list = new ArrayList<String>();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (UpdateProject(file) != null) {
                    list.add(file.getAbsolutePath());
                    File to = new File(file, "ant.properties");
                    System.out.println(to);
                    File from = new File(getProject().getProperty("tools.config"), "signature.properties");
                    System.out.println(from);
                    try {
                        FileUtils.copyFile(from, to);
                    } catch (IOException ex) {
                        Logger.getLogger(UpdateTask.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
            return list;
        }
        return null;
    }

    public String UpdateProject(File dir) {
        return UpdateProject(dir, android, null);
    }

    /**
     * 更新给定目录下面的Android项目
     *
     * @return 项目更新成功返回true,其他情况下返回false.
     */
    public static String UpdateProject(File dir, String android, String channelName) {
        if (!dir.isDirectory()) {
            return null;
        }
        String pram = null;
        String distPackageName = null;
        try {
            int isLib = isLib(dir);
            if (isLib < 0) {
                return null;
            }
            String[] appNameAndVersion = getAppNameAndVersion(dir, channelName, isLib);
            String appName = appNameAndVersion[0];
            if (appName == null) {
                appName = dir.getName();
            }
            if (channelName != null) {
                distPackageName = Utils.toPinYin(appName) + "-" + appNameAndVersion[1] + "-" + channelName;
            } else {
                distPackageName = Utils.toPinYin(appName) + "-" + appNameAndVersion[1];
            }
            if (isLib != 0) {
                pram = android + " update ";
                pram = pram + " lib-project -p " + dir.getAbsolutePath();
            } else {
                System.out.println("DistName    : " + distPackageName + "-release.apk");
                pram = android + " update ";
                pram = pram + " project -p " + dir.getAbsolutePath() + " -n " + distPackageName;
            }
            System.out.println(pram);
        } catch (Exception ex) {
        }
        if (pram != null) {
            Cmd.exec(pram);
            return distPackageName;
        }
        return null;
    }

    /**
     * 根据提供的Android Project target 查找本地Android SDK 对应的 TargetId
     *
     * @return
     */
    private int getAndroidTargetId(String target) {
        return 0;
    }

    /**
     * 获取Dir指定目录下Android项目的名称
     *
     * @param dir
     * @return
     */
    private static String[] getAppNameAndVersion(File dir, String channelName, int islib) throws Exception {
        String appName = null;
        String versionName = null;
        try {
            File androidManifest = new File(dir, "AndroidManifest.xml");
            DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domfac.newDocumentBuilder();
            Document xml = builder.parse(androidManifest);
            Element manifest = xml.getDocumentElement();
            versionName = manifest.getAttribute("android:versionName");
            System.out.println("Package     : " + manifest.getAttribute("package"));
            System.out.println("VersionCode : " + manifest.getAttribute("android:versionCode"));
            System.out.println("VersionName : " + manifest.getAttribute("android:versionName"));
            Node application = manifest.getElementsByTagName("application").item(0);
            NamedNodeMap attributes = application.getAttributes();

            // 获取App名称
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                if (item.getNodeName().indexOf("label") > 0) {
                    appName = item.getNodeValue();
                    String atString = "@string/";
                    int index = appName.indexOf(atString);
                    String substring = appName.substring(index + atString.length());
                    if (index >= 0) {
                        File strings = new File(dir, "res/values/strings.xml");
                        NodeList childNodes = builder.parse(strings).getElementsByTagName("string");
                        for (int j = 0; j < childNodes.getLength(); j++) {
                            Node item1 = childNodes.item(j);
                            if (item1.getAttributes().getNamedItem("name").getNodeValue().equals(substring)) {
                                appName = item1.getTextContent();
                            }
                        }
                    }
                    break;
                }
            }
            System.out.println("AppName     : " + appName);
            if (islib == 0) {
                // 添加发行渠道名称
                boolean isSet = false;
                if (channelName != null) {
                    NodeList childNodes = application.getChildNodes();
                    ExitReadDom:
                    for (int i = 0; i < childNodes.getLength(); i++) {
                        Node node = childNodes.item(i);
                        if ("meta-data".equals(node.getNodeName())) {
                            NamedNodeMap attr = node.getAttributes();
                            for (int j = 0; j < attr.getLength(); j++) {
                                Node item = attr.item(j);
                                if ("UMENG_CHANNEL".equals(item.getNodeValue())) {
                                    isSet = true;
                                    break;
                                }
                            }
                            if (isSet) {
                                for (int j = 0; j < attr.getLength(); j++) {
                                    Node item = attr.item(j);
                                    if ("android:value".equals(item.getNodeName())) {
                                        item.setNodeValue(channelName);
                                        break ExitReadDom;
                                    }
                                }
                            }
                        }
                    }
                    if (!isSet) {
                        Element channelNode = xml.createElement("meta-data");
                        channelNode.setAttribute("android:name", "UMENG_CHANNEL");
                        channelNode.setAttribute("android:value", channelName);
                        application.appendChild(channelNode);
                    }
                    xml.setXmlStandalone(true);
                    saveDocument(xml, androidManifest, !isSet);
                }
            }
        } catch (Exception ex) {
            throw new Exception();
        }
        if (versionName == null) {
            versionName = "UnnamedVersion";
        }
        return new String[]{appName, versionName};
    }

    /**
     * 保存生成或修改完的xml文件
     *
     * @param doc
     * @param file
     * @throws Exception
     */
    private static void saveDocument(Document doc, File file, boolean backup) throws Exception {
        // 开始把Document映射到文件
        TransformerFactory transformerFactory = TransformerFactory
                .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        // 设置输出结果
        DOMSource source = new DOMSource(doc);
        // 判断文件是否存在，如不存在则创建
        if (!file.exists()) {
            file.createNewFile();
        } else {
            if (backup) {
                File backFile = new File(file.getParentFile(), file.getName() + ".back");
                FileUtils.copyFile(file, backFile);
            }
        }
        // 设置输入源
        Result xmlResult = new StreamResult(file);
        // 输出xml文件
        transformer.transform(source, xmlResult);
    }

    /**
     * 判断指定的项目是否是Android库项目
     *
     * @param dir
     * @return -1,不是项目;0,普通项目;1,库项目
     */
    public static int isLib(File dir) {
        File f = new File(dir, "project.properties");
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(f));
            String property = p.getProperty("android.library", "false");
            boolean parseBoolean = Boolean.parseBoolean(property);
            if (parseBoolean) {
                return 1;
            } else {
                return 0;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UpdateTask.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UpdateTask.class.getName()).log(Level.SEVERE, null, ex);
        }
        f = new File(dir, "AndroidManifest.xml");
        if (!f.exists()) {
            return -1;
        } else {
            try {
                throw new BuildException("Find the following documents :" + f.getCanonicalPath() + "\nBut lost :" + dir.getCanonicalPath() + "/project.properties");
            } catch (IOException ex) {
                Logger.getLogger(UpdateTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return -1;
    }
}
