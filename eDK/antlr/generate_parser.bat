@echo off
if not exist "parser" goto ERR
cd parser
del /Q *
cd ..
java -cp antlr-4.0-complete.jar org.antlr.v4.Tool eJVM.g4 -o parser
cd parser
javac -cp ..\antlr-4.0-complete.jar *.java
cd ..
java -cp antlr-4.0-complete.jar;.\parser\ org.antlr.v4.runtime.misc.TestRig eJVM program -gui test.ejvm
goto END
:ERR
echo No folder "parser"
:END
