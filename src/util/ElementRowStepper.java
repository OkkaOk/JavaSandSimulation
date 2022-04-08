package util;

import cellular.CellularMatrix;

public class ElementRowStepper implements Runnable
{
    public CellularMatrix matrix;
    public int minRow;
    public int maxRow;

    public ElementRowStepper(CellularMatrix matrix, int minRow, int maxRow)
    {
        this.matrix = matrix;
        this.minRow = minRow;
        this.maxRow = maxRow;
    }

    @Override
    public void run()
    {
        matrix.stepRows(minRow, maxRow);
    }
}
