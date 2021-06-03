import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Vector;

public class SeedsController {
    enum STATUS {
        UNTAKEN, TAKEN, CRAWLED
    }

    MyDatabaseConnection mySQLConnection = new MyDatabaseConnection();

    public void loadSeedsToDatabase() {
        Vector<String> data = new Vector<>();
        try {
            File txt = new File("./seeds.txt");
            Scanner scan;
            scan = new Scanner(txt);
            while (scan.hasNextLine()) {
                data.add(scan.nextLine());
            }
            scan.close();
            for (String x : data) {
                mySQLConnection.createWebsite(x, SeedsController.STATUS.UNTAKEN.ordinal());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
