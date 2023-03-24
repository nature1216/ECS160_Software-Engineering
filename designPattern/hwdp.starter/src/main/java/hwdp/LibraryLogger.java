package hwdp;
import java.util.ArrayList;

public class LibraryLogger {
    // TODO?
    private static final LibraryLogger libraryLogger = new LibraryLogger();
    private ArrayList<String> lines = new ArrayList<>();

    private LibraryLogger() {
        ExpensiveComputeToy.performExpensiveLogSetup();
    }

    public void writeLine(String line) {
        // TODO?
        lines.add(line);
        System.out.println("LibraryLogger: " + line);
    }

    public String[] getWrittenLines() {
        // TODO?
        return lines.toArray(new String[lines.size()]);
    }

    public void clearWriteLog() {
        // TODO?
        lines.clear();
    }
    
    public static LibraryLogger getInstance() {
        // TODO?
        return libraryLogger;
    }
}
