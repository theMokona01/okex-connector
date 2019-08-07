rm ./target/lmaxconnector.jar
mvn clean compile package spring-boot:repackage
scp ./target/lmaxconnector.jar root@95.217.2.217:/connectors/lmax

