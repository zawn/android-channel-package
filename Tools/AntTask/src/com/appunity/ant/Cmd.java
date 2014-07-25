package com.appunity.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.System.out;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ZhangZhenli
 */
public class Cmd {

    public static void exec(String param) {
        BufferedReader in = null;
        try {
            String cmd = "cmd /c ";
            Process exec = Runtime.getRuntime().exec(cmd + param);
            // 获得输出
            in = new BufferedReader(new InputStreamReader(exec.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
            }
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(Cmd.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Cmd.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void exe(String param) {
        BufferedReader in = null;
        try {
            String cmd = "cmd /c start ";
            Runtime.getRuntime().exec(cmd + param);
        } catch (IOException ex) {
        }
    }

    public static void exeFile(String file) throws IOException {
        exeFile(file, "");
    }

    static void exeFile(String file, String params) throws IOException {
       try {
            File dir = new File(Cmd.class.getResource("/").toURI());
            File f = new File(dir, file);
            if (!f.exists() || !f.canExecute()) {
                throw new IOException("文件不存在或不可执行");
            }
            try {
                String cmd = "cmd /k start ";
                String param = f.getAbsolutePath()+" ";
                Runtime.getRuntime().exec(cmd + param+params);
            } catch (IOException ex) {
                Logger.getLogger(Cmd.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(Cmd.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
