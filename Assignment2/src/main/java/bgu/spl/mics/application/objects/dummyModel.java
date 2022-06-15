package bgu.spl.mics.application.objects;

public class dummyModel {
    private String name;
    private Data.Type type;
    private Integer size;
    public dummyModel(String name,Data.Type type,Integer size)
    {
        this.name=name;
        this.type=type;
        this.size=size;
    }

    public String getName() {
        return name;
    }

    public Data.Type getType() {
        return type;
    }

    public Integer getSize() {
        return size;
    }
}
