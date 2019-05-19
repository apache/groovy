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

import java.io.IOException;

/**
 * An IO exception occurred trying to append to a StringWriter which should never happen.
 */
public class StringWriterIOException extends RuntimeException {

    private static final long serialVersionUID = -504499949457372681L;

    public StringWriterIOException(IOException e) {
        super(e);
    }

    public IOException getIOException() {
        return (IOException) getCause();
    }
}
