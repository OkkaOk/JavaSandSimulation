package elements.liquid;

import elements.Element;
import cellular.CellularMatrix;

public abstract class Liquid extends Element
{
    public Liquid(int x, int y)
    {
        super(x, y);
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        pilingRule(matrix);
        //threeBelowRule(matrix);
        waterRule(matrix);
    }
    void threeBelowRule(CellularMatrix matrix)
    {
        Element Right = matrix.get(x+1, y);
        Element Left = matrix.get(x-1, y);
        boolean threeBelow = (!canDisplace(matrix.get(x, y+1)) && !canDisplace(matrix.get(x+1, y+1)) && !canDisplace(matrix.get(x-1, y+1)));
        if (threeBelow && canDisplace(Left) && canDisplace(Right))
        {
            switch((int) Math.floor(Math.random()*2))
            {
                case 0:
                    dx -= 2;
                    velocity.x -= 0.1;
                    break;
                case 1:
                    dx += 2;
                    velocity.x += 0.1;
                    break;
            }
        }
        else if (threeBelow && canDisplace(Left) && !canDisplace(Right))
        {
            dx -= 2;
            velocity.x -= 0.1;
        }
        else if (threeBelow && canDisplace(Right) && !canDisplace(Left))
        {
            dx += 2;
            velocity.x += 0.1;
        }
    }

    void pilingRule(CellularMatrix matrix)
    {
        Element Down = matrix.get(x, y+1);;
        Element DownLeft = matrix.get(x-1, y+1);;
        Element DownRight = matrix.get(x+1, y+1);;
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

    void waterRule(CellularMatrix matrix)
    {
        Element Left = matrix.get(x-1, y);;
        Element Right = matrix.get(x+1, y);;
        Element Down = matrix.get(x, y+1);;
        if(!canDisplace(Down) && canDisplace(Left) && canDisplace(Right))
        {
            switch((int) Math.floor(Math.random()*2))
            {
                case 0:
                    dx += 5;
                    break;
                case 1:
                    dx -= 5;
                    break;
            }
        }
        else if(!canDisplace(Down) && canDisplace(Left) && !canDisplace(Right))
        {
            dx -= 5;
        }
        else if(!canDisplace(Down) && !canDisplace(Left) && canDisplace(Right))
        {
            dx += 5;
        }
    }
}
