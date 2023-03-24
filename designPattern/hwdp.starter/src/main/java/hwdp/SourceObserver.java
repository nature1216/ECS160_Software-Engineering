package hwdp;
import java.util.HashMap;

// TODO HWDP P3

public class SourceObserver implements Observer {
	private String name; // observer name
	private HashMap<Subject, String> subjectToPastStateName;
	
	public SourceObserver(String n) {
		// TODO?
		name = n;
		subjectToPastStateName = new HashMap<>();
	}

	@Override
	public void update(Subject o) {
		// TODO?
		if(subjectToPastStateName.get(o) == null) {
			LibraryLogger.getInstance().writeLine(name +" OBSERVED "+o.toString()+
					" LEAVING STATE: UNOBSERVED");
		} else {
			LibraryLogger.getInstance().writeLine(name +" OBSERVED "+o.toString()+
					" LEAVING STATE: " + subjectToPastStateName.get(o));
		}
		subjectToPastStateName.put(o, o.getStateName());
	}

	@Override
	public String toString() {
		return name;
	}
}
