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
package groovy.text.markup;

/**
 * An enumeration used internally to map include types to {@link groovy.text.markup.BaseTemplate} method
 * calls.
 *
 * @author Cedric Champeau
 */
enum IncludeType {
    template("includeGroovy"), // includes another Groovy template
    escaped("includeEscaped"), // includes raw content, escaped before rendering
    unescaped("includeUnescaped");

    private final String methodName;

    IncludeType(final String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
