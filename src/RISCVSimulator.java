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
    
    TableGeneralRegViewFrame generalRegFrame;
    TableFloatRegViewFrame floatRegFrame;
    TableSymbolViewFrame symbolFrame;
    TableMemoryViewFrame memoryFrame;
    ConsoleFrame consoleFrame;

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

                setDebugOptionState(STATE_RUNNABLE);
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
                updateMachine();
                setDebugOptionState(STATE_RUNNING);
                MachineManager.messageQueue.add("S");
                Util.sleepIgnoreInterrupt(100);
                refreshState();
                setDebugOptionState(STATE_RUNNABLE);
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
            if (MachineManager.checkRunnable())
            {
                updateMachine();
                setDebugOptionState(STATE_RUNNING);
                MachineManager.messageQueue.add("R");
                return;
            }
            Util.reportError(
                "虚拟机无法继续运行，请查看程序状态，并使用\"运行\"选项重启虚拟机。");
        }
    };
    SMenuItem mnuTerminate;
    ActionListener alsTerminate = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            MachineManager.messageQueue.add("T");
            refreshState();
            setDebugOptionState(STATE_RUNNABLE);
        }
    };
    SMenuItem mnuPause;
    ActionListener alsPause = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            MachineManager.messageQueue.add("P");
            refreshState();
            setDebugOptionState(STATE_RUNNABLE);
        }
    };
    SMenuItem mnuContinue;
    ActionListener alsContinue = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (MachineManager.checkRunnable())
            {
                updateMachine();
                setDebugOptionState(STATE_RUNNING);
                MachineManager.messageQueue.add("C");
                return;
            }
            Util.reportError(
                "虚拟机无法继续运行，请查看程序状态，并使用\"运行\"选项重启虚拟机。");
        }
    };
    SMenu mnuView;
    SMenuItem mnuMemory;
    ActionListener alsMemory = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            memoryFrame.setVisible(true);
        }
    };
    SMenuItem mnuGeneralRegister;
    ActionListener alsGeneralRegister = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            generalRegFrame.setVisible(true);
        }
    };
    SMenuItem mnuFloatRegister;
    ActionListener alsFloatRegister = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            floatRegFrame.setVisible(true);
        }
    };
    SMenuItem mnuSymbolTable;
    ActionListener alsSymbolTable = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            symbolFrame.setVisible(true);
        }
    };
    SMenuItem mnuConsole;
    ActionListener alsConsole = new ActionListener()
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            consoleFrame.setVisible(true);
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
        super("RISCVSimulator " + Util.version);
        addWindowListener(wlsFrame);
        resizeCenterScreen(840, 768);
        setResizable(false);
        initializeMenu();
        setDebugOptionState(STATE_NOTREADY);

        consoleFrame = MachineManager.console;

        //pneFrame.
        setLayout(new GridLayout(1, 1));
        programView = new ProgramView();
        ((JFrame)this).add(programView);

        this.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_PAGE_UP || key == KeyEvent.VK_PAGE_DOWN)
                {
                    int delta = 
                        key == KeyEvent.VK_PAGE_DOWN ? 
                        programView.codeLinesOnScreen : 
                        -programView.codeLinesOnScreen;
                    
                    programView.moveStartIndex(delta);
                    programView.refresh();
                }
            }
        });
    }

    void checkExit()
    {
        Util.closemanager.call(null);
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
        mnuStep = new SMenuItem("下一指令(N)", 'N', mnuDebug);
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
        mnuSymbolTable = new SMenuItem("符号表(S)", 'S', mnuView);
        mnuSymbolTable.addActionListener(alsSymbolTable);
        mnuConsole = new SMenuItem("控制台(T)", 'T', mnuView);
        mnuConsole.addActionListener(alsConsole);
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
            programView.setEditable(false);
            mnuStep.setEnabled(false);
            mnuRun.setEnabled(false);
            mnuTerminate.setEnabled(true);
            mnuPause.setEnabled(true);
            mnuContinue.setEnabled(false);
            memoryFrame.setEnabled(false);
            generalRegFrame.setEnabled(false);
            floatRegFrame.setEnabled(false);
            symbolFrame.setEnabled(false);
            break;
        case STATE_RUNNABLE:
            programView.setEditable(true);
            mnuStep.setEnabled(true);
            mnuRun.setEnabled(true);
            mnuTerminate.setEnabled(false);
            mnuPause.setEnabled(false);
            mnuContinue.setEnabled(true);
            memoryFrame.setEnabled(true);
            generalRegFrame.setEnabled(true);
            floatRegFrame.setEnabled(true);
            symbolFrame.setEnabled(true);
            //mnuConsole.setEnabled(true);
            break;
        case STATE_NOTREADY:
            mnuStep.setEnabled(false);
            mnuRun.setEnabled(false);
            mnuTerminate.setEnabled(false);
            mnuPause.setEnabled(false);
            mnuContinue.setEnabled(false);
            //mnuConsole.setEnabled(false);
            break;
        }
    } 

    private void bindMachine(RISCVMachine machine)
    {
        MachineManager.setMachine(machine);
        programView.bindMachine(machine);

        memoryFrame = new TableMemoryViewFrame(
            MachineManager.snapshot);
        generalRegFrame = new TableGeneralRegViewFrame(
            MachineManager.snapshot.generalRegister);
        floatRegFrame = new TableFloatRegViewFrame(
            MachineManager.snapshot.floatRegister);
        symbolFrame = new TableSymbolViewFrame(
            MachineManager.snapshot.symbol);
        consoleFrame.reset();

        // debug
        testSyscall();
    }

    private void testSyscall()
    {
        MachineController controller = MachineManager.machine.controller;

        Syscall syssbrk = new SYSsbrk();
        syssbrk.num = 214;
        syssbrk.call(MachineManager.machine);

        Syscall sysopen = new SYSopen()
        {
            @Override
            public void call(RISCVMachine machine)
            {
                long[] reg = machine.generalRegister;
                reg[11] = 1;
                reg[12] = 0x1FF;
                super.call(machine);
            }
            @Override
            protected byte[][] getMessages(RISCVMachine machine)
            {
                byte[][] res = new byte[2][];
                long[] reg = machine.generalRegister;
                res[0] = (num + " " + reg[10] + " " + reg[11] + " " + reg[12] + " " + reg[13] + "\n").getBytes();
                res[1] = "5 a.txt".getBytes();
                return res;
            }
        };
        sysopen.num = 1024;

        Syscall sysread = new SYSread()
        {
            @Override
            public void call(RISCVMachine machine)
            {
                machine.generalRegister[10] = 0;
                machine.generalRegister[12] = 11;
                super.call(machine);
            }
        };
        sysread.num = 63;

        Syscall syswrite = new SYSwrite()
        {
            @Override
            public void call(RISCVMachine machine)
            {
                machine.generalRegister[10] = 1;
                machine.generalRegister[12] = 11;
                MachineManager.console.writeToScreen("hello world".getBytes());
            }
            @Override
            protected byte[][] getMessages(RISCVMachine machine)
            {
                byte[][] res = new byte[2][];
                long[] reg = machine.generalRegister;
                res[0] = (num + " " + reg[10] + " " + reg[11] + " " + reg[12] + " " + reg[13] + "\n").getBytes();
                res[1] = "11 hello world".getBytes();
                return res;
            }
        };
        syswrite.num = 64;

        sysopen.call(MachineManager.machine);
        sysread.call(MachineManager.machine);
        syswrite.call(MachineManager.machine);
        System.out.println("????????");
    }

    public void notifyProgram()
    {
        refreshState();
        setDebugOptionState(STATE_RUNNABLE);
    }

    private void refreshState()
    {
        long len = MachineManager.snapshot.memoryFrag.length;
        if (MachineManager.updateSnapshot(Util.STACK_BEGIN - len) == false)
        {
            Util.reportErrorAndExit("致命错误：同步失败，无法停止虚拟机。");
        }

        generalRegFrame.resetTable();
        floatRegFrame.resetTable();
        memoryFrame.resetTable();
        programView.refreshAtPC();
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

    public static void init()
    {
        setCurrrentLookAndFeel();
        MachineManager.instance.start();
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
                MachineManager.frm = instance;
                instance.show();
            }
        }.run();
    }
}