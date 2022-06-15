#ifndef CLIENT_SERVERLISTENER_H
#define CLIENT_SERVERLISTENER_H
#include "connectionHandler.h"
#include "keyboardListener.h"

using namespace std;

class serverListener {
private:
    ConnectionHandler* handler;
    char delimiter;
    std::mutex& mutex;
    std::condition_variable& val;
    keyboardListener& keyboard;
public:
    serverListener(ConnectionHandler* handler, char delimiter,std::mutex& mutex1,std::condition_variable& val1, keyboardListener& keyboard1);
    void run();
};


#endif //CLIENT_SERVERLISTENER_H
