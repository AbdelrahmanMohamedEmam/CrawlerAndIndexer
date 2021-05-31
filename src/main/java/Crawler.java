import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import Models.Website;

enum STATUS {
    UNTAKEN, TAKEN, CRAWLED
}

public class Crawler implements Runnable {
    public static int CRAWLING_LIMIT = 20;
    MyDatabaseConnection myDatabaseConnection;
    SeedsController seedsController;
    int totalNumberOfThreads = 0;
    static int crawledSites = 0;

    Crawler(int totalNumberOfThreads, MyDatabaseConnection myDatabaseConnection, SeedsController seedsController) {
        this.myDatabaseConnection = myDatabaseConnection;
        this.totalNumberOfThreads = totalNumberOfThreads;
        this.seedsController = seedsController;
    }

    @Override
    public void run() {
        startCrawling();
    }

    public void startCrawling() {
        int threadNumber = Integer.parseInt(Thread.currentThread().getName());
        LinkedList<Website> batchSizeQueue = new LinkedList<Website>();
        LinkedList<String> extractedUrlsPerDocument = new LinkedList<String>();

        try {

            /* Getting my seeds */
            batchSizeQueue = (LinkedList<Website>) seedsController.retreiveSeeds(threadNumber, totalNumberOfThreads);

            /* If seeds empty retreive again after sleeping 5 secs */
            while (batchSizeQueue.isEmpty()) {
                Thread.sleep(5000);
                batchSizeQueue = myDatabaseConnection.retreiveUncrawledWebsite(0);
            }

            while (crawledSites < CRAWLING_LIMIT) {
                while (batchSizeQueue.size() != 0 && crawledSites < CRAWLING_LIMIT) {
                    /* Get url of the first site in the queue */
                    String siteUrl = batchSizeQueue.get(0).getUrl();
                    System.out.println("Thread no: " + threadNumber + " is crawling: " + siteUrl + ".............");

                    /* Get html document of this website */
                    Document doc = Jsoup.connect(siteUrl).userAgent("Mozilla").get();
                    /* Get all aTags in this document */
                    Elements aTags = doc.select("a");

                    /*
                     * Iterate on each aTag and get links in it and check for the robots file per
                     * document
                     */
                    aTags.forEach((aTag) -> {

                        /* Get absolute link in the aTag */
                        String urlString = aTag.attr("abs:href");
                        /* Check if this url valid for robots or not */
                        boolean checkRobotsTxt = checkRobots(urlString);
                        if (checkRobotsTxt) {
                            URL url;
                            try {
                                /* Convert url to a Url object */
                                url = new URL(urlString);

                                /* Check the protocol */
                                if (url.getProtocol().equals("https") || url.getProtocol().equals("http")) {
                                    extractedUrlsPerDocument.add(urlString);
                                }
                            } catch (MalformedURLException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    });

                    System.out.println("I am thread: " + threadNumber + "and i finished crawling "
                            + extractedUrlsPerDocument.size() + " link");

                    /*
                     * Add the extracted urls to the database
                     */

                    if (myDatabaseConnection.createWebsites(extractedUrlsPerDocument, STATUS.UNTAKEN.ordinal())) {
                        System.out.println(
                                "-------------------------THE CRAWLED WEBSITES ARE ADDED SUCCESSFULLY!!-------------------------");
                    }

                    /* Empty the extractedUrlsPerDocument */
                    extractedUrlsPerDocument.clear();

                    /*
                     * Update the current working document to crawled and remove it from batch queue
                     */

                    if (myDatabaseConnection.updateStatusOfWebsiteBy_Id(batchSizeQueue.get(0).get_Id(),
                            STATUS.CRAWLED.ordinal(), threadNumber)) {
                        System.out.println("The status of " + batchSizeQueue.get(0).getUrl()
                                + " changed to crawled in the database");
                        batchSizeQueue.remove(0);
                        crawledSites += 1;
                    }
                }
                batchSizeQueue = myDatabaseConnection.retreiveUncrawledWebsite(0);
            }
            System.out.println("################################################################");
            System.out.println("   I am thread: " + threadNumber + "and i finished crawling     ");
            System.out.println("################################################################");
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } catch (InterruptedException e) {
            System.out.println("I am thread: " + threadNumber + " and i have been interrupted");
        }
    }

    public boolean checkRobots(String url) {
        try {
            if (url.contains("#")) {
                return false;
            }
            URL urlObj = null;
            urlObj = new URL(url);
            String path = urlObj.getPath();
            if (path.contains("#")) {
                return false;
            }

            String robotsFileUrl = urlObj.getProtocol() + "://" + urlObj.getHost() + "/robots.txt";
            Document doc = Jsoup.connect(robotsFileUrl).ignoreContentType(true).userAgent("Mozilla").get();
            String robotsText = doc.text();
            int index = robotsText.indexOf("User-Agent: *");
            if (index == -1) {
                index = robotsText.indexOf("User-agent: *");
                if (index == -1)
                    return false;
            }
            String sub = robotsText.substring(index);

            String[] robotTextArray = sub.split(" ");
            for (int i = 3; i < robotTextArray.length; i += 2) {

                if ((robotTextArray[i - 1]).equals("Disallow:")) {
                    if ((robotTextArray[i]).equals("/")) // All are disallowed
                    {
                        return false;
                    }

                    if (!path.equals("/")) {
                        if ((robotTextArray[i]).contains(path)) {
                            return false;
                        }
                    }
                } else if ((robotTextArray[i - 1]).equals("Allow:")) {
                    // check if it is allowed -> return true
                } else {
                    break;
                }

            }
        } catch (IOException ex) {
            return false;

        }
        return true;

    }

    // THIS FUNCTION IS BOOLEAN FOR ONLY 1 REASON IF IT FAILED TO CONNECT TO THE
    // GIVEN URL IT WILL RETURN FALSE
    public boolean downloadAndSave(String fileName, Document doc) {
        try {
            String htmlContent = doc.html();
            File output = new File("./src/Documents/" + fileName + ".html");
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
