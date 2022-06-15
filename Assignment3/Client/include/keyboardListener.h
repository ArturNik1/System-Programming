//
// Created by Tomer Ofek on 28/12/2021.
//

#ifndef CLIENT_KEYBOARDLISTENER_H
#define CLIENT_KEYBOARDLISTENER_H
#include <string>
#include <iostream>
#include "connectionHandler.h"
#include "opCode.h"

using namespace std;

class keyboardListener {
private:
    bool close;
    const short bufsize = 2000;
    ConnectionHandler* handler;
    char delimiter;
    opCode* op;
    std::mutex& mutex;
    std::condition_variable& val;
    bool ready;
public:
    keyboardListener(ConnectionHandler* con,char delimiter,std::mutex& mutex1,std::condition_variable &val1);
    void setClose(bool set);
    void run();
    string decode();
};


#endif //CLIENT_KEYBOARDLISTENER_H
