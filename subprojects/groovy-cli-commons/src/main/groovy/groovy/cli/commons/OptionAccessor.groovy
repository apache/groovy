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
package groovy.cli.commons

import groovy.cli.TypedOption
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option as CliOption
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StringGroovyMethods

import java.lang.reflect.Array

class OptionAccessor {
    CommandLine commandLine
    Map<String, TypedOption> savedTypeOptions

    OptionAccessor(CommandLine commandLine) {
        this.commandLine = commandLine
    }

    boolean hasOption(TypedOption typedOption) {
        commandLine.hasOption(typedOption.longOpt ?: typedOption.opt)
    }

    public <T> T defaultValue(String name) {
        Class<T> type = savedTypeOptions[name]?.type
        String value = savedTypeOptions[name]?.defaultValue() ? savedTypeOptions[name].defaultValue() : null
        return (T) value ? getTypedValue(type, name, value) : null
    }

    public <T> T getOptionValue(TypedOption<T> typedOption) {
        getOptionValue(typedOption, null)
    }

    public <T> T getOptionValue(TypedOption<T> typedOption, T defaultValue) {
        String optionName = (String) typedOption.longOpt ?: typedOption.opt
        if (commandLine.hasOption(optionName)) {
            if (typedOption.containsKey('type') && typedOption.type.isArray()) {
                def compType = typedOption.type.componentType
                return (T) getTypedValuesFromName(optionName, compType)
            }
            return getTypedValueFromName(optionName)
        }
        return defaultValue
    }

    private <T> T[] getTypedValuesFromName(String optionName, Class<T> compType) {
        CliOption option = commandLine.options.find{ it.longOpt == optionName }
        T[] result = null
        if (option) {
            int count = 0
            def optionValues = commandLine.getOptionValues(optionName)
            for (String optionValue : optionValues) {
                if (result == null) {
                    result = (T[]) Array.newInstance(compType, optionValues.length)
                }
                result[count++] = (T) getTypedValue(compType, optionName, optionValue)
            }
        }
        if (result == null) {
            result = (T[]) Array.newInstance(compType, 0)
        }
        return result
    }

    public <T> T getAt(TypedOption<T> typedOption) {
        getAt(typedOption, null)
    }

    public <T> T getAt(TypedOption<T> typedOption, T defaultValue) {
        String optionName = (String) typedOption.longOpt ?: typedOption.opt
        if (savedTypeOptions.containsKey(optionName)) {
            return getTypedValueFromName(optionName)
        }
        return defaultValue
    }

    private <T> T getTypedValueFromName(String optionName) {
        Class type = savedTypeOptions[optionName].type
        String optionValue = commandLine.getOptionValue(optionName)
        return (T) getTypedValue(type, optionName, optionValue)
    }

    private <T> T getTypedValue(Class<T> type, String optionName, String optionValue) {
        if (savedTypeOptions[optionName]?.cliOption?.numberOfArgs == 0) {
            return (T) commandLine.hasOption(optionName)
        }
        def convert = savedTypeOptions[optionName]?.convert
        return getValue(type, optionValue, convert)
    }

    private <T> T getValue(Class<T> type, String optionValue, Closure convert) {
        if (!type) {
            return (T) optionValue
        }
        if (Closure.isAssignableFrom(type) && convert) {
            return (T) convert(optionValue)
        }
        if (type?.simpleName?.toLowerCase() == 'boolean') {
            return (T) Boolean.parseBoolean(optionValue)
        }
        StringGroovyMethods.asType(optionValue, (Class<T>) type)
    }

    def invokeMethod(String name, Object args) {
        return InvokerHelper.getMetaClass(commandLine).invokeMethod(commandLine, name, args)
    }

    def getProperty(String name) {
        if (!savedTypeOptions.containsKey(name)) {
            def alt = savedTypeOptions.find{ it.value.opt == name }
            if (alt) name = alt.key
        }
        def methodname = 'getOptionValue'
        Class type = savedTypeOptions[name]?.type
        def foundArray = type?.isArray()
        if (name.size() > 1 && name.endsWith('s')) {
            def singularName = name[0..-2]
            if (commandLine.hasOption(singularName) || foundArray) {
                name = singularName
                methodname += 's'
                type = savedTypeOptions[name]?.type
            }
        }
        if (type?.isArray()) {
            methodname = 'getOptionValues'
        }
        if (name.size() == 1) name = name as char
        def result = InvokerHelper.getMetaClass(commandLine).invokeMethod(commandLine, methodname, name)
        if (result != null) {
            if (result instanceof String[]) {
                result = result.collect{ type ? getTypedValue(type.isArray() ? type.componentType : type, name, it) : it }
            } else {
                if (type) result = getTypedValue(type, name, result)
            }
        } else if (type?.simpleName != 'boolean' && savedTypeOptions[name]?.defaultValue) {
            result = getTypedValue(type, name, savedTypeOptions[name].defaultValue)
        } else {
            result = commandLine.hasOption(name)
        }
        return result
    }

    List<String> arguments() {
        commandLine.args.toList()
    }
}
