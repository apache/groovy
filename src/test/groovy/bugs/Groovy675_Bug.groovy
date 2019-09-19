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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy675_Bug extends GroovyTestCase {
    void testStringAndGString() {
    assert "\\"!="\\\\" 
    assert "\\\$"=="\\"+"\$" 
    assert "\\"+"\\\\" == "\\"+"\\"+"\\" && "\\\\"+"\\" == "\\"+"\\"+"\\"
    assert ("\\\\"+"\\").length() == 3
    assert "\\3 \$1\$2" == "\\" + "3" + " " + "\$" + "1" + "\$" + "2"
    assert "\\\\3 \\\$1\$2" == "\\" + "\\" + "3" + " " + "\\"+ "\$" + "1" + "\$" + "2"
    assert "\\\\\\3 \\\\\$1\$2" == "\\" + "\\\\" + "3" + " " + "\\\\"+ "\$" + "1" + "\$" + "2"
    assert "\\\\\\\\3 \\\\\\\$1\$2" == "\\\\" + "\\\\" + "3" + " " + "\\\\\\"+ "\$" + "1" + "\$" + "2"

    assert "\\\\" == "\\" + "\\"
    assert "\\\\".length() == 2

    def z = 100 + 200
    assert "\\\\ \\ ${z}" == "\\\\ \\ 300"
    assert "\\\\ \\ ${z}" == "\\" + "\\" + " " + "\\" + " " + "300"
    assert "Hello\\, \\World\\".charAt(4) == "o".charAt(0)
    assert "Hello\\, \\World\\".charAt(5) == "\\".charAt(0)
    assert "Hello\\, \\World\\".charAt(6) == ",".charAt(0)
    }
}
