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
package groovy.transform
/**
 * The {@code @Canonical} meta-annotation combines the {@code @EqualsAndHashCode},
 * {@code @ToString} and {@code @TupleConstructor} annotations. It is used to assist in
 * the creation of mutable classes. It instructs the compiler to execute AST transformations
 * which add positional constructors, equals, hashCode and a pretty print toString to your class.
 * <p>
 * You can write classes in this shortened form:
 * <pre class="groovyTestCase">
 * import groovy.transform.Canonical
 * {@code @Canonical} class Customer {
 *     String first, last
 *     int age
 *     Date since
 *     Collection favItems = ['Food']
 *     def object 
 * }
 * def d = new Date()
 * def anyObject = new Object()
 * def c1 = new Customer(first:'Tom', last:'Jones', age:21, since:d, favItems:['Books', 'Games'], object: anyObject)
 * def c2 = new Customer('Tom', 'Jones', 21, d, ['Books', 'Games'], anyObject)
 * assert c1 == c2
 * </pre>
 *
 * You don't need to provide all arguments in constructor calls. If using named parameters, any property names not
 * referenced will be given their default value (as per Java's default unless an explicit initialization constant is
 * provided when defining the property). If using a tuple constructor, parameters are supplied in the order in which
 * the properties are defined. Supplied parameters fill the tuple from the left. Any parameters missing on the right
 * are given their default value.
 * <pre>
 * def c3 = new Customer(last: 'Jones', age: 21)
 * def c4 = new Customer('Tom', 'Jones')
 *
 * assert null == c3.since
 * assert 0 == c4.age
 * assert c3.favItems == ['Food'] && c4.favItems == ['Food']
 * </pre>
 *
 * If you don't need all of the functionality of {@code @Canonical}, you can simply directly use one or more of the individual
 * annotations which {@code @Canonical} aggregates.
 * In addition, you can use {@code @Canonical} in combination with explicit use one or more of the individual annotations in
 * cases where you need to further customize the annotation attributes.
 * Any applicable annotation attributes from {@code @Canonical} missing from the explicit annotation will be merged
 * but any existing annotation attributes within the explicit annotation take precedence. So, for example in this case here:
 * <pre>
 * {@code @Canonical}(includeNames=true, excludes='c')
 * {@code @}{@link ToString}(excludes='a,b')
 * class MyClass { ... }
 * </pre>
 * The generated {@code toString} will include property names and exclude the {@code a} and {@code b} properties.
 * <p>
 * A class created using {@code @Canonical} has the following characteristics:
 * <ul>
 * <li>A no-arg constructor is provided which allows you to set properties by name using Groovy's normal bean conventions.
 * <li>Tuple-style constructors are provided which allow you to set properties in the same order as they are defined.
 * <li>Default {@code equals}, {@code hashCode} and {@code toString} methods are provided based on the property values.
 * See the GroovyDoc for the individual annotations for more details.
 * <p>
 * If you want similar functionality to what this annotation provides but also require immutability, see the
 * {@code @}{@link Immutable} annotation.
 * 
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 *
 * &#64;Canonical
 * class Building {
 *     String name
 *     int floors
 *     boolean officeSpace
 * }
 *
 * // Constructors are added.
 * def officeSpace = new Building('Initech office', 1, true)
 *
 * // toString() added.
 * assert officeSpace.toString() == 'Building(Initech office, 1, true)'
 *
 * // Default values are used if constructor
 * // arguments are not assigned.
 * def theOffice = new Building('Wernham Hogg Paper Company')
 * assert theOffice.floors == 0
 * theOffice.officeSpace = true
 *
 * def anotherOfficeSpace = new Building(name: 'Initech office', floors: 1, officeSpace: true)
 *
 * // equals() method is added.
 * assert anotherOfficeSpace == officeSpace
 *
 * // equals() and hashCode() are added, so duplicate is not in Set.
 * def offices = [officeSpace, anotherOfficeSpace, theOffice] as Set  
 * assert offices.size() == 2 
 * assert offices.name.join(',') == 'Initech office,Wernham Hogg Paper Company'
 *
 * &#64;Canonical
 * &#64;ToString(excludes='age')  // Customize one of the transformations.
 * class Person {
 *     String name
 *     int age
 * }
 *
 * def mrhaki = new Person('mrhaki', 37)
 * assert mrhaki.toString() == 'Person(mrhaki)'
 * </pre>
 *
 * @see groovy.transform.EqualsAndHashCode
 * @see groovy.transform.ToString
 * @see groovy.transform.TupleConstructor
 * @see groovy.transform.Immutable
 * @since 1.8.0
 */
@AnnotationCollector(value=[ToString, TupleConstructor, EqualsAndHashCode], mode=AnnotationCollectorMode.PREFER_EXPLICIT_MERGED)
@interface Canonical { }
