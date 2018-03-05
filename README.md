Initial setup(for now):
1) change the ip address on the lines 82, 98 and and 130 in iot.html to the ip address where you're hosting the server
Note: This is temporary, later I'll run the server on azure cloud with a static ip 

Steps to run the server:

1) git clone https://github.com/rahulrad94/webserver-dropwizard.git

2) mvn package

3) java -jar target/rahul-webserver-1.0.jar server

4) Open iot.html and play around with the home appliances
