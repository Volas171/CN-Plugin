package CN;

import arc.util.Log;

import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.gen.Call;
import org.json.JSONObject;

import java.io.*;

public class Cycle extends Thread{
    private Thread main;

    public Cycle(Thread mainThread) {
        main = mainThread;
    }

    public void run(){
        while (main.isAlive()) {
            try {
                Thread.sleep(60 * 1000);
            } catch (Exception e) {
            }

            for (Player p : Vars.playerGroup.all()) {
                if (Main.currentLogin.containsKey(p.uuid)) {
                    if (byteCode.has(Main.currentLogin.get(p.uuid))) {
                        byteCode.putInt(Main.currentLogin.get(p.uuid), "mp", byteCode.get(Main.currentLogin.get(p.uuid)).getInt("mp"));
                        Call.onInfoToast(p.con, "+1 Minutes Played", 7);
                    } else {
                        p.sendMessage("ERROR - Your account information is not found!");
                    }
                }
            }

            Main.currentKick.forEach((k,p) -> {
                if (p.getInfo().lastKicked < Time.millis()) {
                    Main.currentKick.remove(k);
                }
            });
        }
        Log.info(">>> Cycle Terminated");
    }
}
