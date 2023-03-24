package hwdp;

// TODO HWDP P2

public class GotBack implements LBState {

    private static final GotBack gotBack = new GotBack();

    @Override
    public void extend(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("extend", "GotBack");
    }

    @Override
    public void issue(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("issue", "GotBack");
    }

    @Override
    public void shelf(LibraryBook lb) {
        lb.changeState(OnShelf.getInstance());
        LibraryLogger.getInstance().writeLine("Leaving State GotBack for State OnShelf");
        lb.notifyObservers();
    }

    @Override
    public void returnIt(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("returnIt", "GotBack");
    }

    public static GotBack getInstance() {
        return gotBack;
    }
}