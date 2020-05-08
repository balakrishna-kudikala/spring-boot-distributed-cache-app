# spring-boot-distributed-cache-app
## Spring Boot Application for Distributed Cache Implementation using spring-boot, Oracle AQ, spring-jms, swagger, spring-data-jpa
 
 1. spring-boot  - for bootstrapping the web app.
 2. spring-boot-starter-cache for basic caching. We should go for a caching implementation for better datastructures, Ejection policies, algorithms and better control over caching.
 3. Oracle AQ for enabling publish-subscribe model for notifying other instances about the cache updates
 4. spring-jms for publishing and receiving messages to/from Oracle AQ.
 5. Swagger - for documentation.
 6. spring-data-jpa - for reading data from the in-memory database.
 
  ## H2 In-Memory Database Details

```
jpa.datasource.url=jdbc:h2:mem:testdb
jpa.datasource.driverClassName=org.h2.Driver
jpa.datasource.username=sa
jpa.datasource.password=password
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```
## Oracle Database Details
* I have created an Oracle RDS instance in AWS and created AQ. I made this DB accessible publicly.
```
aq.datasource.url=jdbc:oracle:thin:@myoracledb.chepwzja9twd.ap-south-1.rds.amazonaws.com:1521:testdb
aq.datasource.driverClassName=oracle.jdbc.driver.OracleDriver
aq.datasource.username=admin
aq.datasource.password=password-1
```
## Oracle AQ Topic Details
* If you want to create your own AQs (Topic). Follow below steps to create AQ Table, AQ(Topic) and start it. Make sure you set the argument multiple_consumers to true. This makes the queue to a topic.

* You may need to have permissions in database to create a queue and start it. Below commands may be helpful for granting the permissions.
```
CONNECT system/manager;
DROP USER aqadm CASCADE;
CREATE USER aqadm IDENTIFIED BY aqadm;
GRANT CONNECT, RESOURCE TO aqadm; 
GRANT EXECUTE ON DBMS_AQADM TO aqadm;
GRANT Aq_administrator_role TO aqadm;
DROP USER aq CASCADE;
CREATE USER aq IDENTIFIED BY aq;
GRANT CONNECT, RESOURCE TO aq; 
GRANT EXECUTE ON dbms_aq TO aq;
```
* Oracle AQ Topic Creation Commands
```
EXECUTE DBMS_AQADM.CREATE_QUEUE_TABLE ( queue_table        => 'user_cache_topic_tbl', multiple_consumers => TRUE, queue_payload_type => 'SYS.AQ$_JMS_TEXT_MESSAGE');

EXECUTE DBMS_AQADM.DROP_QUEUE_TABLE ( queue_table        => 'user_cache_topic_tbl');-- You may not require this, Just in case.

EXECUTE DBMS_AQADM.CREATE_QUEUE (queue_name         => 'user_cache_topic',queue_table        => 'user_cache_topic_tbl');

EXECUTE DBMS_AQADM.DROP_QUEUE (queue_name         => 'user_cache_topic');

EXECUTE DBMS_AQADM.START_QUEUE (queue_name         => 'user_cache_topic');

EXECUTE DBMS_AQADM.STOP_QUEUE (queue_name         => 'user_cache_topic')

```
 ## How to build ?
* There are Oracle AQ specific jars, which are not part of mvnrepository. I had to copy them into src\main\resources\jars folder, mark them system scope, refer them through systemPath in pom.xml and pull them into the jar by configuring spring-boot-maven-plugin to includeSystemScope.
* Go to Project root directory, build the Project and Package the jar.
```
mvn clean
mvn package
```
## How to start ?
* Go to target directory, you would find the app packaged into a fat executable jar. Run the below commands to spin multiple instances of the application so that we can test the cache behaviour in distributed environment.
```
java -jar spring-boot-distributed-cache-app-0.0.1-SNAPSHOT.jar --server.port=9002
java -jar spring-boot-distributed-cache-app-0.0.1-SNAPSHOT.jar --server.port=9003
```
## Swagger-UI

* After starting the application Click on [Swagger-home](http://localhost:9002/swagger-ui.html)
* You can do this for all the instances that you have run the application. 
* I have created database initialization scripts to have default users in the app. You can invoke the /user/all API to know all the users info. Alternatively, you can look into <b>{project.root}/src/main/resources/data.sql</b>
* GET /user?userID=SM1923 service, is to get a user details for a given ID from database. It gets the user from cache if exists, otherwise gets from DB and updates the cache.
* POST /user service, it to update the user details in the database. It updates the user details into cache as well.
* GET /user/all service, is to get all the existing users in the database currently.
* GET /user/cache-synch, is to get all the users present in cache. It doest refer users in database.
* POST /user/cache-synch, is to update the cache with the input user details. It doesnt update the user in database.

## Steps to Test

* invoke GET /user/cache-synch in the all the instances, you will see that there are no users in cache.
* invoke POST /user/cache-synch in any one instance, provide a payload for a user (you can pick it from /user/all API). You will get the success response. i.e. User is added in the cache and published a message to the Oracle AQ Topic. All the other instances listen to the topic and add the user into their cache.
* invoke GET /user/cache-synch in the any other instance, you will see that previously updated user is in cache in another instance. 
