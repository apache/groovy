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
package groovy.sql;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;
import groovy.lang.ReadOnlyPropertyException;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.sql.ResultSetMetaData;

/**
 * This class defines a wrapper for accessing a specific column in <code>ResultSetMetaData</code>.
 * This allows iteration over columns using idiomatic Groovy, e.g.:
 * <pre>
 * meta.each {col {@code ->}
 *   println col.columnName
 * }
 * </pre>
 * All <code>ResultSetMetaData</code> column methods taking a column index
 * are available on a column either as a no-arg getter or via a property.
 * <p>
 * This wrapper is created by an iterator invoked for <code>ResultSetMetaData</code>.
 *
 * @see org.apache.groovy.sql.extensions.SqlExtensions
 */
public class ResultSetMetaDataWrapper extends GroovyObjectSupport {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final ResultSetMetaData target;
    private final int index;

    /**
     * Creates a wrapper for a single metadata column.
     *
     * @param target the metadata instance to wrap
     * @param index the 1-based column index represented by this wrapper
     */
    public ResultSetMetaDataWrapper(ResultSetMetaData target, int index) {
        this.target = target;
        this.index = index;
    }

    private Object[] getIndexedArgs(Object[] originalArgs) {
        Object[] result = new Object[originalArgs.length + 1];
        result[0] = index;
        for (int i = 0, originalArgsLength = originalArgs.length; i < originalArgsLength; i++) {
            result[i + 1] = originalArgs[i];
        }
        return result;
    }

    /**
     * Invokes a metadata method after prepending this wrapper's column index.
     *
     * @param name the metadata method name
     * @param args the original method arguments
     * @return the delegated result
     */
    @Override
    public Object invokeMethod(String name, Object args) {
        Object[] indexedArgs = getIndexedArgs((Object[]) args);
        return InvokerHelper.invokeMethod(target, name, indexedArgs);
    }

    private String getPropertyGetterName(String prop) {
        if (prop == null || prop.isEmpty()) {
            throw new MissingPropertyException(prop, target.getClass());
        }
        return "get" + prop.substring(0, 1).toUpperCase() + prop.substring(1);
    }

    /**
     * Resolves a metadata property by invoking the corresponding getter for this wrapper's column.
     *
     * @param property the property name
     * @return the resolved property value
     */
    @Override
    public Object getProperty(String property) {
        return invokeMethod(getPropertyGetterName(property), EMPTY_OBJECT_ARRAY);
    }

    /**
     * Always rejects property writes because result-set metadata is read-only.
     *
     * @param property the property name
     * @param newValue the requested value
     */
    @Override
    public void setProperty(String property, Object newValue) {
        throw new ReadOnlyPropertyException(property, target.getClass());
    }
}
