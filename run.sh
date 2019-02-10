#!/bin/bash
nohup /usr/bin/java \
--module-path /Users/chad/Downloads/javafx-sdk-11.0.2/lib \
--add-modules=javafx.controls -Dfile.encoding=UTF-8 \
-jar /Users/chad/WorkInProgress/Xmit/XmitMe.jar >nohup.out 2>&1 &
