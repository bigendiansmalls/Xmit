#!/bin/bash

javac -cp "/Users/chad/Downloads/javafx-sdk-11.0.2/lib/*" $(find . -name "*.java")

cat<<EOF> manifest
Manifest-Version: 1.0
Ant-Version: Apache Ant 1.10.5
Created-By: 11.0.2+7 (Oracle Corporation)
Main-Class: com.bytezone.xmit.gui.XmitApp
EOF

jar -cvmf manifest /tmp/XmitMe.jar -C src . -C resources .  >/dev/null 2>&1
git clean -xdf >/dev/null 2>&1

cat<<EOF>run.sh
#!/bin/bash
nohup /usr/bin/java \
--module-path /Users/chad/Downloads/javafx-sdk-11.0.2/lib \
--add-modules=javafx.controls -Dfile.encoding=UTF-8 \
-jar /Users/chad/WorkInProgress/Xmit/XmitMe.jar >/dev/null 2>&1 &
EOF

mv /tmp/XmitMe.jar .
chmod 700 run.sh
