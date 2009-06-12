/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.control;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Jochen Theodorou
 */
public class MultipleCompilationErrorsException extends
        CompilationFailedException {
    
    protected ErrorCollector collector;
    
    public MultipleCompilationErrorsException(ErrorCollector ec) {
        super(0, null);
        if (ec == null) {
            CompilerConfiguration config = super.getUnit() != null ?
                super.getUnit().getConfiguration() :
                new CompilerConfiguration();
            collector = new ErrorCollector(config);
        } else {
            collector = ec;
        }
    }

    public ErrorCollector getErrorCollector() {
        return collector;
    }
    
    public String getMessage() {
        StringWriter data = new StringWriter();
        PrintWriter writer = new PrintWriter(data);
        Janitor janitor = new Janitor();

        writer.write(super.getMessage());
        writer.println(":");
        try {
            collector.write(writer, janitor);
        }
        finally {
            janitor.cleanup();
        }

        return data.toString();
    }
}
