import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.text.*;

package com.github.ShiftAC.RISCVSimulator;

class SLabel extends JLabel
{
    public SLabel(String text, Container frm)
    {
        super(text);
        setOpaque(true);
        setBackground(Color.white);
        frm.add(this);
    }
}

class SScrollPane extends JPanel
{
    boolean scrollEnabled;
    int pixelPerScroll = 100;

    private MouseWheelListener mwlScr = new MouseWheelListener()
    {
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            if (!scrollEnabled)
            {
                return;
            }
            int r = e.getWheelRotation();
            
            Point p = MouseInfo.getPointerInfo().getLocation();
        
            Component arr[] = getComponents();
            for (Component com : arr)
            {
                com.setBounds
                    (com.getX(),
                    com.getY() - r * 100,
                    com.getWidth(),
                    com.getHeight());
            }
        }
    };
    
    public SScrollPane(boolean scroll)
    {
        super();
        setLayout(null);
        scrollEnabled = scroll;
        addMouseWheelListener(mwlScr);
    }
}

class SLogicalPane extends ArrayList<Component>
{
    int xPos, yPos;
    
    public SLogicalPane()
    {
        super();
        xPos = 0;
        yPos = 0;
    }

    public void setBounds(int x, int y, int w, int h)
    {
        xPos = x;
        yPos = y;
    }
    
    public void addTo(JLayeredPane pne, Integer layer)
    {
        for (Component com : this)
        {
            pne.add(com, layer);
            com.setBounds(
                com.getX() + xPos, 
                com.getY() + yPos, 
                com.getWidth(), 
                com.getHeight());
        }
    }
    
    public void removeFrom(Container pne)
    {
        for (Component com : this)
        {
            pne.remove(com);
        }
    }
    
    public void setEnabled(boolean flag)
    {
        for (Component com : this)
        {
            com.setEnabled(flag);
        }
    }
}

class SButton extends JButton
{
    public SButton(String text, Container frm)
    {
        super(text);
        if (frm != null)
        {
            frm.add(this);
        }
    }
}

class SImageButton extends SButton
{
    protected ImageIcon normal;
    protected ImageIcon press;
    protected ImageIcon rollover;
    public SImageButton(Container frm, 
                        ImageIcon icoNormal, 
                        ImageIcon icoPress,
                        ImageIcon icoRollover)
    {
        super("", null);
        
        setOpaque(false);
        setContentAreaFilled(false);
        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        setBorder(null);
        
        setIcon(icoNormal);
        setPressedIcon(icoPress);
        setRolloverIcon(icoRollover);
        
        if (frm != null)
        {
            frm.add(this);
        }
    }
}

class SMenuItem extends JMenuItem
{
    public SMenuItem(String name, int key, JMenu parent)
    {
        super(name, key);
        setAccelerator(KeyStroke.getKeyStroke(key, ActionEvent.CTRL_MASK));
        parent.add(this);
    }
}

class SMenu extends JMenu
{
    public SMenu(String name, char mne, JMenuBar bar)
    {
        super(name);
        setMnemonic(mne);
        bar.add(this);
    }
}

class SFrame extends JFrame
{
    JLayeredPane pneFrame = new JLayeredPane();

    SFrame me = this;
    public SFrame(String title)
    {
        super(title);
        initialize();
    }

    private void initialize()
    {
        setLayout(null);
        pneFrame.setLayout(new GridLayout(1, 1));
        super.add(pneFrame);
    }

    @Override
    public Component add(Component com)
    {
        pneFrame.add(com, JLayeredPane.DEFAULT_LAYER);
        return com;
    }

    public Component addToLayer(Component com, Integer layer)
    {
        pneFrame.add(com, layer);
        return com;
    }

    @Override
    public void remove(Component com)
    {
        pneFrame.remove(com);
    }

    @SuppressWarnings("deprecation")
    public void resizeCenterScreen(int width, int height)
    {
        int pwidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int pheight = Toolkit.getDefaultToolkit().getScreenSize().height;
        setBounds((pwidth - width) / 2, (pheight - height) / 2, width, height);
    }

    @Override
    public void repaint()
    {
        pneFrame.setBounds(0, 0, 
            (int)getSize().getWidth(), (int)getSize().getHeight());
        super.repaint();
    }
}