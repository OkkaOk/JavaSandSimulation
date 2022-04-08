package elements.liquid;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.Element;
import cellular.CellularMatrix;
import elements.ElementType;
import elements.EmptyCell;
import elements.Particle;
import elements.gas.Gas;
import elements.solid.Solid;
import util.MyVector;

import java.util.ArrayList;
import java.util.List;

public abstract class Liquid extends Element
{
    public int dispersionRate;

    public Liquid(int x, int y)
    {
        super(x, y);
        stoppedMovingThreshold = 10;
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        if (stepState == CellularMatrix.stepState) return;
        stepState = !stepState;

        velocity.add(matrix.gravity);
        if (isFreeFalling) velocity.x *= .8;

        MyVector formerLocation = new MyVector(x, y);
        move(matrix);

        stoppedMovingCount = didNotMove(formerLocation) ? stoppedMovingCount + 1 : 0;
        if (stoppedMovingCount > stoppedMovingThreshold)
        {
            stoppedMovingCount = stoppedMovingThreshold;
        }
        if (isIgnited || !stoppedBeyondThreshold())
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

    @Override
    protected boolean actOnElement(Element element, int newX, int newY, CellularMatrix matrix, boolean isFinal, boolean isFirst, MyVector lastValidLocation, int depth)
    {
        boolean acted = actOnOther(element, matrix);
        if (acted) return true;

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
        else if (element instanceof Liquid liquidNeighbor)
        {
            if (compareDensities(liquidNeighbor))
            {
                if (isFinal)
                {
                    if (Math.random() > 0.8f)
                        velocity.x *= -1;
                    velocity.y = 1.1;
                    moveToLastValidAndSwap(matrix, liquidNeighbor, newX, newY, lastValidLocation);
                    return true;
                }
                else
                {
                    lastValidLocation.x = newX;
                    lastValidLocation.y = newY;
                    return false;
                }
            }
            if (depth > 0)
            {
                return true;
            }
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

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            if (isFirst)
                velocity.y = getAverageVelOrGravity(velocity.y, element.velocity.y);
            else
                velocity.y = 2;

            element.velocity.y = velocity.y;
            velocity.x *= frictionFactor;

            Element diagonalNeighbor = matrix.get(x + additionalX, y + additionalY);
            if (diagonalNeighbor != null)
            {
                boolean stoppedDiagonally = iterateToAdditional(matrix, x + additionalX, y + additionalY, distance, lastValidLocation);
                if (!stoppedDiagonally)
                {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(x + additionalX, y);
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor)
            {
                boolean stoppedAdjacently = iterateToAdditional(matrix, x + additionalX, y, distance, lastValidLocation);
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
        else if (element instanceof Solid)
        {
            if (depth > 0)
            {
                return true;
            }
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

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            if (isFirst)
                velocity.y = getAverageVelOrGravity(velocity.y, element.velocity.y);
            else
                velocity.y = 2;

            element.velocity.y = velocity.y;
            velocity.x *= frictionFactor;

            Element diagonalNeighbor = matrix.get(lastValidLocation.getX() + additionalX, lastValidLocation.getY() + additionalY);
            if (diagonalNeighbor != null)
            {
                boolean stoppedDiagonally = iterateToAdditional(matrix, lastValidLocation.getX() + additionalX, lastValidLocation.getY() + additionalY, distance, lastValidLocation);
                if (!stoppedDiagonally)
                {
                    isFreeFalling = true;
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(lastValidLocation.getX() + additionalX, lastValidLocation.getY());
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor)
            {
                boolean stoppedAdjacently = iterateToAdditional(matrix, lastValidLocation.getX() + additionalX, lastValidLocation.getY(), distance, lastValidLocation);
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

    private boolean iterateToAdditional(CellularMatrix matrix, int startingX, int startingY, int distance, MyVector lastValid)
    {
        int distanceModifier = distance > 0 ? 1 : -1;
        MyVector lastValidLocation = lastValid.copy();
        for (int i = 0; i <= Math.abs(distance); i++)
        {
            int modifiedX = startingX + i * distanceModifier;
            Element neighbor = matrix.get(modifiedX, startingY);
            if (neighbor == null)
                return true;

            boolean acted = actOnOther(neighbor, matrix);
            if (acted) return false;

            boolean isFirst = i == 0;
            boolean isFinal = i == Math.abs(distance);
            if (neighbor instanceof EmptyCell)
            {
                if (isFinal)
                {
                    swapPositions(matrix, neighbor, modifiedX, startingY);
                    return false;
                }
                lastValidLocation.x = modifiedX;
                lastValidLocation.y = startingY;
            }
            else if (neighbor instanceof Liquid liquidNeighbor)
            {
                if (isFinal)
                {
                    if (compareDensities(liquidNeighbor))
                    {
                        moveToLastValidAndSwap(matrix, liquidNeighbor, modifiedX, startingY, lastValidLocation);
                        return false;
                    }
                }
            }
            else if (neighbor instanceof Solid)
            {
                if (isFirst)
                {
                    return true;
                }
                moveToLastValid(matrix, lastValidLocation);
                return false;
            }
        }
        return true;
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

    private int getAdditional(double val)
    {
        if (val < -.1f)
            return (int) Math.floor(val);
        else if (val > .1f)
            return (int) Math.ceil(val);
        else
            return 0;
    }

    private boolean compareDensities(Liquid other)
    {
        return (mass > other.mass && other.y >= y);
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
}
