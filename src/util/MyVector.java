package util;

public class MyVector
{
    public double x;
    public double y;

    public MyVector(double i, double j)
    {
        x = i;
        y = j;
    }

    public int getX()
    {
        return (int) x;
    }

    public int getY()
    {
        return (int) y;
    }

    public MyVector copy()
    {
        return new MyVector(x, y);
    }

    public MyVector sub(MyVector other)
    {
        x -= other.x;
        y -= other.y;
        return this;
    }

    public MyVector add(MyVector other)
    {
        x += other.x;
        y += other.y;
        return this;
    }

    public MyVector div(double scalar)
    {
        x /= scalar;
        y /= scalar;
        return this;
    }

    public MyVector mult(double scalar)
    {
        x *= scalar;
        y *= scalar;
        return this;
    }

    public MyVector normalize()
    {
        return mult(1 / mag());
    }

    public MyVector rotate(double radians)
    {
        double oldX = x;
        x = x * Math.cos(radians) - y * Math.sin(radians);
        y = oldX * Math.sin(radians) + y * Math.cos(radians);

        return this;
    }

    public double mag()
    {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public double dot(MyVector other)
    {
        return x*other.x + y*other.y;
    }

    public double angleBetween(MyVector other)
    {
        return Math.cos(dot(other) / (mag() * other.mag()));
    }
}
