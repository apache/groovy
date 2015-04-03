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
import groovy.text.markup.BaseTemplate
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration

// tag::basetemplate_class[]
public abstract class MyTemplate extends BaseTemplate {
    private List<Module> modules
    public MyTemplate(
            final MarkupTemplateEngine templateEngine,
            final Map model,
            final Map<String, String> modelTypes,
            final TemplateConfiguration configuration) {
        super(templateEngine, model, modelTypes, configuration)
    }

    List<Module> getModules() {
        return modules
    }

    void setModules(final List<Module> modules) {
        this.modules = modules
    }

    boolean hasModule(String name) {
        modules?.any { it.name == name }
    }
}
// end::basetemplate_class[]

class Module {
    String name
}
