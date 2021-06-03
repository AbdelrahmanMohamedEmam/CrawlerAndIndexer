package Models;


public class Website { 
       
    private String _id;

    private String url;

    private int status;

    public String get_Id() {
        return _id;
    }

    public void set_Id(String id) {
        this._id = id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
