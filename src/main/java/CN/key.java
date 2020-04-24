package CN;

public class key {
    //Info
    private String username;
    private String action;
    private String value;

    public key(String username, String action, String value) {
        this.username = username;
        this.action = action;
        this.value = value;
    }
    //get
    public String getUsername() {return username;}
    public String getAction() {return action;}
    public String getValue() {return value;}
}
