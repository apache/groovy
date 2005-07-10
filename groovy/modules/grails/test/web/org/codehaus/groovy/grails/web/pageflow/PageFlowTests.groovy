import grails.pageflow.*;

class TestPageFlow {
    @Property Flow flow = new PageFlowBuilder().flow {
        firstStage(view:"someView")
    }
}