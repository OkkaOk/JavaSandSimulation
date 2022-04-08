package elements.gas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.Element;
import cellular.CellularMatrix;
import elements.ElementType;
import elements.EmptyCell;
import elements.Particle;
import elements.liquid.Liquid;
import elements.solid.Solid;
import util.MyVector;

public abstract class Gas extends Element
{
    int dispersionRate;

    public Gas(int x, int y)
    {
        super(x, y);
        stoppedMovingThreshold = 20;
    }

    @Override
    public void step(CellularMatrix matrix)
    {
        if (stepState == CellularMatrix.stepState) return;
        stepState = !stepState;

        velocity.sub(matrix.gravity);
        velocity.mult(0.95); // 0.95 causes terminal velocity to be ~3.8

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
            if (isFinal)
            {
                swapPositions(matrix, element, newX, newY);
            }
            else
            {
                return false;
            }
        }
        else if (element instanceof Gas gasNeighbor)
        {
            if (compareGasDensities(gasNeighbor))
            {
                swapGasForDensities(matrix, gasNeighbor, newX, newY, lastValidLocation);
                return false;
            }

            if (depth > 0)
                return true;

            if (isFinal)
            {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }

            if(velocity.x < 0) velocity.x = -2;
            else if(velocity.x > 0) velocity.x = 2;
            else velocity.x = Math.random() > 0.5 ? -2 : 2;

            MyVector normalizedVel = velocity.copy().normalize();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

                    int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            element.velocity.y = velocity.y;
            velocity.x *= frictionFactor;

            Element diagonalNeighbor = matrix.get(x + additionalX, y + additionalY);
            if (diagonalNeighbor != null)
            {
                boolean stoppedDiagonally = iterateToAdditional(matrix, x + additionalX, y, distance);
                if (!stoppedDiagonally)
                {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(x + additionalX, y);
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor)
            {
                boolean stoppedAdjacently = iterateToAdditional(matrix, x + additionalX, y, distance);
                if (stoppedAdjacently) velocity.x *= -1;
                if (!stoppedAdjacently)
                {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        else if (element instanceof Liquid)
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
            if (element.isFreeFalling)
                return true;

            double absY = Math.max(Math.abs(velocity.y) / 31, 105);
            velocity.x = velocity.x < 0 ? -absY : absY;

            MyVector normalizedVel = velocity.copy().normalize();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            element.velocity.y = velocity.y;
            velocity.x *= frictionFactor;

            Element diagonalNeighbor = matrix.get(x + additionalX, y + additionalY);
            if (diagonalNeighbor != null)
            {
                boolean stoppedDiagonally = iterateToAdditional(matrix, x + additionalX, y, distance);
                if (!stoppedDiagonally)
                {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(x + additionalX, y);
            if (adjacentNeighbor != null && adjacentNeighbor != diagonalNeighbor)
            {
                boolean stoppedAdjacently = iterateToAdditional(matrix, x + additionalX, y, distance);
                if (stoppedAdjacently) velocity.x *= -1;
                if (!stoppedAdjacently)
                {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        else if (element instanceof Solid)
        {
            if (depth > 0)
                return true;

            if (isFinal)
            {
                moveToLastValid(matrix, lastValidLocation);
                return true;
            }
            if (element.isFreeFalling)
                return true;

            double absY = Math.max(Math.abs(velocity.y) / 31, 105);
            velocity.x = velocity.x < 0 ? -absY : absY;

            MyVector normalizedVel = velocity.copy().normalize();
            int additionalX = getAdditional(normalizedVel.x);
            int additionalY = getAdditional(normalizedVel.y);

            int distance = additionalX * (Math.random() > 0.5 ? dispersionRate + 2 : dispersionRate - 1);

            element.velocity.y = velocity.y;
            velocity.x *= frictionFactor;

            Element diagonalNeighbor = matrix.get(x + additionalX, y + additionalY);
            if (diagonalNeighbor != null)
            {
                boolean stoppedDiagonally = iterateToAdditional(matrix, x + additionalX, y + additionalY, distance);
                if (!stoppedDiagonally)
                {
                    return true;
                }
            }

            Element adjacentNeighbor = matrix.get(x + additionalX, y);
            if (adjacentNeighbor != null)
            {
                boolean stoppedAdjacently = iterateToAdditional(matrix, x + additionalX, y, distance);
                if (stoppedAdjacently) velocity.x *= -1;
                if (!stoppedAdjacently)
                {
                    return true;
                }
            }

            moveToLastValid(matrix, lastValidLocation);
            return true;
        }
        return false;
    }

    private boolean iterateToAdditional(CellularMatrix matrix, int startingX, int startingY, int distance)
    {
        int distanceModifier = distance > 0 ? 1 : -1;
        MyVector lastValidLocation = new MyVector(x, y);
        for (int i = 0; i <= Math.abs(distance); i++)
        {
            Element neighbor = matrix.get(startingX + i * distanceModifier, startingY);
            boolean acted = actOnOther(neighbor, matrix);
            if (acted) return false;
            boolean isFirst = i == 0;
            boolean isFinal = i == Math.abs(distance);
            if (neighbor == null) continue;
            if (neighbor instanceof EmptyCell || neighbor instanceof Particle)
            {
                if (isFinal)
                {
                    swapPositions(matrix, neighbor, startingX + i * distanceModifier, startingY);
                    return false;
                }
                lastValidLocation.x = startingX + i * distanceModifier;
                lastValidLocation.y = startingY;
            }
            else if (neighbor instanceof Gas gasNeighbor)
            {
                if (compareGasDensities(gasNeighbor))
                {
                    swapGasForDensities(matrix, gasNeighbor, startingX + i * distanceModifier, startingY, lastValidLocation);
                    return false;
                }
            }
            else if (neighbor instanceof Solid || neighbor instanceof Liquid)
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

    private boolean compareGasDensities(Gas other)
    {
        return (mass > other.mass && other.y >= y); // ||  (density < neighbor.density && neighbor.matrixY >= matrixY);
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

    private void swapGasForDensities(CellularMatrix matrix, Gas neighbor, int neighborX, int neighborY, MyVector lastValidLocation) {
        velocity.y = -1;
        moveToLastValidAndSwap(matrix, neighbor, neighborX, neighborY, lastValidLocation);
    }
}
