package hwdp;

public class BadOperationException extends Exception {
    // TODO HWDP P2
    BadOperationException(String methodName, String currentStateName) {
       super("BadOperationException - Can't use " + methodName + " in "+ currentStateName + " state");
    }
}
