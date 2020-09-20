# File versioning

It is created to to send records to different kafka topics based on trx/message event type and merge all the data at consumer to create a new file .

## Installation

### For Starting this project
1> clone the repository.
2> open docker-compose.yaml
3> add your suitable volume path if required
4> Make sure to share your drive with docker
5> run ```docker compose up```  to run the application
6> run ```docker compose down``` to stop the application

### For building image
1> Clone the repository.
2> run docker build -t=IMAGE_NAME:<TAG_NAME> .
3> It will create a new image in your local.

### How to run

1> To upload a file use this API  
     URL: http://localhost:6001/fynd/praveenapp/sendMessageCSV
     METHOD: POST
     FORM DATA: fileName: <multipartfile>
     CONTENT-TYPE: multipart/form-data
2> Sample file that I used for testing is in the same repository with the name of ```test.csv```
3> I have also placed a sample output that was generated in the same repository with the name of ```output.csv``
4>Thankyou

