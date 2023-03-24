package hwdp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Student_jayki_Test {

    @Test
    void myStudentTest(){
        LibraryBook book1 = new LibraryBook("Hello JavaFX");
        LibraryBook book2 = new LibraryBook("Software Engineering");

        SourceObserver srcObserver = new SourceObserver("SrcObserver");
        DestObserver destObserver = new DestObserver("DestObserver");

        book1.detach(destObserver);
        book1.attach(srcObserver);
        book1.attach(srcObserver);
        book1.attach(destObserver);
        book2.attach(destObserver);

        book1.issue();
        book1.extend();
        book1.shelf();
        book1.detach(srcObserver);
        book1.returnIt();
        book1.shelf();

        book2.extend();
        book2.attach(srcObserver);
        book2.issue();
        book2.extend();
        book2.detach(destObserver);
        book2.returnIt();
        book2.issue();
        book2.shelf();

        assertArrayEquals(
                new String [] {
                        "SrcObserver is now watching Hello JavaFX",
                        "SrcObserver is already attached to Hello JavaFX",
                        "DestObserver is now watching Hello JavaFX",
                        "DestObserver is now watching Software Engineering",
                        "Leaving State OnShelf for State Borrowed",
                        "SrcObserver OBSERVED Hello JavaFX LEAVING STATE: UNOBSERVED",
                        "DestObserver OBSERVED Hello JavaFX REACHING STATE: Borrowed",
                        "Leaving State Borrowed for State Borrowed",
                        "SrcObserver OBSERVED Hello JavaFX LEAVING STATE: Borrowed",
                        "DestObserver OBSERVED Hello JavaFX REACHING STATE: Borrowed",
                        "BadOperationException - Can't use shelf in Borrowed state",
                        "SrcObserver is no longer watching Hello JavaFX",
                        "Leaving State Borrowed for State GotBack",
                        "DestObserver OBSERVED Hello JavaFX REACHING STATE: GotBack",
                        "Leaving State GotBack for State OnShelf",
                        "DestObserver OBSERVED Hello JavaFX REACHING STATE: OnShelf",
                        "BadOperationException - Can't use extend in OnShelf state",
                        "SrcObserver is now watching Software Engineering",
                        "Leaving State OnShelf for State Borrowed",
                        "DestObserver OBSERVED Software Engineering REACHING STATE: Borrowed",
                        "SrcObserver OBSERVED Software Engineering LEAVING STATE: UNOBSERVED",
                        "Leaving State Borrowed for State Borrowed",
                        "DestObserver OBSERVED Software Engineering REACHING STATE: Borrowed",
                        "SrcObserver OBSERVED Software Engineering LEAVING STATE: Borrowed",
                        "DestObserver is no longer watching Software Engineering",
                        "Leaving State Borrowed for State GotBack",
                        "SrcObserver OBSERVED Software Engineering LEAVING STATE: Borrowed",
                        "BadOperationException - Can't use issue in GotBack state",
                        "Leaving State GotBack for State OnShelf",
                        "SrcObserver OBSERVED Software Engineering LEAVING STATE: GotBack"
                },
                LibraryLogger.getInstance().getWrittenLines()
        );
    }  
}
