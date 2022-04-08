package util;

import cellular.GameFrame;
import cellular.InputManager;
import cellular.SouthSelectPanel;
import elements.Element;
import elements.ElementType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ElementButton extends JButton implements ActionListener
{
    ElementType element;

    List<ElementType> elementTypes;
    SouthSelectPanel southSelectPanel;

    public ElementButton(ElementType type) // Buttons that appear on south panel
    {
        element = type;
        Element temp = element.createElementByMatrix(0,0);

        setBackground(temp.baseColor);
        setForeground(new Color(0xf0f0f00f ^ temp.baseColor.getRGB()));
        setText(temp.shortName);
        setMargin(new Insets(0, 0, 0, 0));
        setPreferredSize(new Dimension(50, 20));
        setToolTipText(temp.description);

        addActionListener(this);
        setFocusable(false);
    }

    public ElementButton(List<ElementType> types, SouthSelectPanel southSelectPanel) // Buttons that appear on east panel
    {
        elementTypes = types;
        this.southSelectPanel = southSelectPanel; // Used to remove existing buttons from south panel and add new ones to it
        addActionListener(this);
        setFocusable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        if(element != null)
            InputManager.setCurrentElement(element);
        else if(elementTypes != null)
        {
            southSelectPanel.createButtons(elementTypes);
        }
    }
}
