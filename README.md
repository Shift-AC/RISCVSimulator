# RISCVSimulator

Simple pluggable RISCV64IMF simulator, works on Linux.

## For Contributors

To initialize your workspace after your first `git clone` operation, use __init.sh__.

## 安装程序

运行init.sh初始化工作区，之后运行`compile.sh`以完成编译安装。在完成编译之后需要将riscv-toolchain中附带的`objdump`程序复制到`tools/`文件夹。

## 程序功能简述

1. 实现RISCV-IMF指令集

2. 实现系统调用

3. 调试功能（断点，单步调试，运行程序，控制台，查看寄存器、内存、符号表，修改寄存器、内存）

4. 指令添加：只需要修改对应的config文件，添加指令的数据通路

5. 可插接的模拟器实现：用户实现一组CombineLogic类，实现MachineController类即可改变模拟器的具体实现。

6. 可插接的系统调用实现：用户实现一组Syscall类，并在config文件中注册系统调用即可使用自己的系统调用实现。

## 使用程序

利用`run.sh`运行程序。

进入程序时，程序显示如下图：

![初始界面](READMERef/Init.png)

加载ELF文件后，程序显示如下图，此时PC指向第一行代码（如黄色箭头所示），第10行被添加了一个断点（如红色球所示）。在此界面上按上/下方向键可以翻动代码（每次一行），或者PageUP/PageDown（每次一页），或使用鼠标滚轮（默认每次三行，可定制）。用户可以单击行号左边的区域来添加/删除断点。

![加载文件界面，PC指向第一行，第10行为断点](READMERef/Standby.png)

在控制台界面，用户可以在下方的标准输入框中编辑文字，并使用Ctrl+Enter命令提交输入到上方的标准输出框。它们分别与程序的标准输入流、标准输出/错误流关联，并会实时回显。

![控制台界面](READMERef/Console.png)

在加载ELF文件之后，用户可以利用“调试”菜单栏对程序进行调试。利用“运行”选项可以从头开始运行程序（直到第一个断点），利用“下一指令”选项可以运行下一条指令，利用“继续”选项可以从当前位置开始运行程序（直到下一个断点为止），利用“暂停”选项可以暂停当前的执行任务，利用“停止”选项可以停止程序执行并将虚拟机恢复到刚加载ELF文件时的状态。

利用”查看“菜单栏中的各选项可以查看并修改机器信息（内存，寄存器），可以查看符号表。

在”内存“页面可以输入起始地址以查看或修改从此地址开始的256字节内存内容，如图所示：

![内存编辑器](READMERef/Memory.png)

在“符号表”页面可以输入符号名称前缀以搜索符号，如图所示：

![符号查看器](READMERef/Symbol.png)

## 扩展程序

用户可以从多个方面扩展本程序，从基础层面上，用户可以在不修改源码的情况下扩展指令集。如果用户选择修改源码，则也可以添加系统调用，通过继承一些核心类，用户也可以完全重写模拟器的实现，如实现更加高性能的处理器结构（本程序中实现的是单周期处理器）。

### Config概述

利用`Config`工具包是实现本程序可扩展性的基础手段。其核心思想是将所有不需要编译时确定的常量保存在config文件中，在运行时再读取文件作出对应的决定，而`Config`工具包则确定了一种将数据文件格式化的方式，并给出了扩展自身的方法。

- `Config`类
    `Config`类是Config工具包处理的基本对象。每一个`Config`对象代表用户保存在文件中的一个常量值。一般而言，文件中的Config条目是一行文本，且具有格式：`[char 类型标识符] [String 名称]=[String 值]`。`Config`对象所代表的对象数据类型由类型标识符确定，用户可用名称来寻找Config条目，而值则是这种数据类型的某种文字表示形式。

- `ConfigType<T>`类  
    `ConfigType<T>`类直接处理文件信息。它的核心功能是从一个Config条目中读取一个T类型的对象。可见，它定义了T类型的Config条目的基本格式。  
    ```java
    abstract class ConfigType<T>
    {
        abstract public char valueType();
        abstract public T parse(String line);
        abstract public String getType();
    } 
    ```
    `ConfigType<T>`唯一确定了其类型标识符的值（由`valueType`方法确定）、解析字符串以生成对象的方法（由`parse`方法确定）。  
    用户可以实现自己的`ConfigType`，于是`Config`工具包实现了自己的可扩展性，这对于整个程序的扩展是重要的。

- `ConfigFile`类  
    `ConfigFile`类是`Config`的编组形式。用户将一组Config条目写在文件中，以便读取。同时，`ConfigFile`类也支持简单的注释功能，即若一行的首字符是`‘#’`，则这一行是注释。同时文件中也可以包括空行。

- `ConfigManager`类  
    `ConfigManager`类是Config工具包的对外接口，它管理一组`ConfigFile`。在运行时，用户可以用config条目的全名，即：`[ConfigFile].[Config]`，通过`getConfig(String)方法`来寻找一个`Config`条目，也可以使用`findConfigFile(String)`方法寻找一个ConfigFile。  
    `ConfigManager`的功能依赖于两个特殊文件，即`config/`目录下的`configList`与`configType`。  

    * `configList`文件  
        `configList`文件指明当前ConfigManager管理的文件集合。它的第一行是一个整数n，代表文件数量。之后的n行是n个文件名（相对config/的路径）。
    
    * `configType`文件  
        `configType`文件指明支持的`ConfigType`类型。它的第一行是一个整数n，代表不同的`configType`数量，之后的n行是n个类型名。

- `Config`条目命名规范
    ‘Config’条目的全名应为：`[类型名].[标准java变量名]`。即：若类`A`具有数组`array`，它需要使用一个`Config`代表数组长度，则一般而言推荐的Config全名为"A.arrayLength"，即此常量名应当是语义性的，且符合java变量命名规范，而Config条目应当存放在与类型同名的文件中。

利用`Config`工具包，程序可以在运行时决定虚拟硬件模块的类型，指令的数据通路，以及系统调用的类型及数量（对应上述的三种扩展），也可以允许用户自定义程序使用的字体，语法高亮规则，甚至支持语言包。不过由于时间有限，本程序并未支持UI扩展。

**本节之后的部分与虚拟机实现强相关，推荐在熟悉“具体实现”节内容后阅读。**

### 修改指令集

在不修改代码的前提（即沿用默认的处理器体系结构）下，用户只需适当修改config，即可添加新指令（或删除旧指令）。

具体方法？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？？

### 修改系统调用集

若用户实现`Syscall`类型，并适当修改config，则用户可以修改支持的系统调用集合。

```java
public abstract class Syscall
{
    long num;
    String name;

    public Syscall() {}
    abstract public void call(RISCVMachine machine);
}
```

- 定制系统调用  
    若要定制系统调用，用户需要实现`Syscall`类型。`Syscall`类型具有`num`（系统调用号）与`name`（名称）两个属性，用户需要适当设置之。

    `Syscall`类型的核心是call函数，此函数的参数是一个RISCV虚拟机对象，包含虚拟机的一切资源。用户需要实现的是系统调用执行的全过程，即从传参到执行到写入返回值、改变机器状态寄存器等，都需要在call函数中完成。

- 修改系统调用集  
    用户可以在对应的`MachineController`类的`Config`文件中注册自己的系统调用（或删除/修改原有的系统调用定义）。  
    以新增系统调用为例，用户需要添加一个`Config`条目，格式为：`s [syscallX]=[num] [name] [className]`。这是`SyscallConfig`的标准行格式。`SyscallConfig`解析此行时，会生成一个类型为`className`的`Syscall`对象，同时根据`num`和`name`的值初始化其系统调用号与名称。在此之后，用户需要将syscallCount的值+1。

### 修改虚拟机体系结构

若用户实现`MachineController`类型，并适当修改config，则用户可以修改虚拟机使用的处理器体系结构。

```java
abstract class MachineController extends CombineLogic
{
    RISCVMachine machine;
    CombineLogic[] modules;
    Syscall[] syscalls;

    // do nothing since we don't need signals for a controller.
    protected void initSignals() {}

    abstract protected void initSyscall();
    abstract protected void initControlSignals();
    abstract protected void initCombineLogic();

    // ...
}
```
```java
public abstract class CombineLogic
{
    abstract public void parse();

    abstract protected void initSignals();

    abstract public void reset();

    public void setInput(String name, long value)
    {
        Signal signal = findInputByName(name);

        if (signal != null)
        {
            signal.value = value;
        }
    }

    public Long getOutput(String name)
    {
        parse();
        
        Signal signal = findOutputByName(name);

        if (signal != null)
        {
            return new Long(signal.value);
        }
        return null;
    }

    // ...
}
```

- 实现自己的处理器体系结构  
    若要定制处理器体系结构，用户需要实现`MachineController`类型。‘MachineController’是实际的虚拟机处理器，它包含一组`CombineLogic`结构作为功能模块（如寄存器文件，存储器），以及一组系统调用模块，它的主要任务是将这些模块连接成一个有机的整体。  
    `MachineController`的核心是`parse()`函数，每次执行`parse()`函数，虚拟机应当运行一个周期。  
    另外，`reset()`函数也是必需的。此函数被用于重置机器状态，即恢复到刚载入ELF文件时的状态。  
    需要注意的是，`modules`和`syscalls`之中包含的功能模块和系统调用模块也是需要实现的。或者，用户也可以使用本程序实现的标准模块。这也是此程序之中的基础思想之一，即模块应当是可复用的。

- 注册`MachineController`  
    若要使用自己的`MachineController`，用户需要修改`RISCVMachine.machineControllerName`的值为自己的`MachineController`类型名称。

## 具体实现

时间所限，我们无法为项目编写注释，实际上较为简略的介绍也相当难以给出。以下给出程序核心部分的实现思路及部分代码。

### 综述

程序主要架构如下图所示。

![程序架构简图](READMERef/ProgramAbstract.png)

本程序采用模拟硬件的方式实现RISCV虚拟机。其主要核心结构是`RISCVMachine`，`MachineManager`类与`MachineController`类。掌握本程序核心思想的最简单方法是从装饰者原则入手考虑每一层封装实现的功能。

- `RISCVMachine`类  
    
    ```java
    public class RISCVMachine
    {
        MemorySegment[] memory;

        Symbol[] symbol;

        long[] generalRegister;
        int[] floatRegister;

        long programCounter;

        int machineStateRegister;

        RISCVInstruction[] instructions;

        MachineController controller;

        public void stepOperate()
        {
            controller.parse();
        }

        public void turnBreakpointState(int index)
        {
            instructions[index].isBreakpoint = 
                !instructions[index].isBreakpoint;
        }

        public boolean isRunnable()
        {
            return machineStateRegister == MACHINE_STAT[0].stat;
        }

        int getPCIndex()
        {
            long offset = programCounter - memory[SEGMENT_TEXT].startAddress;
            
            return (int)(offset >>> 2);
        }

        // ...
    }
    ```

    `RISCVMachine`类是RISCV虚拟机的抽象。它拥有机器应当拥有的一切资源，如寄存器，内存（即Von Neumann结构中的存储器），以及控制部件。这也是大部分功能类型需要RISCVMachine类型的对象作为参数的原因。它同时也封装一些较为高层的简单函数，如机器是否可运行，或运行一个周期。

- `MachineManager`类

    ```java
    public class MachineManager extends Thread
    {
        static RISCVSimulatorFrame frm;

        static MessageQueue messageQueue = new MessageQueue();
        static MachineManager instance = new MachineManager();
        static RISCVMachine machine = null;
        static public MachineStateSnapshot snapshot = null;
        static private MachineInitInfo initInfo = null;
        static ConsoleFrame console = new ConsoleFrame();

        static private boolean simulatorWorking = false; 
        static private boolean managerWorking = false;

        static void setMachine(RISCVMachine newMachine)
        {
            // ...
        }

        static boolean checkRunnable()
        {
            // ...
        }

        static public boolean updateSnapshot(long startAddress)
        {
            // ...
        }

        static public boolean updateMachine()
        {
            // ...
        }

        static private void breakpoint(int index)
        {
            machine.turnBreakpointState(index);
        }

        // ...
    }
    ```

    `MachineManager`类直接与上层UI交互。它是一个单例，在程序运行时作为一个独立的线程运行。为了实现一些复杂的高级功能，它拥有更多的资源，也更复杂。如`RISCVMachine`，用于重置虚拟机状态、支持上层UI修改寄存器及内存的Snapshot类型，又如控制台ConsoleFrame。

- `MachineController`类

    ```java
    abstract class MachineController extends CombineLogic
    {
        RISCVMachine machine;
        CombineLogic[] modules;
        Syscall[] syscalls;

        abstract protected void initSyscall();
        abstract protected void initControlSignals();
        abstract protected void initCombineLogic();

        abstract public void parse();

        abstract public void reset();

        // ...
    }
    ```

    `MachineController`类是虚拟机的底层实现核心，对应Von Neumann结构中的Controll Unit，而它所包含的modules和syscalls则对应Arithmetic/Logic Unit。它支持一些简单的包装操作，如`parse()`方法支持单周期运行（其实上层的所有运行操作实际上都是在使用`parse()`方法），`reset()`支持重置虚拟机的某些状态（因为需要适应`Controller`自身的需求，重置操作不能全部由`MachineManager`实现）。

总结来看，程序构造的三个主要元素（Machine，Manager，Controller）之中，Machine和Controller构成一个完整的计算机系统，而Manager可以看做机器外接的硬件调试器，它可以绕过计算机的所有保护机制进行一些“超级”操作，从而满足用户的调试需求。

### 模块概述

首先分析UI的功能及对应的实现目标。

1. UI需要操控虚拟机运行程序，并且应当能显示当前程序状态。  
    需要支持自动反汇编代码，并显示给用户的功能。

2. UI需要实现简单的调试功能，如运行/停止，暂停/继续，需要支持断点。  
    需要虚拟机支持不断运行与单步运行的操作，需要保存虚拟机的初始状态以支持从头开始运行，且最重要地，为了支持停止/暂停等操作，虚拟机不能与主程序在同一线程运行。

3. UI需要实现简单的机器状态读写功能，如修改寄存器，修改内存等。
    需要实现数据结构以支持修改机器状态。

#### MachineManager概述

为了实现上层UI需要的功能，

#### MachineController概述

##### CombineLogic概述

##### 系统调用概述

#### UI元素概述

## 测试

### 综合测试

`test/hello.o`是一个可加载并运行的程序。

### 内存编辑功能测试

内存地址0x10000是text段的起始地址，可以通过查看此地址的内容检视程序（但注意：程序在读取ELF文件时即将代码备份，程序执行时并不读取此段内容，故修改text段的内容不会对程序代码造成影响）。

内存地址0x0FFFFFFFFF800000是栈顶的地址，可以通过将此地址查看并修改栈中的内容。

## 完成度

UI交互与系统调用已经调试通过，虚拟机本身的指令执行功能仍然存在bug，本目录下的代码执行时并不会实际运行指令，而是简单地将PC自增。