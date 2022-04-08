package elements.liquid;

import cellular.CellularMatrix;
import elements.ElementType;

import java.awt.*;

public class Water extends Liquid
{
    public Water(int x, int y)
    {
        super(x, y);
        shortName = "WATR";
        mass = 1000;
        baseColor = new Color(0, 0, 255);
        addRandomColor(0, 10, 20);
        bounciness = 0.7f;
        dispersionRate = 5;

        description = "Water. Freezes at 0°C and boils at 100°C.";
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat)
    {
        dieAndReplace(matrix, ElementType.COAL);
        return true;
    }
}
