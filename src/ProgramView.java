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
    static String[] colorLineNum = 
    {
        "<html><font color=#5070D0>",
        "<html><font color=#8890D8>",
        "<html><font color=#BCC0E4>"
    };
    static String colorTextSuffix = "</font></html>";

    static Color[] backgroundNormal =
    {
        new Color(0xF0, 0xF0, 0xF0, 0xFF),
        new Color(0xF0, 0xF0, 0xF0, 0xFF),
        new Color(0xF0, 0xF0, 0xF0, 0xFF)        
        //new Color(0xF8, 0xF8, 0xEF, 0xFF),
        //new Color(0xF6, 0xF6, 0xEF, 0xFF), //-0x4
        //new Color(0xF3, 0xF3, 0xF0, 0xFF)  //-0xA
    };
    static Color[] backgroundActive =
    {
        new Color(0xB9, 0xD7, 0xF4, 0xFF),
        new Color(0xC7, 0xDD, 0xF3, 0xFF), // -0x4
        new Color(0xDB, 0xE6, 0xF1, 0xFF)  // -0xA
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

        String fontName = (String)(Util.configManager.getConfig(
            "CodeLinePane.fontName"));

        defFont = new Font(fontName, Font.PLAIN, 16);

        lblStatus = new JLabel();
        this.add(lblStatus);
        lblLineIndex = new JLabel();
        lblLineIndex.setFont(defFont);
        this.add(lblLineIndex);
        lblInst = new JLabel();
        lblInst.setFont(defFont);
        this.add(lblInst);
        lblParam = new JLabel();
        lblParam.setFont(defFont);
        this.add(lblParam);

        this.setLayout(null);
        this.setPreferredSize(new Dimension(840, 24));
        this.setBackground(backgroundNormal[transparent]);

        placeComponents();

        this.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                setActive(true);
            }
            @Override
            public void mouseExited(MouseEvent e)
            {
                setActive(false);
            }
            private boolean inlblStatus(int x, int y)
            {
                return x < 33 && x > 8;
            }
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (!inlblStatus(e.getX(), e.getY()))
                {
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON1)
                {
                    Icon current = lblStatus.getIcon();
                    for (int i = 0; i < 4; ++i)
                    {
                        if (current == statusPic[transparent][i])
                        {
                            lblStatus.setIcon(
                                statusPic[transparent][i ^ 1]);
                        }
                    }
                }
            }
        });

        this.refresh(inst, lineIndex, pcIndex);
        repaint();
    }

    public void refresh(RISCVInstruction inst, int lineIndex, int pcIndex)
    {
        this.lineIndex = lineIndex;
        this.inst = inst;

        setlineIndex(lineIndex);

        if (inst != null)
        {
            //System.out.println("at:" + lineIndex);
            //System.out.println(inst.asm);

            setStatus(inst.isBreakpoint, pcIndex == lineIndex);
            setAssembly(inst.asm);
        }

        lblParam.repaint();
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
        repaint();
    }

    private void setlineIndex(int lineIndex)
    {
        lblLineIndex.setText(
            colorLineNum[transparent] + lineIndex + colorTextSuffix);
    }


    boolean isWhiteSpace(char c)
    {
        return c == ' ' || c == '\t';
    }

    boolean isSpilt(char c)
    {
        return c == ' ' || c == '\t' || c == '#'
            || c == ',' || c == '(' || c == ')' || c == '>';
    }

    boolean isCharacter(char c)
    {
        return (c >= 'a' && c <= 'z') || (c <= 'Z' && c >= 'A');
    }

    String paintString(String str, String color)
    {
        return color + str + "</font>";
    }

    public String highLight(String param)
    {
        StringBuilder sb = new StringBuilder("<html>");
        String[] registerColor = 
        {
            "<font color=#30A0A0>",
            "<font color=#60B4B4>",
            "<font color=#A8D2D2>"
        };
        String[] instantColor =
        {
            "<font color=#D06040>",
            "<font color=#D8846C>",
            "<font color=#E4BAAE>"
        };
        String[] commentColor = 
        {
            "<font color=#000000>",
            "<font color=#3E3E3C>",
            "<font color=#9C9C96>"
        };
        for (int i = 0; i < param.length(); ++i)
        {
            int j = i;  
            boolean isRegister = isCharacter(param.charAt(i));
            for (; j < param.length(); ++j)
            {
                if (isSpilt(param.charAt(j)))
                {
                    break;
                }
            }
            String word = param.substring(i, j);
            String[] color = isRegister ? registerColor : instantColor;
            if (j == param.length())
            {
                sb.append(paintString(word, color[transparent]));
                break;
            }
            color = param.charAt(j) == '>' ? commentColor : color;
            sb.append(paintString(word, color[transparent]));
            
            if (param.charAt(j) != '#')
            {
                sb.append(param.charAt(j));
            }
            else
            {
                sb.append(param.substring(j));
                break;
            }
            i = j;
        }

        sb.append("</html>");

        return sb.toString();
    }

    private void setAssembly(String assembly)
    {
        String inst;
        String param;
        int i = 0;
        for (; i < assembly.length(); ++i)
        {
            if (isWhiteSpace(assembly.charAt(i)))
            {
                break;
            }
        }
        inst = assembly.substring(0, i);

        lblInst.setText(colorText[transparent] + inst + colorTextSuffix);

        if (i != assembly.length())
        {
            for (; i < assembly.length(); ++i)
            {
                if (!isWhiteSpace(assembly.charAt(i)))
                {
                    break;
                }
            }
            param = assembly.substring(i);

//            param = colorText[transparent] + param + colorTextSuffix;
            lblParam.setText(highLight(param));
        }
        else
        {
            lblParam.setText("");
        }
    }

    private void placeComponents()
    {
        lblStatus.setBounds(9, 0, 24, 24);
        lblLineIndex.setBounds(39, 0, 47, 24);
        lblInst.setBounds(97, 0, 85, 24);
        lblParam.setBounds(193, 0, 635, 24);
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

        //fileName = (String)(Util.configManager.getConfig(
        //    "CodeLinePane.normalPic"));
        statusPic[0][0] = null;//new ImageIcon(fileName);
        statusPic[1][0] = null;//new ImageIcon(fileName + "F");
        statusPic[2][0] = null;//new ImageIcon(fileName + "FF");

        fileName = (String)(Util.configManager.getConfig(
            "CodeLinePane.breakpointPic"));
        statusPic[0][1] = new ImageIcon(fileName + ".png");
        statusPic[1][1] = new ImageIcon(fileName + "F.png");
        statusPic[2][1] = new ImageIcon(fileName + "FF.png");
        
        fileName = (String)(Util.configManager.getConfig(
            "CodeLinePane.processPic"));
        statusPic[0][2] = new ImageIcon(fileName + ".png");
        statusPic[1][2] = new ImageIcon(fileName + "F.png");
        statusPic[2][2] = new ImageIcon(fileName + "FF.png");
        
        fileName = (String)(Util.configManager.getConfig(
            "CodeLinePane.breakpointAndProcessPic"));
        statusPic[0][3] = new ImageIcon(fileName + ".png");
        statusPic[1][3] = new ImageIcon(fileName + "F.png");
        statusPic[2][3] = new ImageIcon(fileName + "FF.png");
    }

    static public void main(String[] args)
    {
        JFrame frm = new JFrame();
        CodeLinePane clp = new CodeLinePane(null, 0, 0, 2);
        clp.lblInst.setText("ADD");
        clp.lblParam.setText("%AX, %AX");
        frm.setBounds(300, 300, 640, 480);
        frm.add(clp);
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setVisible(true);
    }
}

public class ProgramView extends JPanel
{
    RISCVInstruction[] instructions;
    RISCVMachine machine;
    CodeLinePane[] codeLines;
    int startIndex = 0;
    int codeLinesOnScreen;

    public ProgramView()
    {
        codeLinesOnScreen = ((Integer)(Util.configManager.getConfig(
            "ProgramView.codeLinesOnScreen"))).intValue();
        setLayout(new GridLayout(codeLinesOnScreen, 1));

        codeLines = new CodeLinePane[codeLinesOnScreen];
        for (int i = 2; i < codeLinesOnScreen - 2; ++i)
        {
            codeLines[i] = new CodeLinePane(
                null, i, i, CodeLinePane.MODE_OPAQUE);
        }
        int end = codeLinesOnScreen - 1;
        codeLines[0] = new CodeLinePane(
            null, 0, 0, CodeLinePane.MODE_DOUBLEFADE);
        codeLines[end] = new CodeLinePane(
            null, end, end, CodeLinePane.MODE_DOUBLEFADE);
        codeLines[1] = new CodeLinePane(
            null, 1, 1, CodeLinePane.MODE_FADE);
        codeLines[end - 1] = new CodeLinePane(
            null, end - 1, end - 1, CodeLinePane.MODE_FADE);

        for (int i = 0; i < codeLinesOnScreen; ++i)
        {
            add(codeLines[i]);
        }

        this.addMouseWheelListener(new MouseWheelListener()
        {
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                if (machine == null)
                {
                    return;
                }

                int delta = ((Integer)(Util.configManager.getConfig(
                    "ProgramView.scrollLines"))).intValue();

                int originalStartIndex = startIndex;
                startIndex += delta * e.getWheelRotation();
                if (startIndex + codeLines.length >= instructions.length)
                {
                    startIndex = instructions.length - codeLines.length;
                    if (startIndex < 0)
                    {
                        startIndex = originalStartIndex;
                    }
                }
                else
                {
                    startIndex = startIndex < 0 ? 0 : startIndex;
                }

                refresh();
            }
        });
    }


    public void bindMachine(RISCVMachine machine)
    {
        this.machine = machine;
        this.instructions = machine.instructions;

        /*for (int j = 0; j < this.instructions.length; ++j)
        {
            String assembly = this.instructions[j].asm;

            String inst;
            String param;
            int i = 0;
            for (; i < assembly.length(); ++i)
            {
                if (isWhiteSpace(assembly.charAt(i)))
                {
                    break;
                }
            }
            inst = assembly.substring(0, i);

            if (i != assembly.length())
            {
                for (; i < assembly.length(); ++i)
                {
                    if (!isWhiteSpace(assembly.charAt(i)))
                    {
                        break;
                    }
                }
                param = highLight(assembly.substring(i));

                //param = colorText[transparent] + param + colorTextSuffix;
                //lblParam.setText(highLight(param));
            }
            else
            {
                param = "";
            }
            instructions[j].asm = inst + " " + param;
        }*/

        refreshAtPC();
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

    static public void main(String[] args)
    {
        JFrame frm = new JFrame();
        ProgramView pv = new ProgramView();
        frm.setBounds(300, 300, 480, 960);
        frm.setLayout(null);
        frm.add(pv);
        pv.setBounds(0, 0, 336, 768);

        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setVisible(true);
    }
}