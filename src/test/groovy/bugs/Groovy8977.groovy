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
package groovy.bugs

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.DefaultImportedClassCollectorHelper
import org.codehaus.groovy.control.ResolveVisitor

// The test should be run in a clean environment, i.e. no other tests run together
class Groovy8977 extends GroovyTestCase {
    private Map<String, Set<String>> originalCacheContent = new HashMap<>()
    private boolean originalInited = false

    void setUp() {
        ensureClassGraphLoaded()

        originalCacheContent = ResolveVisitor.DefaultImportsCache.DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.clearAll()
        originalInited = ResolveVisitor.DefaultImportsCache.inited
    }

    void tearDown() throws Exception {
        resetCache()

        ResolveVisitor.DefaultImportsCache.DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.putAll(originalCacheContent)
        ResolveVisitor.DefaultImportsCache.inited = originalInited
    }

    void test() {
        // warmup begin
        long timeElasped1 = runTestWithDefaultOptions()
        long timeElasped2 = runTestWithOptimizedOptions()

        println "0: $timeElasped1, $timeElasped2; ${ (timeElasped1 - timeElasped2) * 100D / timeElasped1 }%"
        assert timeElasped2 < timeElasped1, "0: less time should be cost when `collectDefaultImports` enabled, but $timeElasped2 is not less than $timeElasped1"
        // warmup end

        timeElasped1 = runTestWithDefaultOptions()
        timeElasped2 = runTestWithOptimizedOptions()
        println "1: $timeElasped1, $timeElasped2; ${ (timeElasped1 - timeElasped2) * 100D / timeElasped1 }%"
        assert timeElasped2 < timeElasped1, "1: less time should be cost when `collectDefaultImports` enabled, but $timeElasped2 is not less than $timeElasped1"

        timeElasped1 = runTestWithDefaultOptions()
        timeElasped2 = runTestWithOptimizedOptions()
        println "2: $timeElasped1, $timeElasped2; ${ (timeElasped1 - timeElasped2) * 100D / timeElasped1 }%"
        assert timeElasped2 < timeElasped1, "2: less time should be cost when `collectDefaultImports` enabled, but $timeElasped2 is not less than $timeElasped1"

        // change the order
        timeElasped2 = runTestWithOptimizedOptions()
        timeElasped1 = runTestWithDefaultOptions()
        println "3: $timeElasped1, $timeElasped2; ${ (timeElasped1 - timeElasped2) * 100D / timeElasped1 }%"
        assert timeElasped2 < timeElasped1, "3: less time should be cost when `collectDefaultImports` enabled, but $timeElasped2 is not less than $timeElasped1"

        // change the order
        timeElasped2 = runTestWithOptimizedOptions()
        timeElasped1 = runTestWithDefaultOptions()
        println "4: $timeElasped1, $timeElasped2; ${ (timeElasped1 - timeElasped2) * 100D / timeElasped1 }%"
        assert timeElasped2 < timeElasped1, "4: less time should be cost when `collectDefaultImports` enabled, but $timeElasped2 is not less than $timeElasped1"

    }

    private static void ensureClassGraphLoaded() {
        assert null != DefaultImportedClassCollectorHelper.getClassNameToPackageMap()
    }

    private static long runTestWithOptimizedOptions() {
        resetCache()

        def configuration = new CompilerConfiguration()
        configuration.optimizationOptions = [collectDefaultImports: true]

        long b = System.currentTimeMillis()
        new GroovyShell(configuration).evaluate TEST_DEFAULT_IMPORTS
        long e = System.currentTimeMillis()
        long timeElasped = e - b
        return timeElasped
    }

    private static long runTestWithDefaultOptions() {
        resetCache()

        long b = System.currentTimeMillis()
        new GroovyShell().evaluate TEST_DEFAULT_IMPORTS
        long e = System.currentTimeMillis()
        long timeElasped = e - b

        return timeElasped
    }

    private static void resetCache() {
        ResolveVisitor.DefaultImportsCache.inited = false
        ResolveVisitor.DefaultImportsCache.DEFAULT_IMPORT_CLASS_AND_PACKAGES_CACHE.clear()
    }

    private static final String TEST_DEFAULT_IMPORTS = '''
assert Object.class == java.lang.Object.class;
assert AdaptingMetaClass.class == groovy.lang.AdaptingMetaClass.class;
assert BenchmarkInterceptor.class == groovy.lang.BenchmarkInterceptor.class;
assert Binding.class == groovy.lang.Binding.class;
assert Buildable.class == groovy.lang.Buildable.class;
assert Category.class == groovy.lang.Category.class;
assert Closure.class == groovy.lang.Closure.class;
assert ClosureException.class == groovy.lang.ClosureException.class;
assert ClosureInvokingMethod.class == groovy.lang.ClosureInvokingMethod.class;
assert Delegate.class == groovy.lang.Delegate.class;
assert DelegatesTo.class == groovy.lang.DelegatesTo.class;
assert DelegatesTo.Target.class == groovy.lang.DelegatesTo.Target.class;
assert DelegatingMetaClass.class == groovy.lang.DelegatingMetaClass.class;
assert DeprecationException.class == groovy.lang.DeprecationException.class;
assert EmptyRange.class == groovy.lang.EmptyRange.class;
assert ExpandoMetaClass.class == groovy.lang.ExpandoMetaClass.class;
assert ExpandoMetaClass.ExpandoMetaConstructor.class == groovy.lang.ExpandoMetaClass.ExpandoMetaConstructor.class;
assert ExpandoMetaClass.ExpandoMetaProperty.class == groovy.lang.ExpandoMetaClass.ExpandoMetaProperty.class;
assert ExpandoMetaClassCreationHandle.class == groovy.lang.ExpandoMetaClassCreationHandle.class;
assert GString.class == groovy.lang.GString.class;
assert GeneratedGroovyProxy.class == groovy.lang.GeneratedGroovyProxy.class;
assert Grab.class == groovy.lang.Grab.class;
assert GrabConfig.class == groovy.lang.GrabConfig.class;
assert GrabExclude.class == groovy.lang.GrabExclude.class;
assert GrabResolver.class == groovy.lang.GrabResolver.class;
assert Grapes.class == groovy.lang.Grapes.class;
assert GroovyCallable.class == groovy.lang.GroovyCallable.class;
assert GroovyClassLoader.class == groovy.lang.GroovyClassLoader.class;
assert GroovyClassLoader.ClassCollector.class == groovy.lang.GroovyClassLoader.ClassCollector.class;
assert GroovyClassLoader.InnerLoader.class == groovy.lang.GroovyClassLoader.InnerLoader.class;
assert GroovyCodeSource.class == groovy.lang.GroovyCodeSource.class;
assert GroovyInterceptable.class == groovy.lang.GroovyInterceptable.class;
assert GroovyObject.class == groovy.lang.GroovyObject.class;
assert GroovyObjectSupport.class == groovy.lang.GroovyObjectSupport.class;
assert GroovyResourceLoader.class == groovy.lang.GroovyResourceLoader.class;
assert GroovyRuntimeException.class == groovy.lang.GroovyRuntimeException.class;
assert GroovyShell.class == groovy.lang.GroovyShell.class;
assert GroovySystem.class == groovy.lang.GroovySystem.class;
assert Groovydoc.class == groovy.lang.Groovydoc.class;
assert IllegalPropertyAccessException.class == groovy.lang.IllegalPropertyAccessException.class;
assert IncorrectClosureArgumentsException.class == groovy.lang.IncorrectClosureArgumentsException.class;
assert IntRange.class == groovy.lang.IntRange.class;
assert Interceptor.class == groovy.lang.Interceptor.class;
assert Lazy.class == groovy.lang.Lazy.class;
assert ListWithDefault.class == groovy.lang.ListWithDefault.class;
assert MapWithDefault.class == groovy.lang.MapWithDefault.class;
assert MetaArrayLengthProperty.class == groovy.lang.MetaArrayLengthProperty.class;
assert MetaBeanProperty.class == groovy.lang.MetaBeanProperty.class;
assert MetaClass.class == groovy.lang.MetaClass.class;
assert MetaClassImpl.class == groovy.lang.MetaClassImpl.class;
assert MetaClassImpl.Index.class == groovy.lang.MetaClassImpl.Index.class;
assert MetaClassImpl.MetaConstructor.class == groovy.lang.MetaClassImpl.MetaConstructor.class;
assert MetaClassRegistry.class == groovy.lang.MetaClassRegistry.class;
assert MetaClassRegistry.MetaClassCreationHandle.class == groovy.lang.MetaClassRegistry.MetaClassCreationHandle.class;
assert MetaClassRegistryChangeEvent.class == groovy.lang.MetaClassRegistryChangeEvent.class;
assert MetaClassRegistryChangeEventListener.class == groovy.lang.MetaClassRegistryChangeEventListener.class;
assert MetaExpandoProperty.class == groovy.lang.MetaExpandoProperty.class;
assert MetaMethod.class == groovy.lang.MetaMethod.class;
assert MetaObjectProtocol.class == groovy.lang.MetaObjectProtocol.class;
assert MetaProperty.class == groovy.lang.MetaProperty.class;
assert MissingClassException.class == groovy.lang.MissingClassException.class;
assert MissingFieldException.class == groovy.lang.MissingFieldException.class;
assert MissingMethodException.class == groovy.lang.MissingMethodException.class;
assert MissingPropertyException.class == groovy.lang.MissingPropertyException.class;
assert Mixin.class == groovy.lang.Mixin.class;
assert MutableMetaClass.class == groovy.lang.MutableMetaClass.class;
assert Newify.class == groovy.lang.Newify.class;
assert NonEmptySequence.class == groovy.lang.NonEmptySequence.class;
assert NumberRange.class == groovy.lang.NumberRange.class;
assert ObjectRange.class == groovy.lang.ObjectRange.class;
assert ParameterArray.class == groovy.lang.ParameterArray.class;
assert PropertyAccessInterceptor.class == groovy.lang.PropertyAccessInterceptor.class;
assert PropertyValue.class == groovy.lang.PropertyValue.class;
assert ProxyMetaClass.class == groovy.lang.ProxyMetaClass.class;
assert Range.class == groovy.lang.Range.class;
assert ReadOnlyPropertyException.class == groovy.lang.ReadOnlyPropertyException.class;
assert Reference.class == groovy.lang.Reference.class;
assert Script.class == groovy.lang.Script.class;
assert Sequence.class == groovy.lang.Sequence.class;
assert Singleton.class == groovy.lang.Singleton.class;
assert SpreadListEvaluatingException.class == groovy.lang.SpreadListEvaluatingException.class;
assert SpreadMap.class == groovy.lang.SpreadMap.class;
assert SpreadMapEvaluatingException.class == groovy.lang.SpreadMapEvaluatingException.class;
assert StringWriterIOException.class == groovy.lang.StringWriterIOException.class;
assert TracingInterceptor.class == groovy.lang.TracingInterceptor.class;
assert Tuple.class == groovy.lang.Tuple.class;
assert Tuple0.class == groovy.lang.Tuple0.class;
assert Tuple1.class == groovy.lang.Tuple1.class;
assert Tuple10.class == groovy.lang.Tuple10.class;
assert Tuple11.class == groovy.lang.Tuple11.class;
assert Tuple12.class == groovy.lang.Tuple12.class;
assert Tuple13.class == groovy.lang.Tuple13.class;
assert Tuple14.class == groovy.lang.Tuple14.class;
assert Tuple15.class == groovy.lang.Tuple15.class;
assert Tuple16.class == groovy.lang.Tuple16.class;
assert Tuple2.class == groovy.lang.Tuple2.class;
assert Tuple3.class == groovy.lang.Tuple3.class;
assert Tuple4.class == groovy.lang.Tuple4.class;
assert Tuple5.class == groovy.lang.Tuple5.class;
assert Tuple6.class == groovy.lang.Tuple6.class;
assert Tuple7.class == groovy.lang.Tuple7.class;
assert Tuple8.class == groovy.lang.Tuple8.class;
assert Tuple9.class == groovy.lang.Tuple9.class;
assert Writable.class == groovy.lang.Writable.class;
assert AbstractFactory.class == groovy.util.AbstractFactory.class;
assert BufferedIterator.class == groovy.util.BufferedIterator.class;
assert BuilderSupport.class == groovy.util.BuilderSupport.class;
assert CharsetToolkit.class == groovy.util.CharsetToolkit.class;
assert ClosureComparator.class == groovy.util.ClosureComparator.class;
assert ConfigBinding.class == groovy.util.ConfigBinding.class;
assert ConfigObject.class == groovy.util.ConfigObject.class;
assert ConfigSlurper.class == groovy.util.ConfigSlurper.class;
assert DelegatingScript.class == groovy.util.DelegatingScript.class;
assert Eval.class == groovy.util.Eval.class;
assert Expando.class == groovy.util.Expando.class;
assert Factory.class == groovy.util.Factory.class;
assert FactoryBuilderSupport.class == groovy.util.FactoryBuilderSupport.class;
assert FileNameByRegexFinder.class == groovy.util.FileNameByRegexFinder.class;
assert FileTreeBuilder.class == groovy.util.FileTreeBuilder.class;
assert GroovyCollections.class == groovy.util.GroovyCollections.class;
assert GroovyScriptEngine.class == groovy.util.GroovyScriptEngine.class;
assert IFileNameFinder.class == groovy.util.IFileNameFinder.class;
assert IndentPrinter.class == groovy.util.IndentPrinter.class;
assert MapEntry.class == groovy.util.MapEntry.class;
assert Node.class == groovy.util.Node.class;
assert NodeBuilder.class == groovy.util.NodeBuilder.class;
assert NodeList.class == groovy.util.NodeList.class;
assert NodePrinter.class == groovy.util.NodePrinter.class;
assert ObjectGraphBuilder.class == groovy.util.ObjectGraphBuilder.class;
assert ObjectGraphBuilder.ChildPropertySetter.class == groovy.util.ObjectGraphBuilder.ChildPropertySetter.class;
assert ObjectGraphBuilder.ClassNameResolver.class == groovy.util.ObjectGraphBuilder.ClassNameResolver.class;
assert ObjectGraphBuilder.DefaultChildPropertySetter.class == groovy.util.ObjectGraphBuilder.DefaultChildPropertySetter.class;
assert ObjectGraphBuilder.DefaultClassNameResolver.class == groovy.util.ObjectGraphBuilder.DefaultClassNameResolver.class;
assert ObjectGraphBuilder.DefaultIdentifierResolver.class == groovy.util.ObjectGraphBuilder.DefaultIdentifierResolver.class;
assert ObjectGraphBuilder.DefaultNewInstanceResolver.class == groovy.util.ObjectGraphBuilder.DefaultNewInstanceResolver.class;
assert ObjectGraphBuilder.DefaultReferenceResolver.class == groovy.util.ObjectGraphBuilder.DefaultReferenceResolver.class;
assert ObjectGraphBuilder.DefaultRelationNameResolver.class == groovy.util.ObjectGraphBuilder.DefaultRelationNameResolver.class;
assert ObjectGraphBuilder.IdentifierResolver.class == groovy.util.ObjectGraphBuilder.IdentifierResolver.class;
assert ObjectGraphBuilder.NewInstanceResolver.class == groovy.util.ObjectGraphBuilder.NewInstanceResolver.class;
assert ObjectGraphBuilder.ReferenceResolver.class == groovy.util.ObjectGraphBuilder.ReferenceResolver.class;
assert ObjectGraphBuilder.ReflectionClassNameResolver.class == groovy.util.ObjectGraphBuilder.ReflectionClassNameResolver.class;
assert ObjectGraphBuilder.RelationNameResolver.class == groovy.util.ObjectGraphBuilder.RelationNameResolver.class;
assert ObservableList.class == groovy.util.ObservableList.class;
assert ObservableList.ChangeType.class == groovy.util.ObservableList.ChangeType.class;
assert ObservableList.ElementAddedEvent.class == groovy.util.ObservableList.ElementAddedEvent.class;
assert ObservableList.ElementClearedEvent.class == groovy.util.ObservableList.ElementClearedEvent.class;
assert ObservableList.ElementEvent.class == groovy.util.ObservableList.ElementEvent.class;
assert ObservableList.ElementRemovedEvent.class == groovy.util.ObservableList.ElementRemovedEvent.class;
assert ObservableList.ElementUpdatedEvent.class == groovy.util.ObservableList.ElementUpdatedEvent.class;
assert ObservableList.MultiElementAddedEvent.class == groovy.util.ObservableList.MultiElementAddedEvent.class;
assert ObservableList.MultiElementRemovedEvent.class == groovy.util.ObservableList.MultiElementRemovedEvent.class;
assert ObservableList.ObservableIterator.class == groovy.util.ObservableList.ObservableIterator.class;
assert ObservableList.ObservableListIterator.class == groovy.util.ObservableList.ObservableListIterator.class;
assert ObservableMap.class == groovy.util.ObservableMap.class;
assert ObservableMap.ChangeType.class == groovy.util.ObservableMap.ChangeType.class;
assert ObservableMap.MultiPropertyEvent.class == groovy.util.ObservableMap.MultiPropertyEvent.class;
assert ObservableMap.PropertyAddedEvent.class == groovy.util.ObservableMap.PropertyAddedEvent.class;
assert ObservableMap.PropertyClearedEvent.class == groovy.util.ObservableMap.PropertyClearedEvent.class;
assert ObservableMap.PropertyEvent.class == groovy.util.ObservableMap.PropertyEvent.class;
assert ObservableMap.PropertyRemovedEvent.class == groovy.util.ObservableMap.PropertyRemovedEvent.class;
assert ObservableMap.PropertyUpdatedEvent.class == groovy.util.ObservableMap.PropertyUpdatedEvent.class;
assert ObservableSet.class == groovy.util.ObservableSet.class;
assert ObservableSet.ChangeType.class == groovy.util.ObservableSet.ChangeType.class;
assert ObservableSet.ElementAddedEvent.class == groovy.util.ObservableSet.ElementAddedEvent.class;
assert ObservableSet.ElementClearedEvent.class == groovy.util.ObservableSet.ElementClearedEvent.class;
assert ObservableSet.ElementEvent.class == groovy.util.ObservableSet.ElementEvent.class;
assert ObservableSet.ElementRemovedEvent.class == groovy.util.ObservableSet.ElementRemovedEvent.class;
assert ObservableSet.MultiElementAddedEvent.class == groovy.util.ObservableSet.MultiElementAddedEvent.class;
assert ObservableSet.MultiElementRemovedEvent.class == groovy.util.ObservableSet.MultiElementRemovedEvent.class;
assert ObservableSet.ObservableIterator.class == groovy.util.ObservableSet.ObservableIterator.class;
assert OrderBy.class == groovy.util.OrderBy.class;
assert PermutationGenerator.class == groovy.util.PermutationGenerator.class;
assert Proxy.class == java.net.Proxy.class;
assert ProxyGenerator.class == groovy.util.ProxyGenerator.class;
assert ResourceConnector.class == groovy.util.ResourceConnector.class;
assert ResourceException.class == groovy.util.ResourceException.class;
assert ScriptException.class == groovy.util.ScriptException.class;
assert BufferedInputStream.class == java.io.BufferedInputStream.class;
assert BufferedOutputStream.class == java.io.BufferedOutputStream.class;
assert BufferedReader.class == java.io.BufferedReader.class;
assert BufferedWriter.class == java.io.BufferedWriter.class;
assert ByteArrayInputStream.class == java.io.ByteArrayInputStream.class;
assert ByteArrayOutputStream.class == java.io.ByteArrayOutputStream.class;
assert CharArrayReader.class == java.io.CharArrayReader.class;
assert CharArrayWriter.class == java.io.CharArrayWriter.class;
assert CharConversionException.class == java.io.CharConversionException.class;
assert Closeable.class == java.io.Closeable.class;
assert Console.class == java.io.Console.class;
assert DataInput.class == java.io.DataInput.class;
assert DataInputStream.class == java.io.DataInputStream.class;
assert DataOutput.class == java.io.DataOutput.class;
assert DataOutputStream.class == java.io.DataOutputStream.class;
assert EOFException.class == java.io.EOFException.class;
assert Externalizable.class == java.io.Externalizable.class;
assert File.class == java.io.File.class;
assert FileDescriptor.class == java.io.FileDescriptor.class;
assert FileFilter.class == java.io.FileFilter.class;
assert FileInputStream.class == java.io.FileInputStream.class;
assert FileNotFoundException.class == java.io.FileNotFoundException.class;
assert FileOutputStream.class == java.io.FileOutputStream.class;
assert FilePermission.class == java.io.FilePermission.class;
assert FileReader.class == java.io.FileReader.class;
assert FileWriter.class == java.io.FileWriter.class;
assert FilenameFilter.class == java.io.FilenameFilter.class;
assert FilterInputStream.class == java.io.FilterInputStream.class;
assert FilterOutputStream.class == java.io.FilterOutputStream.class;
assert FilterReader.class == java.io.FilterReader.class;
assert FilterWriter.class == java.io.FilterWriter.class;
assert Flushable.class == java.io.Flushable.class;
assert IOError.class == java.io.IOError.class;
assert IOException.class == java.io.IOException.class;
assert InputStream.class == java.io.InputStream.class;
assert InputStreamReader.class == java.io.InputStreamReader.class;
assert InterruptedIOException.class == java.io.InterruptedIOException.class;
assert InvalidClassException.class == java.io.InvalidClassException.class;
assert InvalidObjectException.class == java.io.InvalidObjectException.class;
assert LineNumberInputStream.class == java.io.LineNumberInputStream.class;
assert LineNumberReader.class == java.io.LineNumberReader.class;
assert NotActiveException.class == java.io.NotActiveException.class;
assert NotSerializableException.class == java.io.NotSerializableException.class;
assert ObjectInput.class == java.io.ObjectInput.class;
assert ObjectInputStream.class == java.io.ObjectInputStream.class;
assert ObjectInputStream.GetField.class == java.io.ObjectInputStream.GetField.class;
assert ObjectInputValidation.class == java.io.ObjectInputValidation.class;
assert ObjectOutput.class == java.io.ObjectOutput.class;
assert ObjectOutputStream.class == java.io.ObjectOutputStream.class;
assert ObjectOutputStream.PutField.class == java.io.ObjectOutputStream.PutField.class;
assert ObjectStreamClass.class == java.io.ObjectStreamClass.class;
assert ObjectStreamConstants.class == java.io.ObjectStreamConstants.class;
assert ObjectStreamException.class == java.io.ObjectStreamException.class;
assert ObjectStreamField.class == java.io.ObjectStreamField.class;
assert OptionalDataException.class == java.io.OptionalDataException.class;
assert OutputStream.class == java.io.OutputStream.class;
assert OutputStreamWriter.class == java.io.OutputStreamWriter.class;
assert PipedInputStream.class == java.io.PipedInputStream.class;
assert PipedOutputStream.class == java.io.PipedOutputStream.class;
assert PipedReader.class == java.io.PipedReader.class;
assert PipedWriter.class == java.io.PipedWriter.class;
assert PrintStream.class == java.io.PrintStream.class;
assert PrintWriter.class == java.io.PrintWriter.class;
assert PushbackInputStream.class == java.io.PushbackInputStream.class;
assert PushbackReader.class == java.io.PushbackReader.class;
assert RandomAccessFile.class == java.io.RandomAccessFile.class;
assert Reader.class == java.io.Reader.class;
assert SequenceInputStream.class == java.io.SequenceInputStream.class;
assert Serializable.class == java.io.Serializable.class;
assert SerializablePermission.class == java.io.SerializablePermission.class;
assert StreamCorruptedException.class == java.io.StreamCorruptedException.class;
assert StreamTokenizer.class == java.io.StreamTokenizer.class;
assert StringBufferInputStream.class == java.io.StringBufferInputStream.class;
assert StringReader.class == java.io.StringReader.class;
assert StringWriter.class == java.io.StringWriter.class;
assert SyncFailedException.class == java.io.SyncFailedException.class;
assert UTFDataFormatException.class == java.io.UTFDataFormatException.class;
assert UncheckedIOException.class == java.io.UncheckedIOException.class;
assert UnsupportedEncodingException.class == java.io.UnsupportedEncodingException.class;
assert WriteAbortedException.class == java.io.WriteAbortedException.class;
assert Writer.class == java.io.Writer.class;
assert AbstractMethodError.class == java.lang.AbstractMethodError.class;
assert Appendable.class == java.lang.Appendable.class;
assert ArithmeticException.class == java.lang.ArithmeticException.class;
assert ArrayIndexOutOfBoundsException.class == java.lang.ArrayIndexOutOfBoundsException.class;
assert ArrayStoreException.class == java.lang.ArrayStoreException.class;
assert AssertionError.class == java.lang.AssertionError.class;
assert AutoCloseable.class == java.lang.AutoCloseable.class;
assert Boolean.class == java.lang.Boolean.class;
assert BootstrapMethodError.class == java.lang.BootstrapMethodError.class;
assert Byte.class == java.lang.Byte.class;
assert CharSequence.class == java.lang.CharSequence.class;
assert Character.class == java.lang.Character.class;
assert Character.Subset.class == java.lang.Character.Subset.class;
assert Character.UnicodeBlock.class == java.lang.Character.UnicodeBlock.class;
assert Character.UnicodeScript.class == java.lang.Character.UnicodeScript.class;
assert Class.class == java.lang.Class.class;
assert ClassCastException.class == java.lang.ClassCastException.class;
assert ClassCircularityError.class == java.lang.ClassCircularityError.class;
assert ClassFormatError.class == java.lang.ClassFormatError.class;
assert ClassLoader.class == java.lang.ClassLoader.class;
assert ClassNotFoundException.class == java.lang.ClassNotFoundException.class;
assert ClassValue.class == java.lang.ClassValue.class;
assert CloneNotSupportedException.class == java.lang.CloneNotSupportedException.class;
assert Cloneable.class == java.lang.Cloneable.class;
assert Comparable.class == java.lang.Comparable.class;
assert Compiler.class == java.lang.Compiler.class;
assert Deprecated.class == java.lang.Deprecated.class;
assert Double.class == java.lang.Double.class;
assert Enum.class == java.lang.Enum.class;
assert EnumConstantNotPresentException.class == java.lang.EnumConstantNotPresentException.class;
assert Error.class == java.lang.Error.class;
assert Exception.class == java.lang.Exception.class;
assert ExceptionInInitializerError.class == java.lang.ExceptionInInitializerError.class;
assert Float.class == java.lang.Float.class;
assert FunctionalInterface.class == java.lang.FunctionalInterface.class;
assert IllegalAccessError.class == java.lang.IllegalAccessError.class;
assert IllegalAccessException.class == java.lang.IllegalAccessException.class;
assert IllegalArgumentException.class == java.lang.IllegalArgumentException.class;
assert IllegalMonitorStateException.class == java.lang.IllegalMonitorStateException.class;
assert IllegalStateException.class == java.lang.IllegalStateException.class;
assert IllegalThreadStateException.class == java.lang.IllegalThreadStateException.class;
assert IncompatibleClassChangeError.class == java.lang.IncompatibleClassChangeError.class;
assert IndexOutOfBoundsException.class == java.lang.IndexOutOfBoundsException.class;
assert InheritableThreadLocal.class == java.lang.InheritableThreadLocal.class;
assert InstantiationError.class == java.lang.InstantiationError.class;
assert InstantiationException.class == java.lang.InstantiationException.class;
assert Integer.class == java.lang.Integer.class;
assert InternalError.class == java.lang.InternalError.class;
assert InterruptedException.class == java.lang.InterruptedException.class;
assert Iterable.class == java.lang.Iterable.class;
assert LinkageError.class == java.lang.LinkageError.class;
assert Long.class == java.lang.Long.class;
assert Math.class == java.lang.Math.class;
assert NegativeArraySizeException.class == java.lang.NegativeArraySizeException.class;
assert NoClassDefFoundError.class == java.lang.NoClassDefFoundError.class;
assert NoSuchFieldError.class == java.lang.NoSuchFieldError.class;
assert NoSuchFieldException.class == java.lang.NoSuchFieldException.class;
assert NoSuchMethodError.class == java.lang.NoSuchMethodError.class;
assert NoSuchMethodException.class == java.lang.NoSuchMethodException.class;
assert NullPointerException.class == java.lang.NullPointerException.class;
assert Number.class == java.lang.Number.class;
assert NumberFormatException.class == java.lang.NumberFormatException.class;
assert OutOfMemoryError.class == java.lang.OutOfMemoryError.class;
assert Override.class == java.lang.Override.class;
assert Package.class == java.lang.Package.class;
assert Process.class == java.lang.Process.class;
assert ProcessBuilder.class == java.lang.ProcessBuilder.class;
assert ProcessBuilder.Redirect.class == java.lang.ProcessBuilder.Redirect.class;
assert ProcessBuilder.Redirect.Type.class == java.lang.ProcessBuilder.Redirect.Type.class;
assert Readable.class == java.lang.Readable.class;
assert ReflectiveOperationException.class == java.lang.ReflectiveOperationException.class;
assert Runnable.class == java.lang.Runnable.class;
assert Runtime.class == java.lang.Runtime.class;
assert RuntimeException.class == java.lang.RuntimeException.class;
assert RuntimePermission.class == java.lang.RuntimePermission.class;
assert SafeVarargs.class == java.lang.SafeVarargs.class;
assert SecurityException.class == java.lang.SecurityException.class;
assert SecurityManager.class == java.lang.SecurityManager.class;
assert Short.class == java.lang.Short.class;
assert StackOverflowError.class == java.lang.StackOverflowError.class;
assert StackTraceElement.class == java.lang.StackTraceElement.class;
assert StrictMath.class == java.lang.StrictMath.class;
assert String.class == java.lang.String.class;
assert StringBuffer.class == java.lang.StringBuffer.class;
assert StringBuilder.class == java.lang.StringBuilder.class;
assert StringIndexOutOfBoundsException.class == java.lang.StringIndexOutOfBoundsException.class;
assert SuppressWarnings.class == java.lang.SuppressWarnings.class;
assert System.class == java.lang.System.class;
assert Thread.class == java.lang.Thread.class;
assert Thread.State.class == java.lang.Thread.State.class;
assert Thread.UncaughtExceptionHandler.class == java.lang.Thread.UncaughtExceptionHandler.class;
assert ThreadDeath.class == java.lang.ThreadDeath.class;
assert ThreadGroup.class == java.lang.ThreadGroup.class;
assert ThreadLocal.class == java.lang.ThreadLocal.class;
assert Throwable.class == java.lang.Throwable.class;
assert TypeNotPresentException.class == java.lang.TypeNotPresentException.class;
assert UnknownError.class == java.lang.UnknownError.class;
assert UnsatisfiedLinkError.class == java.lang.UnsatisfiedLinkError.class;
assert UnsupportedClassVersionError.class == java.lang.UnsupportedClassVersionError.class;
assert UnsupportedOperationException.class == java.lang.UnsupportedOperationException.class;
assert VerifyError.class == java.lang.VerifyError.class;
assert VirtualMachineError.class == java.lang.VirtualMachineError.class;
assert Void.class == java.lang.Void.class;
assert Authenticator.class == java.net.Authenticator.class;
assert Authenticator.RequestorType.class == java.net.Authenticator.RequestorType.class;
assert BindException.class == java.net.BindException.class;
assert CacheRequest.class == java.net.CacheRequest.class;
assert CacheResponse.class == java.net.CacheResponse.class;
assert ConnectException.class == java.net.ConnectException.class;
assert ContentHandler.class == java.net.ContentHandler.class;
assert ContentHandlerFactory.class == java.net.ContentHandlerFactory.class;
assert CookieHandler.class == java.net.CookieHandler.class;
assert CookieManager.class == java.net.CookieManager.class;
assert CookiePolicy.class == java.net.CookiePolicy.class;
assert CookieStore.class == java.net.CookieStore.class;
assert DatagramPacket.class == java.net.DatagramPacket.class;
assert DatagramSocket.class == java.net.DatagramSocket.class;
assert DatagramSocketImpl.class == java.net.DatagramSocketImpl.class;
assert DatagramSocketImplFactory.class == java.net.DatagramSocketImplFactory.class;
assert FileNameMap.class == java.net.FileNameMap.class;
assert HttpCookie.class == java.net.HttpCookie.class;
assert HttpRetryException.class == java.net.HttpRetryException.class;
assert HttpURLConnection.class == java.net.HttpURLConnection.class;
assert IDN.class == java.net.IDN.class;
assert Inet4Address.class == java.net.Inet4Address.class;
assert Inet6Address.class == java.net.Inet6Address.class;
assert InetAddress.class == java.net.InetAddress.class;
assert InetSocketAddress.class == java.net.InetSocketAddress.class;
assert InterfaceAddress.class == java.net.InterfaceAddress.class;
assert JarURLConnection.class == java.net.JarURLConnection.class;
assert MalformedURLException.class == java.net.MalformedURLException.class;
assert MulticastSocket.class == java.net.MulticastSocket.class;
assert NetPermission.class == java.net.NetPermission.class;
assert NetworkInterface.class == java.net.NetworkInterface.class;
assert NoRouteToHostException.class == java.net.NoRouteToHostException.class;
assert PasswordAuthentication.class == java.net.PasswordAuthentication.class;
assert PortUnreachableException.class == java.net.PortUnreachableException.class;
assert ProtocolException.class == java.net.ProtocolException.class;
assert ProtocolFamily.class == java.net.ProtocolFamily.class;
assert Proxy.Type.class == java.net.Proxy.Type.class;
assert ProxySelector.class == java.net.ProxySelector.class;
assert ResponseCache.class == java.net.ResponseCache.class;
assert SecureCacheResponse.class == java.net.SecureCacheResponse.class;
assert ServerSocket.class == java.net.ServerSocket.class;
assert Socket.class == java.net.Socket.class;
assert SocketAddress.class == java.net.SocketAddress.class;
assert SocketException.class == java.net.SocketException.class;
assert SocketImpl.class == java.net.SocketImpl.class;
assert SocketImplFactory.class == java.net.SocketImplFactory.class;
assert SocketOption.class == java.net.SocketOption.class;
assert SocketOptions.class == java.net.SocketOptions.class;
assert SocketPermission.class == java.net.SocketPermission.class;
assert SocketTimeoutException.class == java.net.SocketTimeoutException.class;
assert StandardProtocolFamily.class == java.net.StandardProtocolFamily.class;
assert StandardSocketOptions.class == java.net.StandardSocketOptions.class;
assert URI.class == java.net.URI.class;
assert URISyntaxException.class == java.net.URISyntaxException.class;
assert URL.class == java.net.URL.class;
assert URLClassLoader.class == java.net.URLClassLoader.class;
assert URLConnection.class == java.net.URLConnection.class;
assert URLDecoder.class == java.net.URLDecoder.class;
assert URLEncoder.class == java.net.URLEncoder.class;
assert URLPermission.class == java.net.URLPermission.class;
assert URLStreamHandler.class == java.net.URLStreamHandler.class;
assert URLStreamHandlerFactory.class == java.net.URLStreamHandlerFactory.class;
assert UnknownHostException.class == java.net.UnknownHostException.class;
assert UnknownServiceException.class == java.net.UnknownServiceException.class;
assert AbstractCollection.class == java.util.AbstractCollection.class;
assert AbstractList.class == java.util.AbstractList.class;
assert AbstractMap.class == java.util.AbstractMap.class;
assert AbstractMap.SimpleEntry.class == java.util.AbstractMap.SimpleEntry.class;
assert AbstractMap.SimpleImmutableEntry.class == java.util.AbstractMap.SimpleImmutableEntry.class;
assert AbstractQueue.class == java.util.AbstractQueue.class;
assert AbstractSequentialList.class == java.util.AbstractSequentialList.class;
assert AbstractSet.class == java.util.AbstractSet.class;
assert ArrayDeque.class == java.util.ArrayDeque.class;
assert ArrayList.class == java.util.ArrayList.class;
assert Arrays.class == java.util.Arrays.class;
assert Base64.class == java.util.Base64.class;
assert Base64.Decoder.class == java.util.Base64.Decoder.class;
assert Base64.Encoder.class == java.util.Base64.Encoder.class;
assert BitSet.class == java.util.BitSet.class;
assert Calendar.class == java.util.Calendar.class;
assert Calendar.Builder.class == java.util.Calendar.Builder.class;
assert Collection.class == java.util.Collection.class;
assert Collections.class == java.util.Collections.class;
assert Comparator.class == java.util.Comparator.class;
assert ConcurrentModificationException.class == java.util.ConcurrentModificationException.class;
assert Currency.class == java.util.Currency.class;
assert Date.class == java.util.Date.class;
assert Deque.class == java.util.Deque.class;
assert Dictionary.class == java.util.Dictionary.class;
assert DoubleSummaryStatistics.class == java.util.DoubleSummaryStatistics.class;
assert DuplicateFormatFlagsException.class == java.util.DuplicateFormatFlagsException.class;
assert EmptyStackException.class == java.util.EmptyStackException.class;
assert EnumMap.class == java.util.EnumMap.class;
assert EnumSet.class == java.util.EnumSet.class;
assert Enumeration.class == java.util.Enumeration.class;
assert EventListener.class == java.util.EventListener.class;
assert EventListenerProxy.class == java.util.EventListenerProxy.class;
assert EventObject.class == java.util.EventObject.class;
assert FormatFlagsConversionMismatchException.class == java.util.FormatFlagsConversionMismatchException.class;
assert Formattable.class == java.util.Formattable.class;
assert FormattableFlags.class == java.util.FormattableFlags.class;
assert Formatter.class == java.util.Formatter.class;
assert Formatter.BigDecimalLayoutForm.class == java.util.Formatter.BigDecimalLayoutForm.class;
assert FormatterClosedException.class == java.util.FormatterClosedException.class;
assert GregorianCalendar.class == java.util.GregorianCalendar.class;
assert HashMap.class == java.util.HashMap.class;
assert HashSet.class == java.util.HashSet.class;
assert Hashtable.class == java.util.Hashtable.class;
assert IdentityHashMap.class == java.util.IdentityHashMap.class;
assert IllegalFormatCodePointException.class == java.util.IllegalFormatCodePointException.class;
assert IllegalFormatConversionException.class == java.util.IllegalFormatConversionException.class;
assert IllegalFormatException.class == java.util.IllegalFormatException.class;
assert IllegalFormatFlagsException.class == java.util.IllegalFormatFlagsException.class;
assert IllegalFormatPrecisionException.class == java.util.IllegalFormatPrecisionException.class;
assert IllegalFormatWidthException.class == java.util.IllegalFormatWidthException.class;
assert IllformedLocaleException.class == java.util.IllformedLocaleException.class;
assert InputMismatchException.class == java.util.InputMismatchException.class;
assert IntSummaryStatistics.class == java.util.IntSummaryStatistics.class;
assert InvalidPropertiesFormatException.class == java.util.InvalidPropertiesFormatException.class;
assert Iterator.class == java.util.Iterator.class;
assert LinkedHashMap.class == java.util.LinkedHashMap.class;
assert LinkedHashSet.class == java.util.LinkedHashSet.class;
assert LinkedList.class == java.util.LinkedList.class;
assert List.class == java.util.List.class;
assert ListIterator.class == java.util.ListIterator.class;
assert ListResourceBundle.class == java.util.ListResourceBundle.class;
assert Locale.class == java.util.Locale.class;
assert Locale.Builder.class == java.util.Locale.Builder.class;
assert Locale.Category.class == java.util.Locale.Category.class;
assert Locale.FilteringMode.class == java.util.Locale.FilteringMode.class;
assert Locale.LanguageRange.class == java.util.Locale.LanguageRange.class;
assert LongSummaryStatistics.class == java.util.LongSummaryStatistics.class;
assert Map.class == java.util.Map.class;
assert Map.Entry.class == java.util.Map.Entry.class;
assert MissingFormatArgumentException.class == java.util.MissingFormatArgumentException.class;
assert MissingFormatWidthException.class == java.util.MissingFormatWidthException.class;
assert MissingResourceException.class == java.util.MissingResourceException.class;
assert NavigableMap.class == java.util.NavigableMap.class;
assert NavigableSet.class == java.util.NavigableSet.class;
assert NoSuchElementException.class == java.util.NoSuchElementException.class;
assert Objects.class == java.util.Objects.class;
assert Observable.class == java.util.Observable.class;
assert Observer.class == java.util.Observer.class;
assert Optional.class == java.util.Optional.class;
assert OptionalDouble.class == java.util.OptionalDouble.class;
assert OptionalInt.class == java.util.OptionalInt.class;
assert OptionalLong.class == java.util.OptionalLong.class;
assert PrimitiveIterator.class == java.util.PrimitiveIterator.class;
assert PrimitiveIterator.OfDouble.class == java.util.PrimitiveIterator.OfDouble.class;
assert PrimitiveIterator.OfInt.class == java.util.PrimitiveIterator.OfInt.class;
assert PrimitiveIterator.OfLong.class == java.util.PrimitiveIterator.OfLong.class;
assert PriorityQueue.class == java.util.PriorityQueue.class;
assert Properties.class == java.util.Properties.class;
assert PropertyPermission.class == java.util.PropertyPermission.class;
assert PropertyResourceBundle.class == java.util.PropertyResourceBundle.class;
assert Queue.class == java.util.Queue.class;
assert Random.class == java.util.Random.class;
assert RandomAccess.class == java.util.RandomAccess.class;
assert ResourceBundle.class == java.util.ResourceBundle.class;
assert ResourceBundle.Control.class == java.util.ResourceBundle.Control.class;
assert Scanner.class == java.util.Scanner.class;
assert ServiceConfigurationError.class == java.util.ServiceConfigurationError.class;
assert ServiceLoader.class == java.util.ServiceLoader.class;
assert Set.class == java.util.Set.class;
assert SimpleTimeZone.class == java.util.SimpleTimeZone.class;
assert SortedMap.class == java.util.SortedMap.class;
assert SortedSet.class == java.util.SortedSet.class;
assert Spliterator.class == java.util.Spliterator.class;
assert Spliterator.OfDouble.class == java.util.Spliterator.OfDouble.class;
assert Spliterator.OfInt.class == java.util.Spliterator.OfInt.class;
assert Spliterator.OfLong.class == java.util.Spliterator.OfLong.class;
assert Spliterator.OfPrimitive.class == java.util.Spliterator.OfPrimitive.class;
assert Spliterators.class == java.util.Spliterators.class;
assert Spliterators.AbstractDoubleSpliterator.class == java.util.Spliterators.AbstractDoubleSpliterator.class;
assert Spliterators.AbstractIntSpliterator.class == java.util.Spliterators.AbstractIntSpliterator.class;
assert Spliterators.AbstractLongSpliterator.class == java.util.Spliterators.AbstractLongSpliterator.class;
assert Spliterators.AbstractSpliterator.class == java.util.Spliterators.AbstractSpliterator.class;
assert SplittableRandom.class == java.util.SplittableRandom.class;
assert Stack.class == java.util.Stack.class;
assert StringJoiner.class == java.util.StringJoiner.class;
assert StringTokenizer.class == java.util.StringTokenizer.class;
assert TimeZone.class == java.util.TimeZone.class;
assert Timer.class == java.util.Timer.class;
assert TimerTask.class == java.util.TimerTask.class;
assert TooManyListenersException.class == java.util.TooManyListenersException.class;
assert TreeMap.class == java.util.TreeMap.class;
assert TreeSet.class == java.util.TreeSet.class;
assert UUID.class == java.util.UUID.class;
assert UnknownFormatConversionException.class == java.util.UnknownFormatConversionException.class;
assert UnknownFormatFlagsException.class == java.util.UnknownFormatFlagsException.class;
assert Vector.class == java.util.Vector.class;
assert WeakHashMap.class == java.util.WeakHashMap.class;
        '''
}
