import core.Line;

import java.util.*;

public class JsoupCreator
{
    private Map<String, List<String>> stations = new TreeMap<>();
    private List<Line> lines = new ArrayList<>();

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

}
