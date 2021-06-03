import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.regex.PatternSyntaxException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import Models.Website;

enum STATUS {
    UNTAKEN, TAKEN, CRAWLED
}

public class Crawler implements Runnable {

    MyDatabaseConnection myDatabaseConnection;

    int totalNumberOfThreads = 0;

    Object lock;

    Crawler(int totalNumberOfThreads, MyDatabaseConnection myDatabaseConnection) {
        this.myDatabaseConnection = myDatabaseConnection;
        this.totalNumberOfThreads = totalNumberOfThreads;
    }

    @Override
    public void run() {
        startCrawling();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!! I AM THREAD " + Thread.currentThread().getName()
                + " AND I HAVE TERMINATED !!!!!!!!!!!!!!!!!!!!!!!");
    }

    public void startCrawling() {
        int threadNumber = Integer.parseInt(Thread.currentThread().getName());
        LinkedList<Website> batchSizeQueue = new LinkedList<Website>();
        LinkedList<String> extractedUrlsPerDocument = new LinkedList<String>();
        try {
            batchSizeQueue = myDatabaseConnection.retreiveUncrawledWebsite(0, 2, threadNumber);
            while (true) {
                while (batchSizeQueue.size() != 0) {
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
                    /*
                     * Add the extracted urls to the database
                     */

                    myDatabaseConnection.createWebsites(extractedUrlsPerDocument, STATUS.UNTAKEN.ordinal());

                    /* Empty the extractedUrlsPerDocument */
                    extractedUrlsPerDocument.clear();

                    /*
                     * Update the current working document to crawled and remove it from batch queue
                     */

                    if (myDatabaseConnection.updateStatusOfWebsiteBy_Id(batchSizeQueue.get(0).get_Id(),
                            STATUS.CRAWLED.ordinal(), threadNumber)) {
                        System.out.println("----------------------------THE STATUS OF " + batchSizeQueue.get(0).getUrl()
                                + " CHANGED TO CRAWLED IN THE DATABASE--------------------------------------");
                        batchSizeQueue.remove(0);
                    }
                }
                batchSizeQueue = myDatabaseConnection.retreiveUncrawledWebsite(0, 2, threadNumber);
                if (batchSizeQueue == null) {
                    break;
                }
            }
        } catch (MalformedURLException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public boolean checkRobots(String url) {
        boolean checked = true;
        try {
            URL urlObj = null;
            urlObj = new URL(url);

            // getting the link to the robots.txt file
            String robotsFileUrl = urlObj.getProtocol() + "://" + urlObj.getHost() + "/robots.txt";
            Document doc = Jsoup.connect(robotsFileUrl).ignoreContentType(true).userAgent("Mozilla").get();
            String robotsText = doc.text();

            // checking for user-agent: *
            int index = robotsText.indexOf("User-Agent: *");
            if (index == -1) {
                index = robotsText.indexOf("User-agent: *");
                if (index == -1)
                    return false;
            }

            // spliting the array at the user-agent: *
            String sub = robotsText.substring(index);
            String[] robotTextArray = sub.split(" ");

            // getting the path to be checked
            String path = urlObj.getPath();
            // System.out.println(path);


            for (int i = 3; i < robotTextArray.length; i += 2) {
                // check for disallowed paths
                if ((robotTextArray[i - 1]).equals("Disallow:")) {
                    // System.out.println(robotTextArray[i]);
                    // case 1: Disallow all
                    if ((robotTextArray[i]).equals("/") || (robotTextArray[i]).equals("/*")) {
                        checked = false;
                    }

                    if (!path.equals("/")) {
                        try {
                            // replace any special character can be recognized as regex
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\*", ".*");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\+", "\\\\+");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\?", "");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\$", "");
                            path = path.replaceAll("\\$", "");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\-", "\\\\-");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\/", "\\\\/");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\#", "\\\\#");
                            // case 2: a whole directory is disallowed
                            if (robotTextArray[i].matches(".*\\/")) {
                                if (path.matches(".*" + robotTextArray[i] + ".*"))
                                    checked = false;
                            }
                            // case 3: a single endpoint is disallowed
                            if (path.matches(".*" + robotTextArray[i])) {
                                checked = false;
                            }
                        } catch (PatternSyntaxException e) {
                            // to catch any regex matching errors
                            return false;
                        }
                    }
                } else if ((robotTextArray[i - 1]).equals("Allow:")) {
                    // if the path is not disallowed then it is allowed -> break and return
                    if (checked != false) {

                        // can use enum to handle this case or leave it as it is
                    } else {
                        try {
                            // replace any special character can be recognized as regex
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\*", ".*");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\+", "\\\\+");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\?", "");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\$", "");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\-", "\\\\-");
                            robotTextArray[i] = robotTextArray[i].replaceAll("\\/", "\\\\/");
                            if (robotTextArray[i].matches(".*\\/")) {
                                if (path.matches(".*" + robotTextArray[i] + ".*"))
                                    checked = true;
                            }
                            if (path.matches(".*" + robotTextArray[i])) {
                                checked = true;
                            }
                        } catch (PatternSyntaxException e) {
                            // to catch any regex matching errors
                            return false;
                        }
                    }
                }
                // reaching this step means that i have finished all allow/disallow permissions
                // for this bot
                else {
                    break;
                }

            }
        } catch (IOException ex) {
            //the base url of the url does not have robots.txt file
            return false;

        }
       
        return checked;

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
