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
    MySQLConnection mySQLConnection = new MySQLConnection();
    int totalNumberOfThreads = 0;
    static int crawlingLimit = 0;
    List<Website> batchSizeQueue = new LinkedList<Website>();
    List<String> extractedUrlsPerDocument = new LinkedList<String>();
    Object lock;

    Crawler(Object lock, int totalNumberOfThreads) {
        this.lock = lock;
        this.totalNumberOfThreads = totalNumberOfThreads;
    }

    @Override
    public void run() {
        startCrawling();
    }

    public void startCrawling() {
        int threadNumber = Integer.parseInt(Thread.currentThread().getName());

        try {

            /* Getting my seeds */
            synchronized (lock) {
                batchSizeQueue = SeedsController.retreiveSeeds(threadNumber, totalNumberOfThreads);
                System.out.println("I am thread: " + threadNumber + " My start is " + batchSizeQueue.get(0).getId()
                        + " and my end is: " + batchSizeQueue.get(batchSizeQueue.size() - 1).getId());
            }

            while (crawlingLimit < 9) {

                while (batchSizeQueue.size() != 0 && crawlingLimit < 9) {
                    System.out.println("Ana thread 5ara 3ala dma8i:" + threadNumber
                            + " we da5alt dek om el loop welbatch 5ara size bt3y awl index feh el id bta3o: "
                            + batchSizeQueue.get(0).getId());
                    /* Get url of the first site in the queue */
                    String siteUrl = batchSizeQueue.get(0).getUrl();
                    /* Get html document of this website */
                    Document doc = Jsoup.connect(siteUrl).userAgent("Mozilla").get();
                    /* Get all aTags in this document */
                    Elements aTags = doc.select("a");

                    /*
                     * Iterate on each aTag and get links in it and check for the robots file per
                     * document
                     */
                    System.out.println("I am thread: " + threadNumber + " and i am extracting links from document");
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

                    System.out.println("I am thread: " + threadNumber + "and i finished extracting "
                            + extractedUrlsPerDocument.size() + " link");

                    System.out.println("I am thread: " + threadNumber + " and i want to acquire the lock");

                    synchronized (lock) {

                        //
                        System.out.println("I am thread: " + threadNumber + " and i acquired the lock");
                        /*
                         * Add the extracted urls to the database
                         */

                        for (int i = 0; i < extractedUrlsPerDocument.size(); i++) {
                            if (mySQLConnection.createWebsite(extractedUrlsPerDocument.get(i),
                                    STATUS.UNTAKEN.ordinal())) {
                                System.out.println(
                                        "The url: " + extractedUrlsPerDocument.get(i) + " is added to the database.");
                            }
                        }

                        /* Empty the extractedUrlsPerDocument */
                        extractedUrlsPerDocument.clear();
                        /*
                         * Update the current working document to crawled and download it and remove it
                         * from batch queue
                         */

                        if (mySQLConnection.updateStatusOfWebsiteById(batchSizeQueue.get(0).getId(),
                                STATUS.CRAWLED.ordinal())) {
                            System.out.println("I am thread: " + threadNumber + " and url: "
                                    + batchSizeQueue.get(0).getUrl() + " status is changed to crawled to database.");
                            downloadAndSave(Integer.toString(batchSizeQueue.get(0).getId()), doc);
                            batchSizeQueue.remove(0);
                            crawlingLimit += 1;
                            System.out.println(
                                    "I am thread: " + threadNumber + " and the crawling limit is: " + crawlingLimit);
                        }
                        /* Increment Crawling limit */
                        System.out.println("I am thread: " + threadNumber + " and i released the lock");

                    }
                    System.out.println("Ana thread 5ara 3ala dma8i:" + threadNumber
                            + " we 5aragt mn dek om el loop welbatch 5ara size bt3y awl index feh el id bta3o: "
                            + batchSizeQueue.get(0).getId());
                }
                /* Retreive another batch of websites */
            }

            System.out.println("I am thread: " + threadNumber + " we 5alast");
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
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
