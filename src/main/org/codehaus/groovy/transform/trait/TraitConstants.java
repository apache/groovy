/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.trait;

import groovy.transform.Trait;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;

public abstract class TraitConstants {
    static final Class TRAIT_CLASS = Trait.class;
    static final ClassNode TRAIT_CLASSNODE = ClassHelper.make(TRAIT_CLASS);
    static final String TRAIT_TYPE_NAME = "@" + TRAIT_CLASSNODE.getNameWithoutPackage();
    static final String TRAIT_HELPER = "$Trait$Helper";
    static final String FIELD_HELPER = "$Trait$FieldHelper";
    static final String DIRECT_SETTER_SUFFIX = "$set";
    static final String DIRECT_GETTER_SUFFIX = "$get";
    static final String STATIC_INIT_METHOD = "$init$";
    static final String THIS_OBJECT = "$self";

    static String fieldHelperClassName(final ClassNode traitNode) {
        return traitNode.getName() + FIELD_HELPER;
    }

    static String helperGetterName(final FieldNode field) {
        return field.getName() + DIRECT_GETTER_SUFFIX;
    }

    static String helperSetterName(final FieldNode field) {
        return field.getName() + DIRECT_SETTER_SUFFIX;
    }

    static String helperClassName(final ClassNode traitNode) {
        return traitNode.getName() + TRAIT_HELPER;
    }

    static String remappedFieldName(final ClassNode traitNode, final String name) {
        return traitNode.getName().replace('.','_')+"__"+name;
    }
}
