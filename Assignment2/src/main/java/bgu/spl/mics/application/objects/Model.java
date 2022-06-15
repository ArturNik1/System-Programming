package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status {PreTrained, Training, Trained, Tested};
    public enum Results {None, Good, Bad};

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Results result;

    public Model (String name, Data data, Student student) {
        this.name = name;
        this.data = data;
        this.student = student;
        this.status = Status.PreTrained;
        this.result = Results.None;
    }

    public String getName() {
        return this.name;
    }

    public Data getData() {
        return data;
    }

    public void setStatus(Status current) {
        this.status = current;
    }

    public Student getStudent() {
        return student;
    }

    public void changeStatusToTested() {
        status = Status.Tested;
    }

    public Status getStatus() {
        return this.status;
    }

    public Results getResult() {
        return this.result;
    }

    public void changeResult(String current) {
        switch (current) {
            case "None":
                result = Results.None;
                break;
            case "Good":
                result = Results.Good;
                break;
            case "Bad":
                result = Results.Bad;
                break;
        }
    }

}
