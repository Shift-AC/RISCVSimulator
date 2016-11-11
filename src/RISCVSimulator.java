package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.text.*;

class RISCVSimulatorFrame extends SFrame
{
    ELFReader reader = new ELFReader();

    // DataViewFrame is designed to be a frame that binds with specified data.
    // it provides a interface for users to view and modify the data directly.
    // once the components of DataViewFrame detected that the data has been 
    // modified, it will overwrite the original data block immediately. and if 
    // the original data block was modified by someone else, simply repaint the
    // DataViewFrame will force the components to show the modified data.
    
    DataViewFrame generalRegFrame;
    DataViewFrame floatRegFrame;
    DataViewFrame symbolFrame;
    DataViewFrame memoryFrame;

    ProgramView programView;

    JMenuBar menu;
    SMenu mnuFile;
    SMenuItem mnuOpen;
    ActionListener alsOpen = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("打开ELF文件");
            javax.swing.filechooser.FileFilter filter = 
                new javax.swing.filechooser.FileNameExtensionFilter(
                    "ELF可执行文件(*.o)", "o");
            fc.setFileFilter(filter);
            fc.setMultiSelectionEnabled(false);

            int res = fc.showOpenDialog(me);

            if (res == JFileChooser.APPROVE_OPTION)
            {
                String fileName = fc.getCurrentDirectory().getName() + "/" +
                                  fc.getSelectedFile().getName();
                FileInputStream is;
                try
                {
                    is = new FileInputStream(fileName);
                }
                catch (Exception ex)
                {
                    System.err.println(fileName);
                    Util.reportError("无法打开选定的文件。");
                    return;
                }
                
                RISCVMachine machine = reader.read(is, fileName);
                
                if (machine == null)
                {
                    Util.reportError("无法识别的ELF文件。");
                }
                else
                {
                    bindMachine(machine);
                }

                System.out.println("ELF loaded");
            }
        }
    };
    SMenuItem mnuExit;
    ActionListener alsExit = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            checkExit();
        }
    };
    SMenu mnuDebug;
    SMenuItem mnuStep;
    ActionListener alsStep = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (MachineManager.checkRunnable())
            {
                // step..
                return;
            }
            Util.reportError(
                "虚拟机无法继续单步运行，请查看程序状态，并使用\"运行\"选项重启虚拟机。");
        }
    };
    SMenuItem mnuRun;
    ActionListener alsRun = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (MachineManager.checkMachine())
            {
                // run...
                return;
            }
            Util.reportError("未载入ELF文件。");
        }
    };
    SMenuItem mnuTerminate;
    ActionListener alsTerminate = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenuItem mnuPause;
    ActionListener alsPause = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenuItem mnuContinue;
    ActionListener alsContinue = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenuItem mnuBreakpoint;
    ActionListener alsBreakpoint = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenu mnuView;
    SMenuItem mnuMemory;
    ActionListener alsMemory = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenuItem mnuGeneralRegister;
    ActionListener alsGeneralRegister = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenuItem mnuFloatRegister;
    ActionListener alsFloatRegister = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };
    SMenuItem mnuSymbolTable;
    ActionListener alsSymbolTable = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {

        }
    };

    WindowAdapter wlsFrame = new WindowAdapter()
    {
        @Override
        public void windowClosing(WindowEvent e)
        {
            checkExit();
        }
    };

    public RISCVSimulatorFrame()
    {
        super("RISCVSimulatorFrame " + Util.version);
        addWindowListener(wlsFrame);
        resizeCenterScreen(840, 768);
        setResizable(false);
        initializeMenu();
        setDebugOptionState(STATE_NOTREADY);

        //pneFrame.
        setLayout(new GridLayout(1, 1));
        programView = new ProgramView();
        ((JFrame)this).add(programView);
    }

    void checkExit()
    {
        System.exit(0);
    }

    private void initializeMenu()
    {
        menu = new JMenuBar();
        setJMenuBar(menu);
        
        mnuFile = new SMenu("文件(F)", 'F', menu);
        mnuOpen = new SMenuItem("打开文件(O)", 'O', mnuFile);
        mnuOpen.addActionListener(alsOpen);
        mnuExit = new SMenuItem("退出(X)", 'X', mnuFile);
        mnuExit.addActionListener(alsExit);

        mnuDebug = new SMenu("调试(D)", 'D', menu);
        mnuStep = new SMenuItem("单步调试(S)", 'S', mnuDebug);
        mnuStep.addActionListener(alsStep);
        mnuRun = new SMenuItem("运行(R)", 'R', mnuDebug);
        mnuRun.addActionListener(alsRun);
        mnuTerminate = new SMenuItem("停止(T)", 'T', mnuDebug);
        mnuTerminate.addActionListener(alsTerminate);
        mnuPause = new SMenuItem("暂停(P)", 'P', mnuDebug);
        mnuPause.addActionListener(alsPause);
        mnuContinue = new SMenuItem("继续(C)", 'C', mnuDebug);
        mnuContinue.addActionListener(alsContinue);
        //mnuBreakpoint = new SMenuItem("设置断点(B)", 'B', mnuDebug);
        //mnuBreakpoint.addActionListener(alsBreakpoint);

        mnuView = new SMenu("查看(V)", 'V', menu);
        mnuMemory = new SMenuItem("内存(M)", 'M', mnuView);
        mnuMemory.addActionListener(alsMemory);
        mnuGeneralRegister = new SMenuItem("通用寄存器(G)", 'G', mnuView);
        mnuGeneralRegister.addActionListener(alsGeneralRegister);
        mnuFloatRegister = new SMenuItem("浮点寄存器(F)", 'F', mnuView);
        mnuFloatRegister.addActionListener(alsFloatRegister);
        mnuSymbolTable = new SMenuItem("符号表(T)", 'T', mnuView);
        mnuSymbolTable.addActionListener(alsSymbolTable);
    }

    private static final int
        STATE_RUNNING = 0,
        STATE_RUNNABLE = 1,
        STATE_NOTREADY = 2;
    private void setDebugOptionState(int state)
    {
        switch (state)
        {
        case STATE_RUNNING:
            mnuStep.setEnabled(false);
            mnuRun.setEnabled(false);
            mnuTerminate.setEnabled(true);
            mnuPause.setEnabled(true);
            mnuContinue.setEnabled(false);
            //mnuBreakpoint.setEnabled(false);
            break;
        case STATE_RUNNABLE:
            mnuStep.setEnabled(true);
            mnuRun.setEnabled(true);
            mnuTerminate.setEnabled(false);
            mnuPause.setEnabled(false);
            mnuContinue.setEnabled(true);
            //mnuBreakpoint.setEnabled(true);
            break;
        case STATE_NOTREADY:
            mnuStep.setEnabled(false);
            mnuRun.setEnabled(false);
            mnuTerminate.setEnabled(false);
            mnuPause.setEnabled(false);
            mnuContinue.setEnabled(false);
            //mnuBreakpoint.setEnabled(false);
            break;
        }
    } 

    private void bindMachine(RISCVMachine machine)
    {
        MachineManager.setMachine(machine);
        programView.bindMachine(machine);

        memoryFrame = new TableMemoryViewFrame(
            MachineManager.snapshot.memoryFrag);

        generalRegFrame = new TableGeneralRegViewFrame(
            MachineManager.snapshot.generalRegister);
        floatRegFrame = new TableFloatRegViewFrame(
            MachineManager.snapshot.floatRegister);
        symbolFrame = new TableSymbolViewFrame(
            MachineManager.snapshot.symbol);
    }

    private void refreshState()
    {
        long len = MachineManager.snapshot.memoryFrag.length;
        if (MachineManager.updateSnapshot(Util.STACK_BEGIN - len) == false)
        {
            Util.reportErrorAndExit("致命错误：同步失败，无法停止虚拟机。");
        }

        generalRegFrame.repaint();
        floatRegFrame.repaint();
        symbolFrame.repaint();
        memoryFrame.repaint();
    }

    private void updateMachine()
    {
        if (MachineManager.updateMachine() == false)
        {
            Util.reportErrorAndExit("致命错误：同步失败，无法更新虚拟机。");
        }
    }
}

public class RISCVSimulator
{
    public static void checkFile()
    {
        
    }

    public static void init()
    {
        setCurrrentLookAndFeel();
    }

    public static void setCurrrentLookAndFeel()
    {
        try
        {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            System.err.println("[Warning]: Can't set look and feel!");
        }
    }

    public static void main(String[] args)
    {
        init();

        new Runnable()
        {
            public void run()
            {
                RISCVSimulatorFrame instance = new RISCVSimulatorFrame();
                instance.show();
            }
        }.run();
    }
}