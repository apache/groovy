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
package groovy.lang;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MethodRankHelper;

/**
 * An exception occurred if a dynamic property dispatch fails with an unknown property.
 * <p>
 * Note that the Missing*Exception classes were named for consistency and
 * to avoid conflicts with JDK exceptions of the same name.
 */
public class MissingPropertyException extends GroovyRuntimeException {

    public  static final Object MPE = new Object(); // synchronization?

    private static final long serialVersionUID = -1780027060966200019L;

    private final String property;
    private final Class  type;

    public MissingPropertyException(final String property, final Class type) {
        this.property = property;
        this.type = type;
    }

    public MissingPropertyException(final String property, final Class type, final Throwable cause) {
        super(cause);
        this.property = property;
        this.type = type;
    }

    public MissingPropertyException(final String message) {
        super(message);
        this.property = null;
        this.type = null;
    }

    public MissingPropertyException(final String message, final String property, final Class type) {
        super(message);
        this.property = property;
        this.type = type;
    }

    @Override
    public String getMessageWithoutLocationText() {
        String message = super.getMessageWithoutLocationText();
        String name = getProperty();
        Class  type = getType();

        if (name != null && type != null) {
            if (getCause() != null) {
                message = "No such property: " + name + " for class: " + type.getName() + ". Reason: " + getCause();
            } else if (message == null) {
                var mp = InvokerHelper.getMetaClass(type).getMetaProperty(name); // GROOVY-6260, GROOVY-8064, et al.
                if (mp instanceof MetaBeanProperty
                        && ((MetaBeanProperty) mp).getField() == null
                        && ((MetaBeanProperty) mp).getGetter() == null) {
                    message = "Cannot get write-only property: " + name + " for class: " + type.getName();
                } else {
                    message = "No such property: " + name + " for class: " + type.getName() + MethodRankHelper.getPropertySuggestionString(name, type);
                }
            }
        }

        return message;
    }

    /**
     * @return the name of the property that could not be found
     */
    public String getProperty() {
        return property;
    }

    /**
     * @return the class which cannot answer the property
     */
    public Class getType() {
        return type;
    }
}
