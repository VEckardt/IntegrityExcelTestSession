# IntegrityCustomGateway
An alternative starting form for Gateway export client processes, supporting Labels to pick, direct PDF export, pre-defined file name


## Purpose
The CustomGateway is a custom form to execute the Integrity gateway. It offers some very helpful features that were frequently requested by our PTC user community.

![CustomGateway](doc/CustomGateway.PNG)

## Use Cases
- Replaces the Local Gateway form
- Direct PDF export
- Easier to handle exports
- Pre-defined File names
- Can run on items also (not only documents)

## Install
Option 1: In IntegrityClient folder
- Put the "dist/IntegrityCustomGateway.jar" directly into your IntegrityClient folder
- Copy also the files "dist/lib/IntegrityAPI.jar" and "dist/lib/jfxmessagebox-1.1.0.jar" into your IntegrityClient/lib folder
- Add a custom menu entry with:
```
name: Custopm Gateway
program:  ../jre/bin/javaw.exe
parameter: -jar ../IntegrityCustomGateway.jar
```

Option 2: In a shared folder
- Take all files from "dist" folder and place them somewhere centrally
- Register a custom menu as described before, but with the following change
```
parameter: -jar <your shared folder>/IntegrityCustomGateway.jar
```

## Configuration
I have implemented two different options to limit the list of possible export configurations
a) with a property on type level
Define a custom property on type level
Name:  Gateway.Configurations
Value: <Export Configuration1>,<Export Configuration2>,<Export Configuration3>  (Hint: you need to have at least 2 configs listed here)

b) with an additional XML file on server
t.b.d.


## How to test
- open any document or just stay on one in the query result
- click Custom > Custom Gateway
- The custom form should open
- Start the Gateway with a click at the [Generate] button
- Then review the outcome

##  Development environment
- PTC Integrity LM 10.9 (also 11.0 should be fine)
- Netbeans 7.4 (or 8)
- Java 1.7 (or 1.8)

## Known Limitations
- none
