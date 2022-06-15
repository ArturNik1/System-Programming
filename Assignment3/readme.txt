Server:
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="7777"
// for reactor:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7777 8"

Client:
make
./bin/BGSClient 127.0.0.1 7777


Examples:
REGISTER <name> <password> <date -> 12/12/1222>
LOGIN <name> <password> <0/1>
LOGOUT
FOLLOW <0=follow / 1=unfollow> <name>
POST <content>
PM <name> <content>
LOGSTAT
STAT <userlist = name1|name2|...>
BLOCK <name>


You can find the filtered words at line 25 in Network.java
