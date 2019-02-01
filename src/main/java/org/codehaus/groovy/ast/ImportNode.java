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
package org.codehaus.groovy.ast;

import org.objectweb.asm.Opcodes;

/**
 * Represents an import statement of a single class
 */
public class ImportNode extends AnnotatedNode implements Opcodes {

    private final ClassNode type;
    private final String alias;
    private final String fieldName;
    // TODO use PackageNode instead here?
    private final String packageName;
    private final boolean isStar;
    private final boolean isStatic;

    /**
     * Represent an import of an entire package, i.e.&#160;import package.Classname
     *
     * @param type  the referenced class
     * @param alias optional alias
     */
    public ImportNode(ClassNode type, String alias) {
        this.type = type;
        this.alias = alias;
        this.isStar = false;
        this.isStatic = false;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * Represent an import of an entire package, i.e.&#160;import package.*
     *
     * @param packageName the name of the package
     */
    public ImportNode(String packageName) {
        this.type = null;
        this.alias = null;
        this.isStar = true;
        this.isStatic = false;
        this.packageName = packageName;
        this.fieldName = null;
    }

    /**
     * Represent a static import of a Class, i.e.&#160;import static package.Classname.*
     *
     * @param type the referenced class
     */
    public ImportNode(ClassNode type) {
        this.type = type;
        this.alias = null;
        this.isStar = true;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = null;
    }

    /**
     * Represent a static import of a field or method, i.e.&#160;import static package.Classname.name
     *
     * @param type      the referenced class
     * @param fieldName the field name
     * @param alias     optional alias
     */
    public ImportNode(ClassNode type, String fieldName, String alias) {
        this.type = type;
        this.alias = alias;
        this.isStar = false;
        this.isStatic = true;
        this.packageName = null;
        this.fieldName = fieldName;
    }

    /**
     * @return the text display of this import
     */
    public String getText() {
        String typeName = getClassName();
        if (isStar && !isStatic) {
            return "import " + packageName + "*";
        }
        if (isStar) {
            return "import static " + typeName + ".*";
        }
        if (isStatic) {
            if (alias != null && alias.length() != 0 && !alias.equals(fieldName)) {
                return "import static " + typeName + "." + fieldName + " as " + alias;
            }
            return "import static " + typeName + "." + fieldName;
        }
        if (alias == null || alias.length() == 0) {
            return "import " + typeName;
        }
        return "import " + typeName + " as " + alias;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isStar() {
        return isStar;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getAlias() {
        return alias;
    }

    public ClassNode getType() {
        return type;
    }

    public String getClassName() {
        return type == null ? null : type.getName();
    }

    public void visit(GroovyCodeVisitor visitor) {
    }

}
