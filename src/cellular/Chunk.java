package cellular;

public class Chunk
{
    public boolean sleeping = false;
    public boolean sleepingNextFrame = true;
    public int x; // Topleft coordinates
    public int y;
    public static int size = 32;
    Chunk(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
//    void draw()
//    {
//        noFill();
//        stroke(255, 255, 255, 100);
//        rect(x*elementWidth, y*elementWidth, chunkSize*elementWidth, chunkSize*elementWidth);
//    }
//    void drawFill()
//    {
//        fill(255, 0, 0, 100);
//        rect(x*elementWidth, y*elementWidth, chunkSize*elementWidth, chunkSize*elementWidth);
//    }
    public void shiftSleep()
    {
        sleeping = sleepingNextFrame;
        sleepingNextFrame = true;
    }
}
