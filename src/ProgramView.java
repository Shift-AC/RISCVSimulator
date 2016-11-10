package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.text.*;

class CodeLinePane extends JPanel
{
    int lineIndex;
    int transparent;
    RISCVInstruction inst;

    // assume that dpi is 96.
    Font defFont;

    static public final int 
        MODE_OPAQUE = 0,
        MODE_FADE = 1,
        MODE_DOUBLEFADE = 2;

    static Icon[][] statusPic;
    static String[] colorText = 
    {
        "<html><font color=#000000>",
        "<html><font color=#3E3E3C>",
        "<html><font color=#9C9C96>"
    };
    static String colorTextSuffix = "</font></html>";

    static Color[] backgroundNormal =
    {
        new Color(0xF8, 0xF8, 0xEF, 0xFF),
        new Color(0xF8, 0xF8, 0xEF, 0xBF),
        new Color(0xF8, 0xF8, 0xEF, 0x5E)
    };
    static Color[] backgroundActive =
    {
        new Color(0xB9, 0xD7, 0xF4, 0xFF),
        new Color(0xB9, 0xD7, 0xF4, 0xBF),
        new Color(0xB9, 0xD7, 0xF4, 0x5E)
    };

    JLabel lblStatus;
    JLabel lblLineIndex;
    JLabel lblInst;
    JLabel lblParam;

    public CodeLinePane(
        RISCVInstruction inst, int lineIndex, int pcIndex, int transparent)
    {
        super();
        this.transparent = transparent;
        this.refresh(inst, lineIndex, pcIndex);

        String fontName = (String)(Util.configManager.getConfig(
            "CodeLinePane.fontName"));

        defFont = new Font(fontName, Font.PLAIN, 21);

        lblStatus = new JLabel();
        this.add(lblStatus);
        lblLineIndex = new JLabel();
        this.add(lblLineIndex);
        lblInst = new JLabel();
        this.add(lblInst);
        lblParam = new JLabel();
        this.add(lblParam);

        this.setLayout(null);
        this.setPreferredSize(new Dimension(384, 24));
        this.setBackground(backgroundNormal[transparent]);

        placeComponents();
    }

    public void refresh(RISCVInstruction inst, int lineIndex, int pcIndex)
    {
        this.lineIndex = lineIndex;
        this.inst = inst;

        setStatus(inst.isBreakpoint, pcIndex == lineIndex);
        setlineIndex(lineIndex);
        setAssembly(inst.asm);
    }

    private void setStatus(boolean isBreakPoint, boolean isPCIndex)
    {
        int ind = 0;
        if (isBreakPoint)
        {
            ind += 1;
        }
        if (isPCIndex)
        {
            ind += 2;
        }

        lblStatus.setIcon(statusPic[transparent][ind]);
    }

    private void setlineIndex(int lineIndex)
    {
        lblLineIndex.setText(
            colorText[transparent] + lineIndex + colorTextSuffix);
    }

    private void setAssembly(String assembly)
    {
        String inst;
        String param;
        int i = 0;
        for (; !isWhiteSpace(assembly.charAt(i)); ++i);
        inst = assembly.substring(0, i);

        for (; isWhiteSpace(assembly.charAt(i)); ++i);
        param = assembly.substring(i);

        lblInst.setText(colorText[transparent] + inst + colorTextSuffix);
        lblParam.setText(colorText[transparent] + param + colorTextSuffix);
    }

    private boolean isWhiteSpace(char c)
    {
        return c == ' ' || c == '\t';
    }

    private void placeComponents()
    {
        lblStatus.setBounds(9, 0, 24, 24);
        lblLineIndex.setBounds(39, 0, 47, 24);
        lblInst.setBounds(97, 0, 85, 24);
        lblParam.setBounds(193, 0, 179, 24);
    }

    public void setActive(boolean active)
    {
        if (active)
        {
            setBackground(backgroundActive[transparent]);
        }
        else
        {
            setBackground(backgroundNormal[transparent]);
        }
    }

    static
    {
        statusPic = new Icon[3][];
        String fileName;
        
        statusPic[0] = new Icon[4];
        statusPic[1] = new Icon[4];
        statusPic[2] = new Icon[4];

        fileName = (String)(Util.configManager.getConfig(
            "CodeViewPane.normalPic"));
        statusPic[0][0] = new ImageIcon(fileName);
        statusPic[1][0] = new ImageIcon(fileName + "F");
        statusPic[2][0] = new ImageIcon(fileName + "FF");

        fileName = (String)(Util.configManager.getConfig(
            "CodeViewPane.breakpointPic"));
        statusPic[0][1] = new ImageIcon(fileName);
        statusPic[1][1] = new ImageIcon(fileName + "F");
        statusPic[2][1] = new ImageIcon(fileName + "FF");
        
        fileName = (String)(Util.configManager.getConfig(
            "CodeViewPane.processPic"));
        statusPic[0][2] = new ImageIcon(fileName);
        statusPic[1][2] = new ImageIcon(fileName + "F");
        statusPic[2][2] = new ImageIcon(fileName + "FF");
        
        fileName = (String)(Util.configManager.getConfig(
            "CodeViewPane.breakpointAndProcessPic"));
        statusPic[0][3] = new ImageIcon(fileName);
        statusPic[1][3] = new ImageIcon(fileName + "F");
        statusPic[2][3] = new ImageIcon(fileName + "FF");
    }
}

public class ProgramView extends JPanel
{
    RISCVInstruction[] instructions;
    RISCVMachine machine;
    CodeLinePane[] codeLines;
    int startIndex = 0;
    int codeLinesOnScreen = ((Integer)(Util.configManager.getConfig(
        "ProgramView.codeLinesOnScreen"))).intValue();

    private MouseWheelListener mwl = new MouseWheelListener()
    {
        public void mouseWheelMoved(MouseWheelEvent e)
        {
            int delta = ((Integer)(Util.configManager.getConfig(
                "ProgramView.scrollLines"))).intValue();

            int originalStartIndex = startIndex;
            if (startIndex + delta + codeLines.length >= instructions.length)
            {
                startIndex = instructions.length - codeLines.length;
                if (startIndex < 0)
                {
                    startIndex = originalStartIndex;
                }
            }
            else
            {
                startIndex += delta;
                startIndex = startIndex < 0 ? 0 : startIndex;
            }

            refresh();
        }
    };

    public ProgramView() {}

    public void bindMachine(RISCVMachine machine)
    {
        this.machine = machine;
        this.instructions = machine.instructions;


        setLayout(new GridLayout(codeLinesOnScreen, 1));

        codeLines = new CodeLinePane[codeLinesOnScreen];
        for (int i = 0; i < codeLinesOnScreen; ++i)
        {
            if (i == codeLinesOnScreen)
            {
                break;
            }
            codeLines[i] = new CodeLinePane(
                null, i, i, CodeLinePane.MODE_OPAQUE);
        }

        this.addMouseWheelListener(mwl);
    }

    public void refreshAtPC()
    {
        startIndex = getPCIndex();
        if (startIndex + codeLines.length > instructions.length)
        {
            startIndex = instructions.length - codeLines.length;
            startIndex = startIndex < 0 ? 0 : startIndex;
        }

        refresh();
    }

    public void refresh()
    {
        int i = 0;
        int pcIndex = getPCIndex();
        for (; i < codeLines.length; ++i)
        {
            int offset = i + startIndex;
            codeLines[i].refresh(getInstruction(offset), offset, pcIndex);
        }
    }

    private int getPCIndex()
    {
        long offset = machine.programCounter - 
            machine.memory[RISCVMachine.SEGMENT_TEXT].startAddress;
        
        return (int)(offset >>> 2);
    }
 
    private RISCVInstruction getInstruction(int index)
    {
        if (index < 0 || index >= instructions.length)
        {
            return null;
        }
        return instructions[index];
    }

    // * deprecated
    // use JScrollPane to place the components is ok, but needs
    // setPreferredSize() (setBounds doesn't make sense)


}