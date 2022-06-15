#include "../include/serverListener.h"
#include "../include/opCode.h"
#include "../include/keyboardListener.h"

serverListener::serverListener(ConnectionHandler *handler, char delimiter,std::mutex& mutex1,std::condition_variable &val1, keyboardListener &keyboard1):handler(handler), delimiter(delimiter),mutex(mutex1),val(val1), keyboard(keyboard1) {

}

void serverListener::run() {
    opCode code;
    char* opcodeTemp = new char[2];
    while (true) {
        std::string answer;
        if (!handler->getFrameAscii(answer, delimiter)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
//        cout<<answer<<endl;
        int len = answer.length();
        answer.resize(len-1);
        opcodeTemp[0] = answer[0];
        opcodeTemp[1] = answer[1];
        short result = handler->bytesToShort(opcodeTemp);
        vector<string> split;
        if(result!=10){
            string newAns = answer.substr(2, answer.length());

            string ans = "";
            for (int i = 0; i < (int)newAns.length(); i++) {
                if (newAns[i] == '\0') {
                    split.push_back(ans);
                    ans = "";
                }
                else ans += newAns[i];
            }
            if (ans.length() > 0) split.push_back(ans);
        }


        if (result == 9) {
            string type;
            if (answer[2] == '0') type = "PM ";
            else type = "Public ";
            std::cout << code.getAction(result) + " " + type + split[0].substr(1) + " " + split[1] << endl;
        }
        else if (result == 10) {
            opcodeTemp[0] = answer[2];
            opcodeTemp[1] = answer[3];
            std::cout << code.getAction(result) + " ";
            short resultACK = handler->bytesToShort(opcodeTemp);
            std::cout << resultACK;

            if(answer.length()>4)
            {
                string rest=answer.substr(4);
                while(rest.length()>0)
                {
                    opcodeTemp[0]=rest[0];
                    opcodeTemp[1]=rest[1];
                    cout<<" ";
                    short res=handler->bytesToShort(opcodeTemp);
                    cout<<res;
                    rest=rest.substr(2);
                }
            }
            std::cout << "" << endl;
            if (resultACK == 3) {
                handler->close();
                keyboard.setClose(true);
                break;
            }
        }
        else if (result == 11) {
            opcodeTemp[0] = answer[2];
            opcodeTemp[1] = answer[3];
            std::cout << code.getAction(result) + " ";
            std::cout << handler->bytesToShort(opcodeTemp) << endl;
        }
        val.notify_all();
    }

    val.notify_all();
    handler = nullptr;
    delete[] opcodeTemp;
}
