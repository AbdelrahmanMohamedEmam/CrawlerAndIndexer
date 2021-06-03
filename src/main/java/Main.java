import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        MyDatabaseConnection myDatabaseConnection = new MyDatabaseConnection();

        SeedsController seedsController = new SeedsController();
        /*
         * Initiallizing database with seeds
         */
        seedsController.loadSeedsToDatabase();

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of threads: ");

        int numberOfThreads = sc.nextInt();

        sc.close();

        Thread[] threadArray;
        threadArray = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            Crawler myCrawler = new Crawler(numberOfThreads, myDatabaseConnection);
            threadArray[i] = new Thread(myCrawler);
            threadArray[i].setName(String.valueOf(i));
            threadArray[i].start();
        }

        
    }

}
