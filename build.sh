#!/bin/bash

javac -cp "/Users/chad/Downloads/javafx-sdk-11.0.2/lib/*" $(find . -name "*.java")

cat<<EOF> manifest
Manifest-Version: 1.0
Ant-Version: Apache Ant 1.10.5
Created-By: 11.0.2+7 (Oracle Corporation)
Main-Class: com.bytezone.xmit.gui.XmitApp
EOF

jar -cvmf manifest XmitMe.jar -C src . -C resources .

