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
package org.codehaus.groovy.tools.shell.util

import jline.Terminal
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options

//
// NOTE: Some code duplicated and augmented from commons-cli (1.0) sources to
//       properly render options w/arguments.
//


/**
 * Custom CLI help formatter to render things correctly.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
class HelpFormatter
    extends org.apache.commons.cli.HelpFormatter
{
    HelpFormatter() {
        leftPadding = 2
        descPadding = 4
    }

    // Detect the terminal width late
    int getDefaultWidth() {
        return Terminal.terminal.terminalWidth - 1
    }

    @Override
    protected StringBuffer renderOptions(final StringBuffer sb, final int width, final Options options, final int leftPad, final int descPad) {
        assert sb != null
        assert options

        List<StringBuffer> prefixes = []
        String lpad = ' ' * leftPad

        List<Option> opts = options.shortOpts.values().sort {Option a, Option b ->
            return (a.opt == ' ' ? a.longOpt : a.opt) <=> (b.opt == ' ' ? b.longOpt : b.opt)
        }

        // Render the prefixes (-X,--xxxx muck)
        opts.each {Option option ->
            StringBuffer buff = new StringBuffer(8)

            if (option.opt == ' ') {
                buff << "${lpad}    ${longOptPrefix}${option.longOpt}"
            }
            else {
                buff << "${lpad}${optPrefix}${option.opt}"

                if (option.hasLongOpt()) {
                    buff << ", ${longOptPrefix}${option.longOpt}"
                }
            }

            if (option.hasArg()) {
                if (option.hasArgName()) {
                    if (option.hasOptionalArg()) {
                        buff << "[=${option.argName}]"
                    }
                    else {
                        buff << "=${option.argName}"
                    }
                }
                else {
                    buff << ' '
                }
            }

            prefixes << buff
        }

        // Figure out how long the biggest prefix is
        int maxPrefix = prefixes.max {StringBuffer a, StringBuffer b -> a.size() <=> b.size() }.size()

        String dpad = ' ' * descPad

        // And then render each option's prefix with its description
        opts.eachWithIndex {Option option, int i ->
            def buff = new StringBuffer(prefixes[i].toString())

            if (buff.size() < maxPrefix) {
                buff << ' ' * (maxPrefix - buff.size())
            }
            buff << dpad

            int nextLineTabStop = maxPrefix + descPad
            String text = buff << option.description

            renderWrappedText(sb, width, nextLineTabStop, text)

            if (i < opts.size() - 1) {
                sb << newLine
            }
        }

        return sb
    }
}
