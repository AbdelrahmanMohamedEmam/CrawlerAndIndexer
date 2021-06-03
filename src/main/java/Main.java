import java.util.Scanner;


public class Main {

    public static void main(String[] args) throws Exception {
        // SeedsController.loadAndWriteSeedsInDatabase(); /* Initiallizing database with seeds */
        // Scanner sc = new Scanner(System.in);
        // System.out.print("Enter number of threads: ");
        // int numberOfThreads = sc.nextInt();
        // sc.close();
        // Object lock = new Object();

        // Thread[] threadArray;
        // threadArray = new Thread[numberOfThreads];
        // for (int i = 0; i < numberOfThreads; i++) {
        //     Crawler myCrawler = new Crawler(lock, numberOfThreads);
        //     threadArray[i] = new Thread(myCrawler);
        //     threadArray[i].setName(String.valueOf(i));
        //     threadArray[i].start();
            
        // }
        // for (int i = 0; i < numberOfThreads; i++) {
        //     threadArray[i].join();
        // }



        // MyDatabaseConnection myDatabaseConnection = new MyDatabaseConnection();
        // myDatabaseConnection.createWebsite("https://www.bbc.com/sport", 2);
        // myDatabaseConnection.createWebsite("https://www.marca.com/en/", 2);
        // myDatabaseConnection.createWebsite("https://www.skysports.com/", 2);
        // myDatabaseConnection.createWebsite("https://www.soccernews.com/", 2);
        // myDatabaseConnection.createWebsite("https://www.espn.com/soccer/", 2);
        // myDatabaseConnection.createWebsite("https://www.goal.com/en", 2);
        // myDatabaseConnection.createWebsite("https://www.theguardian.com/football", 2);
        // myDatabaseConnection.createWebsite("https://www.sportingnews.com/us/soccer", 2);
        // myDatabaseConnection.createWebsite("https://www.soccerladuma.co.za/", 2);
        // myDatabaseConnection.createWebsite("https://www.dailymail.co.uk/sport/football/index.html", 2);
        // myDatabaseConnection.createWebsite("https://www.cbssports.com/soccer/", 2);

        Indexer indexer = new Indexer();
        indexer.startIndexing();
    }

}
