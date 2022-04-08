package cellular;

import elements.ElementType;
import util.ElementButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


public class EastSelectPanel extends JPanel
{
    public EastSelectPanel(SouthSelectPanel southSelectPanel)
    {
        initButton(new ElementButton(ElementType.getGases(), southSelectPanel), new ImageIcon("assets/gases.png"), "Gases");
        initButton(new ElementButton(ElementType.getLiquids(), southSelectPanel), new ImageIcon("assets/liquids.png"), "Liquids");
        initButton(new ElementButton(ElementType.getImmovableSolids(), southSelectPanel), new ImageIcon("assets/solids.png"), "Solids");
        initButton(new ElementButton(ElementType.getMovableSolids(), southSelectPanel), new ImageIcon("assets/powders.png"), "Powders");

        this.setPreferredSize(new Dimension(20, GamePanel.PANEL_HEIGHT));
        this.setBackground(Color.black);
        //this.setLayout(new GridLayout(0, 1));
    }

    void initButton(ElementButton button, ImageIcon icon, String tooltip)
    {
        button.setPreferredSize(new Dimension(15, 15));
        button.setBorder(BorderFactory.createLineBorder(Color.white));
        button.setToolTipText(tooltip);
        button.setIcon(icon);
        this.add(button);
    }
}
