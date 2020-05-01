package groovy.util.logging.vm9

import groovy.transform.CompileStatic

@CompileStatic
class LoggerSpyFinder extends System.LoggerFinder {
    static System.Logger spy = null

    @Override
    System.Logger getLogger(String name, Module module) {
        spy = new LoggerSpy()
    }
}