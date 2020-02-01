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
    // URL сайта для парсинга
    private static final String URL = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    // CSS запрос для получения названия линии
    private static final String CSS_GET_LINE_NAME = "td:nth-child(1) span:nth-child(2)";
    // CSS запрос для получения названия станции
    private static final String CSS_GET_STATION_NAME = "td:nth-child(2) a";
    // CSS запрос для получения html блока отдельно взятой станции
    private static final String CSS_GET_STATIONS_HTML_BLOCK = "table.standard tbody tr";
    // CSS запрос для получения html блока отдельно взятой линии
    private static final String CSS_GET_LINES_HTML_BLOCK = "table.navbox-columns-table dd";
    // CSS запрос для получения номера линии
    private static final String CSS_GET_LINE_NUMBER = "span:nth-child(2)";

    // Регулярка для приведения имен станций к единой форме отображения
    private static final String SPOTTER_NAMES_REGEX = "\\s\\(.+\\)";
    // Регулярка для приведения номеров линий к понятной форме
    private static final String SPOTTER_NUMBER_LINES_REGEX = "Линия № ";


    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(URL).maxBodySize(0).get();
        ArrayList<Station> stations = new ArrayList<>();
        ArrayList<Line> lines = new ArrayList<>();

        // TODO Выделить в отдельный метод (принимает документ, возвращает список станций) ps Возможно придется парсить на название линии а номер!!!!
        Elements stationsHtml = document.select(CSS_GET_STATIONS_HTML_BLOCK);
        stationsHtml.forEach(element -> {
                // Проверяю, нет ли пустых значений(а они есть:) )
                if(!element.select(CSS_GET_LINE_NAME).attr("title").equals("")) {
                    stations.add(new Station(element.select(CSS_GET_STATION_NAME).attr("title")
                            .replaceAll(SPOTTER_NAMES_REGEX, "")
                            , element.select(CSS_GET_LINE_NAME).attr("title")));
                }});

        // TODO Выделить в отдельный метод (принимает документ, возвращает список линий)
        Elements lineHtml = document.select(CSS_GET_LINES_HTML_BLOCK);
        lineHtml.forEach(element -> lines.add(new Line(element.select(CSS_GET_LINE_NUMBER).attr("title")
                    .replaceAll(SPOTTER_NUMBER_LINES_REGEX, "")
                    , element.select(">a").attr("title").replaceAll(SPOTTER_NAMES_REGEX, ""))));




        String jsoup = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(lines);
        System.out.println(jsoup);

    }
}
