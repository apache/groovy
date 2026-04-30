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
package org.codehaus.groovy.tools.groovydoc.testfiles;

import java.io.IOException;

class JavaInheritDocRichTagBase {
    /**
     * Java base transform description that should not leak into block tags.
     *
     * @param value java parent parameter description with {@code JAVA_VALUE_TOKEN}
     * @return java parent return description with {@code JAVA_RETURN_TOKEN}
     * @throws IllegalArgumentException java parent illegal-argument description with {@code JAVA_BAD_VALUE_TOKEN}
     * @exception java.io.IOException java parent IO description with {@code JAVA_IO_VALUE_TOKEN}
     */
    public String transform(String value) throws IOException {
        return value.toUpperCase();
    }
}

public class JavaInheritDocRichTagChild extends JavaInheritDocRichTagBase {
    /**
     * Java rich child summary.
     *
     * @param value child prefix {@inheritDoc} child suffix
     * @return leading {@inheritDoc} trailing
     * @throws java.lang.IllegalArgumentException before {@inheritDoc} after
     * @exception IOException wrapped {@inheritDoc} done
     */
    @Override
    public String transform(String value) throws IOException {
        return value.toLowerCase();
    }
}
