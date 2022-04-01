package cellular;

import com.badlogic.gdx.math.Bresenham2;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Array;
import elements.Element;
import elements.ElementType;
import elements.EmptyCell;
import elements.solid.immovable.Iron;
import util.MyVector;

public class CellularMatrix
{
    public int width;
    public int height;
    public Element[][] grid;
    public int elementSize = 1;

    public Chunk[][] chunks;

    public int particles = 0;

    Bresenham2 bresenham = new Bresenham2();

    public CellularMatrix(int width, int height)
    {
        this.width = width;
        this.height = height;
        grid = new Element[width][height];
        for (int i = 0; i < grid.length; i++)
        {
            for (int j = 0; j < grid[0].length; j++)
            {
                set(new EmptyCell(i, j));
            }
        }
        chunks = new Chunk[(int)Math.ceil((double)grid.length/(double)Chunk.size)][(int)Math.ceil((double)grid[0].length/(double)Chunk.size)];
        for (int i = 0; i < chunks.length; i++)
        {
            for (int j = 0; j < chunks[0].length; j++)
            {
                chunks[i][j] = new Chunk(i*Chunk.size, j*Chunk.size);
            }
        }

        for (int i=0; i<grid.length; i++) // Tekee seinät
        {
            set(new Iron(i, 0));
            set(new Iron(i, height-1));

            get(i, 0).locked = true;
            get(i, height-1).locked = true;
        }
        for (int j=0; j<grid[0].length; j++)
        {
            set(new Iron(0, j));
            set(new Iron(width-1, j));

            get(0, j).locked = true;
            get(width-1, j).locked = true;
        }
    }

    boolean rightLeft = false;
    public void stepAll()
    {
        for (int a = 0; a < width; a++)
        {
            int i = a;
            if(rightLeft)
                i = width-1-a;
            for (int j = 0; j < height; j++)
            {
                if(chunkFromCoordinates(i, j).sleeping)
                {
                    j+=Chunk.size-1;
                    continue;
                }

                Element curr = get(i, j);
                if (curr.state == 0 || curr.has_been_updated)
                {
                    curr.has_been_updated = false;
                    continue;
                }

                if(curr.wouldMoveNextFrame(this))
                {
                    chunkFromCoordinates(i, j).sleepingNextFrame = false;
                }

                curr.acceleration.add(new MyVector(0, 0.2));
                curr.applyForces();
                curr.step(this);
                move(i, j);
            }
        }
        if(rightLeft)
            rightLeft = false;
        else
            rightLeft = true;
        for(int i=0; i<chunks.length; i++)
        {
            for (int j=0; j<chunks[0].length; j++)
            {
                chunks[i][j].shiftSleep();
            }
        }
    }

    public Chunk chunkFromCoordinates(int x, int y)
    {
        int chunkX = limit((int)Math.floor((double)x / (double)Chunk.size), 0, chunks.length-1);
        int chunkY = limit((int)Math.floor((double)y / (double)Chunk.size), 0, chunks[0].length-1);
        return chunks[chunkX][chunkY];
    }

    public void activateChunksAtBorder(int i, int j)
    {
        if(i % Chunk.size == 0)
            chunkFromCoordinates(i-1, j).sleepingNextFrame = false;
        if(i % Chunk.size == Chunk.size-1)
            chunkFromCoordinates(i+1, j).sleepingNextFrame = false;
        if(j % Chunk.size == 0)
            chunkFromCoordinates(i, j-1).sleepingNextFrame = false;
        if(j % Chunk.size == Chunk.size-1)
            chunkFromCoordinates(i, j+1).sleepingNextFrame = false;
    }

    public Element get(int x, int y)
    {
        x = limit(x, 0, grid.length - 1);
        y = limit(y, 0, grid[0].length - 1);
        return grid[x][y];
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

    public void swapPositions(int i, int j, int x, int y)
    {
        x = limit(x, 0, grid.length - 1);
        y = limit(y, 0, grid[0].length - 1);
        Element temp = grid[x][y];
        set(x, y, grid[i][j]);
        set(i, j, temp);

        grid[x][y].has_been_updated = true;

        chunkFromCoordinates(i, j).sleepingNextFrame = false;
        activateChunksAtBorder(i, j);
        chunkFromCoordinates(x, y).sleepingNextFrame = false;
        activateChunksAtBorder(x, y);
    }

    public void move(int i, int j)
    {
        Element This = grid[i][j];
        int deltaX = Math.round(This.dx);
        int deltaY = Math.round(This.dy);
        Array<GridPoint2> points = bresenham.line(i, j, i + deltaX, j + deltaY);
        int ableX = 0;
        int ableY = 0;
        for (GridPoint2 point : points)
        {
            if (point.x == i && point.y == j)
                continue;
            point.x = limit(point.x, 0, grid.length - 1);
            point.y = limit(point.y, 0, grid[0].length - 1);
            if (grid[point.x][point.y].mass < This.mass && !(grid[point.x][point.y].state == 1 && This.state == 1)) {
                ableX = point.x;
                ableY = point.y;
            }
            else
            {
                // Tahalleen lasken ennenku vaihan niitten paikkoja koska muuten niiden välinen kulma olisi aina 90 astetta.
                collide(i, j, point.x, point.y, true);
                break;
            }
        }
        if (This.dx < 0)
            This.dx -= Math.ceil(This.dx);
        else
            This.dx -= Math.floor(This.dx);
        if (This.dy < 0)
            This.dy -= Math.ceil(This.dy);
        else
            This.dy -= Math.floor(This.dy);
        if (ableX != 0 || ableY != 0)
        {
            swapPositions(i, j, ableX, ableY);
        }
    }

    void collide(int x1, int y1, int x2, int y2, boolean firstCalculation)
    {
        Element This = grid[x1][y1];
        Element other = grid[x2][y2];
        MyVector velDiff = This.velocity.copy().sub(other.velocity);
        MyVector posDiff = new MyVector(x1 - x2, y1 - y2);
        double dot = velDiff.dot(posDiff);
        double posDiffMagSq = Math.pow(posDiff.mag(), 2);
        MyVector endVelocity = This.velocity.copy().sub(posDiff.mult(2.0 * other.mass / (This.mass + other.mass) * dot / posDiffMagSq));
        if (firstCalculation)
        {
            collide(x2, y2, x1, y1, false);
        }
        This.velocity = endVelocity.mult(This.bounciness);
    }

    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }
}
