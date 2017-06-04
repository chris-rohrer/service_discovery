# service_discovery
This is a small program to learn about and test zeroconf service discovery/registration with JmDNS.

## Functionality
The program allows you to advertise and look for services that are advertised with a zeroconf protocol. To look for and advertise services it uses JmDNS, an implementation of multi-cast DNS in Java, which is fully interoperable with Apple's Bonjour.  
The program will either look for, or register services supporting the Internet Printing Protocol (ipp) or the Hypertext Transfer Protocol (http) and log the process information to the GUI. Every update message is preceded with a timestamp of the format [mm:ss:cs], where [00:00:00] is the start of the discovery/registration task. When the program is closed the session information can be saved as a text file.  
To get http request information, access the server via the Bonjour tab inside Safari's bookmarks. If you can't see the Bonjour tab follow Apple's [instructions](https://support.apple.com/kb/PH21476?locale=en_US).

## Libraries Used

The official home of the JmDNS library:  
[JmDNS](http://www.jmdns.org)

We used Java SE 8 Update 121 (early 2017)  
Download newest: [JDK 8u131](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
