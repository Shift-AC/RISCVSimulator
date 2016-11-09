import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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
}
    
class TableDataViewFrame extends DataViewFrame
{
    JTable table;

    public TableDataViewFrame(
        String title, Object[][] data, String[] columnNames)
    {
        super(title, data);
        setLayout(new GridLayout(1, 0));
        setResizable(false);
        table = new JTable(data, columnNames);
        
        int width = Util.min(columnNames.length * 96, 480);
        int height = Util.min(data.length * 30, 960);

        JScrollPane panel = new JScrollPane(table);
        add(panel);

        table.getTableHeader().setReorderingAllowed(false);
        table .getTableHeader().setResizingAllowed(false);

        resizeCenterScreen(width, height);
    }

    static DataViewFrame instance = null;
    public static void main(String[] args)
    {

        String[] columnNames = {"First Name",
                                "Last Name",
                                "Sport",
                                "# of Years",
                                "Vegetarian"};

        Integer[] intArray =
        {
            new Integer(3),
            new Integer(2),
            new Integer(1),
            new Integer(0),
            new Integer(-1)
        };

        Object[][] data = {
	    {"Kathy", "Smith",
	     "Snowboarding", intArray[0], new Boolean(false)},
	    {"John", "Doe",
	     "Rowing", intArray[1], new Boolean(true)},
	    {"Sue", "Black",
	     "Knitting", intArray[2], new Boolean(false)},
	    {"Jane", "White",
	     "Speed reading", intArray[3], new Boolean(true)},
	    {"Joe", "Brown",
	     "Pool", intArray[4], new Boolean(false)}
        };


                instance = new TableDataViewFrame("?", data, columnNames);
                instance.setVisible(true);


        new Runnable()
        {
            public void run()
            {
                try
                {
                    Thread.sleep(5000);
                }
                catch (Exception e) {}
                data[0][0] = "Kathy???";
                System.out.println("M");
                instance.repaint();

                while (true)
                {
                    MachineManager.sleepIgnoreInterrupt(50);
                    //System.out.println("??????");
                    if (!data[0][3].equals(new Integer(3)))
                    {
                        System.out.println("??");
                        System.exit(0);
                    }
                }
            }
        }.run();
    }
}

class TableMemoryViewFrame extends TableDataViewFrame
{
    static int columnCount = ((Integer)(Util.configManager.getConfig(
        "MemoryViewFrame.columnCount"))).intValue();
    
    static String[] columnNames;

    static
    {
        columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; ++i)
        {
            columnNames[i] = "+" + i;
        }
    }

    public TableMemoryViewFrame(Byte[] memoryFrag)
    {
        super("内存", toDataMatrix(memoryFrag), columnNames);
    }

    static private Object[][] toDataMatrix(Byte[] memoryFrag)
    {
        int rowCount = (memoryFrag.length + columnCount - 1) / columnCount;
        Object[][] dataMatrix = new Object[rowCount][];
        
        for (int i = 0, r = 0, c = 0; i < memoryFrag.length; ++i)
        {
            if (c == 0)
            {
                dataMatrix[r] = new Object[columnCount];
            }
            dataMatrix[r][c] = memoryFrag[i];

            if (++c == columnCount)
            {
                c = 0;
                r++;
            }
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
        super("通用寄存器", toDataMatrix(generalRegisters), columnNames);
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
        super("浮点寄存器", toDataMatrix(floatRegisters), columnNames);
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
    static String[] columnNames = 
    {
        "名称",
        "值"
    };
    public TableSymbolViewFrame(Symbol[] symbol)
    {
        super("符号表", toDataMatrix(symbol), columnNames);
    }

    static private Object[][] toDataMatrix(Symbol[] symbol)
    {
        Object[][] dataMatrix = new Object[symbol.length][];

        for (int i = 0; i < symbol.length; ++i)
        {
            dataMatrix[i] = new Object[2];
            dataMatrix[i][0] = symbol[i].name;
            dataMatrix[i][1] = new Long(symbol[i].address);
        }

        return dataMatrix;
    }
}