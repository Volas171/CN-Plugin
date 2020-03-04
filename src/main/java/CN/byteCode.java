package CN;

public class byteCode {
    public static String nameR(String name) {return name.replaceAll("\\[", "[[");}
    public static String rankI(int i) {
        switch (i) {
            case 6:
                return "[accent]<[scarlet]\uE814[accent]>";
            case 5:
                return "[accent]<[royal]\uE828[accent]>";
            case 4:
                return "[accent]<[lightgray]\uE80F[accent]>";
            case 3:
                return "";
            case 2:
                return "";
            case 1:
                return "";
            default:
                return "ERR";
        }
    }
    public static String verifiedI() {
        return "[accent]<[sky]\uE848[accent]>";
    }
}
