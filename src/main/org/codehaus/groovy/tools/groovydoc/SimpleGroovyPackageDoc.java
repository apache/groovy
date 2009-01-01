/*
 * Copyright 2003-2009 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;

public class SimpleGroovyPackageDoc extends SimpleGroovyDoc implements GroovyPackageDoc {
    private static final char FS = '/';
    final Map classDocs;

    public SimpleGroovyPackageDoc(String name) {
        super(name);
        classDocs = new HashMap();
    }

    public GroovyClassDoc[] allClasses() {
        return (GroovyClassDoc[]) classDocs.values().toArray(new GroovyClassDoc[classDocs.values().size()]); // todo performance? sorting?
    }

    public void putAll(Map classes) {
        // 2 way relationship
        // add reference to classes inside this package
        classDocs.putAll(classes);

        // add reference to this package inside classes
        Iterator itr = classes.values().iterator();
        while (itr.hasNext()) {
            SimpleGroovyProgramElementDoc programElement = (SimpleGroovyProgramElementDoc) itr.next();
            programElement.setContainingPackage(this);
        }
    }

    public String nameWithDots() {
        return name().replace(FS, '.');
    }

    public GroovyClassDoc[] allClasses(boolean arg0) {/*todo*/
        return null;
    }

    public GroovyClassDoc[] enums() {/*todo*/
        return null;
    }

    public GroovyClassDoc[] errors() {/*todo*/
        return null;
    }

    public GroovyClassDoc[] exceptions() {/*todo*/
        return null;
    }

    public GroovyClassDoc findClass(String arg0) {/*todo*/
        return null;
    }

    public GroovyClassDoc[] interfaces() {/*todo*/
        return null;
    }

    public GroovyClassDoc[] ordinaryClasses() {
        List<GroovyClassDoc> classDocValues = new ArrayList(classDocs.values());
        Collections.sort(classDocValues); // todo - performance / maybe move into a sortMe() method
        return classDocValues.toArray(new GroovyClassDoc[classDocValues.size()]); // todo CURRENTLY ALL CLASSES!
    }

    public String getRelativeRootPath() {
        StringTokenizer tokenizer = new StringTokenizer(name(), "" + FS);
        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            sb.append("../");
        }
        return sb.toString();
    }

}
