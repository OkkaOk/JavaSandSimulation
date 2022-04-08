package elements.solid;

import cellular.CellularMatrix;
import elements.Element;
import util.MyVector;

public abstract class Solid extends Element
{
    public Solid(int x, int y)
    {
        super(x, y);
    }

    @Override
    public void step(CellularMatrix matrix)
    {

    }

    /*@Override
    public boolean wouldMoveNextFrame(CellularMatrix matrix)
    {
        return false;
    }*/
}