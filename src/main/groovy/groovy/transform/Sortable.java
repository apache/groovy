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
package groovy.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class annotation used to make a class Comparable by (potentially) multiple Comparators.
 *
 * As an example, given this class:
 * <pre>
 * {@code @Sortable} class Person {
 *     String first
 *     String last
 *     Integer born
 * }
 * </pre>
 * The generated Groovy class will:
 * <ul>
 *     <li>implement the {@code Comparable} interface</li>
 *     <li>have a {@code compareTo} method based on the {@code first},
 *     {@code last} and {@code born} properties (priority ordering will be according
 *     to the ordering of property definition, highest first, unless 'includes' is used; in which case,
 *     priority will be according to the order given in the includes list)</li>
 *     <li>have three {@code Comparator} methods named {@code comparatorByFirst},
 *     {@code comparatorByLast} and {@code comparatorByBorn}</li>
 *     <li>sort by natural order by default, reversed natural order can be specified</li>
 * </ul>
 * The properties within the class must themselves be {@code Comparable} or {@code @Sortable}.
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * import groovy.transform.Sortable
 * import groovy.transform.ToString
 *
 * &#64;Sortable
 * &#64;ToString
 * class Course {
 *     // Order of properties determines priority when sorting
 *     String title
 *     Date beginDate
 *     Integer maxAttendees  // int doesn't implement Comparable, so use Integer
 * }
 *
 *
 * final Course groovy = new Course(
 *         title: 'Groovy', beginDate: new Date() + 7, maxAttendees: 40)
 * final Course groovy2 = new Course(
 *         title: 'Groovy', beginDate: new Date() + 2, maxAttendees: 50)
 * final Course grails = new Course(
 *         title: 'Grails', beginDate: new Date() + 1, maxAttendees: 20)
 *
 *
 * final List&lt;Course&gt; courses = [groovy, groovy2, grails]
 * assert courses.last().title == 'Grails'
 *
 * // Use toSorted() method to sort
 * final List&lt;Course&gt; sorted = courses.toSorted()
 *
 * assert sorted.first().title == 'Grails'
 * assert sorted.last().title == 'Groovy'
 * assert sorted.maxAttendees == [20, 50, 40]
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // Order of fields for includes determines priority when sorting
 * import groovy.transform.Sortable
 * import groovy.transform.ToString
 *
 * &#64;Sortable(includes = ['title', 'maxAttendees'])
 * // Or &#64;Sortable(excludes = ['beginDate'])
 * &#64;ToString
 * class Course {
 *     String title
 *     Date beginDate
 *     Integer maxAttendees
 * }
 *
 * final Course groovy = new Course(
 *         title: 'Groovy', beginDate: new Date() + 7, maxAttendees: 40)
 * final Course groovy2 = new Course(
 *         title: 'Groovy', beginDate: new Date() + 2, maxAttendees: 50)
 * final Course grails = new Course(
 *         title: 'Grails', beginDate: new Date() + 1, maxAttendees: 20)
 *
 *
 * final List&lt;Course&gt; courses = [groovy, groovy2, grails]
 *
 * // Use toSorted() method to sort
 * final List&lt;Course&gt; sorted = courses.toSorted()
 *
 * assert sorted.first().title == 'Grails'
 * assert sorted.last().title == 'Groovy'
 * assert sorted.maxAttendees == [20, 40, 50]
 *
 * //--------------------------------------------------------------------------
 * // Static methods to create comparators.
 * final Comparator byMaxAttendees = Course.comparatorByMaxAttendees()
 * final List&lt;Course&gt; sortedByMaxAttendees = courses.sort(false, byMaxAttendees)
 *
 * assert sortedByMaxAttendees.maxAttendees == [20, 40, 50]
 * // beginDate is not used for sorting
 * assert sortedByMaxAttendees[2].beginDate &lt; sortedByMaxAttendees[1].beginDate
 *
 * assert Course.declaredMethods.name.findAll { it.startsWith('comparatorBy') }.toSorted() == ['comparatorByMaxAttendees', 'comparatorByTitle']
 *
 * //--------------------------------------------------------------------------
 * // Sorting by max attendees using reversed order
 * import groovy.transform.Sortable
 * import groovy.transform.ToString
 *
 * &#64;Sortable(includes = ['points'], reversed = true)
 * &#64;ToString
 * class LeaderBoardEntry {
 *     String team
 *     int points
 * }
 *
 *
 * final LeaderBoardEntry teamA = new LeaderBoardEntry(team: "Team A", points: 30)
 * final LeaderBoardEntry teamB = new LeaderBoardEntry(team: "Team B", points: 80)
 * final LeaderBoardEntry teamC = new LeaderBoardEntry(team: "Team C", points: 50)
 *
 * final List&lt;LeaderBoardEntry&gt; leaderBoard = [teamA, teamB, teamC].toSorted()
 *
 * assert leaderBoard.first().team == 'Team B'
 * assert leaderBoard.last().team == 'Team A'
 * assert leaderBoard.points == [80, 50, 30]
 *
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.SortableASTTransformation")
public @interface Sortable {
    /**
     * Property names to include in the comparison algorithm.
     * Must not be used if 'excludes' is used.
     * The default value is a special marker value indicating that no includes are defined; all properties
     * are included if 'includes' remains undefined and 'excludes' is explicitly or implicitly an empty list.
     */
    String[] includes() default {Undefined.STRING};

    /**
     * Property names to exclude in the comparison algorithm.
     * Must not be used if 'includes' is used.
     */
    String[] excludes() default {};

    /**
     * Set to true so that comparator uses reversed natural order.
     *
     * @since 2.5.0
     */
    boolean reversed() default false;

    /**
     * Whether to include super properties in the comparison algorithm.
     * Groovy properties and potentially JavaBean properties (in that order) from superclasses come before
     * the members from a subclass (unless 'includes' is used to determine the order).
     *
     * @since 2.5.2
     */
    boolean includeSuperProperties() default false;

    /**
     * Whether to include all properties (as per the JavaBean spec) in the comparison algorithm.
     * Groovy recognizes any field-like definitions with no explicit visibility as property definitions
     * and always includes them in the comparison algorithm. Groovy also treats any explicitly created getXxx() or isYyy()
     * methods as property getters as per the JavaBean specification.
     * Set this flag to false explicitly exclude such properties.
     * JavaBean properties come after any Groovy properties for a given class
     * (unless 'includes' is used to determine the order).
     *
     * @since 2.5.2
     */
    boolean allProperties() default true;

    /**
     * Whether to include all fields and/or properties in the comparison algorithm, including those with names that
     * are considered internal.
     *
     * @since 2.5.2
     */
    boolean allNames() default false;
}
