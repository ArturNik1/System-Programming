package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MessageEncDec implements MessageEncoderDecoder<String> {

    private byte[] bytes = new byte[256];
    private int len = 0;
    private short currentOpcode = 0;
    private String answer = "";

    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == ';') {
            updateAnswer();
            return popString();
        } else {
            pushByte(nextByte);
            return null;
        }
    }

    @Override
    public byte[] encode(String message) {
        //System.out.println("ENCODE: "+message);//TODO:REMOVE
        if (message.startsWith("NOTIFICATION")) {
            // notification
            String temp = "";
            LinkedList<byte[]> q = new LinkedList<>();
            q.addLast(shortToBytes((short)9));
            message = message.substring(12);
            if (message.startsWith("PM")) temp += '0';
            else temp += '1';
            message = message.substring(2);
            int spaceIndex = message.indexOf(' ');
            temp += message.substring(0, spaceIndex) + '\0';
            message = message.substring(spaceIndex+1);
            q.addLast(temp.getBytes());
            q.addLast((message + '\0' + ';').getBytes());
            return combineBytes(q);
        }
        else if (message.startsWith("ACK")) {
            // ack
            LinkedList<byte[]> q = new LinkedList<>();
            q.addLast(shortToBytes((short)10));
            message = message.substring(3);
            String sendAck = message.substring(0, 2);
            message = message.substring(2);
            q.addLast(shortToBytes(stringToShort(sendAck)));
            if (message.length() > 0) {
                List<String> info = Arrays.asList(message.split(" "));
                for (int i = 0; i < info.size(); i++) {
                    q.addLast(shortToBytes(stringToShort(info.get(i))));
                }
            }
            q.addLast(";".getBytes());
            return combineBytes(q);
        }
        else {
            // error
            LinkedList<byte[]> q = new LinkedList<>();
            q.addLast(shortToBytes((short)11));
            String msg = message.substring(5);
            if (msg.charAt(0) == '0') q.addLast(shortToBytes((short)(Integer.parseInt(String.valueOf(msg.charAt(1))))));
            else q.addLast(shortToBytes((short)Integer.parseInt(msg)));
            q.addLast(";".getBytes());
            return combineBytes(q);
        }
    }

    private short stringToShort(String s) {
        if (s.charAt(0) == '0'&&s.length()>1) {
            return (short)(Integer.parseInt(String.valueOf(s.charAt(1))));
        }
        else return (short)(Integer.parseInt(s));
    }

    private byte[] combineBytes(LinkedList<byte[]> q) {
        int lengthNow = 0;
        int totalLength = 0;
        for (byte[] b : q) {
            totalLength += b.length;
        }
        byte[] result = new byte[totalLength];
        for (byte[] b : q) {
            System.arraycopy(b, 0, result, lengthNow, b.length);
            lengthNow += b.length;
        }
        return result;
    }

    private String popString() {
        String result = answer;
        len = 0;
        currentOpcode = 0;
        answer = "";
        //System.out.println(result);
        return result;
    }

    private void updateAnswer() {
        answer += new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        if (currentOpcode > 0) {
            afterOpcodeSet(nextByte);
            return;
        }

        bytes[len++] = nextByte;
        if (len == 2 && currentOpcode == 0) {
            currentOpcode = bytesToShort(bytes);
            if (currentOpcode < 10) answer += "0" + currentOpcode;
            else answer += currentOpcode;
            len = 0;
        }
    }

    private void afterOpcodeSet(byte nextByte) {
        bytes[len++] = nextByte;
        if (nextByte == '\0') {
            updateAnswer();
        }
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

}
