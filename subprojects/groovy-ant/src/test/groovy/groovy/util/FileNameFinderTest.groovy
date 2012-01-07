/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.util

/**
 * Make sure FileNameFinder uses Ant filesets correctly.
 *
 * @author Dierk Koenig
 * @author Paul King
 */
class FileNameFinderTest extends GroovyLogTestCase {

    void testFilesInTestDirArePickedUp() {
        def finder = new FileNameFinder()
        def groovyFiles = finder.getFileNames('src/test/groovy', '**/*.groovy')
        assert groovyFiles, 'There should be groovy files in src/test/groovy'
        // now collect all those not starting with the 'Ant'
        def nonAntFiles = finder.getFileNames('src/test/groovy', '**/*.groovy', '**/Ant*')
        assert nonAntFiles, 'There should be non-Ant files in src/test/groovy'
        assert groovyFiles.size() > nonAntFiles.size()
    }
}
