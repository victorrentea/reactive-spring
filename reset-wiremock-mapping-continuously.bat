@echo off
:loop
echo Reset
curl -X POST http://localhost:9999/__admin/reset

timeout 1
goto loop