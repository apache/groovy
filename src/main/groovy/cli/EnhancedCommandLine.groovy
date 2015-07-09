package groovy.cli

import org.apache.commons.cli.CommandLine
import org.codehaus.groovy.runtime.StringGroovyMethods
import org.apache.commons.cli.Option as CliOption
import java.lang.reflect.Array

class EnhancedCommandLine {
    @Delegate CommandLine delegate

    public <T> T getOptionValue(TypedOption<T> typedOption) {
        getOptionValue(typedOption, null)
    }

    public <T> T getOptionValue(TypedOption<T> typedOption, T defaultValue) {
        String optionName = (String) typedOption.get("longOpt")
        if (delegate.hasOption(optionName)) {
            return getTypedValueFromName(optionName)
        }
        return defaultValue
    }

    public <T> T[] getOptionValues(TypedOption<T> typedOption) {
        String optionName = (String) typedOption.get("longOpt")
        CliOption option = delegate.options.find{ it.longOpt == optionName }
        T[] result = null
        if (option) {
            Object type = option.getType()
            int count = 0
            def optionValues = delegate.getOptionValues(optionName)
            for (String optionValue : optionValues) {
                if (result == null) {
                    result = (T[]) Array.newInstance((Class<?>) type, optionValues.length)
                }
                result[count++] = (T) getTypedValue(type, optionValue)
            }
        }
        return result
    }

    private <T> T getTypedValueFromName(String optionName) {
        CliOption option = delegate.options.find{ it.longOpt == optionName }
        if (!option) return null
        Object type = option.getType()
        String optionValue = delegate.getOptionValue(optionName)
        return (T) getTypedValue(type, optionValue)
    }

    private static <T> T getTypedValue(Object type, String optionValue) {
        return StringGroovyMethods.asType(optionValue, (Class<T>) type)
    }
}