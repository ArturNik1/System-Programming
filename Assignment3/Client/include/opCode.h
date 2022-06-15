//
// Created by Tomer Ofek on 28/12/2021.
//

#ifndef CLIENT_OPCODE_H
#define CLIENT_OPCODE_H
#include <string>
using namespace std;
class opCode {
    public:
    opCode();
    short getOpCode(string action);
    string getAction(short opcode);
};


#endif //CLIENT_OPCODE_H
