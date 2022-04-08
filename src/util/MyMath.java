package util;

public class MyMath
{
    public static float random(float min, float max)
    {
        return (float) (min + (max - min) * Math.random());
    }

    public static float map(float value, float start1, float end1, float start2, float end2)
    {
        return start2 + value / (end1 - start1) * (end2 - start2);
    }
}
