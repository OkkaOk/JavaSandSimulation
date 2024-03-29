package elements.solid.immovable;

import cellular.CellularMatrix;
import elements.Element;
import util.MyVector;

import java.awt.*;

public class Iron extends ImmovableSolid
{
    public Iron(int x, int y)
    {
        super(x, y);
        shortName = "IRON";
        mass = 7860;
        baseColor = new Color(140, 140, 140);
        color = baseColor;
        frictionFactor = 0.9f;
    }

    @Override
    protected boolean actOnElement(Element element, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth)
    {
        return false;
    }
}
