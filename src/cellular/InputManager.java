package cellular;

import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.ElementType;
import elements.EmptyCell;

import java.awt.*;

public class InputManager
{
    private enum BrushType
    {
        Square, Circle, Triangle
    }
    private final int minBrushSize = 1;
    private final int maxBrushSize = 200;
    public int brushSize = 5;
    public BrushType brushType = BrushType.Circle;
    public static ElementType currentElement = ElementType.SAND;

    private boolean paused = false;

    public Point mouse;
    public Point lastMouse;
    public boolean mousePressed = false;
    public int mouseButton = 0;

    Bresenham2 bresenham = new Bresenham2();

    public void drawMouse(Graphics g)
    {
        if(mouse != null)
        {
            g.setColor(Color.white);
            if(brushType == BrushType.Square)
                g.drawRect(mouse.x-brushSize+1, mouse.y-brushSize+1, 2*brushSize-1, 2*brushSize-1);
            else if(brushType == BrushType.Circle)
                g.drawOval(mouse.x - brushSize, mouse.y - brushSize, 2*brushSize, 2*brushSize);
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

    public static void setCurrentElement(ElementType elementType)
    {
        InputManager.currentElement = elementType;
    }

    public void adjustBrushSize(int delta)
    {
        brushSize -= delta;
        if (brushSize > maxBrushSize) brushSize = maxBrushSize;
        if (brushSize < minBrushSize) brushSize = minBrushSize;
    }

    public void handleMouseInput(CellularMatrix matrix)
    {
        if(mouse == null && lastMouse != null)
            mouse = lastMouse;
        if(mousePressed)
        {
            if (mouse.x < matrix.width && mouse.y < matrix.height)
            {
                if(mouseButton == 1)
                {
                    Array<GridPoint2> points = bresenham.line(lastMouse.x, lastMouse.y, mouse.x, mouse.y);
                    for(int i = 0; i < points.size; i++)
                    {
                        GridPoint2 pt = points.get(i);
                        spawnElement(matrix, pt, false);
                    }
                }
                else if(mouseButton == 3)
                {
                    Array<GridPoint2> points = bresenham.line(lastMouse.x, lastMouse.y, mouse.x, mouse.y);
                    for(int i = 0; i < points.size; i++)
                    {
                        GridPoint2 pt = points.get(i);
                        spawnElement(matrix, pt, true);
                    }
                }
            }
        }
    }

    public void spawnElement(CellularMatrix matrix, GridPoint2 pos, boolean delete)
    {
        int brushToElementWidth = brushSize / matrix.elementSize;
        int xStart = (int) (pos.x - brushToElementWidth + 1);
        int xEnd = (int) (pos.x + brushToElementWidth - 1);
        int yStart = (int) (pos.y - brushToElementWidth + 1);
        int yEnd = (int) (pos.y + brushToElementWidth - 1);
        switch (brushType)
        {
            case Square:
                for (int x = xStart; x <= xEnd; x++)
                {
                    for (int y = yStart; y <= yEnd; y++)
                    {
                        int x1 = limit(x, 0, matrix.width-1);
                        int y1 = limit(y, 0, matrix.height-1);

                        Chunk currChunk = matrix.chunkFromCoordinates(x1, y1);
                        currChunk.sleeping = false;
                        currChunk.sleepingNextFrame = false;

                        if(!delete)
                        {
                            if(matrix.grid[x1][y1] instanceof EmptyCell)
                            {
                                matrix.set(this.currentElement.createElementByMatrix(x1, y1));
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

                        Chunk currChunk = matrix.chunkFromCoordinates(x1, y1);
                        currChunk.sleeping = false;
                        currChunk.sleepingNextFrame = false;

                        if(!delete)
                        {
                            if(matrix.grid[x1][y1] instanceof EmptyCell)
                            {
                                matrix.set(this.currentElement.createElementByMatrix(x1, y1));
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
            case Triangle:
                break;
        }
    }
    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }
}
