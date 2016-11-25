package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.text.*;

public class ConsoleFrame extends JFrame
{
    JTextArea txtConsole;
    JScrollPane pneConsole;

    JTextArea txtCommand;
    JScrollPane pneCommand;
    boolean firstInput = true;
    
    SYSstdin stdin = new SYSstdin();

    String initConsole = "RISCVSimulator Console " + Util.version + "\n\n";

    public ConsoleFrame()
    {
        super("控制台");
        this.setLayout(null);
        //this.setLayout(new GridLayout(1, 1));
        txtConsole = new JTextArea(initConsole);
        pneConsole = new JScrollPane(txtConsole);
        txtCommand = new JTextArea(
            "Type lines here, press Ctrl + Enter to submit");
        pneCommand = new JScrollPane(txtCommand);

        String fontName = (String)(Util.configManager.getConfig(
            "CodeLinePane.fontName"));

        Font defFont = new Font(fontName, Font.PLAIN, 16);

        txtCommand.setFont(defFont);
        txtConsole.setFont(defFont);
        txtConsole.setEditable(false);

        txtCommand.addKeyListener(new KeyAdapter()
        {
            
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (firstInput)
                {
                    firstInput = false;
                    txtCommand.setText("");
                }
                if (e.isControlDown() && e.getKeyChar() == 10)
                {
                    String text = txtCommand.getText() + "\n";
                    txtConsole.setText(txtConsole.getText() + text);
                    stdin.bytesToWrite = text.getBytes();
                    stdin.call(null);
                    txtCommand.setText("");
                }
            }
        });

        int pwidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int pheight = Toolkit.getDefaultToolkit().getScreenSize().height;
        setBounds((pwidth - 809) / 2, (pheight - 876) / 2, 809, 876);

        placeComponents();

        add(pneCommand);
        add(pneConsole);
        //add(pneText);
        //add(new JButton("..."));
    }
    
    private void placeComponents()
    {
        pneConsole.setBounds(3, 3, 800, 600);
        pneCommand.setBounds(3, 606, 800, 230);
    }

    public void reset()
    {
        firstInput = true;
        txtConsole.setText(initConsole);
        txtCommand.setText("Type lines here, press Ctrl + Enter to submit");
    }

    public void writeToScreen(byte[] string)
    {
        txtConsole.setText(txtConsole.getText() + (new String(string)));
    }
}
