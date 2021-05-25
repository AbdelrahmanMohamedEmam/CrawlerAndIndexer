import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import Models.Website;



public class Crawler implements Runnable {
    MySQLConnection mySQLConnection=new MySQLConnection();
    List<Website>seeds;
    List<Website> sites;
    int seedsCount =0;
    int totalNumberOfThreads=0;
    Object lock=new Object();
    Crawler(Object lock,int totalNumberOfThreads){
        this.lock=lock;
        this.totalNumberOfThreads=totalNumberOfThreads;
    }
    @Override
    public void run() {
        startCrawling();
    }


   
    public void startCrawling() {
        int threadNumber=Integer.parseInt(  Thread.currentThread().getName());
   //     try {
       synchronized(lock){
            seeds=SeedsController.retreiveSeeds(threadNumber,totalNumberOfThreads);
            seedsCount= seeds.size();
            System.out.println("I am thread: "+threadNumber +" My start is "+seeds.get(0).getId()+" and my end is: "+ seeds.get(seeds.size()-1).getId());
       }            
            // //LW MAWSELTESH LEL LIMIT BTA3Y W LESA 3ANDY 7AGAT NOT VISITED
            // while (seedsCount!=50 && sites.size()!=0){
            //     String siteUrl = sites.get(0).getUrl();
            //     Document doc = Jsoup.connect(siteUrl).userAgent("Mozilla").get();
            //     Elements links = doc.select("a");
            //     links.forEach((link)->{
            //         String urlString = link.attr("abs:href");
            //         boolean checkRobotsTxt = checkRobots(urlString);
            //         if (checkRobotsTxt) {
            //         URL url;
            //         try {
            //             url = new URL(urlString);
            //             //CHECK EL PROTOCOL 3LSHAN LW FEH MAILTO: BADAL HTTPS AW HTTP
            //             if (url.getProtocol().equals("https") || url.getProtocol().equals("http")){
                           
            //                 //AT2AKED ENO MSH MAWGODA FEL DATABASE ALREADY
            //                ArrayList<Website> temp; 
            //                synchronized(lock){
            //                         temp = mySQLConnection.retreiveWebsiteByUrl(urlString);
            //                }
                 
            //                 if(temp.size()==0){
            //                     //System.out.println(urlString);
            //                     //ADD IT FEL DATABASE
            //                     synchronized(lock){
            //                             mySQLConnection.createWebsite(urlString, false);
            //                     }
                         
            //                 }
            //             }
            //         } catch (MalformedURLException e) {
            //             System.out.println(e.getMessage());
            //         }   
            //     }
            //     });
            //     //NE2LEB EL ISVISITED W NEZAWED EL seedsCount
            //     synchronized(lock){
            //         mySQLConnection.updateIsVisitedWebsiteById(sites.get(0).getId());
            //     }
            //     seedsCount+=1;
            //     synchronized(lock){
            //         sites = mySQLConnection.retreiveUncrawledWebsite();
            //     }
            // }


            //(SORTED)WHILE THERE URLS WITH FALSE ISVISITED ATTRIBUTE || REACH LIMIT
                //HANEMSEK AWEL WA7DA
                //NEGEB EL BODY W NESHOF HAN3MEL BEH EH
                //NEGEBE EL URLS
                    //LKOL URL NCALL ROBOTS
                    //LKOL URL NCHECK ENAHA MSH MAWGODA
                    //LW MSH MAWGODA W EL ROBOTS AMAN NEZAWEDHA FEL DATABASE
                //NE2LEB EL ISVISITED NE5ALEHA TRUE    
            
            
        // }catch(MalformedURLException ex){
        //     System.out.println(ex.getMessage());
        
        // } catch (IOException ex) {

        //     System.out.println(ex.getMessage());
        // }
    }

     public boolean checkRobots(String url) {
        try {
            if(url.contains("#"))
            {
                return false;
            }
            URL urlObj = null;
            urlObj = new URL(url);
            String path = urlObj.getPath();
            if(path.contains("#"))
            {
                return false;
            }
                
            String robotsFileUrl = urlObj.getProtocol() + "://" + urlObj.getHost() + "/robots.txt";
            Document doc = Jsoup.connect(robotsFileUrl).ignoreContentType(true).userAgent("Mozilla").get();
            String robotsText = doc.text();
            int index = robotsText.indexOf("User-Agent: *");
            if(index == -1)
            {
                index = robotsText.indexOf("User-agent: *");
                if(index == -1) return false;
            }
            String sub = robotsText.substring(index);

            String[] robotTextArray = sub.split(" ");
            for(int i =3 ; i< robotTextArray.length ; i+=2)
            {

                if((robotTextArray[i-1]).equals( "Disallow:"))
                {
                    if((robotTextArray[i]).equals( "/")) //All are disallowed
                    {
                        return false;
                    }
                        
                    if(!path.equals("/"))
                    {
                        if((robotTextArray[i]).contains(path))
                        {
                            return false;
                        }
                    }
                }
                else if((robotTextArray[i-1]).equals( "Allow:"))
                {
                    //check if it is allowed -> return true
                }
                else
                {
                    break;
                }

            }
        } catch (IOException ex) {
            return false;

        }
        return true;

    }

    //THIS FUNCTION IS BOOLEAN FOR ONLY 1 REASON IF IT FAILED TO CONNECT TO THE GIVEN URL IT WILL RETURN FALSE
    public boolean downloadAndSave(String fileName , String inUrl)
    {
        
        Document doc;
        try {
            doc = Jsoup.connect(inUrl).ignoreContentType(true).userAgent("Mozilla").get();
            String htmlContent = doc.html();
            File output = new File(fileName + ".html");
            FileWriter writer = new FileWriter(output);

            writer.write(htmlContent);
            writer.flush();
            writer.close();


        } catch (IOException e) {
            
            e.printStackTrace();
            System.out.println("Error in Connection!");
            return false;
        }
        return true;
        

    }


 

}
