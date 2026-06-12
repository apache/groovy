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
package org.apache.groovy.perf.grails

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Tests composite patterns that simulate real Grails application behavior.
 * These benchmarks combine multiple Groovy features (closures, dynamic
 * dispatch, metaclass modifications, property access, delegation) in
 * patterns that mirror actual Grails framework usage.
 *
 * Unlike the focused single-feature benchmarks, these exercise the
 * interaction effects between features - particularly how metaclass
 * changes in one component cascade to affect call sites in other
 * components through global SwitchPoint invalidation.
 *
 * Each benchmark simulates a specific Grails application pattern:
 * <ul>
 *   <li>Service layer method chains with dependency injection</li>
 *   <li>Domain class CRUD with property access and validation</li>
 *   <li>Controller action dispatch with parameter binding</li>
 *   <li>Configuration DSL with nested delegation</li>
 *   <li>View rendering with builder patterns</li>
 * </ul>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class GrailsLikePatternsBench {
    /** Number of iterations per benchmark. */
    static final int ITERATIONS = 100_000

    /**
     * Simulates a Grails domain class with validation and persistence.
     */
    static class DomainObject {
        /** Unique identifier. */
        Long id
        /** Name field. */
        String name
        /** Email field. */
        String email
        /** Version for optimistic locking. */
        int version = 0
        /** Map of validation errors. */
        Map errors = [:]
        /** Validation constraints. */
        Map constraints = [name: [nullable: false, maxSize: 255], email: [nullable: false, email: true]]

        /** Validates the object against constraints. */
        boolean validate() {
            errors.clear()
            constraints.each { field, rules ->
                def value = this."$field"
                if (!rules.nullable && value == null) {
                    errors[field] = 'nullable'
                }
                if (rules.maxSize && value?.toString()?.length() > rules.maxSize) {
                    errors[field] = 'maxSize.exceeded'
                }
            }
            errors.isEmpty()
        }

        /** Saves the object after validation. */
        DomainObject save() {
            if (validate()) {
                version++
                if (id == null) id = System.nanoTime()
            }
            this
        }

        /** Converts the object to a map. */
        Map toMap() {
            [id: id, name: name, email: email, version: version]
        }
    }

    /**
     * Simulates a service that validates domain fields.
     */
    static class ValidationService {
        /** Validates an email address. */
        boolean validateEmail(String email) {
            email != null && email.contains('@') && email.contains('.')
        }

        /** Validates a name field. */
        boolean validateName(String name) {
            name != null && name.length() >= 2 && name.length() <= 255
        }
    }

    /**
     * Simulates a service that creates and lists domain objects.
     */
    static class DomainService {
        /** Injected validation service. */
        ValidationService validationService

        /** Creates a domain object from parameters. */
        DomainObject create(Map params) {
            def obj = new DomainObject()
            params.each { key, value ->
                obj."$key" = value
            }
            if (validationService.validateName(obj.name) &&
                validationService.validateEmail(obj.email)) {
                obj.save()
            }
            obj
        }

        /** Lists domain objects as maps. */
        List<Map> list(List<DomainObject> objects) {
            objects.findAll { it.id != null }.collect { it.toMap() }
        }
    }

    /**
     * Simulates a Grails controller context.
     */
    static class ControllerContext {
        /** Request parameters. */
        Map params = [:]
        /** Model for view rendering. */
        Map model = [:]
        /** Name of the view to render. */
        String viewName
        /** Flash scope for messages. */
        List flash = []

        /** Renders a view with model. */
        void render(Map args) {
            viewName = args.view ?: 'default'
            if (args.model) model.putAll(args.model)
        }
    }

    /**
     * Simulates a Grails configuration builder DSL.
     */
    static class ConfigBuilder {
        /** Configuration map. */
        Map config = [:]

        /** Configures data source settings. */
        void dataSource(@DelegatesTo(DataSourceConfig) Closure cl) {
            def dsc = new DataSourceConfig()
            cl.delegate = dsc
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            config.dataSource = dsc.toMap()
        }

        /** Configures server settings. */
        void server(@DelegatesTo(ServerConfig) Closure cl) {
            def sc = new ServerConfig()
            cl.delegate = sc
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            config.server = sc.toMap()
        }
    }

    /**
     * Data source configuration holder.
     */
    static class DataSourceConfig {
        /** Database URL. */
        String url = 'jdbc:h2:mem:default'
        /** JDBC driver class name. */
        String driverClassName = 'org.h2.Driver'
        /** Database username. */
        String username = 'sa'
        /** Database password. */
        String password = ''
        /** Connection pool settings. */
        Map pool = [maxActive: 10]

        /** Converts to map representation. */
        Map toMap() { [url: url, driverClassName: driverClassName, username: username, pool: pool] }
    }

    /**
     * Server configuration holder.
     */
    static class ServerConfig {
        /** Server port. */
        int port = 8080
        /** Server host. */
        String host = 'localhost'
        /** SSL configuration. */
        Map ssl = [enabled: false]

        /** Converts to map representation. */
        Map toMap() { [port: port, host: host, ssl: ssl] }
    }

    /**
     * Markup builder context for HTML generation.
     */
    static class MarkupContext {
        /** Buffer for accumulating markup. */
        StringBuilder buffer = new StringBuilder()
        /** Current nesting depth. */
        int depth = 0

        /** Creates a tag with name and body closure. */
        void tag(String name, Closure body) {
            tag(name, [:], body)
        }

        /** Creates a tag with name, attributes, and optional body. */
        void tag(String name, Map attrs = [:], Closure body = null) {
            buffer.append('  ' * depth).append("<$name")
            attrs.each { k, v -> buffer.append(" $k=\"$v\"") }
            if (body) {
                buffer.append('>')
                depth++
                body.delegate = this
                body.resolveStrategy = Closure.DELEGATE_FIRST
                body()
                depth--
                buffer.append("</$name>")
            } else {
                buffer.append('/>')
            }
        }

        /** Appends text content. */
        void text(String content) {
            buffer.append(content)
        }

        /** Renders the accumulated markup. */
        String render() { buffer.toString() }
    }

    /** Validation service instance for benchmarking. */
    ValidationService validationService
    /** Domain service instance for benchmarking. */
    DomainService domainService
    /** Sample domain data for benchmarking. */
    List<DomainObject> sampleData

    /** Sets up services and sample data before each iteration. */
    @Setup(Level.Iteration)
    void setup() {
        GroovySystem.metaClassRegistry.removeMetaClass(DomainObject)
        GroovySystem.metaClassRegistry.removeMetaClass(DomainService)
        GroovySystem.metaClassRegistry.removeMetaClass(ValidationService)
        GroovySystem.metaClassRegistry.removeMetaClass(ControllerContext)
        GroovySystem.metaClassRegistry.removeMetaClass(ConfigBuilder)

        validationService = new ValidationService()
        domainService = new DomainService(validationService: validationService)

        sampleData = (1..20).collect { i ->
            new DomainObject(name: "User$i", email: "user${i}@example.com").save()
        }
    }

    // ===== SERVICE CHAIN PATTERNS =====

    /**
     * Service method chain - simulates a Grails service calling
     * another service, which accesses domain objects. Multiple layers
     * of dynamic dispatch through Groovy property access and method
     * calls.
     */
    @Benchmark
    void serviceChainCreateAndList(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def obj = domainService.create(
                name: "User${i % 100}",
                email: "user${i % 100}@example.com"
            )
            bh.consume(obj.id)
        }
    }

    /**
     * Service chain with collection processing - findAll, collect,
     * inject patterns typical of Grails service layer.
     */
    @Benchmark
    void serviceChainWithCollections(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 10; i++) {
            def listed = domainService.list(sampleData)
            bh.consume(listed.size())
        }
    }

    // ===== CONTROLLER ACTION PATTERNS =====

    /**
     * Controller-like action dispatch - simulates a Grails controller
     * handling a request: reading params, calling service, building
     * model, rendering view.
     */
    @Benchmark
    void controllerActionPattern(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def ctx = new ControllerContext()
            ctx.params = [name: "User${i % 100}", email: "u${i % 100}@test.com"]

            // Simulate action body
            def obj = domainService.create(ctx.params)
            if (obj.errors.isEmpty()) {
                ctx.render(view: 'show', model: [item: obj.toMap()])
            } else {
                ctx.flash << "Validation failed"
                ctx.render(view: 'create', model: [item: obj, errors: obj.errors])
            }
            bh.consume(ctx.viewName)
        }
    }

    /**
     * Controller action pattern with metaclass changes.
     * Simulates a Grails app where framework components are still
     * being initialized (metaclass modifications) while requests
     * are already being served.
     */
    @Benchmark
    void controllerActionDuringMetaclassChurn(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def ctx = new ControllerContext()
            ctx.params = [name: "User${i % 100}", email: "u${i % 100}@test.com"]

            def obj = domainService.create(ctx.params)
            ctx.render(view: 'show', model: [item: obj.toMap()])
            bh.consume(ctx.viewName)

            // Periodic metaclass changes (framework initialization)
            if (i % 1000 == 0) {
                DomainObject.metaClass."helper${i % 5}" = { -> delegate.name }
            }
        }
    }

    // ===== DOMAIN VALIDATION PATTERNS =====

    /**
     * Domain object validation cycle - create, validate, check errors.
     * Exercises dynamic property access (this."$field"), map operations,
     * and closure iteration - all through invokedynamic.
     */
    @Benchmark
    void domainValidationCycle(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def obj = new DomainObject(
                name: (i % 10 == 0) ? null : "User$i",
                email: (i % 7 == 0) ? null : "user${i}@test.com"
            )
            boolean valid = obj.validate()
            bh.consume(valid)
            if (!valid) {
                bh.consume(obj.errors.size())
            }
        }
    }

    // ===== CONFIGURATION DSL PATTERNS =====

    /**
     * Configuration DSL - simulates Grails application.groovy style
     * configuration with nested closures and delegation.
     * Each closure uses DELEGATE_FIRST strategy, which requires
     * dynamic method resolution.
     */
    @Benchmark
    void configurationDsl(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def builder = new ConfigBuilder()
            builder.dataSource {
                url = "jdbc:h2:mem:db${i % 10}"
                driverClassName = 'org.h2.Driver'
                username = 'sa'
                password = ''
                pool = [maxActive: 20 + (i % 10)]
            }
            builder.server {
                port = 8080 + (i % 100)
                host = 'localhost'
                ssl = [enabled: i % 2 == 0]
            }
            bh.consume(builder.config.size())
        }
    }

    // ===== BUILDER / VIEW RENDERING PATTERNS =====

    /**
     * Markup builder pattern - simulates GSP/Groovy template rendering
     * with nested closure delegation. Each tag() call uses a closure
     * with DELEGATE_FIRST, requiring dynamic method resolution at
     * each nesting level.
     */
    @Benchmark
    void markupBuilderPattern(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 10; i++) {
            def markup = new MarkupContext()
            markup.tag('div', [class: 'container']) {
                tag('h1') { text("Item ${i}") }
                tag('ul') {
                    for (int j = 0; j < 5; j++) {
                        tag('li', [class: j % 2 == 0 ? 'even' : 'odd']) {
                            text("Entry $j")
                        }
                    }
                }
                tag('footer') { text('End') }
            }
            bh.consume(markup.render())
        }
    }

    // ===== DYNAMIC PROPERTY MAP ACCESS =====

    /**
     * Dynamic property access pattern - accessing properties by
     * name string (this."$fieldName"). Common in Grails data binding,
     * GORM field access, and controller parameter processing.
     */
    @Benchmark
    void dynamicPropertyByName(Blackhole bh) {
        String[] fields = ['name', 'email', 'version']
        def obj = sampleData[0]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def val = obj."${fields[i % 3]}"
            sum += val?.toString()?.length() ?: 0
        }
        bh.consume(sum)
    }

    /**
     * Dynamic property access with metaclass churn.
     * Combines the dynamic property pattern with metaclass changes
     * happening elsewhere in the application.
     */
    @Benchmark
    void dynamicPropertyDuringMetaclassChurn(Blackhole bh) {
        String[] fields = ['name', 'email', 'version']
        def obj = sampleData[0]
        int sum = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def val = obj."${fields[i % 3]}"
            sum += val?.toString()?.length() ?: 0
            if (i % 1000 == 0) {
                // Metaclass change on a different class affects this call site too
                ValidationService.metaClass."helper${i % 3}" = { -> 'help' }
            }
        }
        bh.consume(sum)
    }

    // ===== COMPOSITE: FULL REQUEST CYCLE =====

    /**
     * Full simulated request cycle combining controller dispatch,
     * service calls, domain validation, and view rendering.
     * This is the closest approximation to what a real Grails
     * request handler exercises.
     */
    @Benchmark
    void fullRequestCycleSimulation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 10; i++) {
            // Controller receives request
            def ctx = new ControllerContext()
            ctx.params = [name: "User${i % 50}", email: "user${i % 50}@test.com"]

            // Service layer processes
            def obj = domainService.create(ctx.params)

            // Build response model
            if (obj.errors.isEmpty()) {
                def markup = new MarkupContext()
                markup.tag('div') {
                    tag('span', [class: 'name']) { text(obj.name) }
                    tag('span', [class: 'email']) { text(obj.email) }
                }
                ctx.render(view: 'show', model: [html: markup.render()])
            } else {
                ctx.render(view: 'edit', model: [errors: obj.errors])
            }
            bh.consume(ctx.model)
        }
    }

    /**
     * Full request cycle with metaclass churn - the worst-case
     * Grails scenario where framework initialization overlaps with
     * request handling, causing constant SwitchPoint invalidation.
     */
    @Benchmark
    void fullRequestCycleDuringMetaclassChurn(Blackhole bh) {
        for (int i = 0; i < ITERATIONS / 10; i++) {
            def ctx = new ControllerContext()
            ctx.params = [name: "User${i % 50}", email: "user${i % 50}@test.com"]

            def obj = domainService.create(ctx.params)

            if (obj.errors.isEmpty()) {
                def markup = new MarkupContext()
                markup.tag('div') {
                    tag('span') { text(obj.name) }
                }
                ctx.render(view: 'show', model: [html: markup.render()])
            } else {
                ctx.render(view: 'edit', model: [errors: obj.errors])
            }
            bh.consume(ctx.model)

            // Metaclass churn from framework components
            if (i % 100 == 0) {
                DomainObject.metaClass."grailsHelper${i % 5}" = { -> delegate.name }
            }
        }
    }
}
