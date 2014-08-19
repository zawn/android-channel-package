package com.appunity.ant;

import com.appunity.ant.util.TestRunCmd;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    private static class StreamDrainer implements Runnable {

        private InputStream ins;

        public StreamDrainer(InputStream ins) {
            this.ins = ins;
        }

        public void run() {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(ins, "GBK"));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static int exec(String param) {
        String[] cmd = new String[]{"cmd.exe", "/C", param};
        try {
            Process process = Runtime.getRuntime().exec(cmd);

            new Thread(new StreamDrainer(process.getInputStream())).start();
            new Thread(new StreamDrainer(process.getErrorStream())).start();

            process.getOutputStream().close();

            int exitValue = process.waitFor();
            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
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
            File dir = new File(Cmd.class
                    .getResource("/").toURI());
            File f = new File(dir, file);

            if (!f.exists()
                    || !f.canExecute()) {
                throw new IOException("文件不存在或不可执行");
            }

            try {
                String cmd = "cmd /k start ";
                String param = f.getAbsolutePath() + " ";
                Runtime.getRuntime().exec(cmd + param + params);
            } catch (IOException ex) {
                Logger.getLogger(Cmd.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }
        } catch (URISyntaxException ex) {
            Logger.getLogger(Cmd.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
