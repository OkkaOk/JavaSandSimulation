package util;

import cellular.GameFrame;
import cellular.InputManager;
import cellular.SouthSelectPanel;
import elements.Element;
import elements.ElementType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ElementButton extends JButton implements ActionListener
{
    ElementType element;

    List<ElementType> elementTypes;
    SouthSelectPanel southSelectPanel;

    public ElementButton(ElementType type)
    {
        element = type;
        Element temp = element.createElementByMatrix(0,0);
        setBackground(temp.color);
        setText(temp.shortName);
        addActionListener(this);
    }

    public ElementButton(List<ElementType> types, SouthSelectPanel southSelectPanel)
    {
        elementTypes = types;
        this.southSelectPanel = southSelectPanel;
        addActionListener(this);
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
