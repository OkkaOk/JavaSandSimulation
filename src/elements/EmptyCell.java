package elements;

import cellular.CellularMatrix;
import util.MyVector;

import java.awt.*;

public class EmptyCell extends Element
{
    public EmptyCell(int x, int y)
    {
        super(x, y);
        baseColor = Color.black;
        color = baseColor;
    }

    @Override
    public void step(CellularMatrix matrix)
    {

    }

    @Override
    protected boolean actOnElement(Element element, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth)
    {
        return true;
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat)
    {
        return false;
    }

    @Override
    public boolean corrode(CellularMatrix matrix)
    {
        return false;
    }
}
