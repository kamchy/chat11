What is it?
=======
Simple chat application with minimum dependencies written in pure Java.
Several types of a client program can be created:
 - [done] console client (reads from stdin/writes to stdout) 
 - [done] window client (using Swing)
 - [done] text client 

![screenshot](/shot.png?raw=true "Chat windows with working emoji")

TODO
====
1. allow username changes
1. personal messages/ chatrooms


USAGE
======
1. Build the app using 
  ```bash
  mvn clean package
  ```
1. Run the app with -h option
  ```bash
  java -jar target/chat11.jar -h
  ```
Three clients in action:

![three clients](/three.png?raw=true "Three chat clients")

