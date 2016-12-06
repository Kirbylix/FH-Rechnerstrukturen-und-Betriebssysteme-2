@echo off
IF NOT DEFINED JAVA_HOME (GOTO NO_HOME)
"%JAVA_HOME%/bin/java" -cp .\edk.jar;..\lib\antlr-4.0-complete.jar;..\lib\args4j-2.32.jar org.ejvm.frontend.EjvmCompile %*
goto END
:NO_HOME
echo Please set JAVA_HOME to point to the base directory of your Java installation!
:END