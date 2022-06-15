package bgu.spl.net.serverLogic;

public class opCode {
    public static final String REGISTER="01";
    public static final String LOGIN ="02";
    public static final String LOGOUT ="03";
    public static final String FOLLOW="04";
    public static final String POST="05";
    public static final String PM="06";
    public static final String LOGSTAT="07";
    public static final String STAT="08";
    public static final String NOTIFICATION="09";
    public static final String ACK="10";
    public static final String ERROR="11";
    public static final String BLOCK="12";

    public static String ErrorMsg(String x)
    {
        return "ERROR"+x;
    }
    public static String AckMsg(String x)
    {
        return "ACK"+x;
    }
    public static String SpecialAckMsg(String x,String content)
    {
        return "ACK"+x+content;
    }
    public static String notificationMsg(String p,String postingUser,String content)
    {
        return "NOTIFICATION"+p+postingUser+" "+content;
    }

    public static String notificationMsg(String p,String postingUser,String content,String date)
    {
        return "NOTIFICATION"+p+postingUser+" "+content+ " "+ date;
    }
}
