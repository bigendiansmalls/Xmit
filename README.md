# Xmit
- view the contents of xmit files
- view xmit files [stored as PDS members](resources/embedded.md)
- view [compressed xmit files](resources/compressed.md)
- extract [individual PDS members](resources/extract.md)
- selectable [codepages](resources/view.md)
- selectable [fonts](resources/fonts.md)

## Sample screens
### MacOS
![Mac](resources/xmitosx.png?raw=true "Mac")
### Windows
![Windows](resources/xmit-win.png?raw=true "Windows")
### Linux
![Linux](resources/xmitlinux.png?raw=true "Linux")

## Installation
- Download and install [Java 11](https://jdk.java.net/11/) and [JavaFX 11](https://gluonhq.com/products/javafx/), which are now separate downloads.
- Download [XmitApp](https://github.com/dmolony/xmit/releases).
- Create executable run file.  
#### MacOS or Unix shell file  

```
/path/to/jdk-11.0.2.jdk/Contents/Home/bin/java    \
--module-path /path/to/javafx-sdk-11/lib          \
--add-modules=javafx.controls                     \
-Dfile.encoding=UTF-8                             \
-jar /path/to/XmitApp.jar
```  
#### Windows batch file  

```
C:\path\to\jdk-11.0.2\bin\java.exe                \
--module-path C:\path\to\javafx-sdk-11\lib        \
--add-modules=javafx.controls                     \
-Dfile.encoding=UTF-8                             \
-jar C:\path\to\XmitApp.jar
```
### First execution
Read the helpful message.  
<img src="resources/xmit-folder1.png" alt="alert" width="500"/>  
Specify the location of your xmit files. Note that this must be a FOLDER, not a file. The specified folder may contain subfolders, these will all appear in the file tree within the application.  
<img src="resources/xmit-folder2.png" alt="file dialog" width="800"/>  
This will remain the xmit file folder until you change it.  
