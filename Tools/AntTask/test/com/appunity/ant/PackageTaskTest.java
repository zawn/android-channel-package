/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.appunity.ant;

import com.appunity.ant.PackageTask;
import java.util.ArrayList;
import junit.framework.TestCase;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class PackageTaskTest extends TestCase {

    public PackageTaskTest(String testName) {
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
     * Test of execute method, of class PackageTask.
     */
    public void testUpdateProjects() {
        System.out.println("UpdateProjects");
        PackageTask instance = new PackageTask();
        instance.packageProjects("G:\\Ant\\Build",new ArrayList<String>());
    }
}
