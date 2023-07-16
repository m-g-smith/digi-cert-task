#Running the task

First you need to open a terminal inside the root of the project.

Then run: ```mvn clean verify```

This should run the tests using maven surefire as well
as create a docker image on your local machine using the fabric 8 plugin. 
The image should be ```digi-cert-task```.

If the image was not created you can run ```mvn docker:build```, which should have been called during the ```mvn package``` step.

You can then run ```docker compose up```. This should first start up the mysql database and then the spring boot app.

At this point the application should be available on port 23232 for use. There is a postman collection that can be used to test the application

To tear down run ```docker compose down```

Please note that the database is not using permanent storage. i.e. if the container is shut down all data is lost.