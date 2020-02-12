import com.google.gson.GsonBuilder;
import core.Line;
import core.Station;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


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
    // CSS запрос для получения номера линии (для линии)
    private static final String CSS_GET_LINE_NUMBER = "span:nth-child(2)";
    // CSS запрос для получения номера линии (для станции)
    private static final String CSS_GET_LINE_NUMBER_TO_STATION = "td:nth-child(1) span:nth-child(1)";
    // CSS запрос для получения цвета линий
    private static final String CSS_GET_LINE_COLOR = ":first-child";

    // Регулярка для приведения имен станций к единой форме отображения
    private static final String SPOTTER_NAMES_REGEX = "\\s\\(.+\\)";
    // Регулярка для приведения номеров линий к понятной форме
    private static final String SPOTTER_NUMBER_LINES_REGEX = "Линия № ";


    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect(URL).maxBodySize(0).get();


        ArrayList<Station> stations = getStations(document);
        ArrayList<Line> lines = getLines(document);
        ArrayList<TreeSet> connections = new ArrayList<>(); // доработать методом


        // Парсим переходы
        AtomicInteger i = new AtomicInteger(0);
        Elements stationsHtml = document.select(CSS_GET_STATIONS_HTML_BLOCK);
        stationsHtml.forEach(station -> {
            // Если станции не пустые
            if(!station.select(CSS_GET_STATION_NAME).attr("title")
                    .replaceAll(SPOTTER_NAMES_REGEX, "").equals("")) {
                // Если есть переходы
                if(!station.select(":nth-child(4)").attr("data-sort-value").equals("Infinity")){
                    String[] transitionNumberArray = station.select(":nth-child(4) span.sortkey").text().split("\\s");
                    String[] transitionNameArray = station.select(":nth-child(4) span").eachAttr("title").toArray(new String[0]);
                    String nameConnection;
                    String numberConnection ;
                    TreeSet<Station> connect = new TreeSet<>();
                    // для переходов с ошибкой
                    if(Arrays.toString(transitionNumberArray).contains("14, 02, 09") || Arrays.toString(transitionNumberArray).contains("13, 02, 10") || Arrays.toString(transitionNameArray).contains("Показать карту")) {
                            //TODO регуляркой выдрать имена станций и привести номера к общему виду
                            // незабыть 11А удалить, удалить дубликаты и добавить итерации для добавления в основные станции
                        nameConnection = transitionNameArray[1].replaceAll(".+нцию ", "").replaceAll("\\s.+", "");
                        numberConnection = transitionNumberArray[2].replaceAll("^0", "");

                        String finalNameConnection = nameConnection;
                        String finalNumberConnection = numberConnection;
                        stations.forEach(s-> {
                            if(s.getName().contains(finalNameConnection) && s.getLine().equals(finalNumberConnection)){
                                connect.add(s);
                            }
                        });
                    } else {
                        // Иначе сделать обычный парсинг
                        for(int iterator = 0; iterator < transitionNumberArray.length; iterator++){
                            // таким макаром добавлять станции
                            nameConnection = transitionNameArray[iterator].replaceAll(".+нцию ", "").replaceAll("\\s.+", "");
                            numberConnection = transitionNumberArray[iterator].replaceAll("^0", "");
                            String finalNameConnection1 = nameConnection;
                            String finalNumberConnection1 = numberConnection;
                            stations.forEach(s-> {
                                if (s.getName().contains(finalNameConnection1) && s.getLine().equals(finalNumberConnection1)) {
                                    connect.add(s);
                                }
                            });
                        }
                    }
                    connect.add(stations.get(i.get()));
                    connections.add(connect);
                }
                i.getAndIncrement();
            }

        });

        /*AtomicInteger i = new AtomicInteger(0);
        stationsHtml.forEach(station -> {
            // Если станции не пустые
            if(!station.select(CSS_GET_STATION_NAME).attr("title")
                            .replaceAll(SPOTTER_NAMES_REGEX, "").equals("")) {
                // Если есть переходы
                if(!station.select(":nth-child(4)").attr("data-sort-value").equals("Infinity")){
                    // Если станции не с удаленной линии
                    if(!stations.get(i.get()).getLine().equals("11А")) {
                        // Создаем блок перехода
                        TreeSet<Station> connect = new TreeSet<>();
                        // Создаем список переходов
                        String[] connectionInfo = station.select(":nth-child(4) span").toString().split("</span>");
                        for(int iterator = 0; iterator < connectionInfo.length; iterator++){

                            if(iterator % 2 == 0 && !Arrays.toString(connectionInfo).contains("coordinates plainlinks nourlexpansion")) {
                                String lineNumber = connectionInfo[iterator].replaceAll(".+key\">", "").trim().replaceAll("^0", "");
                                String stationName = connectionInfo[iterator].replaceAll("^(.+=\")", "");
                                System.out.println(lineNumber);

                            }
                        }*/

                        //TODO ВСЕ ЭТО НАДО БУДЕТ ПЕРЕДЕЛАТЬ ЧЕРЕЗ ПАРСИНГ 1 спанов и имен 2 спанов сразу в арэй листы
                        /*String[] connectionsInfo = station.select(":nth-child(4) span:nth-child(2n)").eachAttr("title").toString().split("\\,");
                            for (int iterator = 0; iterator < connectionsInfo.length; iterator++){
                                // далее работаем если значение перехода не содержит лишних значений
                                if (!connectionsInfo[iterator].equals(" Это место на «Яндекс.Картах»]")
                                        || !connectionsInfo[iterator].equals("[Московское центральное кольцо")
                                        || !connectionsInfo[iterator].equals("[Московский монорельс")){
                                //TODO Писать парсинг переходов тут!!!!
                                    Надо попытаться получить корректные имена станций и в идеале имена линий
                                    String stationName = connectionsInfo[iterator].replaceAll(".+станцию ", "").replaceAll("\\s[А-я]+.+", "");
                                    stations.forEach(s-> {
                                        if(s.getName().contains(stationName)){
                                            System.out.println(stationName);
                                            connect.add(s);
                                        }
                                    });
                                }
                            }*/
                        //Arrays.stream(connectionsInfo).forEach(System.out::println);

                       /* connect.add(stations.get(i.get()));
                        connections.add(connect);
                    }
                }
                i.getAndIncrement();
            }
        });*/



        // TODO попробовать удалить после парсинга переходов
        // Удаление станций закрытой линии
        stations.removeIf(station -> station.getLine().equals("11А"));
        // TODO метод добавления станций на линии.
        lines.forEach(line -> stations.forEach(station -> {
            if(line.getNumber().equals(station.getLine())){
                line.addStation(station.getName());
            }
        }));


        // TODO собираем метро
        Map<String, List<String>> stationCollect = new TreeMap<>();
        lines.forEach(line -> stationCollect.put(line.getNumber(), line.getStations()));
        JsoupCreator jsoupCreator = new JsoupCreator();
        jsoupCreator.setLines(lines);
        jsoupCreator.setStations(stationCollect);
        jsoupCreator.setConnections(connections);



        String jsoup = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(jsoupCreator);
        System.out.println(jsoup);

    }
    // Подметод парсинга имен станций
    private static String getStationsName(Element element){
        return element.select(CSS_GET_STATION_NAME).attr("title")
                .replaceAll(SPOTTER_NAMES_REGEX, "");
    }

    // Подметод парсинга номеров линий станций
    private static String getStationsLineNumbers(Element element){
        return element.select(CSS_GET_LINE_NUMBER_TO_STATION).text()
                // удаляем нули из номеров, для приведения с номерами линий к единой форме
                .replaceAll("^0", "");
    }

    // Подметод парсинга номеров линий
    private static String getLinesNumbers(Element element){
        return element.select(CSS_GET_LINE_NUMBER).attr("title")
                .replaceAll(SPOTTER_NUMBER_LINES_REGEX, "");
    }

    // Подметод парсинга имен линий
    private static String getLinesNames(Element element){
        return element.select(">a").attr("title")
                .replaceAll(SPOTTER_NAMES_REGEX, "");
    }

    // Подметод парсинга цвера линий
    private static String getColorsLines(Element element){
        return element.select(CSS_GET_LINE_COLOR).attr("style")
                .replaceAll(";.+", "")
                .replaceAll(".+#", "#");
    }

    // Подметод проверки соответствия номеров станций номерам линий
    private static String conformityNumbers(Element element){
        return element.select(CSS_GET_LINE_NUMBER_TO_STATION).text()
                .replaceAll("^0", "");
    }

    // Метод получения списка станций
    private static ArrayList<Station> getStations(Document document){
        ArrayList<Station> stations = new ArrayList<>();
        // HTML блок станций
        // TODO мб выделить в блок элементы
        Elements stationsHtml = document.select(CSS_GET_STATIONS_HTML_BLOCK);

        stationsHtml.forEach(element -> {
            // Проверяю, нет ли пустых значений(а они есть:) )
            if(!element.select(CSS_GET_LINE_NAME).attr("title").equals("")) {
                // Добавляем станции в список станций
                stations.add(new Station(getStationsName(element), getStationsLineNumbers(element)));
            }});


        return stations;
    }

    // Метод получения списка линий
    private static ArrayList<Line> getLines(Document document){
        ArrayList<Line> lines = new ArrayList<>();
        // HTML блок станций
        Elements stationsHtml = document.select(CSS_GET_STATIONS_HTML_BLOCK);
        // HTML блок линий
        Elements lineHtml = document.select(CSS_GET_LINES_HTML_BLOCK);

        lineHtml.forEach(element -> lines.add(new Line((getLinesNumbers(element)), getLinesNames(element))));

        // Добавляем линиям цвет
        lines.forEach(line -> stationsHtml.forEach(element -> {
            if(line.getNumber().equals(conformityNumbers(element))){
                line.setColor(getColorsLines(element));
            }
        }));
        return lines;
    }
}