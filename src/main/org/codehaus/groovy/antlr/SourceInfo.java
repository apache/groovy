/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.antlr;

public interface SourceInfo {
    /**
     * get start line
     *
     * @return the starting line
     */
    int getLine();

    /**
     * set start line
     *
     * @param l the line
     */
    void setLine(int l);

    /**
     * get starting column
     *
     * @return the starting column
     */
    int getColumn();

    /**
     * set start column
     *
     * @param c the column
     */
    void setColumn(int c);

    /**
     * get ending line
     *
     * @return the ending line
     */
    int getLineLast();

    /**
     * set ending line
     *
     * @param lineLast the line
     */
    void setLineLast(int lineLast);

    /**
     * get ending column
     *
     * @return the ending column
     */
    int getColumnLast();

    /**
     * set ending column
     *
     * @param colLast the column
     */
    void setColumnLast(int colLast);
}
