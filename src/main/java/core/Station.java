package core;

public class Station
{
    private String line;
    private String name;

    public Station(String name, String line)
    {
        this.name = name;
        this.line = line;
    }

    public String getLine()
    {
        return line;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}
