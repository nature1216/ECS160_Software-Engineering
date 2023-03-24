package hwdp;
// TODO HWDP P2

public class OnShelf implements LBState {

    private static final OnShelf onShelf = new OnShelf();

    @Override
    public void extend(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("extend", "OnShelf");
    }

    @Override
    public void issue(LibraryBook lb) {
        lb.changeState(Borrowed.getInstance());
        LibraryLogger.getInstance().writeLine("Leaving State OnShelf for State Borrowed");
        lb.notifyObservers();
    }

    @Override
    public void shelf(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("shelf", "OnShelf");
    }

    @Override
    public void returnIt(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("returnIt", "OnShelf");
    }

    public static OnShelf getInstance() {
        return onShelf;
    }
}