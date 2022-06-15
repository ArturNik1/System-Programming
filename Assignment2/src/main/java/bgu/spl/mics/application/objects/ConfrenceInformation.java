package bgu.spl.mics.application.objects;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {
    private String name;
    private int date;
    private int ticks;
    private HashSet<String> modelNames;
    private LinkedList<DummyModelJson> models;

    public ConfrenceInformation (String name, int date) {
        this.name = name;
        this.date = date;
        this.ticks = 0;
        this.modelNames = new HashSet<>();
        this.models = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public int getDate() {
        return date;
    }

    public LinkedList<DummyModelJson> getModels() {
        return models;
    }

    public boolean isDone() {
        return ticks == date;
    }

    public void addToList(String modelName) {
        Cluster.getInstance().getStatistics().addModelName(modelName);
        modelNames.add(modelName);
    }

    public void addToModelList(Model model) {
        this.models.add(new DummyModelJson(model.getName(), model.getData(), model.getStatus(), model.getResult()));
    }

    public HashSet<String> getModelNames() {
        return this.modelNames;
    }

    public void incTicks()
    {
        ticks++;
    }
}
