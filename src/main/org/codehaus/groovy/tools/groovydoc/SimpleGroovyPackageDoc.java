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

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SimpleGroovyPackageDoc extends SimpleGroovyDoc implements GroovyPackageDoc {
    private static final char FS = '/';
    final Map<String, GroovyClassDoc> classDocs;
    private String description = "";
    private String summary = "";

    public SimpleGroovyPackageDoc(String name) {
        super(name);
        classDocs = new TreeMap<String, GroovyClassDoc>();
    }

    public GroovyClassDoc[] allClasses() {
        return classDocs.values().toArray(new GroovyClassDoc[classDocs.values().size()]);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void putAll(Map<String, GroovyClassDoc> classes) {
        // 2 way relationship
        // add reference to classes inside this package
        classDocs.putAll(classes);

        // add reference to this package inside classes
        for (GroovyClassDoc doc : classes.values()) {
            SimpleGroovyProgramElementDoc programElement = (SimpleGroovyProgramElementDoc) doc;
            programElement.setContainingPackage(this);
        }
    }

    public String nameWithDots() {
        return name().replace(FS, '.');
    }

    public GroovyClassDoc[] allClasses(boolean arg0) {
        List<GroovyClassDoc> classDocValues = new ArrayList<GroovyClassDoc>(classDocs.values());
        return classDocValues.toArray(new GroovyClassDoc[classDocValues.size()]);
    }

    public GroovyClassDoc[] enums() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.values().size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isEnum()) {
                result.add(doc);
            }
        }
        return result.toArray(new GroovyClassDoc[result.size()]);
    }

    public GroovyClassDoc[] errors() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.values().size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isError()) {
                result.add(doc);
            }
        }
        return result.toArray(new GroovyClassDoc[result.size()]);
    }

    public GroovyClassDoc[] exceptions() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.values().size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isException()) {
                result.add(doc);
            }
        }
        return result.toArray(new GroovyClassDoc[result.size()]);
    }

    public GroovyClassDoc findClass(String arg0) {/*todo*/
        return null;
    }

    public GroovyClassDoc[] interfaces() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.values().size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isInterface()) {
                result.add(doc);
            }
        }
        return result.toArray(new GroovyClassDoc[result.size()]);
    }

    public GroovyClassDoc[] ordinaryClasses() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.values().size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isOrdinaryClass()) {
                result.add(doc);
            }
        }
        return result.toArray(new GroovyClassDoc[result.size()]);
    }

    public String description() {
        return description;
    }

    public String summary() {
        return summary;
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
