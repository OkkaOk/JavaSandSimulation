package elements.gas;

import elements.Element;
import cellular.CellularMatrix;

public abstract class Gas extends Element
{
    public Gas(int x, int y)
    {
        super(x, y);
    }
    @Override
    public void step(CellularMatrix matrix)
    {

    }
}
