package CN;

import arc.util.Log;
import arc.util.Strings;
import mindustry.core.NetServer;
import mindustry.entities.type.Player;
import mindustry.net.Administration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import static mindustry.Vars.*;

public class byteCode {
    public static String nameR(String name) {return name.replaceAll("\\[", "[[");}
    public static String rankI(int i) {
        switch (i) {
            case 6:
                return "<[scarlet]\uE814[accent]>";
            case 5:
                return "<[royal]\uE828[accent]>";
            case 4:
                return "<[lightgray]\uE80F[accent]>";
            case 3:
                return "<[lime]\uE85B[accent]>";
            case 2:
                return "\uE809";
            case 1:
                return "";
            default:
                return "ERR";
        }
    }
    public static String verifiedI() {
        return "<[sky]\uE848[accent]>";
    }
    public static String ban(String IDuuid, String reason) {
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
                pw.println(dateFormat.format(thisDate) + nameR + " | " + uuid + " | " + reason);
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
            return "[B]Success!\n" + dateFormat.format(thisDate) + nameR + " | " + uuid + " | " + reason;
        }
        Log.err("Ban got past return!");
        return "error";
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
