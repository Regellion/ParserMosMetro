package core;

import java.util.ArrayList;
import java.util.List;

public class Line
{
    private String number;
    private String name;
    private String color;
    // Можно конечно было бы воспользоваться transient для удаления ненужного
    // поля у линии, но говорят что использовать свой сериализатор лучше)))
    private List<String> stations;

    public Line(String number, String name)
    {

        this.number = number;
        this.name = name;
        stations = new ArrayList<>();
    }

    public String getNumber()
    {
        return number;
    }

    public String getName()
    {
        return name;
    }

    public void addStation(String station)
    {
        stations.add(station);
    }

    public List<String> getStations()
    {
        return stations;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }
}
