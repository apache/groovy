/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

/** 
 * Tests the use of GroovyLog
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
class LogTest extends GroovyTestCase {

    void testUseLog() {
        def file = "something.txt"

        def log = GroovyLog.newInstance(getClass())
        
        log.starting("Hey I'm starting up...")
        
        log.openFile("Am about to open file ${file}")

        // ...

        log.closeFile("Have closed the file ${file}")

        log.stopping("..Finished")
    }
}
