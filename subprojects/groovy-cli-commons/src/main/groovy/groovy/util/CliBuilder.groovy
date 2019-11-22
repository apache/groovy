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
package groovy.util

import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.HelpFormatter

/**
 * @deprecated use {@code groovy.cli.picocli.CliBuilder} or {@link groovy.cli.commons.CliBuilder} instead.
 */
@Deprecated
class CliBuilder {
    private @Delegate
    groovy.cli.commons.CliBuilder delegate = new groovy.cli.commons.CliBuilder()

    // explicit delegate to convert return type to expected legacy package
    OptionAccessor parse(args) {
        def result = delegate.parse(args)
        return result == null ? null : new OptionAccessor(delegate: result)
    }

    // explicit delegate since groovyObject methods ignored by @Delegate
    def invokeMethod(String name, Object args) {
        delegate.invokeMethod(name, args)
    }

    // delegated versions of the methods below are available but we want
    // IDE warnings to encourage people not to use these methods in particular
    // over and above the warning they should have at the class level

    /**
     * @deprecated This may not be available in future groovy.util.CliBuilder versions.
     * Use groovy.cli.commons.CliBuilder if you need this feature.
     */
    @Deprecated
    void setParser(CommandLineParser parser) {
        delegate.setParser(parser)
    }

    /**
     * @deprecated This may not be available in future groovy.util.CliBuilder versions.
     * Use groovy.cli.commons.CliBuilder if you need this feature.
     */
    @Deprecated
    CommandLineParser getParser() {
        delegate.getParser()
    }

    /**
     * @deprecated This may not be available in future groovy.util.CliBuilder versions.
     * Use groovy.cli.commons.CliBuilder if you need this feature.
     */
    @Deprecated
    void setFormatter(HelpFormatter formatter) {
        delegate.setFormatter(formatter)
    }

    /**
     * @deprecated This may not be available in future groovy.util.CliBuilder versions.
     * Use groovy.cli.commons.CliBuilder if you need this feature.
     */
    @Deprecated
    HelpFormatter getFormatter() {
        delegate.getFormatter()
    }
}
