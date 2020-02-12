package core;

public class Station implements Comparable<Station>
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
    public int compareTo(Station station)
    {
        return line.compareTo(station.getLine());
    }

    @Override
    public boolean equals(Object obj)
    {
        return compareTo((Station) obj) == 0;
    }


    @Override
    public String toString()
    {
        return name;
    }
}
