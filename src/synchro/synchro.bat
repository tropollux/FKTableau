
@echo off

::set _destination="\\MAC-MINI\Dossier public de Cécile"
::set _destination="u:\"
::set _destination=C:\Users\remi\workspaces\maven\tableau.home
set _destination=%1%
set _engine="..\sqlite3.exe"
set _db=ffcanoe.db3
::set _db=..\database\ffcanoe.db3
::set _init=init.sql


:begin
copy ..\database\ffcanoe.db3 %_db% /y > nul

call %_destination%\courseId.bat

if "%fullCopy%" neq "" (
	xcopy /Y %_db%* %_destination% > nul
	echo "full copy"
	goto :waitPing
)

if "%codeCoureur%" neq "" (
	set _whereClause=and Code_coureur='%codeCoureur%'
	) else (
	set _whereClause=
	)
echo %time% : transfert pour la course %courseId% phase %phase% codeCoureur %codeCoureur%
::set courseId=79
::set phase=16
::set timeout=10000

set _output=run.data
set _order=select * from resultat_manche_run where Code_evenement=%courseId% and Code_manche=%phase% %_whereClause%;
call:executeSQL %_order%

set _output=juge.data
set _order=select * from resultat_manche_run_juges where Code_evenement=%courseId% and Code_manche=%phase% %_whereClause%;
call:executeSQL %_order%

xcopy /Y *.data %_destination% > nul

:waitPing
ping 1.1.1.1 -n 1 -w %timeout% > nul
goto:begin

:executeSQL
 :: @echo.Executing SQL: %*
 :: -init %_init% -html -csv -list -separator 'x' -line -header
 call %_engine% -batch %_db% "%*" > %_output%
 if %errorlevel% gtr 0 @echo.Execution Failure
 goto:eof


:eof
echo c'est termine
pause