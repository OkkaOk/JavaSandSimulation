package elements.solid.movable;

import cellular.CellularMatrix;
import elements.Element;
import elements.solid.Solid;

public abstract class MovableSolid extends Solid
{
    public MovableSolid(int x, int y)
    {
        super(x, y);
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        super.step(matrix);
        pilingRule(matrix);
        //System.out.println(dx + " " + dy);
    }

    void pilingRule(CellularMatrix matrix)
    {
        Element Down = matrix.grid[x][limit(y+1, 0, matrix.grid[0].length - 1)];
        Element DownLeft = matrix.grid[ limit(x-1, 0, matrix.grid.length - 1)][limit(y+1, 0, matrix.grid[0].length - 1)];
        Element DownRight = matrix.grid[ limit(x+1, 0, matrix.grid.length - 1)][limit(y+1, 0, matrix.grid[0].length - 1)];
        if (!canDisplace(Down))
        {
            if (canDisplace(DownRight) && canDisplace(DownLeft))
            {
                switch ((int) Math.floor(Math.random()*2))
                {
                    case 0:
                        dx -= 1;
                        dy += 1;
                        break;
                    case 1:
                        dx += 1;
                        dy += 1;
                        break;
                }
            }
            else if (canDisplace(DownLeft) && !canDisplace(DownRight))
            {
                dx -= 1;
                dy += 1;
            }
            else if (canDisplace(DownRight) && !canDisplace(DownLeft))
            {
                dx += 1;
                dy += 1;
            }
            //velocity.y *= 0.3;
        }
    }
}
