package CN;

import arc.util.Log;

import arc.util.Time;
import mindustry.Vars;
import mindustry.entities.type.Player;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import org.json.JSONObject;

import java.io.*;
import java.util.Random;

public class Cycle extends Thread{
    public static int upTime = 0;

    private Thread main;

    public Cycle(Thread mainThread) {
        main = mainThread;
    }

    public void run(){
        try {
            Thread.sleep(60 * 1000);
            upTime++;
        } catch (Exception e) {
        }
        while (main.isAlive()) {
            try {
                Thread.sleep(60 * 1000);
                upTime++;
            } catch (Exception e) {
            }

            JSONObject data = new JSONObject();
            for (Player p : Vars.playerGroup.all()) {
                if (Main.currentLogin.containsKey(p.uuid)) {
                    if (p.getTeam() == Team.sharded) {
                        data = Main.adata.getJSONObject(Main.currentLogin.get(p.uuid));
                        data.put("mp", data.getInt("mp") + 1);
                        Call.onInfoToast(p.con, "+1 Minutes Played", 7);
                        if (data.has("verified") && data.getInt("verified") == 0 && data.has("mp") && data.getInt("mp") == 15) {
                            p.sendMessage("Verification complete! You can now play!");
                            p.setTeam(Team.sharded);
                            p.updateRespawning();
                            Call.sendMessage("[accent]" + byteCode.noColors(p.name) + " has connected.");
                        }

                        int y = data.getInt("mp") / 60;
                        float z = (float) data.getInt("mp") / 60;
                        if ((float) y == z) {
                            Call.sendMessage("Congratulations to " + p.name + " [white]for being active for " + y + " hours!");
                        }
                    } else if (p.getTeam() == Team.derelict) {
                        data = Main.adata.getJSONObject(Main.currentLogin.get(p.uuid));
                        if (data.getInt("mp") < 16) data.put("mp", data.getInt("mp") + 1);
                        if (data.has("verified") && data.getInt("verified") == 0 && data.has("mp") && data.getInt("mp") == 15) {
                            p.sendMessage("Verification complete! You can now play!");
                            p.setTeam(Team.sharded);
                            p.updateRespawning();
                            Call.sendMessage("[accent]" + byteCode.noColors(p.name) + " has connected.");
                        }
                    }
                }
            }

            int y = upTime / 5;
            float z = (float) upTime / 5;
            if ((float) y == z) {
                Random rand = new Random();
                Call.sendMessage("[accent]"+byteCode.tips[rand.nextInt(byteCode.tips.length)]);
            }

            try {
                File file = new File("config\\mods\\database\\settings.cn");
                FileWriter out = new FileWriter(file, false);
                PrintWriter pw = new PrintWriter(out);
                pw.println(Main.adata.toString());
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
            }
        }
        Log.info(">>> Cycle Terminated");
    }
}
