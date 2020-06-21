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
1. Run the app. To get help, you can call the jar with -h option:
  ```bash
  java -jar target/chat11.jar -h
  ```
  which would print following message:
  ``` bash
    Usage: [one of]
    java -jar chat11.jar -h                         prints this message
    java -jar chat11.jar -s [port]                  stars server on port (deault: 8881)
    java -jar chat11.jar -c host port username      starts console client that connects to host on port as username
    java -jar chat11.jar -cc host port username     starts curses console client that connects to host on port as username
    java -jar chat11.jar -cg host port username     starts swing gui client that connects to host on port as username
  ```
Three clients in action:

![three clients](/three.png?raw=true "Three chat clients")

