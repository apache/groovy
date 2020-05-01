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
package org.codehaus.groovy.vmplugin.v9;

import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;

/**
 * Defines new Groovy methods which appear on normal JDK 9
 * classes inside the Groovy environment.
 *
 * @since 3.0.4
 */
public class PluginDefaultGroovyMethods extends DefaultGroovyMethodsSupport {

    // No instances, static methods only
    private PluginDefaultGroovyMethods() {
    }

    /**
     * Convenience method for logging info level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void info(final System.Logger logger, final String msg) {
        logger.log(System.Logger.Level.INFO, msg);
    }

    /**
     * Convenience method for logging info level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void info(final System.Logger logger, final String msg, final Throwable thrown) {
        logger.log(System.Logger.Level.INFO, msg, thrown);
    }

    /**
     * Convenience method for logging info level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void info(final System.Logger logger, final String format, final Object... params) {
        logger.log(System.Logger.Level.INFO, format, params);
    }

    /**
     * Convenience method for logging trace level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void trace(final System.Logger logger, final String msg) {
        logger.log(System.Logger.Level.TRACE, msg);
    }

    /**
     * Convenience method for logging trace level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void trace(final System.Logger logger, final String msg, final Throwable thrown) {
        logger.log(System.Logger.Level.TRACE, msg, thrown);
    }

    /**
     * Convenience method for logging trace level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void trace(final System.Logger logger, final String format, final Object... params) {
        logger.log(System.Logger.Level.TRACE, format, params);
    }

    /**
     * Convenience method for logging warning level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void warn(final System.Logger logger, final String msg) {
        logger.log(System.Logger.Level.WARNING, msg);
    }

    /**
     * Convenience method for logging warning level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void warn(final System.Logger logger, final String msg, final Throwable thrown) {
        logger.log(System.Logger.Level.WARNING, msg, thrown);
    }

    /**
     * Convenience method for logging warning level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void warn(final System.Logger logger, final String format, final Object... params) {
        logger.log(System.Logger.Level.WARNING, format, params);
    }

    /**
     * Convenience method for logging error level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void error(final System.Logger logger, final String msg) {
        logger.log(System.Logger.Level.ERROR, msg);
    }

    /**
     * Convenience method for logging error level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void error(final System.Logger logger, final String msg, final Throwable thrown) {
        logger.log(System.Logger.Level.ERROR, msg, thrown);
    }

    /**
     * Convenience method for logging error level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void error(final System.Logger logger, final String format, final Object... params) {
        logger.log(System.Logger.Level.ERROR, format, params);
    }

    /**
     * Convenience method for logging debug level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void debug(final System.Logger logger, final String msg) {
        logger.log(System.Logger.Level.DEBUG, msg);
    }

    /**
     * Convenience method for logging debug level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void debug(final System.Logger logger, final String msg, final Throwable thrown) {
        logger.log(System.Logger.Level.DEBUG, msg, thrown);
    }

    /**
     * Convenience method for logging debug level messages with the platform logger.
     *
     * @param logger the Optional
     */
    public static void debug(final System.Logger logger, final String format, final Object... params) {
        logger.log(System.Logger.Level.DEBUG, format, params);
    }

}
