package cellular;

import elements.ElementType;
import util.ElementButton;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SouthSelectPanel extends JPanel
{
    JButton clearButton = new JButton("C");
    public SouthSelectPanel(CellularMatrix matrix)
    {
        createButtons(ElementType.getMovableSolids());

        this.setPreferredSize(new Dimension(GamePanel.PANEL_WIDTH-20, 30));
        this.setBackground(Color.black);
        this.setLayout(new FlowLayout(FlowLayout.RIGHT));
        setFocusable(false);

        clearButton.setPreferredSize(new Dimension(20, 20));
        clearButton.setMargin(new Insets(0, 0, 0, 0));
        clearButton.setFocusable(false);
        clearButton.addActionListener(event -> matrix.createMatrix());
    }

    public void createButtons(List<ElementType> elementTypes)
    {
        removeAll();
        for (ElementType elementType : elementTypes)
        {
            if(elementType == ElementType.WALL) continue;
            ElementButton button = new ElementButton(elementType);
            add(button);
        }
        add(clearButton);
        add(Box.createHorizontalStrut(10));
        validate();
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        repaint();
    }
}
