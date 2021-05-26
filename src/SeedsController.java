import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Models.Website;

public class SeedsController {
    static MySQLConnection mySQLConnection = new MySQLConnection(); 
    static ArrayList<Website>seeds;
    public static void loadAndWriteSeedsInDatabase() {
        List<String> data = new ArrayList<String>();
        try {
            File txt = new File("D:/Spring 2021/Advanced programming techniques/CrawlerAndIndexer/seeds.txt");
            Scanner scan;
            scan = new Scanner(txt);
            while (scan.hasNextLine()) {
                data.add(scan.nextLine());
            }
            scan.close();
            for (String x : data) {
                mySQLConnection.createWebsite(x, 0);
            }
            seeds=mySQLConnection.retreiveUncrawledWebsite();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    public static List<Website> retreiveSeeds(int threadNumber,int totalNumberOfThreads){
        /*Case: Number of threads greater than number of seeds.*/
        if(totalNumberOfThreads>seeds.size()){                        
            return  seeds.subList(threadNumber, threadNumber);
        }

        int batchSize=(int) Math.floor(seeds.size()/totalNumberOfThreads);
        if(threadNumber!=totalNumberOfThreads-1){
        List<Website>temp=
        seeds.subList(threadNumber * batchSize,
                    threadNumber * batchSize + (batchSize));
                    
                    return temp;
                 
        }else{
            return  seeds.subList(threadNumber * batchSize,
                    seeds.size()-1);
        }
    
    }

}