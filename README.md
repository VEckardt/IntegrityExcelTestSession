# IntegrityExcelTestSession
This local application offers the download and upload from Integrity Test Sessions into an Excel file. The Tester can take the Excel and enter the results offline, connect then again to Integrity und uploads the results then later.

![ExcelTestSession](doc/ExcelTestSession.PNG)

## Use Cases
- Offline Test Result editor
- Full test session export or just the remainin open test results

## Install
Option 1: In IntegrityClient folder
- Put the "dist/IntegrityExcelTestSession.jar" directly into your IntegrityClient folder
- Copy also all the library files from "dist/lib" into your IntegrityClient/lib folder
- Add a custom menu entry with:
```
name: Excel Test Session
program:  ../jre/bin/javaw.exe
parameter: -jar ../IntegrityExcelTestSession.jar
```

Option 2: In a shared folder
- Take all files from "dist" folder and place them somewhere centrally
- Register a custom menu as described before, but with the following change
```
parameter: -jar <your shared folder>/IntegrityExcelTestSession.jar
```

## How to test
Part 1: Export
- open any Test Session or just stay on one in the query result
- click Custom > Excel Test Session
- The custom form should open
- Start the Export with a click at the [Generate] button
- Then review the outcome

Part 2: Import
- open any Test Session or just stay on one in the query result
- click Custom > Excel Test Session
- The custom form should open
- Start the Import with a click at the [Load] button
- Then review the outcome

##  Development environment
- PTC Integrity LM 10.9 (also 11.0 should be fine)
- Netbeans 7.4 (or 8)
- Java 1.7 (or 1.8)

## Known Limitations
- none
