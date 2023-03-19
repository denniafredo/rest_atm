#Steps:
1. Run Kafka Server on port 9092 (app.properties)
2. Run on CMD -> mvn spring-boot:run

#API Auth
1. localhost:8080/auth/login | @body : username (String)
2. localhost:8080/auth/logout | @header : Authorization (Bearer : [jwtToken])

#API ATM
1. localhost:8080/api/deposit |  @header : Authorization (Bearer : [jwtToken]), @body : amount (Long)
2. localhost:8080/api/deposit-log |  @header : Authorization (Bearer : [jwtToken]), @body : status (String)
3. localhost:8080/api/withdraw |  @header : Authorization (Bearer : [jwtToken]), @body : amount (Long)
4. localhost:8080/api/transfer |  @header : Authorization (Bearer : [jwtToken]), @body : transfer (String),amount (Long)

#POSTMAN
atm-api.postman_collection.json