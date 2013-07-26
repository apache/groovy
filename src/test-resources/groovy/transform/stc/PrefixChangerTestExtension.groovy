import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.syntax.SyntaxException

// An simple extension that will change the prefix of type checking errors
setup {
    context.pushErrorCollector() // collect every type checking error using a dedicated error collector
}

finish {
    def ec = context.popErrorCollector()
    def co = context.errorCollector
    ec.errors.each { err ->
        if (err instanceof SyntaxErrorMessage && err.cause.message.startsWith('[Static type checking] - ')) {
            err.cause = new SyntaxException(
                    err.cause.message.replace('[Static type checking]','[Custom]'),
                    err.cause.cause,
                    err.cause.startLine,
                    err.cause.startColumn,
                    err.cause.endLine,
                    err.cause.endColumn,
            )
        }
        co.addError(err)
    }
}