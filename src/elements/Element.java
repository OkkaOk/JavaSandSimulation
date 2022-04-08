package elements;

import cellular.CellularMatrix;
import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import util.MyVector;

import java.awt.*;

public abstract class Element
{
    public int x;
    public int y;

    public float frictionFactor;
    public boolean isFreeFalling = true;
    public float inertialResistance;
    public int stoppedMovingCount = 0;
    public int stoppedMovingThreshold = 1;
    public float mass;
    public int health = 500;
    public float temperature = 25;
    public float meltingPoint;
    public float boilingPoint;
    public float autoIgnitionTemp;
    public boolean isIgnited = false;
    public float bounciness = 1;

    ElementType elementType;

    public boolean isDead = false;

    public Color baseColor;
    public Color color;

    public String shortName;
    public String description;

    public boolean locked; // Koskee vaan ulkoseiniä ja niitä ei voi poistaa

    public MyVector velocity = new MyVector(0, 0);

    public float dx = 0; // How much to move
    public float dy = 0;

    public boolean stepState;

    public Bresenham2 bresenham = new Bresenham2();

    public Element(int x, int y)
    {
        this.x = x;
        this.y = y;
        stepState = CellularMatrix.stepState;
        elementType = ElementType.valueOf(this.getClass().getSimpleName().toUpperCase());
    }

    public abstract void step(CellularMatrix matrix);

    protected abstract boolean actOnElement(Element element, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth);

    public void applyForce(MyVector force)
    {
        velocity.add(force.div(mass));
    }

    public void swapPositions(CellularMatrix matrix, Element toSwap, int toSwapX, int toSwapY)
    {
        if (x == toSwapX && y == toSwapY)
        {
            return;
        }
        matrix.set(x, y, toSwap);
        matrix.set(toSwapX, toSwapY, this);
    }

    public void moveToLastValidAndSwap(CellularMatrix matrix, Element toSwap, int toSwapX, int toSwapY, MyVector moveToLocation)
    {
        int moveToLocationMatrixX = (int) moveToLocation.x;
        int moveToLocationMatrixY = (int) moveToLocation.y;
        Element thirdNeighbor = matrix.get(moveToLocationMatrixX, moveToLocationMatrixY);
        if (this == thirdNeighbor || thirdNeighbor == toSwap)
        {
            this.swapPositions(matrix, toSwap, toSwapX, toSwapY);
            return;
        }

        if (this == toSwap)
        {
            this.swapPositions(matrix, thirdNeighbor, moveToLocationMatrixX, moveToLocationMatrixY);
            return;
        }

        matrix.set(this.x, this.y, thirdNeighbor);
        matrix.set(toSwapX, toSwapY, this);
        matrix.set(moveToLocationMatrixX, moveToLocationMatrixY, toSwap);
    }

    public void moveToLastValid(CellularMatrix matrix, MyVector moveToLocation)
    {
        if (moveToLocation.getX() == x && moveToLocation.getY() == y)
            return;
        Element toSwap = matrix.get(moveToLocation);
        swapPositions(matrix, toSwap, moveToLocation.getX(), moveToLocation.getY());
    }

    protected void collide(Element first, Element other, boolean firstCalculation)
    {
        MyVector velDiff = first.velocity.copy().sub(other.velocity);
        MyVector posDiff = new MyVector(first.x - other.x, first.y - other.y);
        double dot = velDiff.dot(posDiff);
        double posDiffMagSq = Math.pow(posDiff.mag(), 2);
        MyVector endVelocity = first.velocity.copy().sub(posDiff.mult(2.0 * other.mass / (first.mass + other.mass) * dot / posDiffMagSq));
        if (firstCalculation)
        {
            collide(other, first, false);
        }
        first.velocity = endVelocity.mult(first.bounciness);
    }

    public boolean didNotMove(MyVector formerLocation)
    {
        return (int) formerLocation.x == this.x && (int) formerLocation.y == this.y;
    }

    public boolean stoppedBeyondThreshold()
    {
        return stoppedMovingCount >= stoppedMovingThreshold;
    }

    public void addRandomColor(int red, int green, int blue)
    {
        int r = limit(baseColor.getRed() + (int) (Math.random() * red * (Math.random() > 0.5 ? -1 : 1)), 0, 255);
        int g = limit(baseColor.getGreen() + (int) (Math.random() * green * (Math.random() > 0.5 ? -1 : 1)), 0, 255);
        int b = limit(baseColor.getBlue() + (int) (Math.random() * blue * (Math.random() > 0.5 ? -1 : 1)), 0, 255);

        color = new Color(r, g, b);
    }

    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public boolean actOnOther(Element other, CellularMatrix matrix)
    {
        return false;
    }

    public void checkIfDead(CellularMatrix matrix)
    {
        if (this.health <= 0)
        {
            die(matrix);
        }
    }

    public void die(CellularMatrix matrix)
    {
        die(matrix, ElementType.EMPTYCELL);
    }

    protected void die(CellularMatrix matrix, ElementType type)
    {
        this.isDead = true;
        Element newElement = type.createElementByMatrix(x, y);
        matrix.set(newElement);
        matrix.activateChunkForCoordinates(x, y);
    }

    public void dieAndReplace(CellularMatrix matrix, ElementType type)
    {
        die(matrix, type);
    }

    public boolean receiveHeat(CellularMatrix matrix, int heat)
    {
        if (isIgnited)
            return false;

//        this.flammabilityResistance -= (int) (Math.random() * heat);
        checkIfIgnited();
        return true;
    }

    public boolean receiveCooling(CellularMatrix matrix, int cooling)
    {
        if (isIgnited)
        {
//            this.flammabilityResistance += cooling;
            checkIfIgnited();
            return true;
        }
        return false;
    }

    public void checkIfIgnited()
    {
        if (this.temperature >= autoIgnitionTemp)
        {
            this.isIgnited = true;
        }
        else
        {
            this.isIgnited = false;
        }
    }

    public boolean corrode(CellularMatrix matrix)
    {
        this.health -= 170;
        checkIfDead(matrix);
        return true;
    }
}
