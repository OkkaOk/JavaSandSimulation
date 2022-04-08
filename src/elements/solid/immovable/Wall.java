package elements.solid.immovable;

import cellular.CellularMatrix;
import elements.Element;
import util.MyVector;

import java.awt.*;

public class Wall extends ImmovableSolid
{
    public Wall(int x, int y)
    {
        super(x, y);
        shortName = "WALL";
        mass = 7860;
        baseColor = new Color(140, 140, 140);
        color = baseColor;
        frictionFactor = 0.9f;
        locked = true;
    }

    @Override
    protected boolean actOnElement(Element element, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth)
    {
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
