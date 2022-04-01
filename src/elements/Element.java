package elements;

import cellular.CellularMatrix;
import util.MyVector;

import java.awt.*;

public abstract class Element implements Cloneable
{
    public int x;
    public int y;

    public String shortName;

    public Color color;

    public double mass;
    public int state;
    public double bounciness = 1;

    public boolean locked; // Koskee vaan ulkoseiniä ja niitä ei voi poistaa

    public MyVector force = new MyVector(0, 0);
    public MyVector acceleration = new MyVector(0, 0);
    public MyVector velocity = new MyVector(0, 0);

    public float dx=0; // How much to move
    public float dy=0;

    public boolean has_been_updated = false;

    public Element(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public abstract void step(CellularMatrix matrix);

    public void applyForces()
    {
        acceleration.add(force.div(mass));
        velocity.add(acceleration);
        dx += velocity.x;
        dy += velocity.y;

        force.mult(0);
        acceleration.mult(0);
    }

    public boolean canDisplace(Element other)
    {
        if (this.state == 1 && other.state == 1)
            return false;
        if (this.mass > other.mass)
            return true;
        return false;
    }

    public boolean wouldMoveNextFrame(CellularMatrix matrix)
    {
        Element Down = matrix.get(x, y+1);
        Element DownLeft = matrix.get(x-1, y+1);
        Element DownRight = matrix.get(x+1, y+1);
        if (state == 1 || state == 2)
        {
            if (!canDisplace(Down))
            {
                if (canDisplace(DownRight) || canDisplace(DownLeft))
                    return true;
            }
            else return true;
        }
        return false;
    }

    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public Element copy() throws CloneNotSupportedException
    {
        return (Element) clone();
    }
}
