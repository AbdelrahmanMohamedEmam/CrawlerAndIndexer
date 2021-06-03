import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        MyDatabaseConnection myDatabaseConnection = new MyDatabaseConnection();
        SeedsController seedsController = new SeedsController();
        Object lock = new Object();
        seedsController.loadAndWriteSeedsInDatabase();
        MyDatabaseConnection.crawledSites = 0;
        /*
         * Initiall5izing database with seeds
         */
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of threads: ");
        int numberOfThreads = sc.nextInt();
        sc.close();

        Thread[] threadArray;
        threadArray = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            Crawler myCrawler = new Crawler(numberOfThreads, myDatabaseConnection, seedsController, lock);
            threadArray[i] = new Thread(myCrawler);
            threadArray[i].setName(String.valueOf(i));
            threadArray[i].start();
        }
    }

}
