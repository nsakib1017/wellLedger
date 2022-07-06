# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

Authentication and Authorization server following Oauth2.0 protocol using spring-security-oauth2.


### Oauth2.0 RFC ###
https://tools.ietf.org/html/rfc6749



### Environment Setup ###

* Database : posgresql 12.2
* Java : openjdk 11.0.6
* Build Tool : gradle 6.0.1

### Build ###

    gradle clean
    gradle build
    
### Project Setup

#### Create Database
After installing the packages for the environment, the first step would be to create the database
with proper user and permission. It is advised to create a database with an user who is set as the owner
of the database with all administrative permissions.

The default database is set as a local postgresql db with:
 - Host: `localhost`
 - Port: `5432`
 - Database name: `priyo_auth_db`
 - Database user: `birdeye`
 - Database Password: `p@ssw0rd`
 
You can find these credentials in the [default application.yml](src/main/resources/application.yml).

You can override the credentials in the `.env` file. 

#### Setup .env File
After that, setup the `.env` file suited to your setup.
- Create a `.env` file by copying from [.env.example file](.env.example).
- Set the values in the `.env` file as per your setup

### Launching the application

To launch the application, run it as a SpringBoot project using the command:

    gradle bootRun


### Initial Database Migrations
This project uses liquibase for maintaining the database schema and seeded data management. By default, no action is
required to setup the database. The liquibase scripts will setup the database accordingly up to the latest version.

Just launch the java application. On startup, Liquibase checks if there are any migrations that are needed to be 
executed, and executes them in the defined order prescribed in the 
[Liquibase Changelog](src/main/resources/db/migration/liquibase-changelog.yml). 


### Adding a new DB Migration

Liquibase is used for database versioning. A gradle task is included to create migration script file
which conforms the liquibase conventions of writing migration files.

To create a migration file:
    
    gradle -q createMigration
    
To create a migration file with specified migration name:

    gradle -q createMigration -PmigrationName=<migration_name>
    
#### What happens when a migration file is created through this gradle task

* Create a sql migration file with:
    * Filename: `v<last version + 1>__<migrationName/auto>_<date>_<time upto minute>`
    * Initial file content following liquibase sql structure
* Include the new sql migration file in the `liquibase-changelog.yml` file

### Creating User
A user have the following properties in the system:
- A user must be created under an office (Bureau/DET).
- The user is given permission via a role. A role must also be created under an office.

For creating functioning users, some utility commands have been created in the
[commands subpackage](src/main/java/com/priyo/security/auth/commands). Please be aware that, 
these commands are created by an inexperienced developer and contains many drawbacks, such as:
- The commands load the whole spring context, rather than the required beans one to execute the commands.
- When exiting after the commands are done, it does not ensure that the side effects of the operations (like sending email, invoking an async routine)
would not fire up. In such cases, we would see some exceptions.
- The commands bypass the authentication, which might cause some unknown problems.
- And many others...


#### Super User
The super user can create other users and manage the permissions of other users of the system. Here are some general
traits of the super user:

- The super user is usually created under the office `DGFI HQ`. 
- The super user is granted the all the permissions via a role.
- The super user usually does not have access permissions related to CDMS.    


#### Regular CDMS User
Regular CDMS users are usually created by the super user via CDMS Admin (birdeye-dashboard) app. A regular user 
also be created via sql, like the super user is created. The additional part is that, the role will need to have CDMS related 
permissions.

#### API User
Use createuser command for this. 
- Name, Email, Office and Desk is important.
- Custom password can be passed as parameter.
- Set type to api.

#### Commands

- Create User:

        gradle bootRun --args='createuser --name=<string> --personalNo=<string> --dgfiEmail=<email> --dgfiId=12415123 --office=<OfficeName> --wing=<WingName> --desk=<DeskName>'
        
        example: gradle bootRun --args='createuser --name=fahim123 --personalNo=52312 --dgfiEmail=fahim123@dgfi.com --dgfiId=12415123 --office=IAB --wing=Wing-2 --desk=Desk-6'
    
    Please ensure that the office, wing and desk hierarchy is correct.
    Upon creating the user, the generated username and password will be printed/logged. Please use that credential to login.
    
        Example:
        2021-07-07 20:19:32.766 [main] INFO  c.p.s.a.commands.CreateUserCommand - <===================User created===================>
        2021-07-07 20:19:32.766 [main] INFO  c.p.s.a.commands.CreateUserCommand - USERNAME: iab_dirnull_12455
        2021-07-07 20:19:32.767 [main] INFO  c.p.s.a.commands.CreateUserCommand - PASSWORD: ajCO6icxyO
        


- Create Data Role:

         gradle bootRun --args='createrole --name=<RoleName> --office=<OfficeName> --type=data'
         
         example:  gradle bootRun --args='createrole --name=IAB-man --office=IAB --type=data'
         

- Create Admin Role:

         gradle bootRun --args='createrole --name=<RoleName> --office=<OfficeName> --type=admin'
         
         example:  gradle bootRun --args='createrole --name=IAB-admin --office=IAB --type=admin'
         
         
- Set Role to User:

         gradle bootRun --args='setrole --role=<RoleName> --username=<UserName>'
         
         example:  gradle bootRun --args='setrole --role=IAB-man --username=iab_dirnull_12415123'

### User Authentication

One a user is created, the user would be able to authenticate. Upon authentication, the system will return a JWT token.
This should be used to perform subsequent action or api calls.
- User authentication is performed by the `grant type=password` grant in the OAuth2 protocol. That means the clients through which the user logs in is trusted by the authorization server.
- The client credentials (for trusted client application) has be set in the `Authorization = Basic $Base64(<client_id>:<client_secret>)`.


Here is the curl command for getting an authenticated JWT token:

    curl --location --request POST 'http:/localhost:8080/oauth/token' \
    --header 'Authorization: Basic Y2xpZW50QHByaXlvLmNvbToxMjM0' \
    --header 'Content-Type: application/x-www-form-urlencoded' \
    --data-urlencode 'grant_type=password' \
    --data-urlencode 'username=<username>' \
    --data-urlencode 'password=<password>'
    
    
# Docker Build

Build command:

    docker build . -t birdeye-auth:<version>
    
Run command:

    docker run -it --env-file .env -p 8080:8080 birdeye-auth

Docker swarm stack deploy command:

    docker stack deploy --compose-file=stack.yml <stack-name>
