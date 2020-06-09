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

java -Duser.timezone=UTC -Dlogback.configurationFile=logback.xml -Dfile.encoding=UTF-8 -cp "*;../libs/*" com.backendless.coderunner.CodeRunnerLoader deploy
