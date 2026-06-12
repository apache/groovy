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
import groovy.transform.TypeChecked;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A template engine which leverages {@link groovy.xml.StreamingMarkupBuilder} to generate XML/XHTML.
 */
public class MarkupTemplateEngine extends TemplateEngine {

    /**
     * {@link ClassNode} reference for {@link MarkupTemplateEngine}, reused by AST transformations.
     */
    static final ClassNode MARKUPTEMPLATEENGINE_CLASSNODE = ClassHelper.make(MarkupTemplateEngine.class);

    /**
     * Metadata key storing model type declarations extracted during template compilation.
     */
    static final String MODELTYPES_ASTKEY = "MTE.modelTypes";

    private static final Pattern LOCALIZED_RESOURCE_PATTERN = Pattern.compile("(.+?)(?:_([a-z]{2}(?:_[A-Z]{2,3})))?\\.([\\p{Alnum}.]+)$");

    private static final boolean DEBUG_BYTECODE = Boolean.getBoolean("markuptemplateengine.compiler.debug");

    private static final AtomicLong counter = new AtomicLong();

    private final TemplateGroovyClassLoader groovyClassLoader;
    private final CompilerConfiguration compilerConfiguration;
    private final TemplateConfiguration templateConfiguration;
    private final Map<String, GroovyCodeSource> codeSourceCache = new LinkedHashMap<>();
    private final TemplateResolver templateResolver;

    /**
     * Creates an engine with the default {@link TemplateConfiguration}.
     */
    public MarkupTemplateEngine() {
        this(new TemplateConfiguration());
    }

    /**
     * Creates an engine with the supplied template configuration.
     *
     * @param config template configuration to apply while compiling and rendering templates
     */
    public MarkupTemplateEngine(final TemplateConfiguration config) {
        this(MarkupTemplateEngine.class.getClassLoader(), config, null);
    }

    /**
     * Creates an engine with the supplied parent loader and template configuration.
     *
     * @param parentLoader class loader used to compile generated template classes
     * @param config template configuration to apply while compiling and rendering templates
     */
    public MarkupTemplateEngine(final ClassLoader parentLoader, final TemplateConfiguration config) {
        this(parentLoader, config, null);
    }

    /**
     * Creates an engine with full control over the class loader, configuration, and resolver strategy.
     *
     * @param parentLoader class loader used to compile generated template classes
     * @param config template configuration to apply while compiling and rendering templates
     * @param resolver template resolver to use, or {@code null} for the default resolver
     */
    public MarkupTemplateEngine(final ClassLoader parentLoader, final TemplateConfiguration config, final TemplateResolver resolver) {
        templateConfiguration = config;
        compilerConfiguration = new CompilerConfiguration();
        List<CompilationCustomizer> customizers = compilerConfiguration.getCompilationCustomizers();
        customizers.add(new TemplateASTTransformer(templateConfiguration));
        customizers.add(new ASTTransformationCustomizer(
                Collections.singletonMap("extensions", "groovy.text.markup.MarkupTemplateTypeCheckingExtension"), TypeChecked.class));
        if (templateConfiguration.isAutoNewLine()) {
            customizers.add(new CompilationCustomizer(CompilePhase.CONVERSION) {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public void call(final SourceUnit source, final GeneratorContext context, final ClassNode classNode) {
                    new AutoNewLineTransformer(source).visitClass(classNode);
                }
            });
        }
        if (DEBUG_BYTECODE) {
            compilerConfiguration.setBytecodePostprocessor(BytecodeDumper.STANDARD_ERR);
        }

        groovyClassLoader = new TemplateGroovyClassLoader(parentLoader, compilerConfiguration);

        templateResolver = resolver != null ? resolver : new DefaultTemplateResolver();
        templateResolver.configure(groovyClassLoader, templateConfiguration);
    }

    /**
     * Convenience constructor to build a template engine which searches for templates into a directory
     *
     * @param templateDirectory directory where to find templates
     * @param tplConfig         template engine configuration
     */
    public MarkupTemplateEngine(final ClassLoader parentLoader, final File templateDirectory, TemplateConfiguration tplConfig) {
        this(new URLClassLoader(buildURLs(templateDirectory), parentLoader), tplConfig, null);
    }

    private static URL[] buildURLs(final File templateDirectory) {
        try {
            return new URL[]{templateDirectory.toURI().toURL()};
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid directory", e);
        }
    }

    /**
     * Compiles a template from a reader.
     *
     * @param reader template source
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if reading the template source fails
     */
    @Override
    public Template createTemplate(final Reader reader) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, null, null);
    }

    /**
     * Compiles a template from a reader using an explicit source name.
     *
     * @param reader template source
     * @param sourceName source name used for diagnostics and generated classes
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if reading the template source fails
     */
    public Template createTemplate(final Reader reader, String sourceName) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, sourceName, null);
    }

    /**
     * Resolves and compiles a template by path.
     *
     * @param templatePath logical template path understood by the configured resolver
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if template resolution or reading fails
     */
    public Template createTemplateByPath(final String templatePath) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resolveTemplate(templatePath), null);
    }

    /**
     * Compiles inline template source with explicit model type hints.
     *
     * @param source template source
     * @param modelTypes model variable type hints keyed by variable name
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if reading the template source fails
     */
    public Template createTypeCheckedModelTemplate(final String source, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(new StringReader(source), null, modelTypes);
    }

    /**
     * Compiles inline template source with explicit model type hints and a source name.
     *
     * @param source template source
     * @param sourceName source name used for diagnostics and generated classes
     * @param modelTypes model variable type hints keyed by variable name
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if reading the template source fails
     */
    public Template createTypeCheckedModelTemplate(final String source, String sourceName, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(new StringReader(source), sourceName, modelTypes);
    }

    /**
     * Compiles template source from a reader with explicit model type hints.
     *
     * @param reader template source
     * @param modelTypes model variable type hints keyed by variable name
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if reading the template source fails
     */
    public Template createTypeCheckedModelTemplate(final Reader reader, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, null, modelTypes);
    }

    /**
     * Compiles template source from a reader with explicit model type hints and a source name.
     *
     * @param reader template source
     * @param sourceName source name used for diagnostics and generated classes
     * @param modelTypes model variable type hints keyed by variable name
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if reading the template source fails
     */
    public Template createTypeCheckedModelTemplate(final Reader reader, String sourceName, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(reader, sourceName, modelTypes);
    }

    /**
     * Resolves and compiles a template by path with explicit model type hints.
     *
     * @param templatePath logical template path understood by the configured resolver
     * @param modelTypes model variable type hints keyed by variable name
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if template resolution or reading fails
     */
    public Template createTypeCheckedModelTemplateByPath(final String templatePath, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resolveTemplate(templatePath), modelTypes);
    }

    /**
     * Compiles a template from a resolved resource URL.
     *
     * @param resource template resource
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if loading the resource fails
     */
    @Override
    public Template createTemplate(final URL resource) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resource, null);
    }

    /**
     * Compiles a template from a resolved resource URL with explicit model type hints.
     *
     * @param resource template resource
     * @param modelTypes model variable type hints keyed by variable name
     * @return a compiled markup template
     * @throws CompilationFailedException if Groovy fails to compile the generated template
     * @throws ClassNotFoundException if the generated template class cannot be instantiated
     * @throws IOException if loading the resource fails
     */
    public Template createTypeCheckedModelTemplate(final URL resource, Map<String, String> modelTypes) throws CompilationFailedException, ClassNotFoundException, IOException {
        return new MarkupTemplateMaker(resource, modelTypes);
    }

    /**
     * Returns the class loader used to compile generated template classes.
     *
     * @return the template class loader
     */
    public GroovyClassLoader getTemplateLoader() {
        return groovyClassLoader;
    }

    /**
     * Returns the Groovy compiler configuration used for generated template classes.
     *
     * @return the compiler configuration
     */
    public CompilerConfiguration getCompilerConfiguration() {
        return compilerConfiguration;
    }

    /**
     * Returns the rendering configuration used by this engine.
     *
     * @return the template configuration
     */
    public TemplateConfiguration getTemplateConfiguration() {
        return templateConfiguration;
    }

    /**
     * Resolves a logical template path into a concrete resource URL.
     *
     * @param templatePath logical template path
     * @return the resolved template resource
     * @throws IOException if the template cannot be resolved
     */
    public URL resolveTemplate(String templatePath) throws IOException {
        return templateResolver.resolveTemplate(templatePath);
    }

    /**
     * Implements the {@link groovy.text.Template} interface by caching a compiled template script and keeping a
     * reference to the optional map of types of the model elements.
     */
    private class MarkupTemplateMaker implements Template {
        /**
         * Compiled template class ready to be instantiated for each render.
         */
        final Class<BaseTemplate> templateClass;

        /**
         * Optional model type hints associated with this template.
         */
        final Map<String, String> modeltypes;

        /**
         * Compiles a template from reader-based source.
         *
         * @param reader template source
         * @param sourceName source name used for diagnostics and generated classes
         * @param modelTypes optional model type hints
         */
        @SuppressWarnings("unchecked")
        MarkupTemplateMaker(final Reader reader, String sourceName, Map<String, String> modelTypes) {
            String name = sourceName != null ? sourceName : "GeneratedMarkupTemplate" + counter.getAndIncrement();
            templateClass = groovyClassLoader.parseClass(new GroovyCodeSource(reader, name, "x"), modelTypes);
            this.modeltypes = modelTypes;
        }

        /**
         * Compiles a template from a resolved resource URL.
         *
         * @param resource template resource
         * @param modelTypes optional model type hints
         * @throws IOException if reading the resource fails
         */
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

        /**
         * Creates a writable template view using an empty binding.
         *
         * @return a writable template instance
         */
        @Override
        public Writable make() {
            return make(Collections.emptyMap());
        }

        /**
         * Creates a writable template view using the supplied binding.
         *
         * @param binding binding values made available to the template
         * @return a newly instantiated template ready for rendering
         */
        @Override
        public Writable make(final Map binding) {
            return DefaultGroovyMethods.newInstance(templateClass, new Object[]{MarkupTemplateEngine.this, binding, modeltypes, templateConfiguration});
        }
    }

    /**
     * A specialized GroovyClassLoader which will support passing values to the type checking extension thanks to a
     * thread local.
     */
    static class TemplateGroovyClassLoader extends GroovyClassLoader {
        /**
         * Thread-local model type hints forwarded to the type-checking extension during compilation.
         */
        static final ThreadLocal<Map<String, String>> modelTypes = new ThreadLocal<Map<String, String>>();

        /**
         * Creates a class loader specialized for markup template compilation.
         *
         * @param parentLoader parent class loader
         * @param compilerConfiguration compiler configuration used to compile templates
         */
        TemplateGroovyClassLoader(final ClassLoader parentLoader, final CompilerConfiguration compilerConfiguration) {
            super(parentLoader, compilerConfiguration);
        }

        /**
         * Parses a template source while exposing model type hints to the type-checking extension.
         *
         * @param codeSource Groovy source to compile
         * @param hints model variable type hints keyed by variable name
         * @return the compiled template class
         * @throws CompilationFailedException if Groovy compilation fails
         */
        public Class parseClass(final GroovyCodeSource codeSource, Map<String, String> hints) throws CompilationFailedException {
            modelTypes.set(hints);
            try {
                return super.parseClass(codeSource);
            } finally {
                modelTypes.remove();
            }
        }
    }

    /**
     * Describes a template resource name split into base name, optional locale suffix, and extension.
     */
    public static class TemplateResource {
        private final String baseName;
        private final String locale;
        private final String extension;

        /**
         * Parses a template path into its base name, optional locale suffix, and extension.
         *
         * @param fullPath template path to parse
         * @return a parsed template resource descriptor
         */
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

        /**
         * Returns a copy of this resource descriptor with the supplied locale suffix.
         *
         * @param locale locale suffix to apply, or {@code null} to remove it
         * @return a new resource descriptor with the requested locale
         */
        public TemplateResource withLocale(String locale) {
            return new TemplateResource(baseName, locale, extension);
        }

        /**
         * Returns the resource path represented by this descriptor.
         *
         * @return the resource path with optional locale suffix
         */
        @Override
        public String toString() {
            return baseName + (locale != null ? "_" + locale : "") + "." + extension;
        }

        /**
         * Indicates whether this descriptor carries an explicit locale suffix.
         *
         * @return {@code true} if a locale suffix is present
         */
        public boolean hasLocale() {
            return locale != null && !locale.isEmpty();
        }
    }

    /**
     * Default resolver that loads templates from the configured template class loader and applies locale fallback rules.
     */
    public static class DefaultTemplateResolver implements TemplateResolver {
        private TemplateConfiguration templateConfiguration;
        private ClassLoader templateClassLoader;

        /**
         * Creates a resolver that loads templates from the configured template class loader.
         */
        public DefaultTemplateResolver() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void configure(final ClassLoader templateClassLoader, final TemplateConfiguration configuration) {
            this.templateClassLoader = templateClassLoader;
            this.templateConfiguration = configuration;
        }

        /**
         * {@inheritDoc}
         */
        @Override
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
        /**
         * Backing cache keyed by logical template path.
         */
        protected final Map<String, URL> cache;

        /**
         * Indicates whether this resolver should consult and populate its cache.
         */
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
            this(new ConcurrentHashMap<String, URL>());
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void configure(final ClassLoader templateClassLoader, final TemplateConfiguration configuration) {
            super.configure(templateClassLoader, configuration);
            useCache = configuration.isCacheTemplates();
        }

        /**
         * {@inheritDoc}
         */
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
