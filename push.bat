@echo off
echo Fixing iOS file path API and pushing...
git add -A
git commit -m "Fix iOS NSURL path API compatibility"
git push origin main
echo ====== Exit: %ERRORLEVEL% ======
pause
