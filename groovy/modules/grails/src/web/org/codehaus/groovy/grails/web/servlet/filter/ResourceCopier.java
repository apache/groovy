/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.servlet.filter;

/**
 * @author Graeme Rocher
 * @since 17-Feb-2006
 */
public interface ResourceCopier {

    /**
     * Copies the whole grails-app replacing any changes
     */
    void copyGrailsApp();

    /**
     * Copies only the views
     *
     * @param shouldOverwrite Whether the views should be overwritten
     */
    void copyViews(boolean shouldOverwrite);

    /**
     * The basedir to copy from (defaults to ".")
     * @param basedir
     */
    void setBasedir(String basedir);

    /**
     * * The destdir to copy destdir (defaults to "./tmp/war")
     * @param destdir
     */
    void setDestdir(String destdir);
}
