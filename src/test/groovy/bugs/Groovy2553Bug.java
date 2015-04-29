/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.bugs;

import groovy.lang.GroovyShell;
import junit.framework.TestCase;

class Autobox {

    public static class Util {
        public static void printByte(String str, Byte defaultValue) {
            System.out.println(str + ", " + defaultValue);
        }
    }
}

public class Groovy2553Bug extends TestCase {

    public void testMe () {
        new GroovyShell().evaluate("groovy.bugs.Autobox.Util.printByte(\"1\", Byte.valueOf((byte)1));");
        new GroovyShell().evaluate("groovy.bugs.Autobox.Util.printByte(\"1\", (byte)1);");
    }
}
