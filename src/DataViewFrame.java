package com.github.ShiftAC.RISCVSimulator;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.io.*;
import java.text.*;

public class DataViewFrame extends SFrame
{
    Object[][] data;

    public DataViewFrame(String title, Object[][] data)
    {
        super(title);
        this.data = data;
    }

    public DataViewFrame(String title)
    {
        super(title);
        this.data = null;
    }
}
    
class TableDataViewFrame extends DataViewFrame
{
    JTable table;

    public TableDataViewFrame(
        String title, DefaultTableModel model)
    {
        super(title);
        setLayout(new GridLayout(1, 0));
        setResizable(false);
        table = new JTable(model);
        
        //int width = Util.min(columnNames.length * 96, 480);
        //int height = Util.min(data.length * 30, 960);

        JScrollPane panel = new JScrollPane(table);
        add(panel);

        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);

        //resizeCenterScreen(width, height);
    }
}

class TableMemoryViewFrame extends TableDataViewFrame
{
    static int columnCount = ((Integer)(Util.configManager.getConfig(
        "MemoryViewFrame.columnCount"))).intValue() + 1;
    
    static String[] columnNames;

    static
    {
        columnNames = new String[columnCount];
        columnNames[0] = "";
        for (int i = 1; i < columnCount; ++i)
        {
            columnNames[i] = "+" + i;
        }
    }

    JTextField txtStartAddress;
    JScrollPane pneTable;

    public TableMemoryViewFrame(MachineStateSnapshot snapshot)
    {
        super("内存", getModel(toDataMatrix(snapshot.memoryFrag), columnNames));

        pneFrame.setLayout(null);

        pneTable = new JScrollPane(table);
        add(pneTable);

        txtStartAddress = new JTextField(
            "Type then press Enter to change start address(Hex)");

        txtStartAddress.addKeyListener(new KeyAdapter()
        {
            boolean isFirstInput = true;

            public int toHexNumber(char c)
            {
                if (c > 47 && c < 58)
                {
                    return c - 48;
                }
                else if (c > 64 && c < 91)
                {
                    return c - 55;
                }
                else if (c > 97 && c < 124)
                {
                    return c - 87;
                }
                else
                {
                    return -1;
                }
            }

            @Override
            public void keyTyped(KeyEvent e)
            {
                if (isFirstInput)
                {
                    isFirstInput = false;
                    txtStartAddress.setText("");
                }
                if (e.getKeyChar() != 10)
                {
                    return;
                }

                long address = 0;
                String text = txtStartAddress.getText();
                for (int i = 0; i < text.length(); ++i)
                {
                    int digit = toHexNumber(text.charAt(i));
                    if (digit < 0)
                    {
                        text = text.substring(0, i);
                        break;
                    }
                    address = (address << 4) + digit;
                }
                save();
                MachineManager.snapshot.recordMemory(address);
                txtStartAddress.setText(address + "(0x" + text + ")");
                resetTable();
            }
        });

        add(txtStartAddress);

        int width = Util.min(columnNames.length * 96, 480);
        int height = Util.min(table.getRowCount() * 24 + 95, 1000);

        txtStartAddress.setBounds(0, 0, width, 28);
        pneTable.setBounds(0, 30, width, height - 30);

        resizeCenterScreen(width, height);
    }

    private void resetTable()
    {
        int height = Util.min(table.getRowCount() * 24 + 95, 1000);
        int width = Util.min(columnNames.length * 96, 480);
        remove(pneTable);
        Byte[] source = MachineManager.snapshot.memoryFrag;
        table = new JTable(getModel(toDataMatrix(source), columnNames));
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        pneTable = new JScrollPane(table);
        add(pneTable);
        pneTable.setBounds(0, 30, width, height - 30);
    }

    static private DefaultTableModel getModel(
        Object[][] data, String[] columnNames)
    {
        return new DefaultTableModel(data, (Object[])columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return column != 0;
            }
        };
    }

    public void save()
    {
        int i = 0;
        for (int r = 0; r < table.getRowCount(); ++r)
        {
            for (int c = 1; c < columnNames.length; ++c)
            {
                Object value = table.getValueAt(r, c);
                if (value == null)
                {
                    MachineManager.snapshot.memoryFrag[i++] = null;
                    continue;
                }
                MachineManager.snapshot.memoryFrag[i++] = 
                    new Byte(Byte.parseByte(value.toString()));
            }
        }
        MachineManager.snapshot.saveMemory(MachineManager.machine);
    }

    static private Object[][] toDataMatrix(Byte[] memoryFrag)
    {
        int rowCount = 
            (memoryFrag.length + columnCount - 2) / (columnCount - 1);
        Object[][] dataMatrix = new Object[rowCount][];
        
        //System.out.println(rowCount + "");

        for (int i = 0, r = 0, c = 0; i < memoryFrag.length; ++i)
        {
            if (c == 0)
            {
                dataMatrix[r] = new Object[columnCount];
                dataMatrix[r][c++] = new String("+" + r * (columnCount - 1));
            }
            //System.out.println(i + " ! " + memoryFrag[i]);
            dataMatrix[r][c] = memoryFrag[i];

            if (++c == columnCount)
            {
                c = 0;
                r++;
            }
        }

        for (int i = 0; i < dataMatrix.length; ++i)
        {
            for (int j = 0; j < dataMatrix[i].length; ++j)
            {
                System.out.printf(dataMatrix[i][j] + " ");
            }
            System.out.println("");
        }
        return dataMatrix;
    }
}

class TableGeneralRegViewFrame extends TableDataViewFrame
{
    static String[] rowNames;
    static 
    {
        int count = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.generalRegisterCount"))).intValue();

        rowNames = new String[count];

        for (int i = 0; i < count; ++i)
        {
            rowNames[i] = (String)(Util.configManager.getConfig(
            "RISCVMachine.generalRegisterName" + i));
        }
    }
    static String[] columnNames =
    {
        "名称",
        "值"
    };

    public TableGeneralRegViewFrame(Long[] generalRegisters)
    {
        super("通用寄存器", 
              getModel(toDataMatrix(generalRegisters), columnNames));

        resizeCenterScreen(96 * 3, 26 * generalRegisters.length + 3);

        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
    }

    static private DefaultTableModel getModel(
        Object[][] data, String[] columnNames)
    {
        return new DefaultTableModel(data, (Object[])columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                System.out.println(MachineManager.snapshot.generalRegister[2]);
                return column != 0;
            }
        };
    }

    public void save()
    {
        for (int i = 0; i < rowNames.length; ++i)
        {
            MachineManager.snapshot.generalRegister[i] = 
                new Long(Long.parseLong(table.getValueAt(i, 1).toString()));
        }
    }

    static private Object[][] toDataMatrix(Long[] generalRegisters)
    {
        Object[][] dataMatrix = new Object[rowNames.length][];

        for (int i = 0; i < rowNames.length; ++i)
        {
            dataMatrix[i] = new Object[2];
            dataMatrix[i][0] = rowNames[i];
            dataMatrix[i][1] = generalRegisters[i];
        }

        return dataMatrix;
    }
}

class TableFloatRegViewFrame extends TableDataViewFrame
{
    static String[] rowNames;
    static 
    {
        int count = ((Integer)(Util.configManager.getConfig(
            "RISCVMachine.floatRegisterCount"))).intValue();

        rowNames = new String[count];

        for (int i = 0; i < count; ++i)
        {
            rowNames[i] = (String)(Util.configManager.getConfig(
            "RISCVMachine.floatRegisterName" + i));
        }
    }
    static String[] columnNames =
    {
        "名称",
        "值"
    };

    public TableFloatRegViewFrame(Float[] floatRegisters)
    {
        super("浮点寄存器", getModel(toDataMatrix(floatRegisters), columnNames));
        resizeCenterScreen(96 * 3, 26 * floatRegisters.length + 3);
        table.getColumnModel().getColumn(0).setPreferredWidth(48);
        table.getColumnModel().getColumn(1).setPreferredWidth(240);
    }

    public void save()
    {
        for (int i = 0; i < rowNames.length; ++i)
        {
            MachineManager.snapshot.floatRegister[i] = 
                new Float(Float.parseFloat(table.getValueAt(i, 1).toString()));
        }
    }

    static private DefaultTableModel getModel(
        Object[][] data, String[] columnNames)
    {
        return new DefaultTableModel(data, (Object[])columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return column != 0;
            }
        };
    }

    static private Object[][] toDataMatrix(Float[] floatRegisters)
    {
        Object[][] dataMatrix = new Object[rowNames.length][];

        for (int i = 0; i < rowNames.length; ++i)
        {
            dataMatrix[i] = new Object[2];
            dataMatrix[i][0] = rowNames[i];
            dataMatrix[i][1] = floatRegisters[i];
        }

        return dataMatrix;
    }
}

class TableSymbolViewFrame extends TableDataViewFrame
{
    static Object[][] originalData;
    static Object[][] usingData;
    static JScrollPane pneTable;

    static String[] columnNames = 
    {
        "名称",
        "值"
    };

    static boolean less(Symbol l, Symbol r)
    {
        String ls = l.name;
        String rs = r.name;
        int len = Util.min(ls.length(), rs.length());

        for (int i = 0; i < len; ++i)
        {
            int diff = ls.charAt(i) - rs.charAt(i);
            if (diff < 0)
            {
                return true;
            }
            else if (diff > 0)
            {
                return false;
            }
        }
        if (rs.length() > ls.length())
        {
            return true;
        }

        return false;
    }

    static void qsort(Symbol[] arr, int start, int end)
    {
        int ts = start, te = end - 1;

        while (ts < te)
        {
            for (; te > ts; --te)
            {
                if (less(arr[te], arr[ts]))
                {
                    Symbol tmp = arr[te];
                    arr[te] = arr[ts];
                    arr[ts] = tmp;
                    break;
                }
            }

            for (; ts < te; ++ts)
            {
                if (less(arr[te], arr[ts]))
                {
                    Symbol tmp = arr[te];
                    arr[te] = arr[ts];
                    arr[ts] = tmp;
                    break;
                }
            }
        }

        if (ts - start > 1)
        {
            qsort(arr, start, ts);
        }
        if (end - te > 2)
        {
            qsort(arr, te + 1, end);
        }
    }

    JTextField txtSearch = new JTextField("Type and hit Enter to search.");

    public TableSymbolViewFrame(Symbol[] symbol)
    {
        super("符号表", 
            getModel(usingData = toDataMatrix(symbol), columnNames));
        originalData = toDataMatrix(symbol);
        
//        remove(table);

        pneFrame.setLayout(null);

        pneTable = new JScrollPane(table);

        txtSearch.addKeyListener(new KeyAdapter()
        {
            boolean isFirstInput = true;

            @Override
            public void keyTyped(KeyEvent e)
            {
                if (isFirstInput)
                {
                    isFirstInput = false;
                    txtSearch.setText("");
                }

                if (e.getKeyChar() != 10)
                {
                    return;
                }

                String text = txtSearch.getText();

                int end = originalData.length;
                int start = 0;
                while (end - start > 0)
                {
                    int mid = (end + start) >> 1;
                    int cmp = compare(text, (String)originalData[mid][0]);
                    if (cmp < 0)
                    {
                        start = mid + 1;
                    }
                    else if (cmp > 0)
                    {
                        end = mid;
                    }
                    else if (cmp == 0)
                    {
                        resetData(text, mid);
                        resetTable();
                        return;
                    }
                }
                for (int i = 0; i < originalData.length; ++i)
                {
                    usingData[i][0] = null;
                    usingData[i][1] = null;
                }
                
                resetTable();
            }
        });

        add(pneTable);
        add(txtSearch);//, JLayeredPane.DIALOG_LAYER);

        int tableHeight = Util.min(917, table.getRowCount() * 24 + 5);
        resizeCenterScreen(432, tableHeight + 30);
        table.getColumnModel().getColumn(0).setPreferredWidth(240);
        table.getColumnModel().getColumn(1).setPreferredWidth(192);

        //        table.setPreferredSize(new Dimension(420, tableHeight));
        pneTable.setBounds(0, 30, 420, tableHeight);

//        txtSearch.setPreferredSize(new Dimension(420, 30));
        txtSearch.setBounds(0, 0, 420, 28);
    
    }

    static private DefaultTableModel getModel(
        Object[][] data, String[] columnNames)
    {
        return new DefaultTableModel(data, (Object[])columnNames)
        {
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
    }

    static int compare(String text, String str)
    {
        if (text.length() > str.length())
        {
            return 1;
        }

        for (int i = 0; i < text.length(); ++i)
        {
            int diff = text.charAt(i) - str.charAt(i);
            if (diff < 0)
            {
                return 1;
            }
            if (diff > 0)
            {
                return -1;
            }
        }

        return 0;
    }

    private void resetData(String text, int mid)
    {
        int start;
        int end;
        for (start = mid - 1; start > -1; --start)
        {
            if (compare(text, (String)originalData[start][0]) != 0)
            {
                start++;
                break;
            }
        }
        if (start == -1)
        {
            start = 0;
        }
        for (end = mid + 1; end < originalData.length; ++end)
        {
            if (compare(text, (String)originalData[end][0]) != 0)
            {
                break;
            }
        }
        int displayLen = end - start;
        for (int i = 0; i < displayLen; ++i)
        {
            usingData[i][0] = originalData[start + i][0];
            usingData[i][1] = originalData[start + i][1];
        }
        for (int i = displayLen; i < originalData.length; ++i)
        {
            usingData[i][0] = null;
            usingData[i][1] = null;
        }
    }

    private void resetTable()
    {
        int height = (int)pneTable.getSize().getHeight();
        remove(pneTable);
        table = new JTable(getModel(usingData, columnNames));
        table.setRowHeight(24);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(240);
        table.getColumnModel().getColumn(1).setPreferredWidth(192);
        pneTable = new JScrollPane(table);
        add(pneTable);
        pneTable.setBounds(0, 30, 420, height);
    }

    static private Object[][] toDataMatrix(Symbol[] symbol)
    {
        Object[][] dataMatrix = new Object[symbol.length][];

        qsort(symbol, 0, symbol.length);

        for (int i = 0; i < symbol.length; ++i)
        {
            dataMatrix[i] = new Object[2];
            dataMatrix[i][0] = symbol[i].name;
            dataMatrix[i][1] = new Long(symbol[i].address);
        }

        return dataMatrix;
    }
}