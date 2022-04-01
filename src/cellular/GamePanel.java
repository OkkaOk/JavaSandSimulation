package cellular;

import cellular.CellularMatrix;
import cellular.Chunk;
import cellular.InputManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import com.badlogic.gdx.math.*;
import elements.ElementType;
import util.FPSLogger;

public class GamePanel extends JPanel implements ActionListener, MouseListener, Runnable, KeyListener, MouseWheelListener
{
    final static int PANEL_WIDTH = 960;
    final static int PANEL_HEIGHT = 540;
    final Dimension PANEL_SIZE = new Dimension(PANEL_WIDTH, PANEL_HEIGHT);

    public BufferedImage screen;
    public int[] pixels;

    Thread gameThread;

    boolean showChunks = false;

    InputManager inputManager = new InputManager();
    CellularMatrix matrix;
    FPSLogger fpsLogger = new FPSLogger();
    Bresenham2 bresenham = new Bresenham2();

    int frameRate;

    GamePanel()
    {
        this.setPreferredSize(PANEL_SIZE);
        this.setBackground(Color.black);
        this.setFocusable(true);

        screen = new BufferedImage(PANEL_WIDTH, PANEL_HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = new int[screen.getWidth() * screen.getHeight()];

        matrix = new CellularMatrix(PANEL_WIDTH, PANEL_HEIGHT);

        gameThread = new Thread(this);
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
        pixels[x+y*screen.getWidth()] = colorValue;
    }

    public void paint(Graphics g)
    {
        //super.paint(g);
        //loadPixels();
        for(int i = 0; i < matrix.grid.length; i++)
        {
            for (int j = 0; j < matrix.grid[0].length; j++)
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
                    g.drawRect(chunk.x, chunk.y, Chunk.size, Chunk.size);
                    if(!chunk.sleeping)
                    {
                        g.setColor(new Color(255,0,0, 100));
                        g.fillRect(chunk.x, chunk.y, Chunk.size, Chunk.size);
                    }
                }
            }
        }
        inputManager.drawMouse(g);

        g.drawString("fps: " + frameRate, 5, 15);
        g.drawString("particles: " + matrix.particles, 5, 25);
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {

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
                //System.out.println(delta);
                inputManager.lastMouse = inputManager.mouse;
                inputManager.mouse = this.getMousePosition();
                inputManager.handleMouseInput(matrix);

                if(!inputManager.getPaused())
                    matrix.stepAll();

                repaint();
                delta--;
                fpsLogger.endFrame();
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
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        inputManager.mousePressed = false;
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
        else if(key == KeyEvent.VK_1)
            InputManager.setCurrentElement(ElementType.SAND);
        else if(key == KeyEvent.VK_2)
            InputManager.setCurrentElement(ElementType.WATER);
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
