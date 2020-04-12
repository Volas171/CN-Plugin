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

            try {
                File file = new File("config\\mods\\database\\settings.json");
                FileWriter out = new FileWriter(file, false);
                PrintWriter pw = new PrintWriter(out);
                pw.println(Main.adata.toString());
                out.close();
            } catch (IOException i) {
                i.printStackTrace();
            }

            for (Player p : Vars.playerGroup.all()) {
                if (Main.currentLogin.containsKey(p.uuid)) {
                    JSONObject data = Main.adata.getJSONObject(Main.currentLogin.get(p.uuid));
                    data.put("mp", data.getInt("mp") + 1);
                    Call.onInfoToast(p.con, "+1 Minutes Played", 7);
                }
            }
        }
        Log.info(">>> Cycle Terminated");
    }
}
