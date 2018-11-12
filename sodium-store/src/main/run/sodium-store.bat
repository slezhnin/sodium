SET BASEDIR=%~dp0
SET RUN_SCRIPT=%BASEDIR%\..\..\..\build\install\sodium-store\bin\sodium-store.bat

%BUILD_PATH% -conf %BASEDIR%\sodium-store.json
