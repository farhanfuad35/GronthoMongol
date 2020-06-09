@echo off

title Backendless CodeRunner

set MINIMAL_VERSION=1.8.0

java -version 1>nul 2>nul || (
   echo "Not able to find Java executable or version. Please check your Java installation."
   exit /b 1
)

for /f tokens^=2-5^ delims^=.-_^" %%j in ('java -fullversion 2^>^&1') do set jver=%%j.%%k.%%l_%%m

if %jver% LSS %MINIMAL_VERSION% (
  echo "Error: Java version is too low to run CodeRunner. At least Java >= %MINIMAL_VERSION% needed."
  exit /b 1
)

echo Starting CodeRunner

java -Djava.net.preferIPv4Stack=true -Duser.timezone=UTC -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -Dfile.encoding=UTF-8 -Dlogback.configurationFile=logback.xml -XX:+HeapDumpOnOutOfMemoryError -cp "*;../libs/*" com.backendless.coderunner.CodeRunnerLoader

pause
