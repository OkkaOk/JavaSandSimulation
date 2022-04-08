package elements.liquid;

import cellular.CellularMatrix;
import elements.Element;
import elements.ElementType;

import java.awt.*;

public class Acid extends Liquid
{
    public int corrosionCount = 3;
    public Acid(int x, int y)
    {
        super(x, y);
        shortName = "ACID";
        mass = 2885;
        baseColor = new Color(150, 255, 150);
        addRandomColor(10, 0, 10);
        bounciness = 0.7f;
        dispersionRate = 5;

        description = "Fluoroantimonic acid (HSbF6). Reacts violently with water and creates a lot of heat";
        // Fluoroantimonic acid thermally decomposes when heated, generating free hydrogen fluoride gas and liquid antimony pentafluoride.
        // At a temperature of 40 °C, fluoroantimonic acid will release HF into the gas phase.
    }

    @Override
    public boolean actOnOther(Element other, CellularMatrix matrix)
    {
        if (other == null) return false;
//        if(other instanceof Water)
//        {
//            other.receiveHeat(matrix, 100);
//            corrosionCount--;
//            return true;
//        }
        boolean corroded = other.corrode(matrix);
        if (corroded) corrosionCount -= 1;
        if (corrosionCount <= 0)
        {
            dieAndReplace(matrix, ElementType.HF);
            return true;
        }
        return false;
    }

    @Override
    public boolean corrode(CellularMatrix matrix)
    {
        return false;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat)
    {
        return false;
    }
}
