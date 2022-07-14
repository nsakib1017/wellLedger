# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

Get HealthLedger DApp up and running.



### Environment Setup ###

* Java : openjdk 11.0.6
* Build Tool : gradle 6.0.1

### Build ###

    gradle clean
    gradle build
    
### Project Setup

#### Launching the application

The link to Hyperledger fabric bin files used for the network is,
https://drive.google.com/drive/folders/16HkyMHWTRZV8dh_V_GSxg_Bf1N3bsi1S?usp=sharing

Copy the contents of this folder and add them to **bin** directory of the **fabricnetwork** module. Also, you need to create a database using `postgresql` and map the settings in `application.yml` file (You can also create a `application-dev.yml` file if iou want to). 

To launch the application, run it as a SpringBoot project using the command:

    gradle bootRun


#### Launching the Fabric Network

Change directory to the `fabricnetwork` module and open the terminal and run `./startFabric.sh`. 
Consequently, to tear down the network run `./networkDown.sh`. After that, delete the contents of the `wallet` folder.