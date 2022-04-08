package cellular;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.Element;
import elements.ElementType;
import elements.EmptyCell;
import util.MyVector;

import java.awt.*;

public class InputManager
{
    private enum MouseMode
    {
        SPAWN, VELOCITY
    }
    public enum BrushType
    {
        Square, Circle, Rectangle
    }
    private final int minBrushSize = 1;
    private final int maxBrushSize = 200;
    public int brushSize = 5;
    public BrushType brushType = BrushType.Circle;
    public MouseMode mouseMode = MouseMode.SPAWN;
    public static ElementType currentElement = ElementType.SAND;

    public Point rectangleStart;
    public Point rectangleEnd;

    private boolean paused = false;

    public Point mouse;
    public Point lastMouse;
    public MyVector mouseDiff;
    public boolean mousePressed = false;
    public int mouseButton = 0;

    Bresenham2 bresenham = new Bresenham2();

    public void drawMouse(Graphics g)
    {
        if(mouse != null)
        {
            g.setColor(Color.white);
            int size = GamePanel.pixelSize;
            int mx = mouse.x * size;
            int my = mouse.y * size;
            int d = 2*brushSize-size;
            if(brushType == BrushType.Square)
                g.drawRect(mx-brushSize+size, my-brushSize+size, d-1, d-1);
            else if(brushType == BrushType.Circle)
                g.drawOval(mx-brushSize+size, my-brushSize+size, d, d);
            else if(brushType == BrushType.Rectangle && rectangleStart != null)
            {
                Point start = new Point(Math.min(rectangleStart.x*size, mx), Math.min(rectangleStart.y*size, my));
                Point end = new Point(Math.max(rectangleStart.x*size, mx), Math.max(rectangleStart.y*size, my));
                g.drawRect(start.x, start.y, end.x-start.x, end.y-start.y);
            }
        }
    }

    public void togglePaused()
    {
        paused = !paused;
    }

    public boolean getPaused()
    {
        return paused;
    }

    public void setPaused(boolean state)
    {
        paused = state;
    }

    public static void setCurrentElement(ElementType elementType)
    {
        InputManager.currentElement = elementType;
    }

    public void adjustBrushSize(int delta)
    {
        brushSize -= delta*GamePanel.pixelSize;
        if (brushSize > maxBrushSize) brushSize = maxBrushSize;
        if (brushSize < minBrushSize*GamePanel.pixelSize) brushSize = minBrushSize*GamePanel.pixelSize;
    }

    public void handleMouseInput(CellularMatrix matrix)
    {
        if(mouse == null && lastMouse != null && mouseDiff != null)
            mouse = toBound(matrix, new Point(lastMouse.x + mouseDiff.getX()*10, lastMouse.y + mouseDiff.getY()*10));
        if(mousePressed)
        {
            if (mouse.x < matrix.width && mouse.y < matrix.height)
            {
                Array<GridPoint2> points = bresenham.line(lastMouse.x, lastMouse.y, mouse.x, mouse.y);
                if(mouseButton == 1)
                {
                    if(brushType == BrushType.Rectangle) return;
                    if(mouseMode == MouseMode.SPAWN)
                    {
                        for (GridPoint2 point : points)
                        {
                            spawnElement(matrix, point, false);
                        }
                    }
                }
                else if(mouseButton == 3)
                {
                    if(brushType == BrushType.Rectangle) return;
                    for(GridPoint2 point : points)
                    {
                        spawnElement(matrix, point, true);
                    }
                }
            }
        }
    }

    public Point toBound(CellularMatrix matrix, Point point)
    {
        double currX = point.x;
        double currY = point.y;
        while (!matrix.isWithinBounds((int) currX, (int) currY))
        {
            currX -= mouseDiff.normalize().x;
            currY -= mouseDiff.normalize().y;
        }

        return new Point((int) currX, (int) currY);
    }

    public void spawnElement(CellularMatrix matrix, GridPoint2 pos, boolean delete)
    {
        int brushToElementWidth = brushSize / GamePanel.pixelSize;
        int xStart = pos.x - brushToElementWidth + 1;
        int xEnd = pos.x + brushToElementWidth - 1;
        int yStart = pos.y - brushToElementWidth + 1;
        int yEnd = pos.y + brushToElementWidth - 1;
        switch (brushType)
        {
            case Square:
                for (int x = xStart; x <= xEnd; x++)
                {
                    for (int y = yStart; y <= yEnd; y++)
                    {
                        int x1 = limit(x, 0, matrix.width-1);
                        int y1 = limit(y, 0, matrix.height-1);

                        matrix.activateChunkForCoordinates(x1, y1);

                        if(!delete)
                        {
                            if(matrix.grid[x1][y1] instanceof EmptyCell)
                            {
                                matrix.set(currentElement.createElementByMatrix(x1, y1));
                                matrix.particles++;
                            }
                        }
                        else
                        {
                            if(matrix.grid[x1][y1] instanceof EmptyCell)
                                continue;
                            if(!matrix.grid[x1][y1].locked)
                            {
                                matrix.set(new EmptyCell(x1, y1));
                                matrix.particles--;
                            }
                        }
                    }
                }
                break;
            case Circle:
                for (int x = xStart; x <= xEnd; x++)
                {
                    for (int y = yStart; y <= yEnd; y++)
                    {
                        int x1 = limit(x, 0, matrix.width-1);
                        int y1 = limit(y, 0, matrix.height-1);
                        int xDiff = pos.x - x1;
                        int yDiff = pos.y - y1;

                        if(Math.sqrt(xDiff*xDiff + yDiff*yDiff) > brushToElementWidth)
                            continue;

                        matrix.activateChunkForCoordinates(x1, y1);

                        if(!delete)
                        {
                            if(matrix.grid[x1][y1] instanceof EmptyCell)
                            {
                                matrix.set(currentElement.createElementByMatrix(x1, y1));
                                matrix.particles++;
                            }
                        }
                        else
                        {
                            if(matrix.grid[x1][y1] instanceof EmptyCell)
                                continue;
                            if(!matrix.grid[x1][y1].locked)
                            {
                                matrix.set(new EmptyCell(x1, y1));
                                matrix.particles--;
                            }
                        }
                    }
                }
                break;
        }
    }
    public void spawnElementWithGrid(CellularMatrix matrix, boolean delete)
    {
        spawnElementWithGrid(matrix, rectangleStart, rectangleEnd, delete);
    }
    public void spawnElementWithGrid(CellularMatrix matrix, Point a, Point b, boolean delete)
    {
        Point start = new Point(Math.min(a.x, b.x), Math.min(a.y, b.y));
        Point end = new Point(Math.max(a.x, b.x), Math.max(a.y, b.y));
        if(start == null || end == null) return;
        for (int x = start.x; x <= end.x; x++)
        {
            for (int y = start.y; y <= end.y; y++)
            {
                int x1 = limit(x, 0, matrix.width-1);
                int y1 = limit(y, 0, matrix.height-1);

                matrix.activateChunkForCoordinates(x1, y1);
                Element curr = matrix.get(x1, y1);
                if(!delete)
                {
                    if(curr instanceof EmptyCell)
                    {
                        matrix.set(currentElement.createElementByMatrix(x1, y1));
                        matrix.particles++;
                    }
                }
                else
                {
                    if(curr instanceof EmptyCell)
                        continue;
                    if(!curr.locked)
                    {
                        matrix.set(new EmptyCell(x1, y1));
                        matrix.particles--;
                    }
                }
            }
        }
    }

    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public void cycleBrushType()
    {
        switch (brushType)
        {
            case Square:
                this.brushType = BrushType.Circle;
                break;
            case Circle:
                this.brushType = BrushType.Rectangle;
                break;
            case Rectangle:
                this.brushType = BrushType.Square;
                break;
        }
    }

    public void cycleMouseModes()
    {
        switch (mouseMode)
        {
            case SPAWN:
                this.mouseMode = MouseMode.VELOCITY;
                break;
            case VELOCITY:
                this.mouseMode = MouseMode.SPAWN;
                break;
        }
    }
}
