package CN;

public class pi implements java.io.Serializable {
    //Info
    private int rank;
    private int tp;
    private int gp;
    private int bb;
    private boolean verified;
    private String discordTag;


    public pi() {
        this.rank = 0;
        this.gp = 0;
        this.tp = 0;
        this.bb = 0;
        this.verified = false;
        this.discordTag = "N/A";
    }

    //modify
    public void setRank(int rank) {this.rank = rank;}
    public void setVerified(boolean verified) {this.verified = verified;}
    public void addTp(int tp) {this.tp = this.tp + tp;}
    public void setTp(int tp) {this.tp = tp;}
    public void addGp(int gp){this.gp = this.gp + gp;}
    public void setGp(int gp){this.gp = gp;}
    public void addBb(int bb){this.bb = this.bb + 1;}
    public void setBb(int bb){this.bb = bb;}
    public void setDiscordTag(String discordTag) {this.discordTag = discordTag;}
    //get
    public int getTP() {return tp;}
    public int getGP() {return gp;}
    public int getRank() {return rank;}
    public int getBB() {return bb;}
    public boolean getVerified() {return verified;}
    public String getDiscordTag() {return discordTag;}
}
