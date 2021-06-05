import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner sc = new Scanner(System.in);
    
        System.out.println("==================Doodle Admin Panel==================");
        System.out.println("|                                                    |");    
        System.out.println("|                 1- Start Crawling                  |"); 
        System.out.println("|                                                    |");    
        System.out.println("|                 2- Start Indexing                  |");
        System.out.println("|                                                    |");
        System.out.println("======================================================"); 
        System.out.println("==================Enter Your Chocice:=================");
        
        String str= "";
        while(!str.equals("1")  && !str.equals("2")){
            str = sc.nextLine();
        }
        
        if(str.equals("1")){

            
            MyDatabaseConnection myDatabaseConnection = new MyDatabaseConnection();
            myDatabaseConnection.initializeCrawlerData();
            SeedsController seedsController = new SeedsController();
            /*
            * Initiall5izing database with seeds
            */
            seedsController.loadSeedsToDatabase();



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


        }else if(str.equals("2")){
            Indexer indexer = new Indexer();
            indexer.startIndexing();

            MyDatabaseConnection myDatabaseConnection = new MyDatabaseConnection();
            myDatabaseConnection.calculateIDF();

            System.out.print("================HURRAAAAY FINISHED INDEXING===================");

        }


      

    }

}
