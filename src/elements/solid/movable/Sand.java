package elements.solid.movable;

import cellular.CellularMatrix;

import java.awt.*;

public class Sand extends MovableSolid
{

    public Sand(int x, int y)
    {
        super(x, y);
        shortName = "SAND";
        mass = 1602;
        state = 1;
        color = new Color(207, 226, 122);
        bounciness = 0.4;
    }
}