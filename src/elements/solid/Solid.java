package elements.solid;

import cellular.CellularMatrix;
import elements.Element;

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
}