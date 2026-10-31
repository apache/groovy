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
package org.apache.groovy.ginq.provider.sql

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.groovy.ginq.provider.sql.ir.SqlBinary
import org.apache.groovy.ginq.provider.sql.ir.SqlCast
import org.apache.groovy.ginq.provider.sql.ir.SqlCastTypes
import org.apache.groovy.ginq.provider.sql.ir.SqlExpr
import org.apache.groovy.ginq.provider.sql.ir.SqlFunction
import org.apache.groovy.ginq.provider.sql.ir.SqlLiteral
import org.apache.groovy.lang.annotation.Incubating
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression

/**
 * One Groovy method or function and the SQL it becomes.
 * <p>
 * A function whose SQL is {@code NAME(receiver, args...)} or
 * {@code NAME(args...)} is a {@link #receiver}, {@link #freeFunction} or
 * {@link #math} entry. Adding one does not require a change to the
 * translator. Shapes that are not a plain call ({@code cast},
 * {@code indexOf}, {@code concat}, {@code isEmpty}, {@code substring},
 * {@code contains}) are still registry entries; a further shape is a new
 * shape constant and a branch in {@link #translate}.
 *
 * @since 6.0.0
 */
@Incubating
@CompileStatic
final class SqlFunctionMapping {
    /**
     * Where the Groovy call sits.
     */
    enum Kind {
        /** A method on a value, e.g. {@code e.name.toUpperCase()}. */
        RECEIVER,
        /** An implicit-this call, e.g. {@code coalesce(a, b)}. */
        FREE,
        /** A static call on {@code java.lang.Math}, e.g. {@code Math.max(a, b)}. */
        MATH
    }

    private enum Shape {
        SQL_FUNCTION, CONCAT, IS_EMPTY, SUBSTRING, LIKE, INDEX_OF, CAST
    }

    final String groovyName
    final Kind kind
    /** ANSI function name for {@link Shape#SQL_FUNCTION}, otherwise {@code null}. */
    final String sqlName

    private final Shape shape
    private final int minArgs
    private final int maxArgs
    private final String arityMessage
    private final boolean matchBefore
    private final boolean matchAfter

    private SqlFunctionMapping(String groovyName, Kind kind, Shape shape, String sqlName,
                               int minArgs, int maxArgs, String arityMessage,
                               boolean matchBefore, boolean matchAfter) {
        this.groovyName = groovyName
        this.kind = kind
        this.shape = shape
        this.sqlName = sqlName
        this.minArgs = minArgs
        this.maxArgs = maxArgs
        this.arityMessage = arityMessage
        this.matchBefore = matchBefore
        this.matchAfter = matchAfter
    }

    /**
     * A receiver method with no arguments, e.g. {@code toUpperCase} to {@code UPPER}.
     *
     * @param groovyName the Groovy method name
     * @param sqlName the ANSI function name
     * @return the mapping
     */
    static SqlFunctionMapping receiver(String groovyName, String sqlName) {
        return receiver(groovyName, sqlName, 0, 0, null)
    }

    /**
     * A receiver method with a fixed number of arguments, e.g. {@code replace}.
     *
     * @param groovyName the Groovy method name
     * @param sqlName the ANSI function name
     * @param arguments how many arguments the method takes
     * @return the mapping
     */
    static SqlFunctionMapping receiver(String groovyName, String sqlName, int arguments) {
        return receiver(groovyName, sqlName, arguments, arguments, null)
    }

    /**
     * A receiver method with an argument range, e.g. {@code round} with 0 or 1.
     *
     * @param groovyName the Groovy method name
     * @param sqlName the ANSI function name
     * @param minArgs minimum argument count
     * @param maxArgs maximum argument count
     * @return the mapping
     */
    static SqlFunctionMapping receiver(String groovyName, String sqlName, int minArgs, int maxArgs) {
        return receiver(groovyName, sqlName, minArgs, maxArgs, null)
    }

    /**
     * An implicit-this function, e.g. {@code coalesce}.
     *
     * @param groovyName the Groovy function name
     * @param sqlName the ANSI function name
     * @param minArgs minimum argument count
     * @param maxArgs maximum argument count
     * @return the mapping
     */
    static SqlFunctionMapping freeFunction(String groovyName, String sqlName, int minArgs, int maxArgs) {
        return freeFunction(groovyName, sqlName, minArgs, maxArgs, null)
    }

    /**
     * A two-argument {@code java.lang.Math} method, e.g. {@code max} to {@code GREATEST}.
     *
     * @param groovyName the method name without the {@code Math.} receiver
     * @param sqlName the ANSI function name
     * @return the mapping
     */
    static SqlFunctionMapping math(String groovyName, String sqlName) {
        return math(groovyName, sqlName, null)
    }

    @PackageScope
    static SqlFunctionMapping receiver(String groovyName, String sqlName, int minArgs, int maxArgs, String arityMessage) {
        checkMethod(groovyName)
        checkSql(sqlName)
        checkRange(minArgs, maxArgs)
        return new SqlFunctionMapping(groovyName, Kind.RECEIVER, Shape.SQL_FUNCTION, sqlName,
                minArgs, maxArgs, arityMessage, false, false)
    }

    @PackageScope
    static SqlFunctionMapping freeFunction(String groovyName, String sqlName, int minArgs, int maxArgs, String arityMessage) {
        checkMethod(groovyName)
        checkSql(sqlName)
        checkRange(minArgs, maxArgs)
        return new SqlFunctionMapping(groovyName, Kind.FREE, Shape.SQL_FUNCTION, sqlName,
                minArgs, maxArgs, arityMessage, false, false)
    }

    @PackageScope
    static SqlFunctionMapping math(String groovyName, String sqlName, String arityMessage) {
        checkMethod(groovyName)
        checkSql(sqlName)
        return new SqlFunctionMapping(groovyName, Kind.MATH, Shape.SQL_FUNCTION, sqlName,
                2, 2, arityMessage, false, false)
    }

    @PackageScope
    static SqlFunctionMapping concat() {
        return new SqlFunctionMapping('concat', Kind.RECEIVER, Shape.CONCAT, null, 1, 1, null, false, false)
    }

    @PackageScope
    static SqlFunctionMapping isEmpty() {
        return new SqlFunctionMapping('isEmpty', Kind.RECEIVER, Shape.IS_EMPTY, null, 0, 0, null, false, false)
    }

    @PackageScope
    static SqlFunctionMapping substring() {
        return new SqlFunctionMapping('substring', Kind.RECEIVER, Shape.SUBSTRING, null, 1, 2, null, false, false)
    }

    @PackageScope
    static SqlFunctionMapping like(String groovyName, boolean matchBefore, boolean matchAfter) {
        checkMethod(groovyName)
        return new SqlFunctionMapping(groovyName, Kind.RECEIVER, Shape.LIKE, null, 1, 1, null, matchBefore, matchAfter)
    }

    @PackageScope
    static SqlFunctionMapping indexOf() {
        return new SqlFunctionMapping('indexOf', Kind.RECEIVER, Shape.INDEX_OF, null, 1, 1, null, false, false)
    }

    @PackageScope
    static SqlFunctionMapping cast() {
        return new SqlFunctionMapping('cast', Kind.FREE, Shape.CAST, null, 2, 2, null, false, false)
    }

    /**
     * Translates the call. A wrong arity or an illegal {@code cast} type
     * is reported through {@code site} and yields {@code null}.
     *
     * @param call the Groovy call
     * @param args its arguments
     * @param site translator operations
     * @return the SQL IR, or {@code null} when the call was rejected
     */
    SqlExpr translate(MethodCallExpression call, List<Expression> args, SqlFunctionSite site) {
        switch (shape) {
            case Shape.LIKE:
                return site.like(call, args, matchBefore, matchAfter)
            case Shape.SUBSTRING:
                return site.substring(call, args)
            case Shape.CONCAT:
                return concat(call, args, site)
            case Shape.IS_EMPTY:
                return isEmpty(call, args, site)
            case Shape.INDEX_OF:
                return indexOf(call, args, site)
            case Shape.CAST:
                return cast(call, args, site)
            default:
                return sqlFunction(call, args, site)
        }
    }

    private SqlExpr sqlFunction(MethodCallExpression call, List<Expression> args, SqlFunctionSite site) {
        if (args.size() < minArgs || args.size() > maxArgs) {
            site.fail(wrongArity(), call)
            return null
        }
        List<SqlExpr> sqlArgs = new ArrayList<SqlExpr>()
        if (kind == Kind.RECEIVER) {
            sqlArgs.add(site.expr(call.objectExpression))
        }
        for (Expression arg : args) {
            sqlArgs.add(site.expr(arg))
        }
        return new SqlFunction(sqlName, sqlArgs)
    }

    private SqlExpr concat(MethodCallExpression call, List<Expression> args, SqlFunctionSite site) {
        if (args.size() != 1) {
            site.fail('`concat` expects one argument', call)
            return null
        }
        return new SqlBinary('||', site.expr(call.objectExpression), site.expr(args.get(0)))
    }

    private SqlExpr isEmpty(MethodCallExpression call, List<Expression> args, SqlFunctionSite site) {
        if (args) {
            site.fail('`isEmpty` takes no arguments', call)
            return null
        }
        return new SqlBinary('=', new SqlFunction('CHAR_LENGTH', [site.expr(call.objectExpression)]), new SqlLiteral(0))
    }

    // POSITION/LOCATE/INSTR are 1-based and return 0 when absent. Subtracting 1
    // matches Groovy indexOf: the first character is 0, and absence is -1.
    private SqlExpr indexOf(MethodCallExpression call, List<Expression> args, SqlFunctionSite site) {
        if (args.size() != 1) {
            site.fail('`indexOf` expects one argument, the text to search for. A from-index has no portable SQL form', call)
            return null
        }
        SqlExpr needle = site.expr(args.get(0))
        SqlExpr haystack = site.expr(call.objectExpression)
        return new SqlBinary('-', new SqlFunction('POSITION', [needle, haystack]), new SqlLiteral(1))
    }

    private SqlExpr cast(MethodCallExpression call, List<Expression> args, SqlFunctionSite site) {
        if (args.size() != 2) {
            site.fail("`cast` expects an expression and a SQL type name, e.g. `cast(e.qty, 'DECIMAL')`", call)
            return null
        }
        Expression typeExpr = args.get(1)
        if (!(typeExpr instanceof ConstantExpression) || !(((ConstantExpression) typeExpr).value instanceof String)) {
            site.fail("`cast` requires a string literal SQL type, e.g. 'DECIMAL' or 'VARCHAR(20)'. A variable type name is not translated", call)
            return null
        }
        String raw = (String) ((ConstantExpression) typeExpr).value
        String canonical = SqlCastTypes.canonical(raw)
        if (canonical == null) {
            site.fail("`cast` type `${raw}` is not a supported SQL type", call)
            return null
        }
        return new SqlCast(site.expr(args.get(0)), canonical)
    }

    private String wrongArity() {
        if (arityMessage != null) return arityMessage
        if (minArgs == 0 && maxArgs == 0) {
            return "`${groovyName}()` is translated only with no arguments; extra arguments have no faithful SQL equivalent"
        }
        if (minArgs == maxArgs) {
            return "`${groovyName}` expects ${minArgs == 1 ? 'one argument' : minArgs + ' arguments'}"
        }
        if (maxArgs == Integer.MAX_VALUE) {
            return "`${groovyName}` expects at least ${minArgs} arguments"
        }
        return "`${groovyName}` expects ${minArgs} to ${maxArgs} arguments"
    }

    private static void checkMethod(String name) {
        if (!identifier(name, true)) {
            throw new IllegalArgumentException("Groovy method name `${name}` is not an identifier")
        }
    }

    private static void checkSql(String name) {
        if (!identifier(name, false)) {
            throw new IllegalArgumentException("SQL function name `${name}` must be an uppercase identifier")
        }
    }

    private static void checkRange(int minArgs, int maxArgs) {
        if (minArgs < 0 || maxArgs < minArgs) {
            throw new IllegalArgumentException("argument range ${minArgs}..${maxArgs} is not valid")
        }
    }

    private static boolean identifier(String name, boolean groovyName) {
        if (!name) return false
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i)
            // char literals are Strings under static compilation
            boolean upper = ch >= 65 && ch <= 90
            boolean lower = ch >= 97 && ch <= 122
            boolean digit = ch >= 48 && ch <= 57
            boolean underscore = ch == 95
            if (i == 0) {
                if (groovyName) {
                    if (!(upper || lower || underscore)) return false
                } else if (!upper) {
                    return false
                }
            } else if (groovyName) {
                if (!(upper || lower || digit || underscore)) return false
            } else if (!(upper || digit || underscore)) {
                return false
            }
        }
        return true
    }
}
