package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-5675:
 * Stub compiler expands generic-inner-class variable declaration incorrectly
 *
 * GROOVY-6048
 * Stub compiler expends generics for outer class while using static inner class 
 * like a non-static inner class
 * 
 * @author Guillaume Laforge
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
class BadGenericsExpansionOnInnerClassStubsTest extends StringSourcesStubTestCase  {

    Map<String, String> provideSources() {
        [
                    'AbstractProcessingQueue.groovy': '''
                    abstract class AbstractProcessingQueue<T> extends AbstractAgent {
                        protected Queue<ProcessingQueueMember<T>> items
                        private class ProcessingQueueMember<E> {}

                        interface ItemGenerator1{}
                        static <T> AbstractProcessingQueue<List<T>> createQueue1(List<Closure<T>> closures, List<AbstractProcessingQueue.ItemGenerator1> generators) {}
                        static class ItemGenerator2{}
                        static <T> AbstractProcessingQueue<List<T>> createQueue2(List<Closure<T>> closures, List<AbstractProcessingQueue.ItemGenerator2> generators) {}
                    }
                ''',
                    'AbstractAgent.java': '''
                    public abstract class AbstractAgent {}
                '''
                ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('AbstractProcessingQueue')

        assert stubSource.contains('protected java.util.Queue<AbstractProcessingQueue<T>.ProcessingQueueMember<T>> items;')
        assert !stubSource.contains('protected java.util.Queue<AbstractProcessingQueue.ProcessingQueueMember<T>> items;')
        assert stubSource.contains('public static <T> AbstractProcessingQueue<java.util.List<T>> createQueue1(java.util.List<groovy.lang.Closure<T>> closures, java.util.List<AbstractProcessingQueue.ItemGenerator1> generators)')
        assert stubSource.contains('public static <T> AbstractProcessingQueue<java.util.List<T>> createQueue2(java.util.List<groovy.lang.Closure<T>> closures, java.util.List<AbstractProcessingQueue.ItemGenerator2> generators)')
    }
}
