package hwdp;
// TODO HWDP P2

import java.util.ArrayList;
import java.util.List;

public class LibraryBook implements Subject {
    //                   ^ uncomment for P3
    private String name; // book name
    // TODO? Are other instance variables needed for this pattern?

    private LBState currentState;

    private List<Observer> observers = new ArrayList<>();

    public LibraryBook(String n) {
        // TODO HWDP P2
        this.name = n;

        this.currentState = OnShelf.getInstance();
    }
    
    public LBState getState() {
        // TODO?
        return this.currentState;
    }
    
    public void returnIt() {
        // TODO?
        try{
            this.currentState.returnIt(this);
        } catch (BadOperationException e) {
            LibraryLogger.getInstance().writeLine(e.getMessage());
        }
    }
    
    public void shelf() {
        // TODO?
        try {
            this.currentState.shelf(this);
        } catch (BadOperationException e) {
            LibraryLogger.getInstance().writeLine(e.getMessage());
        }
    }
    
    public void extend() {
        // TODO?
        try {
            currentState.extend(this);
        } catch (BadOperationException e) {
            LibraryLogger.getInstance().writeLine(e.getMessage());
        }
    }
    
    public void issue() {
        // TODO?
        try {
            currentState.issue(this);
        } catch (BadOperationException e) {
            LibraryLogger.getInstance().writeLine(e.getMessage());
        }
    }

    void changeState(LBState nextState) {
        this.currentState = nextState;
    }
    
    @Override
    public String toString() {
        // TODO?
        return name;
    }

    @Override
    public void attach(Observer o) {
        if(observers.contains(o)) {
            LibraryLogger.getInstance().writeLine(o.toString() + " is already attached to " + toString());
        } else {
            observers.add(o);
            LibraryLogger.getInstance().writeLine(o.toString() + " is now watching " + toString());
        }
    }

    @Override
    public void detach(Observer o) {
        if(observers.isEmpty()) {
        } else {
            observers.remove(o);
            LibraryLogger.getInstance().writeLine(o.toString() + " is no longer watching " + toString());
        }
    }

    @Override
    public void notifyObservers() {
        for(Observer observer: observers) {
            observer.update(this);
        }
    }

    @Override
    public String getStateName() {
        return currentState.getClass().getSimpleName();
    }
}