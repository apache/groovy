/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.transform;

/**
 * Intended target when {@code @}PackageScope is placed at the class level.
 *
 * @author Paul King
 * @since 1.8.0
 */
public enum PackageScopeTarget {
    /**
     * Make the Class have package protected visibility.
     */
    CLASS,

    /**
     * Make the Class methods have package protected visibility.
     */
    METHODS,

    /**
     * Make the Class fields have package protected visibility.
     */
    FIELDS
}
