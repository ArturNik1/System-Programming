//
// Created by Tomer Ofek on 28/12/2021.
//

#include "../include/opCode.h"

opCode::opCode() {}
short opCode::getOpCode(string action) {
    if(action=="REGISTER")
        return 1;
    if(action=="LOGIN")
        return 2;
    if(action=="LOGOUT")
        return 3;
    if(action=="FOLLOW")
        return 4;
    if(action=="POST")
        return 5;
    if(action=="PM")
        return 6;
    if(action=="LOGSTAT")
        return 7;
    if(action=="STAT")
        return 8;
    if(action=="NOTIFICATION")
        return 9;
    if(action=="ACK")
        return 10;
    if(action=="ERROR")
        return 11;
    if(action=="BLOCK")
        return 12;
    return -1;
}

string opCode::getAction(short opcode) {
    if (opcode == 9) return "NOTIFICATION";
    else if (opcode == 10) return "ACK";
    else if (opcode == 11) return "ERROR";
    else return "Nothing";
}