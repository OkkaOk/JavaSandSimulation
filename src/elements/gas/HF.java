package elements.gas;


import cellular.CellularMatrix;

import java.awt.*;

public class HF extends Gas
{
    public HF(int x, int y)
    {
        super(x, y);
        shortName = "HF";
        mass = 1.15f;
        baseColor = new Color(90, 161, 90);
        addRandomColor(10, 20, 10);
        dispersionRate = 2;

        description = "Put description here";
    }

    @Override
    public boolean corrode(CellularMatrix matrix)
    {
        return false;
    }
}
