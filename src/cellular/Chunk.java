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

    public void shiftSleep()
    {
        sleeping = sleepingNextFrame;
        sleepingNextFrame = true;
    }
}
