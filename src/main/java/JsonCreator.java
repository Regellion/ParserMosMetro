import core.Line;
import core.Station;

import java.util.*;

public class JsonCreator
{
    private Map<String, List<String>> stations = new TreeMap<>();
    private List<Line> lines = new ArrayList<>();
    private TreeSet<TreeSet<Station>> connections = new TreeSet<>(Comparator.comparing(AbstractCollection::toString));

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

    public TreeSet<TreeSet<Station>> getConnections() {
        return connections;
    }

    public void setConnections(TreeSet<TreeSet<Station>> connections) {
        this.connections = connections;
    }



}
