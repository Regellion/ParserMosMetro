import com.google.gson.GsonBuilder;
import core.Line;
import core.Station;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    // CSS запрос для получения наличия переходов на линии
    private static final String CSS_GET_CONNECTION_EXISTENCE = ":nth-child(4)";
    // CSS запрос для получения массива номеров переходов
    private static final String CSS_GET_CONNECTION_NUMBERS_ARRAY = ":nth-child(4) span.sortkey";
    // CSS запрос для получения массива имен переходов
    private static final String CSS_GET_CONNECTION_NAMES_ARRAY = ":nth-child(4) span";

    // Регулярка для приведения имен станций к единой форме отображения
    private static final String SPOTTER_NAMES_REGEX = "\\s\\(.+\\)";
    // Регулярка для приведения номеров линий к понятной форме
    private static final String SPOTTER_NUMBER_LINES_REGEX = "Линия № ";
    // Путь создания json файла
    private static final String JSON_FILE_PATH = "src/main/resources/map.json";


    public static void main(String[] args) {
        try {
            // получаем структуру сайта
        Document document = Jsoup.connect(URL).maxBodySize(0).get();

        // Получаем станции со страницы википедии
        ArrayList<Station> stationsToJson = getStations(document);
        // Получаем линии со страницы википедии
        ArrayList<Line> linesToJson = getLines(document);
        // Получаем переходы со страницы википедии
        TreeSet<TreeSet<Station>> connectionsToJson = getConnections(document, stationsToJson);
        // Добавляем список станции на линии
        addStationsOnLines(stationsToJson, linesToJson);
        // Создаем объект который будет сериализован в json
        JsonCreator jsonCreator = buildJsonObject(linesToJson, connectionsToJson);
        // Сериализуем обьекты в json файл
        serializationObjects(jsonCreator);

        // Десериализуем обьекты из json файла и добавляем в лист линии и станции
        ArrayList<Line> linesFromJSON = deserializationLines();

        // Печатаем результат выполнения программы
        printData(linesFromJSON);

        // Такое количество станций переходов и линий получилось из за того, что я не парсил 11А линию,
        // т.к. она закрыта. Из-за этого вышло на 1 линию меньше, на 3 станции меньше и соответственно меньше переходов.
        // Так же, не удалял неполные дубли из переходов, потому что в Википедии написано именно так. Я понимаю,
        // что если есть переход  из 3х смежных станций на первую и вторую, то должен быть переход и на третию, что логично.
        // Но в данном случае, по моему мнению, мы всего лишь принимаем данные из какого то ресурса, поэтому
        // не можем проверить подлинность данных и вынуждены опираться только на предоставленные факты.

        // Отдельно хочу сказать, что на 11 линии меньше станций чем у некоторых студентов. Дело в том, что в википедии
        // указывается что всего должно быть 269 станций. То есть при вычитании 3 станций с закрытой линии получаем 266
        // Поэтому, если бы я парсил совместыне станции 8А и 11 линии на две линии дублируя, то это создало бы избыток
        // станций. Поэтому мной было принято решение парсить их на 8А. Если что, их можно парсить на вторую линию
        // по аналогу переходов.
        System.out.println("\nВсего получено " + linesFromJSON.size() + " линий, на которых в общей сложности "
                + linesFromJSON.stream().mapToLong(line-> line.getStations().size()).sum() + " станций");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Метод печати полученых данных
    private static void printData(ArrayList<Line> linesFromJSON){
        System.out.println("\n\t\tСПИСОК ЛИНИЙ И СТАНЦИЙ МОСКОВСКОГО МЕТРО.\n\n");
        linesFromJSON.forEach(line -> System.out.printf("Линия № %-5s%-36s%-3d станции.%n",
                line.getNumber(), line.getName(),line.getStations().size()));
    }

    // Метод десериализации json файла
    private static ArrayList<Line> deserializationLines() throws Exception {
        JSONParser parser = new JSONParser();
        JSONObject jsonData = (JSONObject) parser.parse(getJsonFile());

        JSONArray linesArray = (JSONArray) jsonData.get("lines");
        ArrayList<Line> lines = parseLines(linesArray);


        JSONObject stationsArray = (JSONObject) jsonData.get("stations");
        addStationsOnLinesFromJSON(stationsArray, lines);
        return lines;
    }
    // Метод добавления станций из json файла на линии
    private static void addStationsOnLinesFromJSON(JSONObject stationsObject, ArrayList<Line> lines)
    {
        stationsObject.keySet().forEach(lineNumberObject ->
        {
            String lineNumber = (String) lineNumberObject;
            for (Line value : lines) {
                if (value.getNumber().equals(lineNumber)) {
                    Line line = value;

                    JSONArray stationsArray = (JSONArray) stationsObject.get(lineNumberObject);
                    stationsArray.forEach(stationObject ->
                    {
                        Station station = new Station((String) stationObject, lineNumber);
                        line.addStation(station.getName());
                    });
                }
            }
        });
    }

    // Метод парсинга линий из json файла
    private static ArrayList<Line> parseLines(JSONArray linesArray)
    {
        ArrayList<Line> lines = new ArrayList<>();
        linesArray.forEach(lineObject -> {
            JSONObject lineJsonObject = (JSONObject) lineObject;
            Line line = new Line(
                    ((String) lineJsonObject.get("number")),
                    (String) lineJsonObject.get("name")
            );
            line.setColor((String) lineJsonObject.get("color"));
            lines.add(line);
        });
        return lines;
    }

    // Метод преобразования json файла в строку
    private static String getJsonFile() throws IOException {
        StringBuilder builder = new StringBuilder();

        List<String> lines = Files.readAllLines(Paths.get(JSON_FILE_PATH));
        lines.forEach(builder::append);

        return builder.toString();
    }

    // Подметод добавления станций в переход
    private static void putStationToConnectionSet(ArrayList<Station> stations, String stationName, String stationNumber, TreeSet<Station> connect){
        stations.forEach(s-> {
            if (s.getName().contains(stationName) && s.getLine().equals(stationNumber)) {
                if(!s.getLine().equals("11А")) {
                    connect.add(s);
                }
            }
        });
    }

    // Подметод получения элементов из документа
    private static Elements getElements(Document document){
        return document.select(CSS_GET_STATIONS_HTML_BLOCK);
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

    // Подметод парсинга цвета линий
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
        Elements stationsHtml = getElements(document);

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
        Elements stationsHtml = getElements(document);
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

    // Метод парсинга переходов
    private static TreeSet<TreeSet<Station>> getConnections(Document document, ArrayList<Station> stations){
        TreeSet<TreeSet<Station>> connections = new TreeSet<>(Comparator.comparing(AbstractCollection::toString));

        AtomicInteger i = new AtomicInteger(0);
        Elements stationsHtml = getElements(document);
        stationsHtml.forEach(station -> {
            // Если станции не пустые
            if(!station.select(CSS_GET_STATION_NAME).attr("title")
                    .replaceAll(SPOTTER_NAMES_REGEX, "").equals("")) {
                // Если есть переходы
                if(!station.select(CSS_GET_CONNECTION_EXISTENCE).attr("data-sort-value").equals("Infinity")){
                    // Создаем массивы новеров и имен станций переходов
                    String[] connectionNumberArray = station.select(CSS_GET_CONNECTION_NUMBERS_ARRAY)
                            .eachText().toArray(new String[0]);
                    String[] connectionNameArray = station.select(CSS_GET_CONNECTION_NAMES_ARRAY)
                            .eachAttr("title").toArray(new String[0]);
                    // вводим имя конкретной станции перехода
                    String nameConnection;
                    // Вводим номер конкретной станции перехода
                    String numberConnection;
                    // Вводим сет станций одного перехода
                    TreeSet<Station> connect = new TreeSet<>();
                    // парсинг переходов с ошибкой
                    if(Arrays.toString(connectionNumberArray).contains("14, 02, 09")
                            || Arrays.toString(connectionNumberArray).contains("13, 02, 10")
                            || Arrays.toString(connectionNameArray).contains("Показать карту")) {

                        nameConnection = connectionNameArray[1].replaceAll(".+станцию ", "")
                                .replaceAll("\\s.+", "");
                        numberConnection = connectionNumberArray[2].replaceAll("^0", "");

                        putStationToConnectionSet(stations, nameConnection, numberConnection, connect);
                    } else {
                        // общий парсинг переходов
                        for(int iterator = 0; iterator < connectionNumberArray.length; iterator++){
                            // таким макаром добавлять станции
                            nameConnection = connectionNameArray[iterator].replaceAll(".+станцию ", "")
                                    .replaceAll("\\s.+", "");
                            numberConnection = connectionNumberArray[iterator].replaceAll("^0", "");
                            putStationToConnectionSet(stations, nameConnection, numberConnection, connect);
                        }
                    }
                    // не добавляем станции 11А линии, т.к. она закрыта
                    if(!stations.get(i.get()).getLine().equals("11А")) {
                        connect.add(stations.get(i.get()));
                    }
                    // не добавляем переходы ведущие только на 11А линию
                    if(connect.size() != 1) {
                        connections.add(connect);
                    }
                }
                i.getAndIncrement();
            }
        });
        return connections;
    }

    // Метод добавления станций на линии
    private static void addStationsOnLines(ArrayList<Station> stations, ArrayList<Line> lines){
        // Удаление станций с закрытой линии
        stations.removeIf(station -> station.getLine().equals("11А"));
        // Добавляем станции на линию
        lines.forEach(line -> stations.forEach(station -> {
            if(line.getNumber().equals(station.getLine())){
                line.addStation(station.getName());
            }
        }));
    }

    // Метод составления объекта который будет сериализован в json файл
    private static JsonCreator buildJsonObject(ArrayList<Line> lines, TreeSet<TreeSet<Station>> connections){
        // собираем объект

        Map<String, List<String>> stationCollect = new TreeMap<>();
        lines.forEach(line -> stationCollect.put(line.getNumber(), line.getStations()));

        JsonCreator jsonCreator = new JsonCreator();
        jsonCreator.setLines(lines);
        jsonCreator.setStations(stationCollect);
        jsonCreator.setConnections(connections);

        return jsonCreator;
    }

    // Метод сериализации обьектов в json файл
    private static void serializationObjects(JsonCreator jsonCreator) throws IOException {
        FileWriter writer = new FileWriter(JSON_FILE_PATH);
        new GsonBuilder()
                // Для более удобного отображения создал свой сериализатор
                .registerTypeAdapter(Line.class, new LineSerializer())
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create().toJson(jsonCreator, writer);
        writer.close();
    }
}