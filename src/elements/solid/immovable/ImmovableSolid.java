package elements.solid.immovable;

import cellular.CellularMatrix;
import elements.Element;
import elements.solid.Solid;
import util.MyVector;

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

    /*@Override
    public boolean wouldMoveNextFrame(CellularMatrix matrix)
    {
        return false;
    }*/
}
