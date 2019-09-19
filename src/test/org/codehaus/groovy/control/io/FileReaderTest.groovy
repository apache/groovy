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
package org.codehaus.groovy.control.io

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilerConfiguration

class FileReaderTest extends GroovyTestCase {

    void testFileBOM() {
        def file = File.createTempFile("encoding", ".groovy")
        file.deleteOnExit()
        
        def fos = new FileOutputStream(file)
        // first write the byteorder mark for UTF-8
        fos.write(0xEF)
        fos.write(0xBB)
        fos.write(0xFF)
        fos.write("return 1".getBytes("US-ASCII"))
        fos.flush()
        fos.close()
        
        def config = new CompilerConfiguration()
        config.sourceEncoding = "UTF-8"
        def gcl = new GroovyClassLoader(this.class.classLoader, config)
        def clazz = gcl.parseClass(file)
        def result = clazz.newInstance().run()
        assert result == 1
    }
}
