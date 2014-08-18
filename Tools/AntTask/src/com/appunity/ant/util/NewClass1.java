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
package com.appunity.ant.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
public class NewClass1 {

    private boolean found;

    public void find(File file) throws FileNotFoundException, IOException {
        // Initalize the nsDetector() ;
        nsDetector det = new nsDetector(nsPSMDetector.ALL);
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserverImpl());
        BufferedInputStream imp = new BufferedInputStream(new FileInputStream(file));
        getEncoding(imp, det);
    }

    public void getEncoding(InputStream imp, nsDetector det) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;
        while ((len = imp.read(buf, 0, buf.length)) != -1) {
            // Check if the stream is only ascii.
            if (isAscii) {
                isAscii = det.isAscii(buf, len);
            }
            // DoIt if non-ascii and not done yet.
            if (!isAscii && !done) {
                done = det.DoIt(buf, len, false);
            }
        }
        det.DataEnd();
        if (isAscii) {
            System.out.println("CHARSET = ASCII");
            found = true;
        }
        if (!found) {
            String prob[] = det.getProbableCharsets();
            for (int i = 0; i < prob.length; i++) {
                System.out.println("Probable Charset = " + prob[i]);
            }
        }
    }

    private class nsICharsetDetectionObserverImpl implements nsICharsetDetectionObserver {

        public void Notify(String charset) {
            found = true;
            System.out.println("CHARSET = " + charset);
        }
    }

}
