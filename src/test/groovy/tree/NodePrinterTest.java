/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package groovy.tree;

import groovy.lang.GroovyObject;
import org.codehaus.groovy.classgen.TestSupport;

import java.util.logging.Logger;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class NodePrinterTest extends TestSupport {

    public void testTree() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/TreeTest.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testVerboseTree() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/VerboseTreeTest.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testSmallTree() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/SmallTreeTest.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testLittleClosure() throws Exception {
        GroovyObject object = compile("src/test/groovy/LittleClosureTest.groovy");
        object.invokeMethod("testClosure", null);
    }

    public void testNestedClosureBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/NestedClosureBugTest.groovy");
        object.invokeMethod("testNestedClosureBug", null);
    }

    public void testClosureClassLoaderBug() throws Exception {
        GroovyObject object = compile("src/test/groovy/tree/ClosureClassLoaderBug.groovy");
        object.invokeMethod("testTree", null);
    }

    public void testLogging() {
        Logger log = Logger.getLogger(getClass().getName());
        log.info("Logging using JDK 1.4 logging");
    }
}
