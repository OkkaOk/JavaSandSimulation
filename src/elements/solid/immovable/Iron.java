package elements.solid.immovable;

import java.awt.*;

public class Iron extends ImmovableSolid
{
    public Iron(int x, int y)
    {
        super(x, y);
        shortName = "IRON";
        mass = 7860;
        color = new Color(140, 140, 140);
    }
}
