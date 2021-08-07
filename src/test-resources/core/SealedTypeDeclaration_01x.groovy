import groovy.transform.NonSealed
import groovy.transform.Sealed

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

sealed interface ShapeI permits Circle, Rectangle { }
final class Circle implements ShapeI { }
non-sealed class Rectangle implements ShapeI { }
final class Square extends Rectangle { }

def c = new Circle()
def r = new Rectangle()
def s = new Square()
assert [c, r, s]*.class.name == ['Circle', 'Rectangle', 'Square']

def shapeIAnnotations = ShapeI.class.annotations
assert 1 == shapeIAnnotations.size()
Sealed sealedAnnotation = (Sealed) shapeIAnnotations[0]
assert [Circle.class, Rectangle.class] == sealedAnnotation.permittedSubclasses()
