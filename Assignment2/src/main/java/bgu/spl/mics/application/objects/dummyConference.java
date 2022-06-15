package bgu.spl.mics.application.objects;

public class dummyConference {
    private String name;
    private int date;
    public dummyConference(String name,int date)
    {
        this.name=name;
        this.date=date;
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }
}
