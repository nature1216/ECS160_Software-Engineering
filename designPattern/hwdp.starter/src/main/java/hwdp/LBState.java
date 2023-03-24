package hwdp;

// TODO HWDP P2
public interface LBState {
    void extend(LibraryBook lb) throws BadOperationException;
    void issue(LibraryBook lb) throws BadOperationException;
    void shelf(LibraryBook lb) throws BadOperationException;
    void returnIt(LibraryBook lb) throws BadOperationException;
}
