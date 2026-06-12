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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Default {@link GroovyPackageDoc} implementation.
 */
public class SimpleGroovyPackageDoc extends SimpleGroovyDoc implements GroovyPackageDoc {
    private static final char FS = '/';
    private static final GroovyClassDoc[] EMPTY_GROOVYCLASSDOC_ARRAY = new GroovyClassDoc[0];
    /**
     * Classes indexed by their names within this package.
     */
    final Map<String, GroovyClassDoc> classDocs;
    private String description = "";
    private String summary = "";

    /**
     * Creates a documented package with the supplied name.
     *
     * @param name the package name
     */
    public SimpleGroovyPackageDoc(String name) {
        super(name);
        classDocs = new TreeMap<String, GroovyClassDoc>();
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] allClasses() {
        return classDocs.values().toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /**
     * Sets the package description text.
     *
     * @param description the package description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the package summary text.
     *
     * @param summary the package summary
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Adds all supplied classes to this package and back-links them to the package.
     *
     * @param classes the classes to add
     */
    public void putAll(Map<String, GroovyClassDoc> classes) {
        // 2 way relationship for visible classes:
        // add reference to classes inside this package
        // add reference to this package inside classes
        for (Map.Entry<String, GroovyClassDoc> docEntry : classes.entrySet()) {
            final GroovyClassDoc classDoc = docEntry.getValue();
            classDocs.put(docEntry.getKey(), classDoc);
            SimpleGroovyProgramElementDoc programElement = (SimpleGroovyProgramElementDoc) classDoc;
            programElement.setContainingPackage(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String nameWithDots() {
        return name().replace(FS, '.');
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] allClasses(boolean arg0) {
        List<GroovyClassDoc> classDocValues = new ArrayList<GroovyClassDoc>(classDocs.values());
        return classDocValues.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] enums() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isEnum()) {
                result.add(doc);
            }
        }
        return result.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] errors() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isError()) {
                result.add(doc);
            }
        }
        return result.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] exceptions() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isException()) {
                result.add(doc);
            }
        }
        return result.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc findClass(String arg0) {/*todo*/
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] interfaces() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isInterface()) {
                result.add(doc);
            }
        }
        return result.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public GroovyClassDoc[] ordinaryClasses() {
        List<GroovyClassDoc> result = new ArrayList<GroovyClassDoc>(classDocs.size());
        for (GroovyClassDoc doc : classDocs.values()) {
            if (doc.isOrdinaryClass()) {
                result.add(doc);
            }
        }
        return result.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    /** {@inheritDoc} */
    @Override
    public String description() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public String summary() {
        return summary;
    }

    /** {@inheritDoc} */
    @Override
    public String getRelativeRootPath() {
        StringTokenizer tokenizer = new StringTokenizer(name(), "" + FS);
        StringBuilder sb = new StringBuilder();
        while (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            sb.append("../");
        }
        return sb.toString();
    }

}
