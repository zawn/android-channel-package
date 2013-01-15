/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.mimail.ant;

import java.io.File;
import java.util.ArrayList;
import junit.framework.TestCase;
import org.w3c.dom.Document;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class UpdateTaskTest extends TestCase {

    public UpdateTaskTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of UpdateProject method, of class UpdateTask.
     */
    public void testUpdateProject_File_String() {
        System.out.println("UpdateProject");
        File dir = new File("G:\\android-channel-package\\Build\\HuYing");
        String channelName = "WanDouJi1";
        UpdateTask instance = new UpdateTask();
        boolean expResult = true;
        String result = UpdateTask.UpdateProject(dir,"D:\\Android\\android-sdk-windows\\tools\\android.bat", channelName);
        System.out.println(result);
//        assertEquals(expResult, result);
    }
}
