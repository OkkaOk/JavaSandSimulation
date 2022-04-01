package elements;

import cellular.CellularMatrix;

import java.awt.*;

public class EmptyCell extends Element
{
    public EmptyCell(int x, int y)
    {
        super(x, y);
        state = 0;
        color = Color.black;
    }

    @Override
    public void step(CellularMatrix matrix) {

    }
}
