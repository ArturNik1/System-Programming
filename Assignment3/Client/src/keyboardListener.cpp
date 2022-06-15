//
// Created by Tomer Ofek on 28/12/2021.
//

#include "../include/keyboardListener.h"
keyboardListener::keyboardListener(ConnectionHandler* con,char delimiter,std::mutex &mutex1,std::condition_variable &val1):close(false), handler(con),delimiter(delimiter),op(new opCode()),mutex(mutex1),val(val1),ready(false) {}
void keyboardListener::setClose(bool set) { this->close=set;}
void keyboardListener::run() {
    char* temp=new char[2];

    while (!this->close)
    {
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        vector<string> split;
        stringstream ss(line);
        string word;
        while (ss >> word) {
            split.push_back(word);
        }

        handler->shortToBytes(op->getOpCode( split.front()),temp);
        if(split.front()=="REGISTER")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1)+'\0'+split.at(2)+'\0'+split.at(3)+'\0';
            handler->sendFrameAscii(toSend,delimiter);
        } else if(split.front()=="LOGIN")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1)+'\0'+split.at(2)+'\0'+split.at(3);
            handler->sendFrameAscii(toSend,delimiter);
        } else if(split.front()=="LOGOUT")
        {
            string toSend= string(1,temp[0])+string(1,temp[1]);
            handler->sendFrameAscii(toSend,delimiter);
            std::unique_lock<std::mutex> lck(this->mutex);
            val.wait(lck);
        } else if(split.front()=="FOLLOW")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1)+split.at(2);
            handler->sendFrameAscii(toSend,delimiter);
        } else if(split.front()=="POST")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1);
            for (int i = 2; i < (int)split.size(); i++) {
                toSend += " " + split.at(i);
            }
            toSend += '\0';
            handler->sendFrameAscii(toSend,delimiter);
            //cout << toSend << endl;
        } else if(split.front()=="PM")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1)+'\0'+split.at(2);
            for (int i = 3; i < (int)split.size(); i++) {
                toSend += " " + split.at(i);
            }
            toSend += '\0';
            std::time_t t = std::chrono::system_clock::to_time_t(std::chrono::system_clock::now());
            char buf[20];
            strftime(buf, 20, "%d.%m.%Y %H:%M:%S", localtime(&t));
            std::string s(buf);
            toSend += s + '\0';
            handler->sendFrameAscii(toSend,delimiter);
        } else if(split.front()=="LOGSTAT")
        {
            string toSend= string(1,temp[0])+string(1,temp[1]);
            handler->sendFrameAscii(toSend,delimiter);
        } else if(split.front()=="STAT")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1)+'\0';
            handler->sendFrameAscii(toSend,delimiter);
        } else if(split.front()=="BLOCK")
        {
            string toSend= string(1,temp[0])+string(1,temp[1])+split.at(1)+'\0';
            handler->sendFrameAscii(toSend,delimiter);
        }
    }
    delete[] temp;
    delete op;
}
