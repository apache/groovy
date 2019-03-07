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
package groovy.text.markup;

import java.io.IOException;
import java.net.URL;

/**
 * Interface for template resolvers, which, given a template identifier, return an URL where the template
 * can be loaded.
 */
public interface TemplateResolver {
    /**
     * This method is called once the template engine is initialized, providing the resolver with the
     * template engine configuration and its template class loader.
     * @param templateClassLoader the classloader where templates will be searched for
     * @param configuration the configuration of the template engine
     */
    void configure(ClassLoader templateClassLoader, TemplateConfiguration configuration);

    /**
     * Resolvers must implement this method in order to resolve a template, given a template path. They
     * must return a valid URL or an IOException.
     * @param templatePath path to the template
     * @return the template URL, that will be used to load the template
     * @throws IOException
     */
    URL resolveTemplate(String templatePath) throws IOException;
}
