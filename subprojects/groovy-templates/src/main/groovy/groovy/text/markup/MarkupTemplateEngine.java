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
package groovy.text.markup;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.Writable;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.classgen.asm.BytecodeDumper;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A template engine which leverages {@link groovy.xml.StreamingMarkupBuilder} to generate XML/XHTML.
 */
public class MarkupTemplateEngine extends TemplateEngine {

    static final ClassNode MARKUPTEMPLATEENGINE_CLASSNODE = ClassHelper.make(MarkupTemplateEngine.class);
    static final String MODELTYPES_ASTKEY = "MTE.modelTypes";

    private static final Pattern LOCALIZED_RESOURCE_PATTERN = Pattern.compile("(.+?)(?:_([a-z]{2}(?:_[A-Z]{2,3})))?\\.([\\p{Alnum}.]+)$");

    private static final boolean DEBUG_BYTECODE = Boolean.valueOf(System.getProperty("markuptemplateengine.compiler.debug","false"));

    private static final AtomicLong counter = new AtomicLong();

    private final TemplateGroovyClassLoader groovyClassLoader;
    private final CompilerConfiguration compilerConfiguration;
    private final TemplateConfiguration templateConfiguration;
    private final Map<String, GroovyCodeSource> codeSourceCache = new LinkedHashMap<>();
    private final TemplateResolver templateResolver;

    public MarkupTemplateEngine() {
        this(new TemplateConfiguration());
    }

    public MarkupTemplateEngine(final TemplateConfiguration tplConfig) {
        this(MarkupTemplateEngine.class.getClassLoader(), tplConfig);
    }

    public MarkupTemplateEngine(final ClassLoader parentLoader, final TemplateConfiguration tplConfig) {
        this(parentLoader, tplConfig, null);
    }

    public MarkupTemplateEngine(final ClassLoader parentLoader, final TemplateConfiguration tplConfig, final TemplateResolver resolver) {
        compilerConfiguration = new CompilerConfiguration();
        templateConfiguration = tplConfig;
        compilerConfiguration.addCompilationCustomizers(new TemplateASTTransformer(tplConfig));
        compilerConfiguration.addCompilationCustomizers(
                new ASTTransformationCustomizer(Collections.singletonMap("extensions", "groovy.text.markup.MarkupTemplateTypeCheckingExtension"), CompileStatic.class));
        if (templateConfiguration.isAutoNewLine()) {
            compilerConfiguration.addCompilationCustomizers(
                    new CompilationCustomizer(CompilePhase.CONVERSION) {
                        @Override
                        public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) throws CompilationFailedException {
                            new AutoNewLineTransformer(source).visitClass(classNode);
                        }
                    }
            );
        }
        groovyClassLoader = AccessController.doPrivileged(new PrivilegedAction<TemplateGroovyClassLoader>() {
            public TemplateGroovyClassLoader run() {
                return new TemplateGroovyClassLoader(parentLoader, compilerConfiguration);
            }
        });
        if (DEBUG_BYTECODE) {
            compilerConfiguration.setBytecodePostprocessor(BytecodeDumper.STANDARD_ERR);
        }
        templateResolver = resolver == null ? new DefaultTemplateResolver() : resolver;
        templateResolver.configure(groovyClassLoader, templateConfiguration);
    }

    /**
     * Convenience constructor to build a template engine which searches for templates into a directory
     *
     * @param templateDirectory directory where to find templates
     * @param tplConfig         template engine configuration
     */
    public MarkupTemplateEngine(final ClassLoader parentLoader, final File templateDirectory, TemplateConfiguration tplConfig) {
        this(AccessController.doPrivileged(
                new PrivilegedAction<URLClassLoader>() {
                    @Override
                    public URLClassLoader run() {
                        return new URLClassLoader(buildURLs(templateDirectory), parentLoader);
                    }
                }),
                tplConfig,
                null);
    }

    private static URL[] buildURLs(final File templateDirectory) {
        try {
            return new URL[]{templateDirectory.toURI().toURL()};
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid directory", e);
        }
    }

    public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, null, null);
    }

    public Template createTemplate(final Reader reader, String sourceName) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, sourceName, null);
    }

    public Template createTemplateByPath(final String templatePath) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resolveTemplate(templatePath), null);
    }

    public Template createTypeCheckedModelTemplate(final String source, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(new StringReader(source), null, modelTypes);
    }

    public Template createTypeCheckedModelTemplate(final String source, String sourceName, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(new StringReader(source), sourceName, modelTypes);
    }

    public Template createTypeCheckedModelTemplate(final Reader reader, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, null, modelTypes);
    }

    public Template createTypeCheckedModelTemplate(final Reader reader, String sourceName, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, sourceName, modelTypes);
    }

    public Template createTypeCheckedModelTemplateByPath(final String templatePath, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resolveTemplate(templatePath), modelTypes);
    }

    @Override
    public Template createTemplate(final URL resource) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resource, null);
    }

    public Template createTypeCheckedModelTemplate(final URL resource, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resource, modelTypes);
    }

    public GroovyClassLoader getTemplateLoader() {
        return groovyClassLoader;
    }

    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }

    public TemplateConfiguration getTemplateConfiguration() {
        return templateConfiguration;
    }

    public URL resolveTemplate(String templatePath) throws IOException {
        return templateResolver.resolveTemplate(templatePath);
    }

    /**
     * Implements the {@link groovy.text.Template} interface by caching a compiled template script and keeping a
     * reference to the optional map of types of the model elements.
     */
    private class MarkupTemplateMaker implements Template {
        final Class<BaseTemplate> templateClass;
        final Map<String, String> modeltypes;

        @SuppressWarnings("unchecked")
        public MarkupTemplateMaker(final Reader reader, String sourceName, Map<String, String> modelTypes) {
            String name = sourceName != null ? sourceName : "GeneratedMarkupTemplate" + counter.getAndIncrement();
            templateClass = groovyClassLoader.parseClass(new GroovyCodeSource(reader, name, "x"), modelTypes);
            this.modeltypes = modelTypes;
        }

        @SuppressWarnings("unchecked")
        public MarkupTemplateMaker(final URL resource, Map<String, String> modelTypes) throws IOException {
            boolean cache = templateConfiguration.isCacheTemplates();
            GroovyCodeSource codeSource;
            if (cache) {
                // we use a map in addition to the internal caching mechanism of Groovy because the latter
                // will always read from the URL even if it's cached
                String key = resource.toExternalForm();
                codeSource = codeSourceCache.get(key);
                if (codeSource == null) {
                    codeSource = new GroovyCodeSource(resource);
                    codeSourceCache.put(key, codeSource);
                }
            } else {
                codeSource = new GroovyCodeSource(resource);
            }
            codeSource.setCachable(cache);
            templateClass = groovyClassLoader.parseClass(codeSource, modelTypes);
            this.modeltypes = modelTypes;
        }

        public Writable make() {
            return make(Collections.emptyMap());
        }

        public Writable make(final Map binding) {
            return DefaultGroovyMethods.newInstance(templateClass, new Object[]{MarkupTemplateEngine.this, binding, modeltypes, templateConfiguration});
        }
    }

    /**
     * A specialized GroovyClassLoader which will support passing values to the type checking extension thanks to a
     * thread local.
     */
    static class TemplateGroovyClassLoader extends GroovyClassLoader {
        static final ThreadLocal<Map<String, String>> modelTypes = new ThreadLocal<>();

        public TemplateGroovyClassLoader(final ClassLoader parentLoader, final CompilerConfiguration compilerConfiguration) {
            super(parentLoader, compilerConfiguration);
        }

        public Class parseClass(final GroovyCodeSource codeSource, Map<String, String> hints) throws CompilationFailedException {
            modelTypes.set(hints);
            try {
                return super.parseClass(codeSource);
            } finally {
                modelTypes.set(null);
            }
        }
    }

    public static class TemplateResource {
        private final String baseName;
        private final String locale;
        private final String extension;

        public static TemplateResource parse(String fullPath) {
            Matcher matcher = LOCALIZED_RESOURCE_PATTERN.matcher(fullPath);
            if (!matcher.find()) {
                throw new IllegalArgumentException("Illegal template path: " + fullPath);
            }
            return new TemplateResource(matcher.group(1), matcher.group(2), matcher.group(3));
        }

        private TemplateResource(final String baseName, final String locale, final String extension) {
            this.baseName = baseName;
            this.locale = locale;
            this.extension = extension;
        }

        public TemplateResource withLocale(String locale) {
            return new TemplateResource(baseName, locale, extension);
        }

        public String toString() {
            return baseName + (locale != null ? "_" + locale : "") + "." + extension;
        }

        public boolean hasLocale() {
            return locale != null && !"".equals(locale);
        }
    }

    public static class DefaultTemplateResolver implements TemplateResolver {
        private TemplateConfiguration templateConfiguration;
        private ClassLoader templateClassLoader;

        public DefaultTemplateResolver() {
        }

        public void configure(final ClassLoader templateClassLoader, final TemplateConfiguration configuration) {
            this.templateClassLoader = templateClassLoader;
            this.templateConfiguration = configuration;
        }

        public URL resolveTemplate(final String templatePath) throws IOException {
            MarkupTemplateEngine.TemplateResource templateResource = MarkupTemplateEngine.TemplateResource.parse(templatePath);
            String configurationLocale = templateConfiguration.getLocale().toString().replace("-", "_");
            URL resource = templateResource.hasLocale() ? templateClassLoader.getResource(templateResource.toString()) : null;
            if (resource == null) {
                // no explicit locale in the template path or resource not found
                // fallback to the default configuration locale
                resource = templateClassLoader.getResource(templateResource.withLocale(configurationLocale).toString());
            }
            if (resource == null) {
                // no resource found with the default locale, try without any locale
                resource = templateClassLoader.getResource(templateResource.withLocale(null).toString());
            }
            if (resource == null) {
                throw new IOException("Unable to load template:" + templatePath);
            }
            return resource;
        }
    }

    /**
     * A template resolver which avoids calling {@link ClassLoader#getResource(String)} if a template path already has
     * been queried before. This improves performance if caching is enabled in the configuration.
     */
    public static class CachingTemplateResolver extends DefaultTemplateResolver {
        // Those member should stay protected so that subclasses may use different
        // cache keys as the ones used by this implementation
        protected final Map<String, URL> cache;
        protected boolean useCache = false;

        /**
         * Creates a new caching template resolver. The cache implementation being used depends on
         * the use of the template engine. If multiple templates can be rendered in parallel, it <b>must</b>
         * be using a thread-safe cache.
         * @param cache the backing cache
         */
        public CachingTemplateResolver(final Map<String, URL> cache) {
            this.cache = cache;
        }

        /**
         * Creates a new caching template resolver using a concurrent hash map as the backing cache.
         */
        public CachingTemplateResolver() {
            this(new ConcurrentHashMap<>());
        }


        @Override
        public void configure(final ClassLoader templateClassLoader, final TemplateConfiguration configuration) {
            super.configure(templateClassLoader, configuration);
            useCache = configuration.isCacheTemplates();
        }

        @Override
        public URL resolveTemplate(final String templatePath) throws IOException {
            if (useCache) {
                URL cachedURL = cache.get(templatePath);
                if (cachedURL!=null) {
                    return cachedURL;
                }
            }
            URL url = super.resolveTemplate(templatePath);
            if (useCache) {
                cache.put(templatePath, url);
            }
            return url;
        }
    }

}
