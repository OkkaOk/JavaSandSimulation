package elements;

import cellular.CellularMatrix;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.gas.Gas;
import elements.liquid.Liquid;
import elements.solid.Solid;
import util.MyVector;

import java.awt.*;

public class Particle extends Element
{
    public ElementType containedElementType;

    public Particle(int x, int y, MyVector velocity, ElementType elementType, Color color, boolean isIgnited)
    {
        super(x, y);
        if (ElementType.PARTICLE.equals(elementType))
        {
            throw new IllegalStateException("Containing element cannot be particle");
        }
        this.containedElementType = elementType;

        this.velocity.x = velocity.x;
        this.velocity.y = velocity.y;

        this.color = color;
        this.isIgnited = isIgnited;
        if (isIgnited)
        {
//            this.flammabilityResistance = 0;
        }
    }

    public Particle(int x, int y, MyVector velocity, Element sourceElement)
    {
        super(x, y);
        if (ElementType.PARTICLE.equals(sourceElement.elementType))
        {
            throw new IllegalStateException("Containing element cannot be particle");
        }
        this.containedElementType = sourceElement.elementType;

        this.velocity.x = velocity.x;
        this.velocity.y = velocity.y;

        this.color = sourceElement.color;
        this.isIgnited = sourceElement.isIgnited;
        if (isIgnited)
        {
//            this.flammabilityResistance = 0;
        }
    }

    @Override
    public boolean receiveHeat(CellularMatrix matrix, int heat)
    {
        return false;
    }

    @Override
    public void dieAndReplace(CellularMatrix matrix, ElementType elementType)
    {
        particleDeathAndSpawn(matrix);
    }

    private void particleDeathAndSpawn(CellularMatrix matrix)
    {
        Element currentLocation = matrix.get(x, y);
        if (currentLocation == this || currentLocation instanceof EmptyCell)
        {
            die(matrix);
            Element newElement = containedElementType.createElementByMatrix(x, y);
            newElement.color = this.color;

            newElement.isIgnited = this.isIgnited;
//            if (newElement.isIgnited)
//                newElement.flammabilityResistance = 0;

            matrix.set(newElement);
            matrix.activateChunkForCoordinates(x, y);
        }
        else
        {
            int yIndex = 0;
            while (true)
            {
                Element elementAtNewPos = matrix.get(x, y + yIndex);
                if (elementAtNewPos == null)
                {
                    break;
                }
                else if (elementAtNewPos instanceof EmptyCell)
                {
                    die(matrix);
                    matrix.set(containedElementType.createElementByMatrix(x, y + yIndex));
                    matrix.activateChunkForCoordinates(x, y + yIndex);
                    break;
                }
                yIndex++;
            }
        }
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        if (stepState == matrix.stepState) return;
        stepState = !stepState;

        velocity.add(matrix.gravity);

        dx += velocity.x;
        dy += velocity.y;

        move(matrix);
    }

    public void move(CellularMatrix matrix)
    {
        int deltaX = (int) (dx > 0 ? Math.floor(dx) : Math.ceil(dx));
        int deltaY = (int) (dy > 0 ? Math.floor(dy) : Math.ceil(dy));
        Array<GridPoint2> points = bresenham.line(x, y, x + deltaX, y + deltaY);
        MyVector lastValidLocation = new MyVector(x, y);
        for (GridPoint2 point : points)
        {
            if (matrix.isWithinBounds(point.x, point.y))
            {
                Element checkingElement = matrix.get(point.x, point.y);
                if (checkingElement == this) continue;

                boolean stopped = actOnElement(checkingElement, point.x, point.y, matrix, point == points.get(points.size - 1), point == points.get(0), lastValidLocation, 0);
                if (stopped) break;

                lastValidLocation.x = point.x;
                lastValidLocation.y = point.y;
            }
            else
            {
                matrix.set(ElementType.EMPTYCELL.createElementByMatrix(x, y));
                return;
            }
        }
        dx -= deltaX;
        dy -= deltaY;
    }

    @Override
    protected boolean actOnElement(Element element, int modifiedMatrixX, int modifiedMatrixY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth)
    {
        if (element instanceof EmptyCell || element instanceof Particle)
        {
            if (isFinal)
                swapPositions(matrix, element, modifiedMatrixX, modifiedMatrixY);
            else
                return false;
        }
        else if (element instanceof Liquid || element instanceof Solid)
        {
            moveToLastValid(matrix, lastValidLocation);
            dieAndReplace(matrix, containedElementType);
            return true;
        }
        else if (element instanceof Gas)
        {
            if (isFinal)
            {
                moveToLastValidAndSwap(matrix, element, modifiedMatrixX, modifiedMatrixY, lastValidLocation);
                return true;
            }
            return false;
        }
        return false;
    }
}
