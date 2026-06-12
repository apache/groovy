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

import groovy.lang.GroovySystem

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

import java.util.concurrent.TimeUnit

/**
 * Groovy collection and DSL patterns from the grails7-performance-regression demo app.
 *
 * @see <a href="https://github.com/jglapa/grails7-performance-regression">Demo app</a>
 * @see <a href="https://issues.apache.org/jira/browse/GROOVY-10307">GROOVY-10307</a>
 */
@Warmup(iterations = 3, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
class GrailsWorkloadBench {
    /** Number of iterations per benchmark. */
    static final int ITERATIONS = 10_000

    /**
     * Employee entity from the demo app.
     */
    static class Employee {
        /** Employee ID. */
        Long id
        /** First name. */
        String firstName
        /** Last name. */
        String lastName
        /** Email address. */
        String email
        /** Job title. */
        String jobTitle
        /** Department name. */
        String department
        /** Salary amount. */
        BigDecimal salary
        /** Active status. */
        boolean isActive
        /** Performance rating. */
        int performanceRating
        /** List of skills. */
        List<String> skills = []

        /** Returns full name. */
        String getFullName() { "$firstName $lastName" }
        /** Converts employee to map. */
        Map toMap() {
            [id: id, name: getFullName(), email: email, title: jobTitle,
             dept: department, salary: salary, active: isActive,
             rating: performanceRating, skillCount: skills.size()]
        }
    }

    /**
     * Project entity from the demo app.
     */
    static class Project {
        /** Project ID. */
        Long id
        /** Project name. */
        String name
        /** Project status. */
        String status
        /** Project budget. */
        BigDecimal budget
        /** Owning department. */
        String department
        /** Priority level. */
        int priority
        /** List of tasks. */
        List<Task> tasks = []
        /** List of milestones. */
        List<Milestone> milestones = []

        /** Converts project to map. */
        Map toMap() {
            [id: id, name: name, status: status, budget: budget,
             taskCount: tasks.size(), milestoneCount: milestones.size()]
        }
    }

    /**
     * Task entity from the demo app.
     */
    static class Task {
        /** Task ID. */
        Long id
        /** Task name. */
        String name
        /** Task status. */
        String status
        /** Priority level. */
        int priority
        /** Estimated hours. */
        int estimatedHours
        /** Assigned person. */
        String assignee

        /** Converts task to map. */
        Map toMap() { [id: id, name: name, status: status, priority: priority] }
    }

    /**
     * Milestone entity from the demo app.
     */
    static class Milestone {
        /** Milestone ID. */
        Long id
        /** Milestone name. */
        String name
        /** Completion status. */
        boolean isCompleted
        /** Converts milestone to map. */
        Map toMap() { [id: id, name: name, completed: isCompleted] }
    }

    /**
     * Unrelated type for cross-type invalidation.
     */
    static class PluginConfig {
        /** Configuration setting. */
        String setting = "default"
    }

    /** List of employee test data. */
    List<Employee> employees
    /** List of project test data. */
    List<Project> projects
    /** List of task test data. */
    List<Task> tasks
    /** Counter for varying invalidation patterns. */
    int invalidationCounter

    /** Sets up test data before each iteration. */
    @Setup(Level.Iteration)
    void setup() {
        GroovySystem.metaClassRegistry.removeMetaClass(Employee)
        GroovySystem.metaClassRegistry.removeMetaClass(Project)
        GroovySystem.metaClassRegistry.removeMetaClass(Task)
        GroovySystem.metaClassRegistry.removeMetaClass(Milestone)
        GroovySystem.metaClassRegistry.removeMetaClass(PluginConfig)

        def statuses = ['TODO', 'IN_PROGRESS', 'DONE', 'BLOCKED']
        def departments = ['Engineering', 'Marketing', 'Sales', 'Support', 'HR']
        def titles = ['Developer', 'Designer', 'Manager', 'Analyst', 'Lead']

        // Sample data matching demo app scale
        employees = (1..50).collect { i ->
            new Employee(
                id: i,
                firstName: "First$i",
                lastName: "Last$i",
                email: "user${i}@example.com",
                jobTitle: titles[i % titles.size()],
                department: departments[i % departments.size()],
                salary: 50000 + (i * 1000),
                isActive: i % 5 != 0,
                performanceRating: (i % 5) + 1,
                skills: (1..(i % 4 + 1)).collect { s -> "Skill$s" }
            )
        }

        tasks = (1..100).collect { i ->
            new Task(
                id: i,
                name: "Task$i",
                status: statuses[i % statuses.size()],
                priority: (i % 10) + 1,
                estimatedHours: (i % 8) + 1,
                assignee: "First${(i % 50) + 1}"
            )
        }

        projects = (1..20).collect { i ->
            def projectTasks = tasks.subList(
                (i - 1) * 5, Math.min(i * 5, tasks.size())
            )
            def milestones = (1..3).collect { m ->
                new Milestone(id: (i * 3) + m, name: "M${i}-${m}", isCompleted: m <= 2)
            }
            new Project(
                id: i,
                name: "Project$i",
                status: statuses[i % statuses.size()],
                budget: 100000 + (i * 50000),
                department: departments[i % departments.size()],
                priority: (i % 10) + 1,
                tasks: projectTasks,
                milestones: milestones
            )
        }
    }

    /** Baseline: findAll/collect/groupBy/collectEntries closure chains. */
    @Benchmark
    void baselineCollectionClosureChain(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def activeEmployees = employees.findAll { it.isActive }
            def mapped = activeEmployees.collect { it.toMap() }
            def byDept = mapped.groupBy { it.dept }
            def deptStats = byDept.collectEntries { dept, emps ->
                [dept, [count: emps.size(), avgRating: emps.sum { it.rating } / emps.size()]]
            }
            bh.consume(deptStats.size())
        }
    }

    /** Collection closure chains with periodic cross-type invalidation. */
    @Benchmark
    void collectionClosureChainWithInvalidation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def activeEmployees = employees.findAll { it.isActive }
            def mapped = activeEmployees.collect { it.toMap() }
            def byDept = mapped.groupBy { it.dept }
            def deptStats = byDept.collectEntries { dept, emps ->
                [dept, [count: emps.size(), avgRating: emps.sum { it.rating } / emps.size()]]
            }
            bh.consume(deptStats.size())
            if (i % 100 == 0) {
                PluginConfig.metaClass."helper${i % 5}" = { -> i }
            }
        }
    }

    /** Baseline: spread operator (employees*.salary). */
    @Benchmark
    void baselineSpreadOperator(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def names = employees*.firstName
            def salaries = employees*.salary
            def ratings = employees*.performanceRating
            bh.consume(names.size() + salaries.size() + ratings.size())
        }
    }

    /** Spread operator with periodic cross-type invalidation. */
    @Benchmark
    void spreadOperatorWithInvalidation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def names = employees*.firstName
            def salaries = employees*.salary
            def ratings = employees*.performanceRating
            bh.consume(names.size() + salaries.size() + ratings.size())
            if (i % 100 == 0) {
                PluginConfig.metaClass."helper${i % 5}" = { -> i }
            }
        }
    }

    /**
     * GORM-like criteria builder for queries.
     */
    static class CriteriaBuilder {
        /** Accumulated criteria. */
        Map result = [:]

        /** Adds equality criterion. */
        void eq(String field, Object value) {
            result[field] = value
        }

        /** Adds greater-than criterion. */
        void gt(String field, Object value) {
            result["${field}_gt"] = value
        }

        /** Adds nested criteria block. */
        void nested(String name, @DelegatesTo(CriteriaBuilder) Closure cl) {
            def inner = new CriteriaBuilder()
            cl.delegate = inner
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            result[name] = inner.result
        }

        /** Builds the criteria map. */
        Map build() { result }
    }

    /** Baseline: 3-level nested closure delegation (GORM criteria pattern). */
    @Benchmark
    void baselineNestedClosureDelegation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def builder = new CriteriaBuilder()
            builder.nested('project') {
                eq('status', 'IN_PROGRESS')
                gt('priority', 5)
                nested('department') {
                    eq('name', "Dept${i % 5}")
                    nested('company') {
                        eq('active', true)
                    }
                }
            }
            bh.consume(builder.build().size())
        }
    }

    /** Nested closure delegation with periodic cross-type invalidation. */
    @Benchmark
    void nestedClosureDelegationWithInvalidation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def builder = new CriteriaBuilder()
            builder.nested('project') {
                eq('status', 'IN_PROGRESS')
                gt('priority', 5)
                nested('department') {
                    eq('name', "Dept${i % 5}")
                    nested('company') {
                        eq('active', true)
                    }
                }
            }
            bh.consume(builder.build().size())
            if (i % 100 == 0) {
                PluginConfig.metaClass."helper${i % 5}" = { -> i }
            }
        }
    }

    /** Baseline: GString interpolation with dynamic property access. */
    @Benchmark
    void baselineGStringInterpolation(Blackhole bh) {
        int totalLen = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def emp = employees[i % employees.size()]
            String full = "${emp.firstName} ${emp.lastName}"
            String detail = "${emp.jobTitle} at ${emp.department} - \$${emp.salary}"
            String summary = "Employee #${emp.id}: ${full} (${emp.performanceRating}/5)"
            totalLen += full.length() + detail.length() + summary.length()
        }
        bh.consume(totalLen)
    }

    /** GString interpolation with periodic cross-type invalidation. */
    @Benchmark
    void gstringInterpolationWithInvalidation(Blackhole bh) {
        int totalLen = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def emp = employees[i % employees.size()]
            String full = "${emp.firstName} ${emp.lastName}"
            String detail = "${emp.jobTitle} at ${emp.department} - \$${emp.salary}"
            String summary = "Employee #${emp.id}: ${full} (${emp.performanceRating}/5)"
            totalLen += full.length() + detail.length() + summary.length()
            if (i % 100 == 0) {
                PluginConfig.metaClass."helper${i % 5}" = { -> i }
            }
        }
        bh.consume(totalLen)
    }

    /** Baseline: dynamic property access by name string. */
    @Benchmark
    void baselineDynamicPropertyByName(Blackhole bh) {
        String[] fields = ['firstName', 'lastName', 'email', 'jobTitle', 'department']
        int totalLen = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def emp = employees[i % employees.size()]
            for (int f = 0; f < fields.length; f++) {
                def val = emp."${fields[f]}"
                totalLen += val?.toString()?.length() ?: 0
            }
        }
        bh.consume(totalLen)
    }

    /** Dynamic property access with periodic cross-type invalidation. */
    @Benchmark
    void dynamicPropertyByNameWithInvalidation(Blackhole bh) {
        String[] fields = ['firstName', 'lastName', 'email', 'jobTitle', 'department']
        int totalLen = 0
        for (int i = 0; i < ITERATIONS; i++) {
            def emp = employees[i % employees.size()]
            for (int f = 0; f < fields.length; f++) {
                def val = emp."${fields[f]}"
                totalLen += val?.toString()?.length() ?: 0
            }
            if (i % 100 == 0) {
                PluginConfig.metaClass."helper${i % 5}" = { -> i }
            }
        }
        bh.consume(totalLen)
    }

    /** Baseline: project metrics aggregation (demo app's getProjectMetrics). */
    @Benchmark
    void baselineProjectMetrics(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def project = projects[i % projects.size()]
            def completedTasks = project.tasks.count { it.status == 'DONE' }
            def totalHours = project.tasks.sum { it.estimatedHours } ?: 0
            def completedMilestones = project.milestones.count { it.isCompleted }
            def completion = project.tasks.size() > 0 ?
                (completedTasks / project.tasks.size() * 100) : 0
            def metrics = [
                name: project.name,
                tasks: project.tasks.size(),
                completed: completedTasks,
                hours: totalHours,
                milestones: completedMilestones,
                completion: completion
            ]
            bh.consume(metrics.size())
        }
    }

    /** Project metrics with periodic cross-type invalidation. */
    @Benchmark
    void projectMetricsWithInvalidation(Blackhole bh) {
        for (int i = 0; i < ITERATIONS; i++) {
            def project = projects[i % projects.size()]
            def completedTasks = project.tasks.count { it.status == 'DONE' }
            def totalHours = project.tasks.sum { it.estimatedHours } ?: 0
            def completedMilestones = project.milestones.count { it.isCompleted }
            def completion = project.tasks.size() > 0 ?
                (completedTasks / project.tasks.size() * 100) : 0
            def metrics = [
                name: project.name,
                tasks: project.tasks.size(),
                completed: completedTasks,
                hours: totalHours,
                milestones: completedMilestones,
                completion: completion
            ]
            bh.consume(metrics.size())
            if (i % 100 == 0) {
                PluginConfig.metaClass."helper${i % 5}" = { -> i }
            }
        }
    }

    /** Baseline: full analysis combining all patterns (demo app's runComplexAnalysis). */
    @Benchmark
    void baselineFullAnalysis(Blackhole bh) {
        // Employee analysis
        def activeEmps = employees.findAll { it.isActive }
        def empNames = activeEmps*.getFullName()
        def byDept = activeEmps.groupBy { it.department }
        def deptSummary = byDept.collectEntries { dept, emps ->
            def avgSalary = emps.sum { it.salary } / emps.size()
            def topPerformer = emps.max { it.performanceRating }
            [dept, [count: emps.size(), avgSalary: avgSalary,
                    top: topPerformer.getFullName()]]
        }

        // Project metrics
        def projectSummary = projects.collect { proj ->
            def done = proj.tasks.count { it.status == 'DONE' }
            def blocked = proj.tasks.count { it.status == 'BLOCKED' }
            [name: proj.name, status: proj.status,
             done: done, blocked: blocked, budget: proj.budget]
        }

        // Cross-entity: high-priority tasks by department
        def highPriority = tasks.findAll { it.priority > 7 }
        def taskSummary = highPriority.groupBy { it.status }
            .collectEntries { status, tl ->
                [status, tl.collect { "${it.name} (P${it.priority})" }]
            }

        bh.consume(deptSummary.size() + projectSummary.size() +
                   taskSummary.size() + empNames.size())
    }

    /** Full analysis with cross-type invalidation before and during execution. */
    @Benchmark
    void fullAnalysisWithInvalidation(Blackhole bh) {
        // Ongoing framework metaclass activity
        PluginConfig.metaClass."preRequest${invalidationCounter++ % 3}" = { -> 'init' }

        // Employee analysis
        def activeEmps = employees.findAll { it.isActive }
        def empNames = activeEmps*.getFullName()
        def byDept = activeEmps.groupBy { it.department }
        def deptSummary = byDept.collectEntries { dept, emps ->
            def avgSalary = emps.sum { it.salary } / emps.size()
            def topPerformer = emps.max { it.performanceRating }
            [dept, [count: emps.size(), avgSalary: avgSalary,
                    top: topPerformer.getFullName()]]
        }

        // Mid-request metaclass change
        PluginConfig.metaClass."midRequest${invalidationCounter++ % 3}" = { -> 'lazy' }

        // Project metrics
        def projectSummary = projects.collect { proj ->
            def done = proj.tasks.count { it.status == 'DONE' }
            def blocked = proj.tasks.count { it.status == 'BLOCKED' }
            [name: proj.name, status: proj.status,
             done: done, blocked: blocked, budget: proj.budget]
        }

        // Cross-entity analysis
        def highPriority = tasks.findAll { it.priority > 7 }
        def taskSummary = highPriority.groupBy { it.status }
            .collectEntries { status, tl ->
                [status, tl.collect { "${it.name} (P${it.priority})" }]
            }

        bh.consume(deptSummary.size() + projectSummary.size() +
                   taskSummary.size() + empNames.size())
    }
}
