# RISCVSimulator

Simple pluggable RISCV simulator implements RISCV64IMF

## For Contributors

To initialize your workspace after your first `git clone` operation, use __init.bat__.

## 安装程序

运行`compile.sh`以完成编译安装，在完成编译之后需要将riscv-toolchain中附带的`objdump`程序复制到`bin/`文件夹。

## 使用程序

利用`run.sh`运行程序。

在程序主界面使用菜单中的“打开”选项可以读取并打开ELF文件，打开成功后程序将在主界面加载文件中的代码，使用鼠标滚轮可进行上下翻动。在每一行单击行号左方的区域可以添加/删除断点。

利用“调试”菜单栏中的“运行“选项可运行程序，”单步运行“选项可单步执行程序，”暂停“选项可以暂停运行中的程序，”停止“选项可将虚拟机复位，”继续“选项可从暂停处开始继续执行。

利用”查看“菜单栏中的各选项可以查看并修改机器信息（内存，寄存器），可以查看符号表。在”内存“页面可以输入起始地址以查看或修改从此地址开始的256字节内存内容，在“符号表”页面可以输入符号名称前缀以搜索符号。

## 测试样例

`test/hello.o`是一个可加载并运行的程序。