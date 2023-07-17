package entity;

import java.io.Serializable;

public class SIBParameter implements Package, Serializable {
    private String name;
    private double value;
    private int quality;

    public SIBParameter(String name, double value, int quality) {
        this.name = name;
        this.value = value;
        this.quality = quality;
    }

    public SIBParameter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return "SIBParameter{" +
                "name='" + name + '\'' +
                ", value=" + value +
                ", quality=" + quality +
                '}';
    }
}
