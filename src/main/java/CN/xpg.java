package CN;

import mindustry.world.Block;

public class xpg {
    //Info
    private Block block;
    private long time;

    public xpg(Block block, long time) {
        this.block = block;
        this.time = time;
    }
    //get
    public Block getBlock() {return block;}
    public long getTime() {return time;}
}
