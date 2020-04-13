package CN;

import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.content.Fx;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.graphics.Pal;
import mindustry.net.Administration;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import static mindustry.Vars.*;

public class byteCode {
    //storage
    public static String[] tips;

    //code
    public static String nameR(String name) {return name.replaceAll("\\[", "[[");}
    public static String rankI(int i) {
        switch (i) {
            case 7:
                return "[accent]<[scarlet]\uE814[accent]>";
            case 6:
                return "[accent]<[royal]\uE828[accent]>";
            case 5:
                return "[accent]<[lightgray]\uE80F[accent]>";
            case 4:
                return "[accent]<[lime]\uE85B[accent]>";
            case 3:
                return "[accent]<[gold]\uE80E[accent]>";
            case 2:
                return "[accent]<\uE809>";
            case 1:
                return "[accent]<>";
            case 0:
                return "[lightgray]<>";
            default:
                return "ERR";
        }
    }
    public static String verifiedI() {
        return "<[sky]\uE848[accent]>";
    }
    public static Integer sti(String Input) {
        if (Strings.canParseInt(Input)) {
            return Strings.parseInt(Input);
        }
        return -643; //GAE XD
    }
    public static String noColors(String string){
        String finalString = string;
        finalString = finalString.replaceAll("\\[clear\\]","");
        finalString = finalString.replaceAll("\\[black\\]","");
        finalString = finalString.replaceAll("\\[white\\]","");
        finalString = finalString.replaceAll("\\[lightgray\\]","");
        finalString = finalString.replaceAll("\\[gray\\]","");
        finalString = finalString.replaceAll("\\[darkgray\\]","");
        finalString = finalString.replaceAll("\\[blue\\]","");
        finalString = finalString.replaceAll("\\[navy\\]","");
        finalString = finalString.replaceAll("\\[royal\\]","");
        finalString = finalString.replaceAll("\\[slate\\]","");
        finalString = finalString.replaceAll("\\[sky\\]","");
        finalString = finalString.replaceAll("\\[cyan\\]","");
        finalString = finalString.replaceAll("\\[teal\\]","");
        finalString = finalString.replaceAll("\\[green\\]","");
        finalString = finalString.replaceAll("\\[acid\\]","");
        finalString = finalString.replaceAll("\\[lime\\]","");
        finalString = finalString.replaceAll("\\[forest\\]","");
        finalString = finalString.replaceAll("\\[olive\\]","");
        finalString = finalString.replaceAll("\\[yellow\\]","");
        finalString = finalString.replaceAll("\\[gold\\]","");
        finalString = finalString.replaceAll("\\[goldenrod\\]","");
        finalString = finalString.replaceAll("\\[orange\\]","");
        finalString = finalString.replaceAll("\\[brown\\]","");
        finalString = finalString.replaceAll("\\[tan\\]","");
        finalString = finalString.replaceAll("\\[brick\\]","");
        finalString = finalString.replaceAll("\\[red\\]","");
        finalString = finalString.replaceAll("\\[scarlet\\]","");
        finalString = finalString.replaceAll("\\[coral\\]","");
        finalString = finalString.replaceAll("\\[salmon\\]","");
        finalString = finalString.replaceAll("\\[pink\\]","");
        finalString = finalString.replaceAll("\\[magenta\\]","");
        finalString = finalString.replaceAll("\\[purple\\]","");
        finalString = finalString.replaceAll("\\[violet\\]","");
        finalString = finalString.replaceAll("\\[maroon\\]","");
        finalString = finalString.replaceAll("\\[#(.*)\\]","");
        finalString = finalString.replace("[]","");
        return  finalString;}
    public static void loadTips() {
        tips = new String[14];
        tips[0] = "Tip #1: When on the [lightgray]Upgrade []Menu, [white]\uE850[], little colored short arrows will point you towards Upgrade Pads.";
        tips[1] = "Tip #1: When on the [lightgray]Upgrade []Menu, [white]\uE850[], little colored short arrows will point you towards Upgrade Pads.";
        tips[2] = "Tip #2: You can Overdrive [white]\uF899 []Weapons to make them shoot faster.";
        tips[3] = "Tip #3: If you disconnect a power source and connect it with Diodes [white]\uF87C[], you can prevent them from dying when power crashes.";
        tips[4] = "Tip #4: You can overdrive Upgrade Pads [white]\uF842 []to make spawning faster, this includes the core!";
        tips[5] = "Tip #5: You can report a Griefer using [lightgray]/gr[]. By doing so, [forest]CN Mods []will be notified.";
        tips[6] = "Tip #6: You can call for help by doing /halp.";
        tips[7] = "Tip #7: Use [lightgray]/tips []to get a random tip!";
        tips[8] = "Tip #8: Different Mechs and Ships have different build speeds. Use trident [white]\uF842 []for fastest build speed.";
        tips[9] = "Tip #9: Do [lightgray]/info []for server info.";
        tips[10] ="Tip #10: Do [lightgray]/info colors []to get all available colors for Mindustry.";
        tips[11] ="Tip #11: To use color tags, do [lightgray][color] []to change color. \nExample: [[red]Hi -> [red]Hi[].";
        tips[12] ="Tip #12: If you have a Grievance, Praise or Recommendation, you can tell us in the #recommendations discord [sky]\uE848 []channel.";
        tips[13] ="Tip #13: Water Extractor, Oil extractor and Cultivator speeds can vary depending on what type of land they are placed in.";
    }
    public static String hash(int length) { //not my code - https://www.baeldung.com/java-random-string
        int leftLimit = 48; //0
        int rightLimit = 122; //z
        Random rand = new Random();

        String hash = rand.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        if (!Main.adata.has(hash)) {
            return hash;
        }
        return hash(length);
    }
    public static String dec(String string) { //discord escape codes
        return string.replace("@", "\\@").replace("*","\\*").replace("~","\\~").replace("`","\\`").replace("|","\\|").replace("_","\\_");
    }
    public static Boolean safeName(String name) {
        if (noColors(name).equals("")) return false;
        List<String> improper = Arrays.asList("IGGAMES","fuck","nazi","hitler");
        name = noColors(name);
        return improper.parallelStream().anyMatch(name::contains);
    };
    public static int xpn(int level)
    {
        if(level >= 1 && level <= 16)
        {
            return (int)(Math.pow(level, 2) + 6 * level);
        }
        else if(level >= 17 && level <= 31)
        {
            return (int)( 2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        }
        else if(level >= 32)
        {
            return (int)(4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
        else
        {
            return 0;
        }
    }
    public static int bbXPGainMili(float buildTime) {
        return (int) (((0.0014457 * Math.pow(buildTime, 3)) - (0.0263212 * Math.pow(buildTime, 2)) + (buildTime * 2.31398) - (0.125684)) * 1000);
        //0.0014457x^3 - 0.026313x^2 + 2.31398x - 0.125684
    }
    public static String tag(int rank, int lvl) {
        switch (rank) {
            case 7:
                return "[darkgray]<[white]" + lvl + "[darkgray]>[]";
            case 6:
                return "[scarlet]<[white]" + lvl + "[scarlet]>[]";
            case 5:
                return "[royal]<[white]" + lvl + "[royal]>[]";
            case 4:
                return "[lime]<[white]" + lvl + "[lime]>[]";
            case 3:
                return "[gold]<[white]" + lvl + "[gold]>[]";
            case 2:
                return "[sky]<[white]" + lvl + "[sky]>[]";
            case 1:
                return "[accent]<[white]" + lvl + "[accent]>[]";
            case 0:
                return "[lightgray]<[white]" + lvl + "[lightgray]>[]";
            default:
                return "<ERROR>";
        }
    }
    public static String censor(String string) {
        StringBuilder builder = new StringBuilder();
        String sentence[] = string.split(" ");
        JSONObject badList = Main.adata.getJSONObject("badList");
        for (String word: sentence) {
            if (badList.has(word.toLowerCase())) {
                builder.append(word.charAt(0));
                for (int i = 1; i < word.length(); i++) {
                    builder.append("*");
                }
            } else {
                builder.append(word);
            }
            builder.append(" ");
        }
        return builder.toString();
    }
}
/*
if (arg[1].startsWith("#") && arg[1].length() > 3 && Strings.canParseInt(arg[1].substring(1))){
    //run
    int id = Strings.parseInt(arg[1].substring(1));
} else if (arg[1].startsWith("#")){
    player.sendMessage("ID can only contain numbers!");
} else if (netServer.admins.getInfo(arg[1]).timesJoined > 0) {
    //run
} else {
    player.sendMessage("UUID not found!");
}
*/
