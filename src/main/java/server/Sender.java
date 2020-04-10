package server;

import commom.Message;

interface Sender {
    void send(Message m, ServerUser target);
    void removeUser(ServerUser user);
}
