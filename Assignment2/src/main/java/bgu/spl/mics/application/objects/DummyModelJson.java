package bgu.spl.mics.application.objects;

public class DummyModelJson {
    private String name;
    private DummyDataJson data;
    private Model.Status status;
    private Model.Results result;

    public DummyModelJson(String name, Data data, Model.Status status, Model.Results result) {
        this.name = name;
        this.data = new DummyDataJson(data.getType(), data.getSize());
        this.status = status;
        this.result = result;
    }
}

class DummyDataJson {
    private Data.Type type;
    private int size;

    public DummyDataJson(Data.Type type, int size) {
        this.type = type;
        this.size = size;
    }
}