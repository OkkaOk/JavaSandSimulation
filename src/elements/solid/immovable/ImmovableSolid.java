package elements.solid.immovable;

import cellular.CellularMatrix;
import elements.solid.Solid;

public abstract class ImmovableSolid extends Solid
{
    public ImmovableSolid(int x, int y)
    {
        super(x, y);
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        super.step(matrix);
        //System.out.println(dx + " " + dy);
    }
}
