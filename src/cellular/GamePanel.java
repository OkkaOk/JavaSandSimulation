package cellular;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.*;
import elements.Element;
import util.ElementRowStepper;
import util.FPSLogger;
import util.MyVector;

public class GamePanel extends JPanel implements MouseListener, Runnable, KeyListener, MouseWheelListener
{
    final static int PANEL_WIDTH = 960;
    final static int PANEL_HEIGHT = 540;
    final Dimension PANEL_SIZE = new Dimension(PANEL_WIDTH, PANEL_HEIGHT);

    public static final int pixelSize = 2;

    public BufferedImage screen;
    public int[] pixels;

    Thread gameThread;
    public int numThreads = 12;

    boolean showChunks = false;

    InputManager inputManager = new InputManager();
    public CellularMatrix matrix;
    FPSLogger fpsLogger = new FPSLogger();
    Bresenham2 bresenham = new Bresenham2();

    int frameRate;
    public static int frameCount=0;

    GamePanel()
    {
        this.setPreferredSize(PANEL_SIZE);
        this.setBackground(Color.black);
        this.setFocusable(true);

        screen = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = new int[screen.getWidth() * screen.getHeight()];

        matrix = new CellularMatrix(PANEL_WIDTH/pixelSize, PANEL_HEIGHT/pixelSize);

        gameThread = new Thread(this);
        gameThread.setName("Game Thread");
        gameThread.start();

        addMouseListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);
    }

    public void loadPixels()
    {
        pixels = screen.getRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, null, 0, PANEL_WIDTH);
    }

    public void updatePixels()
    {
        screen.setRGB(0, 0, PANEL_WIDTH, PANEL_HEIGHT, pixels, 0, PANEL_WIDTH);
    }

    private void setPixel(int x, int y, int colorValue)
    {
        x *= pixelSize;
        y *= pixelSize;
        for(int i = 0; i < pixelSize; i++)
        {
            for(int j = 0; j < pixelSize; j++)
            {
                pixels[(x+i)+(y+j)*screen.getWidth()] = colorValue;
            }
        }
    }

    public void paint(Graphics g)
    {
        //super.paint(g);
        //loadPixels();
        for(int i = 0; i < matrix.width; i++)
        {
            for (int j = 0; j < matrix.height; j++)
            {
                setPixel(i, j, matrix.grid[i][j].color.getRGB());
            }
        }
        updatePixels();
        g.drawImage(screen, 0, 0, this);

        if(showChunks)
        {
            for(int i = 0; i < matrix.chunks.length; i++)
            {
                for (int j = 0; j < matrix.chunks[0].length; j++)
                {
                    g.setColor(new Color(255,255,255, 100));
                    Chunk chunk = matrix.chunks[i][j];
                    g.drawRect(chunk.x*pixelSize, chunk.y*pixelSize, Chunk.size*pixelSize, Chunk.size*pixelSize);
                    if(!chunk.sleeping)
                    {
                        g.setColor(new Color(255,0,0, 100));
                        g.fillRect(chunk.x*pixelSize, chunk.y*pixelSize, Chunk.size*pixelSize, Chunk.size*pixelSize);
                    }
                    else if(!chunk.sleepingNextFrame)
                    {
                        g.setColor(new Color(0,255,0, 100));
                        g.fillRect(chunk.x*pixelSize, chunk.y*pixelSize, Chunk.size*pixelSize, Chunk.size*pixelSize);
                    }
                }
            }
        }
        inputManager.drawMouse(g);

        g.drawString("fps: " + frameRate, 5, 15);
        g.drawString("particles: " + matrix.particles, 5, 25);
        if(inputManager.mouse != null)
        {
            Element mouseOver = matrix.get(inputManager.mouse.x, inputManager.mouse.y);
            if(mouseOver != null)
            {
                g.drawString("Mass: " + mouseOver.mass + "  vx: " + mouseOver.velocity.x + " vy: " + mouseOver.velocity.y + " " + mouseOver.isFreeFalling, 5, 35);
                g.drawString("dx: " + mouseOver.dx + " dy: " + mouseOver.dy + " stopped: " + mouseOver.stoppedMovingCount, 5, 45);
                if(inputManager.mouse != null)
                    g.drawString("x: " + inputManager.mouse.x + " y: " + inputManager.mouse.y, 5, 55);
            }
        }

        if(inputManager.getPaused())
        {
            g.setFont(new Font("Serif", Font.BOLD, 36));
            String pauseText = "PAUSED";
            int xOff = g.getFontMetrics().stringWidth(pauseText) / 2;
            g.drawString(pauseText, PANEL_WIDTH/2 - xOff, PANEL_HEIGHT/2);
        }
    }

    @Override
    public void run()
    {
        long lastTime = System.nanoTime();
        double ns = 1000000000 / fpsLogger.fpsCap;
        double delta = 0;
        while(true)
        {
            fpsLogger.startFrame();
            long now = System.nanoTime();
            double diff = now - lastTime;
            frameRate = fpsLogger.getFPS();
            delta += (now - lastTime) / ns;
            lastTime = now;
            if(delta >= 1)
            {
                repaint();

                inputManager.lastMouse = inputManager.mouse;
                inputManager.mouse = this.getMousePosition();
                if(inputManager.mouse != null)
                {
                    inputManager.mouse.x /= pixelSize;
                    inputManager.mouse.y /= pixelSize;
                }

                Point m = inputManager.mouse;
                Point pm = inputManager.lastMouse;
                if(m != null && pm != null) inputManager.mouseDiff = new MyVector(m.x-pm.x, m.y-pm.y);

                inputManager.handleMouseInput(matrix);

//                if(!inputManager.getPaused())
//                    matrix.stepAll();
                if(!inputManager.getPaused())
                {
                    int rowsPerThread = (PANEL_WIDTH / pixelSize) / numThreads;
                    List<Thread> threads = new ArrayList<>(numThreads);
                    int offset = (int) (Math.random() * 50/pixelSize * (Math.random() > 0.5 ? -1 : 1));

                    Thread newThread0 = new Thread(new ElementRowStepper(matrix,0,rowsPerThread+offset));
                    threads.add(newThread0);
                    Thread newThread1 = new Thread(new ElementRowStepper(matrix,(numThreads-1)*rowsPerThread+offset, PANEL_WIDTH/pixelSize));
                    threads.add(newThread1);
                    for (int i = 1; i < numThreads-1; i++)
                    {
                        Thread newThread = new Thread(new ElementRowStepper(matrix,i*rowsPerThread+offset,(i+1)*rowsPerThread+offset));
                        threads.add(newThread);
                    }
                    CellularMatrix.stepState = !CellularMatrix.stepState;
                    matrix.shiftChunks();
                    if(CellularMatrix.stepState)
                    {
                        startAndWaitOnGivenThreads(threads, false);
                        startAndWaitOnGivenThreads(threads, true);
                    }
                    else
                    {
                        startAndWaitOnGivenThreads(threads, true);
                        startAndWaitOnGivenThreads(threads, false);
                    }
                }

                delta--;
                frameCount++;
                fpsLogger.endFrame();
            }
        }
    }

    private void startAndWaitOnGivenThreads(List<Thread> threads, boolean even)
    {
        for(int i = 0; i < threads.size(); i++)
        {
            if((i % 2 == 0) == even)
                threads.get(i).start();
        }
        for(int i = 0; i < threads.size(); i++)
        {
            if((i % 2 == 0) == even)
            {
                try
                {
                    threads.get(i).join();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public int limit(int value, int min, int max)
    {
        return Math.max(min, Math.min(value, max));
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        inputManager.mousePressed = true;
        inputManager.mouseButton = e.getButton();

        if(inputManager.brushType == InputManager.BrushType.Rectangle && inputManager.rectangleStart == null && inputManager.mouse != null)
            inputManager.rectangleStart = inputManager.mouse;
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        inputManager.mousePressed = false;

        if(inputManager.brushType == InputManager.BrushType.Rectangle && inputManager.rectangleEnd == null && inputManager.mouse != null)
            inputManager.rectangleEnd = inputManager.mouse;

        if(inputManager.rectangleStart != null && inputManager.rectangleEnd != null)
        {
            if(e.getButton() == 1)
                inputManager.spawnElementWithGrid(matrix, false);
            else if(e.getButton() == 3)
                inputManager.spawnElementWithGrid(matrix, true);
            inputManager.rectangleEnd = null;
            inputManager.rectangleStart = null;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {

    }

    @Override
    public void mouseExited(MouseEvent e)
    {

    }

    @Override
    public void keyTyped(KeyEvent e)
    {

    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_SPACE)
            inputManager.togglePaused();
        else if(key == KeyEvent.VK_C)
            showChunks = !showChunks;
        else if(key == KeyEvent.VK_SHIFT)
            inputManager.cycleMouseModes();
        else if(key == KeyEvent.VK_CONTROL)
            inputManager.cycleBrushType();
        else if(key == KeyEvent.VK_F)
        {
            inputManager.setPaused(true);
            matrix.stepAll();
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        inputManager.adjustBrushSize(e.getWheelRotation());
    }
}
