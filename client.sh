#/usr/bin/bash
CHATTER="$1"
CHATTER=${CHATTER:-$USER}
java -jar target/chat11.jar -c localhost 8010 $CHATTER
