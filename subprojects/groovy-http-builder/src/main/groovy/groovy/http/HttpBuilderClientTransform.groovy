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
package groovy.http

import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.objectweb.asm.Opcodes

import java.util.concurrent.CompletableFuture

import static org.codehaus.groovy.ast.ClassHelper.*
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

/**
 * AST transform that generates an implementation class for interfaces
 * annotated with {@link HttpBuilderClient}.
 *
 * @since 6.0.0
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class HttpBuilderClientTransform extends AbstractASTTransformation {

    private static final ClassNode HELPER_TYPE = make(HttpClientHelper)
    private static final ClassNode FUTURE_TYPE = make(CompletableFuture)
    private static final ClassNode HTTP_RESULT_TYPE = make(HttpResult)

    private static final Map<String, String> HTTP_METHOD_ANNOTATIONS = [
        'groovy.http.Get'   : 'GET',
        'groovy.http.Post'  : 'POST',
        'groovy.http.Put'   : 'PUT',
        'groovy.http.Delete': 'DELETE',
        'groovy.http.Patch' : 'PATCH',
    ]

    /**
     * Validates {@link HttpBuilderClient} usage and synthesizes the corresponding client implementation.
     *
     * @param nodes the annotation and annotated interface nodes
     * @param source the source unit being transformed
     */
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotationNode anno = (AnnotationNode) nodes[0]
        AnnotatedNode target = (AnnotatedNode) nodes[1]

        if (!(target instanceof ClassNode) || !target.isInterface()) {
            addError("@HttpBuilderClient can only be applied to interfaces", target)
            return
        }

        ClassNode interfaceNode = (ClassNode) target
        String baseUrl = getMemberStringValue(anno, 'value')
        if (!baseUrl) {
            addError("@HttpBuilderClient requires a base URL", anno)
            return
        }

        int connectTimeout = getMemberIntValue(anno, 'connectTimeout')
        int requestTimeout = getMemberIntValue(anno, 'requestTimeout')
        boolean followRedirects = memberHasValue(anno, 'followRedirects', true)

        // Collect interface-level @Header annotations
        Map<String, String> interfaceHeaders = collectHeaders(interfaceNode)

        // Generate the implementation class
        ClassNode implClass = generateImplClass(interfaceNode, baseUrl, interfaceHeaders,
                connectTimeout, requestTimeout, followRedirects)
        source.AST.addClass(implClass)

        // Add static create() factory method to the interface
        addCreateMethod(interfaceNode, implClass, baseUrl)
    }

    private ClassNode generateImplClass(ClassNode interfaceNode, String baseUrl, Map<String, String> interfaceHeaders,
                                          int connectTimeout, int requestTimeout, boolean followRedirects) {
        String implName = interfaceNode.name + '$Client'
        ClassNode implClass = new ClassNode(implName, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC,
                OBJECT_TYPE, [interfaceNode.getPlainNodeReference()] as ClassNode[], null)
        implClass.sourcePosition = interfaceNode

        // Field: private final HttpClientHelper __helper
        FieldNode helperField = implClass.addField('__helper', Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                HELPER_TYPE, null)

        // Constructor: takes baseUrl string with default
        Parameter baseUrlParam = param(STRING_TYPE, 'baseUrl')
        baseUrlParam.setInitialExpression(constX(baseUrl))
        BlockStatement ctorBody = block(
            assignS(fieldX(helperField),
                ctorX(HELPER_TYPE, args(
                    varX(baseUrlParam),
                    buildHeadersMapExpression(interfaceHeaders),
                    constX(connectTimeout),
                    constX(requestTimeout),
                    constX(followRedirects)
                )))
        )
        implClass.addConstructor(Opcodes.ACC_PUBLIC, params(baseUrlParam), ClassNode.EMPTY_ARRAY, ctorBody)

        // Constructor: takes pre-built HttpBuilder (for create(Closure) factory)
        Parameter httpBuilderParam = param(make(HttpBuilder), 'httpBuilder')
        BlockStatement ctorBody2 = block(
            assignS(fieldX(helperField),
                ctorX(HELPER_TYPE, args(
                    varX(httpBuilderParam),
                    buildHeadersMapExpression(interfaceHeaders)
                )))
        )
        implClass.addConstructor(Opcodes.ACC_PUBLIC, params(httpBuilderParam), ClassNode.EMPTY_ARRAY, ctorBody2)

        // Generate a method for each abstract interface method
        for (MethodNode method : interfaceNode.abstractMethods) {
            String httpMethod = null
            String urlTemplate = null

            for (Map.Entry<String, String> entry : HTTP_METHOD_ANNOTATIONS) {
                AnnotationNode methodAnno = method.getAnnotations(make(entry.key)).find()
                if (methodAnno) {
                    httpMethod = entry.value
                    urlTemplate = getMemberStringValue(methodAnno, 'value')
                    break
                }
            }

            if (!httpMethod || !urlTemplate) {
                addError("Method '${method.name}' in @HttpBuilderClient interface must have an HTTP method annotation (@Get, @Post, @Put, @Delete, @Patch)", method)
                continue
            }

            Map<String, String> methodHeaders = collectHeaders(method)
            int methodTimeout = getMethodTimeout(method)
            MethodNode implMethod = generateMethod(method, httpMethod, urlTemplate,
                    methodHeaders, helperField, methodTimeout)
            implClass.addMethod(implMethod)
        }

        return implClass
    }

    private MethodNode generateMethod(MethodNode method, String httpMethod, String urlTemplate,
                                       Map<String, String> methodHeaders, FieldNode helperField,
                                       int methodTimeout) {
        Parameter[] params = method.parameters
        boolean isAsync = isAsyncReturn(method.returnType)
        String returnTypeName = resolveReturnTypeName(method.returnType)
        boolean isForm = !method.getAnnotations(make(Form)).isEmpty()

        // Determine body mode: 'json' (default), 'form', or 'text'
        String bodyMode = isForm ? 'form' : 'json'

        // Build path params map: params whose names appear as {name} in the URL
        MapExpression pathParams = new MapExpression()
        MapExpression queryOrFormParams = new MapExpression()
        Expression bodyExpr = constX(null)

        for (Parameter p : params) {
            if (urlTemplate.contains("{${p.name}}")) {
                pathParams.addMapEntryExpression(
                    new MapEntryExpression(constX(p.name), varX(p)))
            } else if (hasAnnotation(p, Body)) {
                bodyExpr = varX(p)
            } else if (hasAnnotation(p, BodyText)) {
                bodyExpr = varX(p)
                bodyMode = 'text'
            } else {
                // Query parameter (or form field if @Form) — use @Query name if specified, else param name
                String queryName = getQueryParamName(p)
                queryOrFormParams.addMapEntryExpression(
                    new MapEntryExpression(constX(queryName), varX(p)))
            }
        }

        // Error type from throws clause (first declared exception with HttpException-compatible constructor)
        String errorTypeName = resolveErrorTypeName(method)

        String executeMethod = isAsync ? 'executeAsync' : 'execute'
        Expression callExpr = callX(fieldX(helperField), executeMethod, args(
            constX(httpMethod),
            constX(urlTemplate),
            constX(returnTypeName),
            pathParams,
            queryOrFormParams,
            buildHeadersMapExpression(methodHeaders),
            bodyExpr,
            constX(methodTimeout),
            constX(bodyMode),
            constX(errorTypeName)
        ))

        boolean isVoid = method.returnType == VOID_TYPE || method.returnType.name == 'void'
        Statement body = isVoid ? block(stmt(callExpr), returnS(constX(null))) : returnS(callExpr)

        // Preserve declared exceptions on the generated method
        ClassNode[] exceptions = method.exceptions ?: ClassNode.EMPTY_ARRAY

        return new MethodNode(method.name, Opcodes.ACC_PUBLIC, method.returnType,
                cloneParams(params), exceptions, body)
    }

    private void addCreateMethod(ClassNode interfaceNode, ClassNode implClass, String baseUrl) {
        // create() — uses the annotation URL
        if (!hasDeclaredMethod(interfaceNode, 'create', 0)) {
            Statement noArgBody = returnS(ctorX(implClass, args(constX(baseUrl))))
            MethodNode createNoArg = new MethodNode('create',
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, interfaceNode.getPlainNodeReference(),
                    Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, noArgBody)
            interfaceNode.addMethod(createNoArg)
        }

        // create(String baseUrl) — override URL
        if (!hasDeclaredMethod(interfaceNode, 'create', 1)) {
            Parameter baseUrlParam = param(STRING_TYPE, 'baseUrl')
            Statement withArgBody = returnS(ctorX(implClass, args(varX(baseUrlParam))))
            MethodNode createWithArg = new MethodNode('create',
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, interfaceNode.getPlainNodeReference(),
                    params(baseUrlParam), ClassNode.EMPTY_ARRAY, withArgBody)
            interfaceNode.addMethod(createWithArg)
        }

        // create(Closure config) — advanced configuration
        // The closure delegates to HttpBuilder.Config, giving access to
        // baseUri, headers, timeouts, redirects, clientConfig, etc.
        ClassNode closureType = make(Closure).getPlainNodeReference()
        Parameter configParam = param(closureType, 'config')
        // HttpBuilder.http(config) returns an HttpBuilder; pass it to the HttpBuilder constructor
        Expression httpBuilderExpr = callX(make(HttpBuilder), 'http', args(varX(configParam)))
        Statement configBody = returnS(ctorX(implClass, args(httpBuilderExpr)))
        MethodNode createWithConfig = new MethodNode('create',
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, interfaceNode.getPlainNodeReference(),
                params(configParam), ClassNode.EMPTY_ARRAY, configBody)
        interfaceNode.addMethod(createWithConfig)
    }

    private static Map<String, String> collectHeaders(AnnotatedNode node) {
        Map<String, String> headers = new LinkedHashMap<>()
        for (AnnotationNode anno : node.getAnnotations(make(Header))) {
            headers[getMemberStringValue(anno, 'name')] = getMemberStringValue(anno, 'value')
        }
        for (AnnotationNode anno : node.getAnnotations(make(Headers))) {
            Expression members = anno.getMember('value')
            if (members instanceof ListExpression) {
                for (Expression expr : ((ListExpression) members).expressions) {
                    if (expr instanceof AnnotationConstantExpression) {
                        AnnotationNode inner = ((AnnotationConstantExpression) expr).value
                        headers[getMemberStringValue(inner, 'name')] = getMemberStringValue(inner, 'value')
                    }
                }
            }
        }
        return headers
    }

    private static MapExpression buildHeadersMapExpression(Map<String, String> headers) {
        MapExpression map = new MapExpression()
        headers.each { k, v ->
            map.addMapEntryExpression(new MapEntryExpression(constX(k), constX(v)))
        }
        return map
    }

    private static boolean isAsyncReturn(ClassNode returnType) {
        return returnType.name == CompletableFuture.name ||
               returnType.redirect()?.name == CompletableFuture.name
    }

    private static String resolveReturnTypeName(ClassNode returnType) {
        if (isAsyncReturn(returnType)) {
            GenericsType[] generics = returnType.genericsTypes
            if (generics?.length == 1) {
                return generics[0].type.name
            }
            return Object.name
        }
        if (returnType == VOID_TYPE || returnType.name == 'void') return 'void'
        return returnType.name
    }

    private static String getQueryParamName(Parameter p) {
        AnnotationNode queryAnno = p.getAnnotations(make(Query)).find()
        if (queryAnno) {
            String name = getMemberStringValue(queryAnno, 'value')
            return name ?: p.name
        }
        return p.name
    }

    private static String resolveErrorTypeName(MethodNode method) {
        ClassNode[] exceptions = method.exceptions
        if (exceptions != null && exceptions.length > 0) {
            return exceptions[0].name
        }
        return ''
    }

    private static int getMethodTimeout(MethodNode method) {
        AnnotationNode timeoutAnno = method.getAnnotations(make(Timeout)).find()
        if (timeoutAnno) {
            Expression expr = timeoutAnno.getMember('value')
            if (expr instanceof ConstantExpression) {
                return ((Number) ((ConstantExpression) expr).value).intValue()
            }
        }
        return 0
    }

    private static boolean hasAnnotation(Parameter p, Class<?> annoType) {
        return !p.getAnnotations(make(annoType)).isEmpty()
    }

    private static Parameter[] cloneParams(Parameter[] source) {
        source.collect { Parameter p ->
            new Parameter(p.type, p.name)
        } as Parameter[]
    }
}
