import com.google.gson.GsonBuilder;
import core.Line;
import core.Station;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class Main
{
    private static final String URL = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(URL).maxBodySize(0).get();
        Elements lineNames = document.select("table.standard tbody tr td:nth-child(1)");  // Получаем названия линий
        Elements stationNames = document.select("table.standard tbody tr td:nth-child(2)"); // Получаем названия станций
        ArrayList<String> lineName = new ArrayList<>();
        ArrayList<String> stationName = new ArrayList<>();
        lineNames.forEach(element -> lineName.add(element.getElementsByAttribute("title").attr("title")));
        stationNames.forEach(element -> stationName.add(element.getElementsByAttribute("title").attr("title").replaceAll("\\s\\(.+\\)", "")));


        ArrayList<Station> stations = new ArrayList<>();
        for (int i = 0; i < stationName.size(); i++){
            stations.add(new Station(stationName.get(i), lineName.get(i)));
        }

        stations.forEach(e-> System.out.println("Имя Станции: " + e.getName() + ", имя линии: " + e.getLine()));


        Elements lineNumber = document.select("table.navbox-columns-table tbody tr td.navbox-list span");
        Elements lllllll = document.select("table.navbox-columns-table tbody tr td.navbox-list dl dd a:last-child");
        // TODO lines
        ArrayList<String> lines = new ArrayList<>();
        lineNumber.forEach(element -> lines.add(element.getElementsByAttribute("title").attr("title").replaceAll("Линия № ", "")));
        ArrayList<String> linesnamenamename = new ArrayList<>();
        lllllll.forEach(element -> linesnamenamename.add(element.text()));


        ArrayList<Line> lineArrayList = new ArrayList<>();
        for(int i = 0; i < lines.size(); i ++){
            if(i%2 != 0) {
                lineArrayList.add(new Line(lines.get(i), linesnamenamename.get(i)));
            }
        }

        lineArrayList.forEach(line -> System.out.println("имя : " + line.getName() + ", номер: " + line.getNumber()));


        String jsoup = new GsonBuilder().setPrettyPrinting().create().toJson(lineArrayList);
        System.out.println(jsoup);
    }
}
