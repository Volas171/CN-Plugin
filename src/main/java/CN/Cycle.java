package CN;

import arc.util.Log;

import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.Team;
import mindustry.gen.Call;
import org.json.JSONObject;

import javax.swing.*;
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
                        JSONObject data = byteCode.get(Main.currentLogin.get(p.uuid));
                        byteCode.putInt(Main.currentLogin.get(p.uuid), "mp", data.getInt("mp")+1);
                        Call.onInfoToast(p.con, "+1 Minutes Played", 7);
                        if (p.getTeam().equals(Team.derelict)) {
                            if (data.has("rank") && data.getInt("rank") == 1) {
                                if (data.getInt("mp") > 14) {
                                    p.setTeam(Team.sharded);
                                    p.updateRespawning();
                                    Call.sendMessage("[accent]" + byteCode.noColors(p.name) + " has connected.");
                                    Log.info(byteCode.noColors(p.name) + " : " + p.uuid + " > has connected");
                                }
                            }
                        }
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

            if (!Main.currentLogin.isEmpty()) {
                Main.currentLogin.forEach((k, p) -> {
                    if (p == null) Main.currentLogin.remove(k);
                });
            }

        }
        Log.info(">>> Cycle Terminated");
    }
}
