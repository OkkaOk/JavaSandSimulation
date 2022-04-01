package util;

public class FPSLogger
{
    public int fpsCap = 60;
    long lastTime;
    long now;

    public FPSLogger()
    {

    }
    public int getFPS()
    {
        double diff = now - lastTime;
        if(diff != 0)
        {
            int fps = (int)(1 / diff * 1000000000);
            if (fps < 0) fps = 0;
            if (fps > 60) fps = 60;
            return fps;
        }
        else
            return 60;
    }

    public void startFrame()
    {
        now = System.nanoTime();
    }

    public void endFrame()
    {
        lastTime = now;
    }
}
