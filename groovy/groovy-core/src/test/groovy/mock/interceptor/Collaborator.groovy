package groovy.mock.interceptor

class Collaborator {
    def one() {
        throw new RuntimeException('Never reach here. Should have been mocked.')
    }
    def one(int arg) {
        throw new RuntimeException('Never reach here. Should have been mocked.')
    }
    def one(int one, int two) {
        throw new RuntimeException('Never reach here. Should have been mocked.')
    }
    def two() {
        throw new RuntimeException('Never reach here. Should have been mocked.')
    }
}