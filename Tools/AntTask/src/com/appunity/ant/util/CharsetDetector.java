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

/**
 *
 * @author ZhangZhenli <zhangzhenli@live.com>
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.System.in;
import java.util.Arrays;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 *
 * @author xddai
 */
public class CharsetDetector {

    private Object mLock = new Object();
    private boolean found = false;
    private String result;
    private int lang;

    public String[] detectCharset(File file) throws IOException {
//        lang = nsPSMDetector.ALL;
        BufferedInputStream imp = new BufferedInputStream(new FileInputStream(file));
        lang = nsPSMDetector.CHINESE;
        String[] prob;
        // Initalize the nsDetector() ;
        nsDetector det = new nsDetector(lang);
        // Set an observer...
        // The Notify() will be called when a matching charset is found.
        det.Init(new nsICharsetDetectionObserverImpl(file.getAbsolutePath()));
        byte[] buf = new byte[1024];
        int len;
        boolean isAscii = true;
        while ((len = imp.read(buf, 0, buf.length)) != -1) {
            // Check if the stream is only ascii.
            if (isAscii) {
                isAscii = det.isAscii(buf, len);
            }
            // DoIt if non-ascii and not done yet.
            if (!isAscii) {
                if (det.DoIt(buf, len, false)) {
                    break;
                }
            }
        }
        imp.close();
        in.close();
        det.DataEnd();
        if (isAscii) {
            found = true;
            prob = new String[]{
                "ASCII"
            };
        } else if (found) {
            prob = new String[]{result};
        } else {
            prob = det.getProbableCharsets();
        }
        return prob;
    }

    private class nsICharsetDetectionObserverImpl implements nsICharsetDetectionObserver {

        private final String name;

        public nsICharsetDetectionObserverImpl(String name) {
            this.name = name;
        }

        @Override
        public void Notify(String charset) {
            found = true;
            result = charset;
//                System.out.println(" ===============" + result + "  " + name);
        }
    }
}
