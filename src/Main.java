import java.util.Scanner;


public class Main {
    public static void main(String[] args) throws Exception {
        // SeedsController.loadAndWriteSeedsInDatabase();              /*Initiallizing database with seeds*/
        //  Scanner sc = new Scanner(System.in);
        //  System.out.print("Enter number of threads: ");
        //  int numberOfThreads = sc.nextInt();
        //  sc.close();
        //  Object lock = new Object();
        //  Crawler myCrawler = new Crawler(lock,numberOfThreads);
        //  Thread [] threadArray;
        //  threadArray=new Thread[numberOfThreads];
        // for(int i=0;i<numberOfThreads;i++){
        //     threadArray[i]=new Thread(myCrawler);
        //     threadArray[i].setName(String.valueOf(i));
        //     threadArray[i].start();
        // }

        Indexer indexer = new Indexer();
        indexer.startIndexing();



    }

}