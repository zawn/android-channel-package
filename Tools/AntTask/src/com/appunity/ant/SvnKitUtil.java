package com.appunity.ant;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.*;
import org.tmatesoft.svn.examples.wc.CommitEventHandler;
import org.tmatesoft.svn.examples.wc.UpdateEventHandler;
import org.tmatesoft.svn.examples.wc.WCEventHandler;

/**
 * 封装Svn的辅助类
 *
 * @author Zawn
 */
public class SvnKitUtil {

    public static final String PROPERTY_USERNAME = "svn.username";
    public static final String PROPERTY_PASSWORD = "svn.password";
    public static final String PROPERTY_URL = "svn.url";
    public static final String PROPERTY_LOCAL = "svn.local";
    public static final String PROPERTY_CONFIG = "svn.config";
    public static final String PROPERTY_PATH = "svn.path";
    private static SVNClientManager svnManager;
    private static ISVNEventHandler commitEventHandler;
    private static ISVNEventHandler updateEventHandler;
    private static ISVNEventHandler WCEventHandler;
    private static String svnName;
    private static String svnPassword;
    private static boolean isInit = false;
    public static File svnConfigDir;
    public static SVNURL[] svnURLs;
    public static File local;

    public static boolean isInit() {
        return isInit;
    }

    /**
     * 工具初始化
     */
    static {
        /*
         * Initializes the library (it must be done before ever using the
         * library itself)
         */
        setupLibrary();

        /*
         * Creating custom handlers that will process events
         */
        commitEventHandler = new CommitEventHandler();

        updateEventHandler = new UpdateEventHandler();

        WCEventHandler = new WCEventHandler();
    }

    public static void init(Properties properties) throws IOException {
        if (isInit) {
            return;
        }
        System.out.println("The initialization SVN components...");
        if (properties == null) {
            throw new NullPointerException("If you need to check out the code from SVN above, you must configuration svn.properties files in a certain directory ! ");
        }
        svnName = properties.getProperty(PROPERTY_USERNAME, "anonymous");
        svnPassword = properties.getProperty(PROPERTY_PASSWORD, "anonymous");
        String svnUrlString = properties.getProperty(PROPERTY_URL);
        try {
            String[] s;
            s = svnUrlString.split("\\*");
            svnURLs = new SVNURL[s.length];
            for (int i = 0; i < s.length; i++) {
                svnURLs[i] = SVNURL.parseURIEncoded(s[i]);
            }
        } catch (SVNException e) {
            throw new NullPointerException("If you need to check out the code from SVN above, you must configure the svn url svn.properties file");
        }
        local = new File(properties.getProperty(PROPERTY_PATH), properties.getProperty(PROPERTY_LOCAL, "../../CheckOut"));
        svnConfigDir = new File(properties.getProperty(PROPERTY_PATH), properties.getProperty(PROPERTY_CONFIG));
        try {
            System.out.println("svnConfigDir:"+svnConfigDir.getCanonicalPath());
        } catch (IOException ex) {
            Logger.getLogger(SvnKitUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (svnConfigDir.exists() && !svnConfigDir.isDirectory()) {
            svnConfigDir.delete();
        }
        if (!svnConfigDir.exists()) {
            svnConfigDir.mkdirs();
        }
        try {
            System.out.println("svnName     :" + svnName);
            System.out.println("svnPassword :" + svnPassword);
            for (int i = 0; i < svnURLs.length; i++) {
                SVNURL svnurl = svnURLs[i];
                System.out.println("svnUrls[" + i + "]  :" + svnurl);

            }
            System.out.println("localPath   :" + local.getCanonicalPath());
            System.out.println("configDir   :" + svnConfigDir.getCanonicalPath());
        } catch (IOException ex) {
            throw ex;
        }
        /*
         * Creates a default run-time configuration options driver. Default
         * options created in this way use the Subversion run-time configuration
         * area (for instance, on a Windows platform it can be found in the
         * '%APPDATA%\Subversion' directory).
         *
         * readonly = true - not to save any configuration changes that can be
         * done during the program run to a config file (config settings will
         * only be read to initialize; to enable changes the readonly flag
         * should be set to false).
         *
         * SVNWCUtil is a utility class that creates a default options driver.
         */
        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(svnConfigDir, true);
        
        /*
         * 不存数认证数据
         */
        options.setAuthStorageEnabled(false);
        
        /*
         * Creates an instance of SVNClientManager providing authentication
         * information (name, password) and an options driver
         */
        svnManager = SVNClientManager.newInstance(options, svnName, svnPassword);

        /*
         * Sets a custom event handler for operations of an SVNCommitClient
         * instance
         */
        svnManager.getCommitClient().setEventHandler(commitEventHandler);

        /*
         * Sets a custom event handler for operations of an SVNUpdateClient
         * instance
         */
        svnManager.getUpdateClient().setEventHandler(updateEventHandler);

        /*
         * Sets a custom event handler for operations of an SVNWCClient instance
         */
        svnManager.getWCClient().setEventHandler(WCEventHandler);

        /**
         * 初始化成功
         */
        isInit = true;
    }

    /**
     * 与SVN同步代码
     *
     * @param properties
     * @return
     */
    public static boolean syncProjectCode(Properties properties) throws IOException {
        init(properties);
        if (local.exists() && !local.isDirectory()) {
            local.delete();
        }
        if (!local.exists()) {
            boolean mkdirs = local.mkdirs();
            if (!mkdirs) {
                System.out.println("Unable to create the specified directory : " + local.getCanonicalPath());
                return false;
            }
        }
        if (!local.canWrite()) {
            System.out.println("Can not be written to the file to the specified directory : " + local.getCanonicalPath());
            return false;
        }
        String dirString;
        File dir;
        for (int i = 0; i < svnURLs.length; i++) {
            SVNURL svnURL = svnURLs[i];
            dirString = (new File(svnURL.getPath())).getName();
            System.out.println("Checking out a working copy(" + (i + 1) + "/" + svnURLs.length + ") \n  from: " + svnURL);
            dir = new File(local, dirString);
            dirCheck(dir);
            System.out.println("    to: " + dir.getCanonicalPath());
            try {
                /*
                 * recursively checks out a working copy from url into wcDir.
                 * SVNRevision.HEAD means the latest revision to be checked out.
                 */
                checkout(svnURL, SVNRevision.HEAD, dir, true);
            } catch (SVNException svne) {
                if (svne.getErrorMessage().getErrorCode().equals(SVNErrorCode.WC_LOCKED)) {
                    try {
                        System.out.println(svne.getErrorMessage().getErrorCode().toString());
                        cleanup(dir);
                        checkout(svnURL, SVNRevision.HEAD, dir, true);
                    } catch (SVNException ex) {
                        error("error while checking out a working copy for the location '"
                                + svnURL + "'", svne);
                    }
                } else {
                    error("error while checking out a working copy for the location '"
                            + svnURL + "'", svne);
                }
            }
        }
        return true;
    }

    /*
     * Displays error information and exits.
     */
    private static void error(String message, Exception e) {
        System.err.println(message + (e != null ? ": " + e.getMessage() : ""));
        System.exit(1);
    }

    /**
     * Initializes the library to work with a repository via different
     * protocols.
     */
    private static void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();

        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

    /*
     * Checks out a working copy from a repository. Like 'svn checkout URL[@REV]
     * PATH (-r..)' command; It's done by invoking
     *
     * SVNUpdateClient.doCheckout(SVNURL url, File dstPath, SVNRevision
     * pegRevision, SVNRevision revision, boolean recursive)
     *
     * which takes the following parameters:
     *
     * url - a repository location from where a working copy is to be checked
     * out;
     *
     * dstPath - a local path where the working copy will be fetched into;
     *
     * pegRevision - an SVNRevision representing a revision to concretize url
     * (what exactly URL a user means and is sure of being the URL he needs); in
     * other words that is the revision in which the URL is first looked up;
     *
     * revision - a revision at which a working copy being checked out is to be;
     *
     * recursive - if true and url corresponds to a directory then
     * doCheckout(..) recursively fetches out the entire directory, otherwise -
     * only child entries of the directory;
     */
    private static long checkout(SVNURL url,
            SVNRevision revision, File destPath, boolean isRecursive)
            throws SVNException {

        SVNUpdateClient updateClient = svnManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the checkout
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision at which the working copy is
         */
        return updateClient.doCheckout(url, destPath, revision, revision, SVNDepth.fromRecurse(isRecursive),
                false);
    }

    private static void cleanup(File destPath) throws SVNException {
        SVNWCClient wcClient = svnManager.getWCClient();
        wcClient.doCleanup(destPath);
    }

    /*
     * Updates a working copy (brings changes from the repository into the
     * working copy). Like 'svn update PATH' command; It's done by invoking
     *
     * SVNUpdateClient.doUpdate(File file, SVNRevision revision, boolean
     * recursive)
     *
     * which takes the following parameters:
     *
     * file - a working copy entry that is to be updated;
     *
     * revision - a revision to which a working copy is to be updated;
     *
     * recursive - if true and an entry is a directory then doUpdate(..)
     * recursively updates the entire directory, otherwise - only child entries
     * of the directory;
     */
    private static long update(File wcPath, SVNRevision updateToRevision, boolean isRecursive) throws SVNException {

        SVNUpdateClient updateClient = svnManager.getUpdateClient();
        /*
         * sets externals not to be ignored during the update
         */
        updateClient.setIgnoreExternals(false);
        /*
         * returns the number of the revision wcPath was updated to
         */
        return updateClient.doUpdate(wcPath, updateToRevision, SVNDepth.fromRecurse(isRecursive), false, false);
    }

    private static void dirCheck(File dir) throws IOException {
        if (dir.exists() && !dir.isDirectory()) {
            dir.delete();
        }
        if (!dir.exists()) {
            boolean mkdirs = dir.mkdirs();
            if (!mkdirs) {
                throw new IOException("Could not create directory : " + dir.getCanonicalPath());
            }
        }
        if (!dir.canWrite()) {
            throw new IOException("The directory is not write :  " + dir.getCanonicalPath());
        }
    }
}
