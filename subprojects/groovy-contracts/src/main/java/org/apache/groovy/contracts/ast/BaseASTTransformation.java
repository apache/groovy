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
package org.apache.groovy.contracts.ast;

import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.ReaderSource;
import org.codehaus.groovy.transform.ASTTransformation;

import java.lang.reflect.Field;

/**
 * Base AST transformation encapsulating all common helper methods and implementing {@link org.codehaus.groovy.transform.ASTTransformation}.
 *
 * @see org.codehaus.groovy.transform.ASTTransformation
 */
public abstract class BaseASTTransformation implements ASTTransformation {

    /**
     * Reads the protected <tt>source1</tt> instance variable of {@link org.codehaus.groovy.control.SourceUnit}.
     *
     * @param unit the {@link org.codehaus.groovy.control.SourceUnit} to retrieve the {@link org.codehaus.groovy.control.io.ReaderSource} from
     * @return the {@link org.codehaus.groovy.control.io.ReaderSource} of the given <tt>unit</tt>.
     */
    protected ReaderSource getReaderSource(SourceUnit unit) {

        try {
            Class sourceUnitClass = unit.getClass();

            while (sourceUnitClass != SourceUnit.class) {
                sourceUnitClass = sourceUnitClass.getSuperclass();
            }

            Field field = sourceUnitClass.getDeclaredField("source1");
            field.setAccessible(true);

            return (ReaderSource) field.get(unit);
        } catch (Exception e) {
            return null;
        }
    }

}
