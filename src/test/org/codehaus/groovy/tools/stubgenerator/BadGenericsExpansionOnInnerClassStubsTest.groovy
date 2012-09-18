package org.codehaus.groovy.tools.stubgenerator

/**
 * GROOVY-5675:
 * Stub compiler expands generic-inner-class variable declaration incorrectly
 *
 * @author Guillaume Laforge
 */
class BadGenericsExpansionOnInnerClassStubsTest extends StringSourcesStubTestCase  {

    Map<String, String> provideSources() {
        [
                'AbstractProcessingQueue.groovy': '''
                    abstract class AbstractProcessingQueue<T> extends AbstractAgent {
                        protected Queue<ProcessingQueueMember<T>> items
                        private class ProcessingQueueMember<E> {}
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
    }
}
