package battlecode.engine;

// TODO: pass messages along to the client
public class ErrorReporter {

    // reports the error, and tells the contestant to contact the devs
    public static void report(Throwable e) {
        report(e, true);
    }

    public static void report(String message) {
        report(message, true);
    }

    public static void report(String message, boolean ourFault) {
        printHeader();
        System.out.println(message);
        if (ourFault) {
            System.out.print("\n\n");
            printReportString();
        }
        printFooter();
    }

    public static void report(String message, String thingsToTry) {
        printHeader();
        System.out.println(message);
        System.out.print("\n\n");
        printThingsToTry(thingsToTry);
        printFooter();
    }

    public static void report(Throwable e, boolean ourFault) {
        printHeader();
        e.printStackTrace();
        if (ourFault) {
            System.out.print("\n\n");
            printReportString();
        }
        printFooter();
    }

    public static void report(Throwable e, String thingsToTry) {
        printHeader();
        e.printStackTrace();
        System.out.print("\n\n");
        printThingsToTry(thingsToTry);
        printFooter();
    }

    private static void printHeader() {
        System.out.println("~~~~~~~ERROR~~~~~~~");
    }

    private static void printFooter() {
        System.out.println("~~~~~~~~~~~~~~~~~~~");
    }

    private static void printThingsToTry(String thingsToTry) {
        System.out.println("Please try the following:");
        System.out.println(thingsToTry);
        System.out.println("\n\nIf that doesn't work....");
        printReportString();
    }

    private static void printReportString() {
        System.out.println("Please report this to the 6.370 devs, by posting to the forum\n"
                + "under the \"bugs\" thread.  Include a copy of this printout and\n"
                + "a brief description of the bug, including whether it's consistent\n"
                + "or sporadic.  Thanks!");
    }
}
