package elements.solid.movable;

import cellular.CellularMatrix;
import cellular.GamePanel;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.Element;
import elements.ElementType;
import elements.EmptyCell;
import elements.Particle;
import elements.gas.Gas;
import elements.liquid.Liquid;
import elements.solid.Solid;
import util.MyVector;

import java.util.ArrayList;
import java.util.List;

public abstract class MovableSolid extends Solid
{
    public MovableSolid(int x, int y)
    {
        super(x, y);
        stoppedMovingThreshold = 5;
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        if (stepState == matrix.stepState) return;
        stepState = !stepState;

        velocity.add(matrix.gravity);
        if (isFreeFalling) velocity.mult(0.95); // 0.95 causes terminal velocity to be ~3.8

//        velocity.add(new MyVector(matrix.width/2-x, matrix.height/2-y).div(1000)); // fun

        MyVector formerLocation = new MyVector(x, y);
        move(matrix);

        stoppedMovingCount = didNotMove(formerLocation) && !isIgnited ? stoppedMovingCount + 1 : 0;
        if (stoppedMovingCount > stoppedMovingThreshold)
        {
            stoppedMovingCount = stoppedMovingThreshold;
        }
        if (isFreeFalling || isIgnited || !stoppedBeyondThreshold())
        {
            matrix.activateChunkForCoordinates(x, y);
            matrix.activateChunkForCoordinates(formerLocation.getX(), formerLocation.getY());
        }
    }

    public void move(CellularMatrix matrix)
    {
        dx += velocity.x;
        dy += velocity.y;

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

    // returns if it got stopped by an element
    @Override
    protected boolean actOnElement(Element element, int newX, int newY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth)
    {
        if (element instanceof EmptyCell || element instanceof Particle)
        {
            setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
            if (isFinal)
            {
                isFreeFalling = true;
                swapPositions(matrix, element, newX, newY);
            }
            else
                return false;
        }
        else if (element instanceof Liquid) // Problem, doesn't care about speed
        {
            if (depth > 0)
            {
                isFreeFalling = true;
                setAdjacentNeighborsFreeFalling(matrix, depth, lastValidLocation);
                swapPositions(matrix, element, newX, newY);
            }
            else
            {
                isFreeFalling = true;
                moveToLastValidAndSwap(matrix, element, newX, newY, lastValidLocation);
                return true;
            }

        }
        else if (element instanceof Solid)
        {
            if (depth > 0) return true;
            if (isFinal)
            {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (isFreeFalling)
            {
                double absY = Math.max(Math.abs(velocity.y) / 2, 1);
                if (velocity.x < 0) velocity.x = -absY;
                else if (velocity.x > 0) velocity.x = absY;
                else velocity.x = Math.random() > 0.5 ? absY : -absY;
            }
            MyVector normalizedVel = velocity.copy().normalize();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            if (isFirst)
                velocity.y = getAverageVelOrGravity(velocity.y, element.velocity.y);
            else
                velocity.y = 2;

            element.velocity.y = velocity.y;
            velocity.x *= frictionFactor * element.frictionFactor;

            Element diagonalNeighbor = matrix.get(lastValidLocation.getX() + additionalX, lastValidLocation.getY() + additionalY);
            if (diagonalNeighbor != null)
            {
                boolean stoppedDiagonally = actOnElement(diagonalNeighbor, lastValidLocation.getX() + additionalX, lastValidLocation.getY() + additionalY, matrix, true, false, lastValidLocation, depth + 1);
                if (!stoppedDiagonally)
                {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(lastValidLocation.getX() + additionalX, lastValidLocation.getY());
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor)
            {
                boolean stoppedAdjacently = actOnElement(adjacentNeighbor, lastValidLocation.getX() + additionalX, lastValidLocation.getY(), matrix, true, false, lastValidLocation, depth + 1);
                if (stoppedAdjacently) velocity.x *= -1;
                if (!stoppedAdjacently)
                {
                    isFreeFalling = false;
                    return true;
                }
            }

            isFreeFalling = false;

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        else if (element instanceof Gas)
        {
            if (isFinal)
            {
                moveToLastValidAndSwap(matrix, element, newX, newY, lastValidLocation);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean canDisplace(Element other)
    {
        if (other instanceof Solid)
            return false;
        return this.mass > other.mass;
    }

    private void setAdjacentNeighborsFreeFalling(CellularMatrix matrix, int depth, MyVector lastValidLocation)
    {
        if (depth > 0) return;
        List<Element> neighbors = new ArrayList<>();
        neighbors.add(matrix.get(lastValidLocation.getX() + 1, lastValidLocation.getY()));
        neighbors.add(matrix.get(lastValidLocation.getX() - 1, lastValidLocation.getY()));
        neighbors.add(matrix.get(lastValidLocation.getX(), lastValidLocation.getY() + 1));
        neighbors.add(matrix.get(lastValidLocation.getX(), lastValidLocation.getY() - 1));

        for (Element neighbor : neighbors)
        {
            if (neighbor instanceof Solid)
            {
                boolean wasSet = setElementFreeFalling(neighbor);
                if (wasSet)
                {
                    matrix.activateChunkForElement(neighbor);
                }
            }
        }
    }

    private boolean setElementFreeFalling(Element element)
    {
        element.isFreeFalling = Math.random() > element.inertialResistance || element.isFreeFalling;
        return element.isFreeFalling;
    }

    private int getAdditional(double val)
    {
        if (val < -.1f)
            return (int) Math.floor(val);
        else if (val > .1f)
            return (int) Math.ceil(val);
        else
            return 0;
    }

    private double getAverageVelOrGravity(double velocity, double otherVel)
    {
        if (otherVel < 4)
            return 4;

        double avg = (velocity + otherVel) / 2;
        if (avg < 0)
            return avg;
        else
            return Math.max(avg, 4);
    }
}
