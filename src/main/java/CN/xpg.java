package CN;

import mindustry.world.Block;

public class xpg {
    //Info
    private Block block;
    private float x;
    private float y;
    private long time;

    public xpg(Block block, float x, float y, long time) {
        this.block = block;
        this.x = x;
        this.y = y;
        this.time = time;
    }
    //get
    public Block getBlock() {return block;}
    public float getx() {return x;}
    public float gety() {return y;}
    public long getTime() {return time;}
}
