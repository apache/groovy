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
package groovy.transform.construction;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.transform.AbstractASTTransformation;

import java.util.List;

public abstract class PropertyHandler {
    public abstract boolean validateAttributes(AbstractASTTransformation xform, AnnotationNode anno);

    public boolean validateProperties(AbstractASTTransformation xform, BlockStatement body, ClassNode cNode, List<PropertyNode> props) {
        return true;
    }

    public abstract void createStatement(AbstractASTTransformation xform, AnnotationNode anno, BlockStatement body, ClassNode cNode, PropertyNode pNode, Parameter namedArgMap);

    protected boolean isValidAttribute(AbstractASTTransformation xform, AnnotationNode anno, String memberName) {
        if (xform.getMemberValue(anno, memberName) != null) {
            xform.addError("Error during " + xform.getAnnotationName() + " processing: Annotation attribute '" + memberName +
                    "' not supported for property handler " + getClass().getSimpleName(), anno);
            return false;
        }
        return true;
    }

    public static PropertyHandler createPropertyHandler(AbstractASTTransformation xform, AnnotationNode anno, GroovyClassLoader loader) {
        ClassNode handlerClass = xform.getMemberClassValue(anno, "propertyHandler", ClassHelper.make(DefaultPropertyHandler.class));

        if (handlerClass == null) {
            xform.addError("Couldn't determine propertyHandler class", anno);
            return null;
        }

        String className = handlerClass.getName();
        try {
            Object instance = loader.loadClass(className).newInstance();
            if (instance == null) {
                xform.addError("Can't load propertyHandler '" + className + "'", anno);
                return null;
            }
            if (!PropertyHandler.class.isAssignableFrom(instance.getClass())) {
                xform.addError("The propertyHandler class '" + handlerClass.getName() + "' on " + xform.getAnnotationName() + " is not a propertyHandler", anno);
                return null;
            }

            return (PropertyHandler) instance;
        } catch (Exception e) {
            xform.addError("Can't load propertyHandler '" + className + "' " + e, anno);
            return null;
        }
    }
}
