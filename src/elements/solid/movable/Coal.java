package elements.solid.movable;

import java.awt.*;

public class Coal extends MovableSolid
{

    public Coal(int x, int y)
    {
        super(x, y);
        shortName = "COAL";
        description = "Coal. Starts burning when it touches fire.";

        mass = 1100;
        baseColor = new Color(40, 35, 31, 255);
        addRandomColor(0, 5, 5);
        bounciness = 0.3f;
        inertialResistance = 0.8f;
        frictionFactor = 0.4f;
    }
}