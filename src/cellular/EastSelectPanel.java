package cellular;

import elements.ElementType;
import util.ElementButton;

import javax.swing.*;
import java.awt.*;


public class EastSelectPanel extends JPanel
{
    public EastSelectPanel(SouthSelectPanel southSelectPanel)
    {
        ElementButton gasesButton = new ElementButton(ElementType.getGases(), southSelectPanel);
        ElementButton liquidsButton = new ElementButton(ElementType.getLiquids(), southSelectPanel);
        ElementButton immovableSolidsButton = new ElementButton(ElementType.getImmovableSolids(), southSelectPanel);
        ElementButton movableSolidsButton = new ElementButton(ElementType.getMovableSolids(), southSelectPanel);
        this.add(gasesButton);
        this.add(liquidsButton);
        this.add(immovableSolidsButton);
        this.add(movableSolidsButton);

        this.setPreferredSize(new Dimension(20, GamePanel.PANEL_HEIGHT));
        this.setBackground(Color.black);
        this.setFocusable(true);
        this.setLayout(new FlowLayout(FlowLayout.LEADING));
    }
}
