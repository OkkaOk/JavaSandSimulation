package cellular;

import elements.ElementType;
import util.ElementButton;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SouthSelectPanel extends JPanel
{
    public SouthSelectPanel()
    {
        createButtons(ElementType.getMovableSolids());

        this.setPreferredSize(new Dimension(GamePanel.PANEL_WIDTH-20, 30));
        this.setBackground(Color.black);
        this.setLayout(new FlowLayout(FlowLayout.RIGHT));
        this.setFocusable(true);
    }

    public void createButtons(List<ElementType> elementTypes)
    {
        removeAll();
        for (ElementType elementType : elementTypes)
        {
            System.out.println(elementType.clazz);
            ElementButton button = new ElementButton(elementType);
            add(button);
        }
        validate();
        System.out.println(getComponents().length);
    }
}
