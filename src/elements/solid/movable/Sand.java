package elements.solid.movable;

import java.awt.*;

public class Sand extends MovableSolid
{

    public Sand(int x, int y)
    {
        super(x, y);
        shortName = "SAND";
        description = "Sand. Melts into glass in high temperatures.";

        baseColor = new Color(207, 226, 122);
        addRandomColor(20, 20, 20);

        mass = 1602;
        bounciness = 0.4f;
        inertialResistance = 0.1f;
        frictionFactor = 0.8f;
    }
}