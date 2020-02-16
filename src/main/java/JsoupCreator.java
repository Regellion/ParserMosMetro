import core.Line;
import core.Station;

import java.util.*;

public class JsoupCreator
{
    private Map<String, List<String>> stations = new TreeMap<>();
    private List<Line> lines = new ArrayList<>();
    private ArrayList<TreeSet> connections = new ArrayList<>();

    public Map<String, List<String>> getStations() {
        return stations;
    }

    public void setStations(Map<String, List<String>> stations) {
        this.stations = stations;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public ArrayList<TreeSet> getConnections() {
        return connections;
    }

    public void setConnections(ArrayList<TreeSet> connections) {
        this.connections = connections;
    }



}
