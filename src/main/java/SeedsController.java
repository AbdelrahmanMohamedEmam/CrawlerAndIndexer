import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import java.util.Scanner;
import java.util.Vector;

import Models.Website;

public class SeedsController {
    enum STATUS {
        UNTAKEN, TAKEN, CRAWLED
    }

    MyDatabaseConnection mySQLConnection = new MyDatabaseConnection();
    LinkedList<Website> seeds;
    int seedsNumber = 0;

    public void loadAndWriteSeedsInDatabase() {
        Vector<String> data = new Vector<>();
        try {
            File txt = new File("seeds.txt");
            Scanner scan;
            scan = new Scanner(txt);
            while (scan.hasNextLine()) {
                data.add(scan.nextLine());
                seedsNumber++;
            }
            scan.close();
            for (String x : data) {
                mySQLConnection.createWebsite(x, SeedsController.STATUS.TAKEN.ordinal());
            }
         
            seeds = mySQLConnection.retreiveUncrawledWebsite(SeedsController.STATUS.TAKEN.ordinal(), seedsNumber, -1);
            
          

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public synchronized LinkedList<Website> retreiveSeeds(int threadNumber, int totalNumberOfThreads) {
        System.out.println("I am thread:" + threadNumber + "and i acquired the lock");
        /* Case: Number of threads greater than number of seeds. */
        LinkedList<Website> temp = new LinkedList<Website>();
        if (totalNumberOfThreads <= seedsNumber) {

            int batchSize = (int) Math.ceil((double) seedsNumber / (double) totalNumberOfThreads);

            for (int i = 0; i < batchSize; i++) {
                if (!seeds.isEmpty()) {
                    temp.add(seeds.remove(0));
                }

            }
            System.out.println("I am thread:" + threadNumber + "and i released the   lock");
        } else {
            if (!seeds.isEmpty()) {
                temp.add(seeds.remove(0));
            }
        }
        return temp;
    }

}
