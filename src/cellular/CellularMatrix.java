package cellular;

import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.Element;
import elements.ElementType;
import elements.EmptyCell;
import elements.solid.immovable.Iron;
import elements.solid.immovable.Wall;
import elements.solid.movable.Sand;
import util.MyMath;
import util.MyVector;

public class CellularMatrix
{
    public int width;
    public int height;
    public Element[][] grid;

    public Chunk[][] chunks;

    public MyVector gravity = new MyVector(0, 0.2);

    public int particles = 0;

    public static boolean stepState = true;

    public CellularMatrix(int width, int height)
    {
        this.width = width;
        this.height = height;

        createMatrix();

        chunks = new Chunk[(int)Math.ceil((float)width/Chunk.size)][(int)Math.ceil((float)height/Chunk.size)];
        for (int i = 0; i < chunks.length; i++)
        {
            for (int j = 0; j < chunks[0].length; j++)
            {
                chunks[i][j] = new Chunk(i*Chunk.size, j*Chunk.size);
            }
        }
//        Element test = new Sand(width/2, height/2);
//        test.velocity.x = 15;
//        activateChunkForCoordinates(width/2, height/2);
//        set(test);
    }

    public void createMatrix()
    {
        grid = new Element[width][height];
        particles = 0;
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                grid[i][j] = null;
                set(new EmptyCell(i, j));
            }
        }
        for (int i = 0; i < width; i++) // Tekee seinät
        {
            set(new Wall(i, 0));            // North
            set(new Wall(i, height-1));     // South
        }
        for (int j = 0; j < height; j++)
        {
            set(new Wall(0, j));            // West
            set(new Wall(width-1, j));      // East
        }
    }

    public void stepAll()
    {
        shiftChunks();
        stepState = !stepState;
        for (int a = 0; a < width; a++)
        {
            int i = a;
            if(stepState)
                i = width-1-a;
            for (int j = 0; j < height; j++)
            {
                if(getChunkForCoordinates(i, j).sleeping)
                {
                    j+=Chunk.size-1;
                    continue;
                }
                Element curr = get(i, j);
                if(curr == null || curr instanceof EmptyCell) continue; // Null happens for a brief time when clearing the matrix
                curr.step(this);
            }
        }
    }

    public void stepRows(int minRow, int maxRow)
    {
        for(int i = minRow; i < maxRow; i++)
        {
            for(int b = 0; b < height; b++)
            {
                int j = b;
                if(stepState)
                    j = height-1-b; // TODO: Some chunk problems are caused by this
                if(getChunkForCoordinates(i, j).sleeping)
                {
//                    b+=Chunk.size-1;
                    continue;
                }
                Element curr = get(i, j);
                if(curr == null) continue; // Null happens for a brief time when clearing the matrix
                curr.step(this);
            }
        }
    }

    public void shiftChunks()
    {
        for (Chunk[] chunkCol : chunks)
            for (Chunk chunk : chunkCol)
                chunk.shiftSleep();
    }

    public Chunk getChunkForElement(Element element)
    {
        return getChunkForCoordinates(element.x, element.y);
    }

    public Chunk getChunkForCoordinates(int x, int y)
    {
        if(isWithinBounds(x, y))
        {
            int chunkX = (int)Math.floor((double)x / (double)Chunk.size);
            int chunkY = (int)Math.floor((double)y / (double)Chunk.size);
            return chunks[chunkX][chunkY];
        }
        return null;
    }

    public void activateChunkForElement(Element element)
    {
        activateChunkForCoordinates(element.x, element.y);
    }

    public void activateChunkForCoordinates(int x, int y)
    {
        if (isWithinBounds(x, y))
        {
            if (x % Chunk.size == 0)
            {
                Chunk chunk = getChunkForCoordinates(x - 1 , y);
                if (chunk != null) chunk.sleepingNextFrame = false;
            }
            if (x % Chunk.size == Chunk.size - 1)
            {
                Chunk chunk = getChunkForCoordinates(x + 1 , y);
                if (chunk != null) chunk.sleepingNextFrame = false;
            }
            if (y % Chunk.size == 0)
            {
                Chunk chunk = getChunkForCoordinates(x, y - 1);
                if (chunk != null) chunk.sleepingNextFrame = false;
            }
            if (y % Chunk.size == Chunk.size - 1)
            {
                Chunk chunk = getChunkForCoordinates(x, y + 1);
                if (chunk != null) chunk.sleepingNextFrame = false;
            }
            getChunkForCoordinates(x, y).sleepingNextFrame = false;
        }
    }


    public Element get(int x, int y)
    {
        if(!isWithinBounds(x, y))
            return null;
        return grid[x][y];
    }

    public Element get(MyVector pos)
    {
        return get(pos.getX(), pos.getY());
    }

    public void set(int x, int y, Element element)
    {
        grid[x][y] = element;
        grid[x][y].x = x;
        grid[x][y].y = y;
    }

    public void set(Element element)
    {
        grid[element.x][element.y] = element;
    }

    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    public boolean isWithinBounds(int x, int y)
    {
        return (x < width && x >= 0 && y < height && y >= 0);
    }
}
