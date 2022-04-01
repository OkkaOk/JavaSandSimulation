package elements.liquid;

import java.awt.*;

public class Water extends Liquid
{
    public Water(int x, int y)
    {
        super(x, y);
        shortName = "WATR";
        mass = 1000;
        state = 2;
        color = new Color(0, 0, 255);
        bounciness = 0.7;
    }
}
