import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import Models.Website;

public class MySQLConnection {
    private Connection connect = null;
    private Statement statement = null;
    private PreparedStatement preStatement = null;
    private ResultSet resultSet = null;

    private Connection connectToMySQLDatabase() throws Exception {
        try {
            // This will load the MySQL driver, each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");   
            // Setup the connection with the DB
            if(connect==null){
                    connect = DriverManager.getConnection("jdbc:mysql://localhost:3306/webcrawler","root","webcrawler_1");
                    createCrawlerTable();
            }
        return connect;
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean createCrawlerTable() throws Exception {
        try {
            boolean result = false;
            Connection myConnection = connectToMySQLDatabase();
            preStatement = myConnection.prepareStatement("SHOW TABLES LIKE 'Crawler';");
            resultSet = preStatement.executeQuery();
          
                preStatement = myConnection.prepareStatement(
                        "CREATE TABLE `Crawler` ( `id` int(11) NOT NULL AUTO_INCREMENT, `url` varchar(255) DEFAULT NULL,`isVisited` BOOLEAN, PRIMARY KEY (`id`)) ENGINE=InnoDB;");
                result = preStatement.execute();
                this.close();
            return result;
        } catch (Exception e) {
            System.out.println(e.toString());
            this.close();
            return false;
        }
    }



    public void createWebsite(String url,boolean isVisited) {
        try{
            Connection myConnection=connectToMySQLDatabase();
   
            boolean result;
            preStatement = myConnection.prepareStatement("INSERT INTO Crawler (url, isVisited) VALUES (?,?);",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preStatement.setString(1,url);
            preStatement.setBoolean(2,isVisited);
            result = preStatement.execute();
            //System.out.println(result);
        }catch(Exception e){
            System.out.println(e.toString());
        }
       
    }
    
    public ArrayList<Website> retreiveUncrawledWebsite() {
        try {
            Connection myConnection = connectToMySQLDatabase();
            int size = 0;
            preStatement = myConnection.prepareStatement("SELECT * FROM Crawler E where E.isVisited = false",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            resultSet = preStatement.executeQuery();
            if (resultSet != null) {
                resultSet.beforeFirst();  
                resultSet.last(); 
 
                size = resultSet.getRow(); 
                resultSet.beforeFirst();
                ArrayList<Website> uncrawledSites = new ArrayList<>(size);
                while (resultSet.next()) {
                    Website temp=new Website();
                    temp.setId(resultSet.getInt("id"));
                    temp.setUrl(resultSet.getString("url"));
                    temp.setIsVisited(resultSet.getBoolean("isVisited"));
                    uncrawledSites.add(temp);
                }
                this.close();
                return uncrawledSites;
            }

        } catch (Exception e) {
            System.out.println(e.toString());

        }
        this.close();
        return null;

    }
    
    public ArrayList<Website> retreiveWebsiteByUrl(String url) {
        try {
            Connection myConnection = connectToMySQLDatabase();
            int size = 0;
            preStatement = myConnection.prepareStatement("SELECT * FROM Crawler E where E.url = ?" , ResultSet.TYPE_SCROLL_SENSITIVE, 
                        ResultSet.CONCUR_UPDATABLE);
            preStatement.setString(1, url);
            resultSet = preStatement.executeQuery();
            if (resultSet != null) {
                resultSet.beforeFirst();
                resultSet.last();
                size = resultSet.getRow();
                ArrayList<Website> uncrawledSites = new ArrayList<>(size);
                while (resultSet.next()) {
                    Website temp = new Website();
                    temp.setId(resultSet.getInt("id"));
                    temp.setUrl(resultSet.getString("url"));
                    temp.setIsVisited(resultSet.getBoolean("isVisited"));
                    uncrawledSites.add(temp);
                }
                this.close();
                return uncrawledSites;
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        
        }
        this.close();
        return null;
    }

    public boolean updateIsVisitedWebsiteById(int id) {
        boolean result = false;
        try {
            Connection myConnection = connectToMySQLDatabase();
            preStatement = myConnection.prepareStatement("UPDATE Crawler SET isVisited = true WHERE id = ?",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            preStatement.setInt(1, id);
            result = preStatement.execute();
            this.close();
            return result;
        } catch (Exception e) {
            System.out.println(e.toString());
            this.close();
            return result;
        }
    }

    // private void writeResultSet(ResultSet resultSet) throws SQLException {

    //     while (resultSet.next()) {

    //         int id = resultSet.getInt("id");
    //         boolean isVisited = resultSet.getBoolean("isVisited");
    //         System.out.print("id: "+ id);
    //         System.out.print(" ");
    //         System.out.println("isVisited: " + isVisited);

    //     }
    // }

    // You need to close the resultSet
    private void close() {
        try {
            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {

        }
    }

}