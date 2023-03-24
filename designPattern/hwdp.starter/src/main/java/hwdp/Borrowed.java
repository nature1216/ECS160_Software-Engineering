package hwdp;

// TODO HWDP P2
public class Borrowed implements LBState {

    private static final Borrowed borrowed = new Borrowed();

    @Override
    public void extend(LibraryBook lb) {
        LibraryLogger.getInstance().writeLine("Leaving State Borrowed for State Borrowed");
        lb.notifyObservers();
    }

    @Override
    public void issue(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("returnIt", "Borrowed");
    }

    @Override
    public void shelf(LibraryBook lb) throws BadOperationException {
        throw new BadOperationException("shelf", "Borrowed");
    }

    @Override
    public void returnIt(LibraryBook lb) {
        lb.changeState(GotBack.getInstance());
        LibraryLogger.getInstance().writeLine("Leaving State Borrowed for State GotBack");
        lb.notifyObservers();
    }

    public static Borrowed getInstance() {
        return borrowed;
    }
}