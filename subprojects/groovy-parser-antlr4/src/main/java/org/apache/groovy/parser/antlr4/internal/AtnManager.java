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
package org.apache.groovy.parser.antlr4.internal;

import org.antlr.v4.runtime.atn.ATN;
import org.apache.groovy.parser.antlr4.GroovyLangLexer;
import org.apache.groovy.parser.antlr4.GroovyLangParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Manage ATN for lexer and parser to avoid memory leak
 *
 * @author <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on 2016/08/14
 */
public class AtnManager {
    public static final ReentrantReadWriteLock RRWL = new ReentrantReadWriteLock(true);
    private static final String CACHE_THRESHOLD_NAME = "groovy.antlr4.cache.threshold";
    private static final int CACHE_THRESHOLD;
    private final Class ownerClass;
    private final ATN atn;
    private static final Map<Class, AtnWrapper> ATN_MAP =
            Collections.unmodifiableMap(new HashMap<Class, AtnWrapper>() {
                {
                    put(GroovyLangLexer.class, new AtnWrapper(GroovyLangLexer._ATN));
                    put(GroovyLangParser.class, new AtnWrapper(GroovyLangParser._ATN));
                }
            });

    static {
        int t = 50;

        try {
            t = Integer.parseInt(System.getProperty(CACHE_THRESHOLD_NAME));

            // cache threshold should be at least 50 for better performance
            t = t < 50 ? 50 : t;
        } catch (Exception e) {
            // ignored
        }

        CACHE_THRESHOLD = t;
    }

    public AtnManager(GroovyLangLexer lexer) {
        this.ownerClass = lexer.getClass();
        this.atn = GroovyLangLexer._ATN; //getAtnWrapper(this.ownerClass).checkAndClear();
    }

    public AtnManager(GroovyLangParser parser) {
        this.ownerClass = parser.getClass();
        this.atn = getAtnWrapper(this.ownerClass).checkAndClear();
    }

    public ATN getATN() {
        return this.atn;
    }

    private AtnWrapper getAtnWrapper(Class ownerClass) {
        return ATN_MAP.get(ownerClass);
    }

    private static class AtnWrapper {
        private final ATN atn;
        private final AtomicLong counter = new AtomicLong(0);

        public AtnWrapper(ATN atn) {
            this.atn = atn;
        }

        public ATN checkAndClear() {
            if (0 != counter.incrementAndGet() % CACHE_THRESHOLD) {
                return atn;
            }

            RRWL.writeLock().lock();
            try {
                atn.clearDFA();
            } finally {
                RRWL.writeLock().unlock();
            }

            return atn;
        }
    }
}
