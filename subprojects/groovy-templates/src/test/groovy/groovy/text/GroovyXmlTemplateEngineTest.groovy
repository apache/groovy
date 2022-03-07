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
package groovy.text

import groovy.xml.XmlParser
import org.junit.Test

class GroovyXmlTemplateEngineTest {
    @Test
    void testFormat1() {
        String xmlScript =
"""<?xml version="1.0"?>
<person>
   <name>Daniel</name>
</person>
"""
        def name1 = new XmlParser().parseText(xmlScript).name.text()
        def name2 = new XmlParser().parseText(new XmlTemplateEngine().createTemplate(xmlScript).make().toString()).name.text()
        assert name1 == name2
    }

    @Test
    void testFormat2() {
        String xmlScript =
'''<person>
   <name>
       <firstName>Daniel</firstName>
       <lastName>Sun</lastName>
   </name>
   <age>37</age>
</person>
'''

        def expected =
'''<person>
  <name>
    <firstName>Daniel</firstName>
    <lastName>Sun</lastName>
  </name>
  <age>37</age>
</person>
'''
        assert expected == new XmlTemplateEngine().createTemplate(xmlScript).make().toString()
    }
}