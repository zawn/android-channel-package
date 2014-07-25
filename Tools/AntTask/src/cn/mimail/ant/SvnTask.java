package cn.mimail.ant;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.LogLevel;

/**
 * 从SVN检出代码
 *
 * @author Zawn
 */
public class SvnTask extends Task {
    
    public static final String TAG = "SvnTask ";
    
    Properties svnProperties;
    String message;
    String text;
    private File baseDir;
    private String relativePath;
    
    public void addText(String t) {
        text = t;
    }
    
    public void setMessage(String msg) {
        message = msg;
    }
    
    @Override
    public void execute() {
        svnProperties = new Properties();
        baseDir = getProject().getBaseDir();
        String mSubversionLocal = getProject().getProperty("svn.local");
        String mSubversionConfig = getProject().getProperty("svn.config");
        mSubversionLocal = obtainValidPath(mSubversionLocal);
        mSubversionConfig = obtainValidPath(mSubversionConfig);
        System.out.println("baseDir     :" + baseDir.getAbsolutePath());
        File localDir = new File(mSubversionLocal);
        File configFile = new File(mSubversionConfig);
        svnProperties.setProperty("svn.username", getProject().getProperty("svn.username"));
        svnProperties.setProperty("svn.password", getProject().getProperty("svn.password"));
        svnProperties.setProperty("svn.url", getProject().getProperty("svn.url"));
        try {
            svnProperties.setProperty("svn.local", localDir.getCanonicalPath());
        } catch (IOException ex) {
            throw new BuildException("The svn.properties svn.local parameter configuration is invalid");
        }
        try {
            svnProperties.setProperty("svn.config", configFile.getCanonicalPath());
        } catch (IOException ex) {
            throw new BuildException("The svn.properties svn.config parameter configuration is invalid");
        }
        try {
            
            syncProjectCode(svnProperties);
        } catch (IOException ex) {
            log(ex, LogLevel.ERR.getLevel());
        }
        if (message == null) {
            throw new BuildException("No message set.");
        }
//        log(message);
//        log(text);
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
            e.printStackTrace();
        }
        return path;
    }
}
