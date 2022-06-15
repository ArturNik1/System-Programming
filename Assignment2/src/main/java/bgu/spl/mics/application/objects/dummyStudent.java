package bgu.spl.mics.application.objects;

import java.util.List;

public class dummyStudent {
    private String name;
    private String department;
    private Student.Degree status;
    private List<dummyModel> models;
    public dummyStudent(String name, String  department,Student.Degree status,List<dummyModel>models)
    {
        this.name=name;
        this.department=department;
        this.status=status;
        this.models=models;

    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public Student.Degree getStatus() {
        return status;
    }

    public List<dummyModel> getModels() {
        return models;
    }
}
