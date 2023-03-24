package hwdp;

import java.util.HashMap;

// TODO HWDP P3
public class DestObserver implements Observer {
    private String name; // observer name
    private HashMap<Subject, String> subjectToFutureStateName;

    public DestObserver(String n) {
        subjectToFutureStateName = new HashMap<>();
        name = n;
    }

    @Override
    public void update(Subject o) {
        subjectToFutureStateName.put(o, o.getStateName());
        LibraryLogger.getInstance().writeLine(name + " OBSERVED " + o.toString() + " REACHING STATE: " + subjectToFutureStateName.get(o));
    }

    public String toString() {
        return name;
    }
}
