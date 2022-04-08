package cellular;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame
{
    GamePanel myPanel;
    SouthSelectPanel southSelectPanel;
    EastSelectPanel eastSelectPanel;

    public GameFrame()
    {
        myPanel = new GamePanel();
        southSelectPanel = new SouthSelectPanel(myPanel.matrix);
        eastSelectPanel = new EastSelectPanel(southSelectPanel);

        this.add(myPanel, BorderLayout.CENTER);
        this.add(eastSelectPanel, BorderLayout.EAST);
        this.add(southSelectPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Falling Sand Game");
        this.setResizable(false);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
