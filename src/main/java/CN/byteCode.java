package CN;

import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import mindustry.core.NetServer;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import mindustry.net.Administration;
import org.javacord.api.entity.channel.TextChannel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import static mindustry.Vars.*;

public class byteCode {
    //storage
    public static String[] tips;

    public byteCode() {
        tips = new String[]{
                tips[0] = "Tip #1: When on the [lightgray]Upgrade [white]Menu, \uE850, little colored short arrows will point you towards Upgrade Pads.",
                tips[1] = "Tip #1: When on the [lightgray]Upgrade [white]Menu, \uE850, little colored short arrows will point you towards Upgrade Pads.",
                tips[2] = "Tip #2: You can Overdrive  Weapons to make them shoot faster.",
                tips[3] = "Tip #3: If you disconnect a power source and connect it with Diodes \uF87C, you can prevent them from dying when power crashes.",
                tips[4] = "Tip #4: You can overdrive Upgrade Pads to make spawning faster, this includes the core!"
        };
    }

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
    public static String ban(String IDuuid, String reason, String who) {
        //setup
        Date thisDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("[MM/dd/Y | HH:mm:ss] ");
        String uuid = null;
        String nameR = null;
        boolean proceed = false;

        //ban
        if (IDuuid.startsWith("#") && IDuuid.length() > 3 && Strings.canParseInt(IDuuid.substring(1))) {
            int id = Strings.parseInt(IDuuid.substring(1));
            Player p = playerGroup.getByID(id);
            if (p == null) {
                return "Player ID " + id + " not found.";
            } else if (p.isAdmin) {
                return "[scarlet]Did you really expect to be able to kick an admin?";
            }
            nameR = nameR(p.name);
            uuid = p.uuid;
            proceed = true;
            p.con.kick(reason);
        } else if (IDuuid.startsWith("#")) {
            return "ID can only contain numbers!"; //if contains letters
        } else if (netServer.admins.getInfo(IDuuid).timesJoined > 0) {
            Administration.PlayerInfo p = netServer.admins.getInfo(IDuuid);
            nameR = nameR(p.lastName);
            uuid = IDuuid;
            //check for admin
            for (Administration.PlayerInfo pi : netServer.admins.getAdmins()) {
                if (pi.id.equals(IDuuid)) {
                    return "[scarlet]Did you really expected to ban a admin?";
                }
            }
            proceed = true;
        } else {
            return "UUID not found!"; // not found
        }
        if (proceed) {
            netServer.admins.banPlayer(uuid);
            try {
                File file = new File("bl.cn");
                FileWriter out = new FileWriter(file, true);
                PrintWriter pw = new PrintWriter(out);
                pw.println(dateFormat.format(thisDate) + nameR + " | " + uuid + " | " + reason + " | by: " + who + " ;");
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
            Main.milisecondSinceBan = Time.millis() + 250;
            return "[B]Success!\n" + dateFormat.format(thisDate) + nameR + " | " + reason + " | by: " + who + " ;";
        }
        Log.err("Ban got past return!");
        return "error";
    }
    public static Integer sti(String Input) {
        if (Strings.canParseInt(Input)) {
            return Strings.parseInt(Input);
        }
        return -643; //GAE XD
    }
    public static void aRank(String uuid) {
        if (Main.database.containsKey(uuid)) {
            if (Main.sandbox) {
                if (Main.database.get(uuid).getRank() == 1) {
                    pi d = Main.database.get(uuid);
                    if (d.getTP() > 8 * 60 * 60 && d.getBB() > 25000) {
                        Call.sendMessage("Rank Updated for " + player.name + " [white]to [accent]Active Player[white]!");
                    }
                } else if (Main.database.get(uuid).getRank() == 2) {
                    pi d = Main.database.get(uuid);
                    if (d.getTP() > 24 * 60 * 60 && d.getBB() > 100000) {
                        Call.sendMessage("Rank Updated for " + player.name + " [white]to [gold]Super Active [white]Player!");
                    }
                }
            } else {
                if (Main.database.get(uuid).getRank() == 1) {
                    pi d = Main.database.get(uuid);
                    if (d.getTP() > 8 * 60 * 60 && d.getGP() > 15) {
                        Call.sendMessage("Rank Updated for " + player.name + " [white]to [accent]Active Player[white]!");
                    }
                } else if (Main.database.get(uuid).getRank() == 2) {
                    pi d = Main.database.get(uuid);
                    if (d.getTP() > 24 * 60 * 60 && d.getGP() > 45) {
                        Call.sendMessage("Rank Updated for " + player.name + " [white]to [gold]Super Active [white]Player!");
                    }
                }
            }
        }
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
        return  finalString;}
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
