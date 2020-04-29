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
package groovy.cli.internal

import groovy.cli.TypedOption
import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.runtime.StringGroovyMethods
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.ParseResult

class OptionAccessor {
    ParseResult parseResult
    Map<String, TypedOption> savedTypeOptions

    OptionAccessor(ParseResult parseResult) {
        this.parseResult = parseResult
    }

    boolean hasOption(TypedOption typedOption) {
        parseResult.hasMatchedOption(typedOption.longOpt ?: typedOption.opt as String)
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
        if (parseResult.hasMatchedOption(optionName)) {
            return parseResult.matchedOptionValue(optionName, defaultValue)
        } else {
            OptionSpec option = parseResult.commandSpec().findOption(optionName)
            return option ? option.value : defaultValue
        }
    }

    public <T> T getAt(TypedOption<T> typedOption) {
        getAt(typedOption, null)
    }

    public <T> T getAt(TypedOption<T> typedOption, T defaultValue) {
        getOptionValue(typedOption, defaultValue)
    }

    private <T> T getTypedValue(Class<T> type, String optionName, String optionValue) {
        if (savedTypeOptions[optionName]?.cliOption?.arity?.min == 0) { // TODO is this not a bug?
            return (T) parseResult.hasMatchedOption(optionName) // TODO should defaultValue not simply convert the type regardless of the matched value?
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
        if (type == Boolean || type == Boolean.TYPE) {
            return type.cast(Boolean.parseBoolean(optionValue))
        }
        StringGroovyMethods.asType(optionValue, (Class<T>) type)
    }

    Properties getOptionProperties(String name) {
        if (!parseResult.hasMatchedOption(name)) {
            return null
        }
        List<String> keyValues = parseResult.matchedOption(name).stringValues()
        Properties result = new Properties()
        keyValues.toSpreadMap().each { k, v -> result.setProperty(k, v) }
        result
    }

    def invokeMethod(String name, Object args) {
        // TODO we could just declare normal methods to map commons-cli CommandLine methods to picocli ParseResult methods
        if (name == 'hasOption')      { name = 'hasMatchedOption';   args = [args[0]      ].toArray() }
        if (name == 'getOptionValue') { name = 'matchedOptionValue'; args = [args[0], null].toArray() }
        return InvokerHelper.getMetaClass(parseResult).invokeMethod(parseResult, name, args)
    }

    def getProperty(String name) {
        if (name == 'parseResult') { return parseResult }
        if (parseResult.hasMatchedOption(name)) {
            def result = parseResult.matchedOptionValue(name, null)

            // if user specified an array type, return the full array (regardless of 's' suffix on name)
            Class userSpecifiedType = savedTypeOptions[name]?.type
            if (userSpecifiedType?.isArray()) { return result }

            // otherwise, if the result is multi-value, return the first value
            Class derivedType = parseResult.matchedOption(name).type()
            if (derivedType.isArray()) {
                return result ? result[0] : null
            } else if (Collection.class.isAssignableFrom(derivedType)) {
                return (result as Collection)?.first()
            }
            if (!userSpecifiedType && result == '' && parseResult.matchedOption(name).arity().min == 0) {
                return true
            }
            return parseResult.matchedOption(name).typedValues().get(0)
        }
        if (parseResult.commandSpec().findOption(name)) { // requested option was not matched: return its default
            def option = parseResult.commandSpec().findOption(name)
            def result = option.value

            // GROOVY-9519: zero default for non-Boolean type options should not be converted to false
            def longOpt = option.longestName()
            longOpt = longOpt?.startsWith("--") ? longOpt.substring(2) : longOpt
            Class userSpecifiedType = savedTypeOptions[longOpt]?.type
            if (userSpecifiedType && Boolean != userSpecifiedType) { return result }

            return result ? result : false
        }
        if (name.size() > 1 && name.endsWith('s')) { // user wants multi-value result
            def singularName = name[0..-2]
            if (parseResult.hasMatchedOption(singularName)) {
                // if picocli has a strongly typed multi-value result, return it
                Class type = parseResult.matchedOption(singularName).type()
                if (type.isArray() || Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                    return parseResult.matchedOptionValue(singularName, null)
                }
                // otherwise, return the raw string values as a list
                return parseResult.matchedOption(singularName).stringValues()
            }
        }
        false
    }

    List<String> arguments() {
        parseResult.hasMatchedPositional(0) ? parseResult.matchedPositional(0).stringValues() : []
    }
}
