package hwdp;

public interface Subject {
	// TODO HWDP P3
    void attach(Observer o);
    void detach(Observer o);
    void notifyObservers();
    String getStateName();
}
