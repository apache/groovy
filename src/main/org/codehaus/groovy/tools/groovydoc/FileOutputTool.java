/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.tools.groovydoc;

import java.io.File;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

public class FileOutputTool implements OutputTool {
	public void makeOutputArea(String filename) {
		File dir = new File(filename);
		dir.mkdirs();
	}

	public void writeToOutput(String fileName, String text) throws Exception {
        File file = new File(fileName);
        file.getParentFile().mkdirs();
        DefaultGroovyMethods.write(file, text);
	}
}
