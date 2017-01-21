package com.alexeus.map;

import com.alexeus.logic.constants.TextInfo;
import com.alexeus.logic.enums.AdjacencyType;
import com.alexeus.logic.enums.AreaType;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by alexeus on 02.01.2017.
 * Карта "Игры престолов". Содержит только ссылки на области и методы, разъясняющие отношения между ними
 */
public class GameOfThronesMap {

    /*  массив, хранящий тип соседства для пар областей
     */
    private AdjacencyType[][] adjacencyType;

    // количество областей карты
    public static final int NUM_AREA = 58;

    private String[] areaNameRus = new String[NUM_AREA];

    private String[] areaNameRusAccusative = new String[NUM_AREA];

    private String[] areaNameRusGenitive = new String[NUM_AREA];

    private String[] areaNameRusLocative = new String[NUM_AREA];

    private String[] areaNm = new String[NUM_AREA];

    private AreaType[] areaType = new AreaType[NUM_AREA];

    private int[] numCastle = new int[NUM_AREA];

    private int[] numCrown = new int[NUM_AREA];

    private int[] numBarrel = new int[NUM_AREA];

    private static List<HashSet<Integer>> adjacentAreasToArea = new ArrayList<>();
    
    public GameOfThronesMap() {
        adjacencyType = new AdjacencyType[NUM_AREA][NUM_AREA];
        addNewArea(AreaType.sea, 0, "Ледовый залив", "в Ледовом заливе", "из Ледового заливе", "лз", 0, 0, 0);
        addNewArea(AreaType.sea, 1, "Закатное море", "в Закатном море", "из Закатного моря", "зм", 0, 0, 0);
        addNewArea(AreaType.sea, 2, "Залив железных людей", "в Заливе железных людей", "из Залива Железных людей",
                "зжл", 0, 0, 0);
        addNewArea(AreaType.sea, 3, "Золотой пролив", "в Золотом проливе", "из Золотого пролива", "зп", 0, 0, 0);
        addNewArea(AreaType.sea, 4, "Западное летнее море", "в Западном летнем море", "из Западного летнего моря",
                "злм", 0, 0, 0);
        addNewArea(AreaType.sea, 5, "Пролив Редвин", "в Проливе Редвин", "из Пролива Редвин", "пр", 0, 0, 0);
        addNewArea(AreaType.sea, 6, "Восточное летнее море", "в Восточном летнем море", "из Восточного летнего моря",
                "влм", 0, 0, 0);
        addNewArea(AreaType.sea, 7, "Дорнское море", "в Дорнском море", "из Дорнского моря", "дрн", 0, 0, 0);
        addNewArea(AreaType.sea, 8, "Губительные валы", "в Губительных валах", "из Губительных валов", "гв", 0, 0, 0);
        addNewArea(AreaType.sea, 9, "Черноводный залив", "в Черноводном заливе", "из Черноводного залива", "чвз", 0, 0, 0);
        addNewArea(AreaType.sea, 10, "Узкое море", "в Узком море", "из Узкого моря", "ум", 0, 0, 0);
        addNewArea(AreaType.sea, 11, "Дрожащее море", "в Дрожащем море", "из Дрожащего моря", "држ", 0, 0, 0);

        addNewArea(AreaType.port, 12, "Порт Винтерфелла", "в порту Винтерфелла", "из порта Винтерфелла", "пВФ", 0, 0, 0);
        addNewArea(AreaType.port, 13, "Порт Пайка", "в порту Пайка", "из порта Пайка", "пП", 0, 0, 0);
        addNewArea(AreaType.port, 14, "Порт Ланниспорта", "в порту Ланниспорта", "из порта Ланниспорта", "пЛП", 0, 0, 0);
        addNewArea(AreaType.port, 15, "Порт Староместа", "в порту Староместа", "из порта Староместа", "пСТ", 0, 0, 0);
        addNewArea(AreaType.port, 16, "Порт Солнечного Копья", "в порту Солнечного Копья", "из порта Солнечного Копья",
                "пСК", 0, 0, 0);
        addNewArea(AreaType.port, 17, "Порт Штормового Предела", "в порту Штормового Предела", "из порта Штормового Предела",
                "пШП", 0, 0, 0);
        addNewArea(AreaType.port, 18, "Порт Драконьего Камня", "в порту Драконьего Камня", "из порта Драконьего Камня",
                "пДК", 0, 0, 0);
        addNewArea(AreaType.port, 19, "Порт Белой Гавани", "в порту Белой Гавани", "из порта Белой Гавани", "пБГ", 0, 0, 0);

        addNewArea(AreaType.land, 20, "Арбор", "в Арборе", "из Арбора", "АРБ", 0, 1, 0);
        addNewArea(AreaType.land, 21, "Винтерфелл", "в Винтерфелле", "из Винтерфелла", "ВФ", 2, 1, 1);
        addNewArea(AreaType.land, 22, "Каменный берег", "на Каменном берегу", "с Каменного берега", "КБ", 0, 0, 1);
        addNewArea(AreaType.land, 23, "Чёрный замок", "в Чёрном замке", "из Чёрного замка", "ЧЗ", 0, 1, 0);
        addNewArea(AreaType.land, 24, "Кархолд", "в Кархолде", "из Кархолда", "КРХ", 0, 1, 0);
        addNewArea(AreaType.land, 25, "Белая Гавань", "в Белой Гавани", "из Белой Гавани", "БГ", 1, 0, 0);
        addAreaAccusative(25, "Белую Гавань");
        addNewArea(AreaType.land, 26, "Вдовий Дозор", "во Вдовьем Дозоре", "из Вдовьего Дозора", "ВД", 0, 0, 1);
        addNewArea(AreaType.land, 27, "Ров Кейлин", "во Рву Кейлин", "из Рва Кейлин", "РОВ", 1, 0, 0);
        addNewArea(AreaType.land, 28, "Близнецы", "в Близнецах", "из Близнецов", "БЛЗ", 0, 1, 0);
        addNewArea(AreaType.land, 29, "Персты", "в Перстах", "из Перстов", "ПРС", 0, 0, 1);
        addNewArea(AreaType.land, 30, "Лунные горы", "в Лунных горах", "из Лунных гор", "ЛГ", 0, 0, 1);
        addNewArea(AreaType.land, 31, "Орлиное гнездо", "в Орлином гнезде", "из Орлиного гнезда", "ОГ", 1, 1, 1);
        addNewArea(AreaType.land, 32, "Кремень", "в Кремне", "из Кремня", "КНЬ", 1, 0, 0);
        addNewArea(AreaType.land, 33, "Сероводье", "в Сероводье", "из Сероводья", "СВ", 0, 0, 1);
        addNewArea(AreaType.land, 34, "Сигард", "в Сигарде", "из Сигарда", "СИГ", 2, 1, 1);
        addNewArea(AreaType.land, 35, "Риверран", "в Риверране", "из Риверрана", "РИВ", 2, 1, 1);
        addNewArea(AreaType.land, 36, "Ланниспорт", "в Ланниспорте", "из Ланниспорта", "ЛП", 2, 0, 2);
        addNewArea(AreaType.land, 37, "Каменная септа", "в Каменной септе", "из Каменной септы", "КС", 0, 1, 0);
        addAreaAccusative(37, "Каменную септу");
        addNewArea(AreaType.land, 38, "Харенхол", "в Харенхоле", "из Харенхола", "ХАР", 1, 1, 0);
        addNewArea(AreaType.land, 39, "Черноводная", "в Черноводной", "из Черноводной", "ЧВ", 0, 0, 2);
        addAreaAccusative(39, "Черноводную");
        addNewArea(AreaType.land, 40, "Приморские марки", "в Приморских марках", "из Приморских марок", "ПМ", 0, 0, 1);
        addNewArea(AreaType.land, 41, "Хайгарден", "в Хайгардене", "из Хайгардена", "ХГ", 2, 0, 2);
        addNewArea(AreaType.land, 42, "Простор", "в Просторе", "из Простора", "ПРО", 1, 0, 0);
        addNewArea(AreaType.land, 43, "Дорнские марки", "в Дорнских марках", "из Дорнских марок", "ДМ", 0, 1, 0);
        addNewArea(AreaType.land, 44, "Три башни", "в Трёх башнях", "из Трёх башен", "БББ", 0, 0, 1);
        addNewArea(AreaType.land, 45, "Старомест", "в Староместе", "из Староместа", "СТ", 2, 0, 0);
        addNewArea(AreaType.land, 46, "Звездопад", "в Звездопаде", "из Звездопада", "ЗДП", 1, 0, 1);
        addNewArea(AreaType.land, 47, "Солёный берег", "на Солёном берегу", "с Солёного берега", "СБ", 0, 0, 1);
        addNewArea(AreaType.land, 48, "Солнечное Копьё", "в Солнечном Копье", "из Солнечного Копья", "СК", 2, 1, 1);
        addNewArea(AreaType.land, 49, "Айронвуд", "в Айронвуде", "из Айронвуда", "АЙР", 1, 0, 0);
        addNewArea(AreaType.land, 50, "Принцев перевал", "в Принцевом перевале", "из Принцевого перевала", "ПП", 0, 1, 1);
        addNewArea(AreaType.land, 51, "Костяной путь", "в Костяном пути", "из Костяного пути", "КП", 0, 1, 0);
        addNewArea(AreaType.land, 52, "Штормовой Предел", "в Штормовом Пределе", "из Штормового предела", "ШП", 1, 0, 0);
        addNewArea(AreaType.land, 53, "Королевский лес", "в Королевском лесу", "из Королевского леса", "КЛ", 0, 1, 1);
        addNewArea(AreaType.land, 54, "Королевская Гавань", "в Королевской Гавани", "из Королевской Гавани", "КГ", 2, 2, 0);
        addAreaAccusative(54, "Королевскую Гавань");
        addNewArea(AreaType.land, 55, "Клешня", "в Клешне", "из Клешни", "КЛЯ", 1, 0, 0);
        addAreaAccusative(55, "Клешню");
        addNewArea(AreaType.land, 56, "Драконий Камень", "в Драконьем Камне", "из Драконьего камня", "ДК", 2, 1, 1);
        addNewArea(AreaType.land, 57, "Пайк", "в Пайке", "из Пайка", "П", 2, 1, 1);

        // соседства морей
        ArrayList<Point> matchingPairs = new ArrayList<>();
        matchingPairs.add(new Point(0, 1));
        matchingPairs.add(new Point(1, 2));
        matchingPairs.add(new Point(1, 3));
        matchingPairs.add(new Point(2, 3));
        matchingPairs.add(new Point(1, 4));
        matchingPairs.add(new Point(4, 5));
        matchingPairs.add(new Point(4, 6));
        matchingPairs.add(new Point(6, 7));
        matchingPairs.add(new Point(6, 8));
        matchingPairs.add(new Point(8, 9));
        matchingPairs.add(new Point(8, 10));
        matchingPairs.add(new Point(10, 11));

        // соседства морей и суш
        matchingPairs.add(new Point(0, 21));
        matchingPairs.add(new Point(0, 22));
        matchingPairs.add(new Point(0, 23));
        matchingPairs.add(new Point(0, 32));
        matchingPairs.add(new Point(0, 33));
        matchingPairs.add(new Point(1, 32));
        matchingPairs.add(new Point(1, 40));
        matchingPairs.add(new Point(2, 57));
        matchingPairs.add(new Point(2, 32));
        matchingPairs.add(new Point(2, 33));
        matchingPairs.add(new Point(2, 34));
        matchingPairs.add(new Point(2, 35));
        matchingPairs.add(new Point(3, 35));
        matchingPairs.add(new Point(3, 36));
        matchingPairs.add(new Point(3, 40));
        matchingPairs.add(new Point(4, 40));
        matchingPairs.add(new Point(4, 41));
        matchingPairs.add(new Point(4, 44));
        matchingPairs.add(new Point(4, 46));
        matchingPairs.add(new Point(4, 20));
        matchingPairs.add(new Point(5, 20));
        matchingPairs.add(new Point(5, 41));
        matchingPairs.add(new Point(5, 44));
        matchingPairs.add(new Point(5, 45));
        matchingPairs.add(new Point(6, 46));
        matchingPairs.add(new Point(6, 47));
        matchingPairs.add(new Point(6, 48));
        matchingPairs.add(new Point(6, 52));
        matchingPairs.add(new Point(7, 48));
        matchingPairs.add(new Point(7, 49));
        matchingPairs.add(new Point(7, 51));
        matchingPairs.add(new Point(7, 52));
        matchingPairs.add(new Point(8, 52));
        matchingPairs.add(new Point(8, 56));
        matchingPairs.add(new Point(8, 53));
        matchingPairs.add(new Point(8, 55));
        matchingPairs.add(new Point(9, 53));
        matchingPairs.add(new Point(9, 54));
        matchingPairs.add(new Point(9, 55));
        matchingPairs.add(new Point(10, 55));
        matchingPairs.add(new Point(10, 25));
        matchingPairs.add(new Point(10, 26));
        matchingPairs.add(new Point(10, 27));
        matchingPairs.add(new Point(10, 28));
        matchingPairs.add(new Point(10, 29));
        matchingPairs.add(new Point(10, 30));
        matchingPairs.add(new Point(10, 31));
        matchingPairs.add(new Point(11, 21));
        matchingPairs.add(new Point(11, 23));
        matchingPairs.add(new Point(11, 24));
        matchingPairs.add(new Point(11, 25));
        matchingPairs.add(new Point(11, 26));

        // соседство портов и прилежащих морей/крепостей
        matchingPairs.add(new Point(12, 0));
        matchingPairs.add(new Point(12, 21));
        matchingPairs.add(new Point(13, 2));
        matchingPairs.add(new Point(13, 57));
        matchingPairs.add(new Point(14, 3));
        matchingPairs.add(new Point(14, 36));
        matchingPairs.add(new Point(15, 5));
        matchingPairs.add(new Point(15, 45));
        matchingPairs.add(new Point(16, 6));
        matchingPairs.add(new Point(16, 48));
        matchingPairs.add(new Point(17, 8));
        matchingPairs.add(new Point(17, 52));
        matchingPairs.add(new Point(18, 8));
        matchingPairs.add(new Point(18, 56));
        matchingPairs.add(new Point(19, 10));
        matchingPairs.add(new Point(19, 25));

        // соседство суш
        matchingPairs.add(new Point(21, 22));
        matchingPairs.add(new Point(21, 23));
        matchingPairs.add(new Point(21, 24));
        matchingPairs.add(new Point(21, 25));
        matchingPairs.add(new Point(21, 27));
        matchingPairs.add(new Point(23, 24));
        matchingPairs.add(new Point(25, 26));
        matchingPairs.add(new Point(25, 27));
        matchingPairs.add(new Point(27, 28));
        matchingPairs.add(new Point(27, 33));
        matchingPairs.add(new Point(27, 34));
        matchingPairs.add(new Point(28, 29));
        matchingPairs.add(new Point(28, 30));
        matchingPairs.add(new Point(28, 34));
        matchingPairs.add(new Point(29, 30));
        matchingPairs.add(new Point(30, 31));
        matchingPairs.add(new Point(30, 55));

        matchingPairs.add(new Point(32, 33));
        matchingPairs.add(new Point(33, 34));
        matchingPairs.add(new Point(34, 35));
        matchingPairs.add(new Point(35, 36));
        matchingPairs.add(new Point(35, 37));
        matchingPairs.add(new Point(35, 38));
        matchingPairs.add(new Point(36, 37));
        matchingPairs.add(new Point(36, 40));
        matchingPairs.add(new Point(37, 38));
        matchingPairs.add(new Point(37, 39));
        matchingPairs.add(new Point(37, 40));
        matchingPairs.add(new Point(38, 39));
        matchingPairs.add(new Point(38, 55));
        matchingPairs.add(new Point(39, 40));
        matchingPairs.add(new Point(39, 42));
        matchingPairs.add(new Point(39, 54));
        matchingPairs.add(new Point(39, 55));
        matchingPairs.add(new Point(40, 41));
        matchingPairs.add(new Point(40, 42));
        matchingPairs.add(new Point(40, 42));
        matchingPairs.add(new Point(41, 42));
        matchingPairs.add(new Point(41, 43));
        matchingPairs.add(new Point(41, 45));
        matchingPairs.add(new Point(42, 43));
        matchingPairs.add(new Point(42, 51));
        matchingPairs.add(new Point(42, 53));
        matchingPairs.add(new Point(42, 54));
        matchingPairs.add(new Point(43, 44));
        matchingPairs.add(new Point(43, 45));
        matchingPairs.add(new Point(43, 50));
        matchingPairs.add(new Point(43, 51));
        matchingPairs.add(new Point(44, 45));
        matchingPairs.add(new Point(44, 50));

        matchingPairs.add(new Point(46, 47));
        matchingPairs.add(new Point(46, 49));
        matchingPairs.add(new Point(46, 50));
        matchingPairs.add(new Point(47, 48));
        matchingPairs.add(new Point(47, 49));
        matchingPairs.add(new Point(48, 49));
        matchingPairs.add(new Point(49, 50));
        matchingPairs.add(new Point(49, 51));
        matchingPairs.add(new Point(50, 51));
        matchingPairs.add(new Point(51, 52));
        matchingPairs.add(new Point(51, 53));
        matchingPairs.add(new Point(52, 53));
        matchingPairs.add(new Point(53, 54));
        matchingPairs.add(new Point(54, 55));

        for (int i = 0; i < NUM_AREA; i++) {
            adjacentAreasToArea.add(new HashSet<>());
            for (int j = 0; j < NUM_AREA; j++) {
                adjacencyType[i][j] = AdjacencyType.noAdjacency;
            }
        }

        for (Point point : matchingPairs) {
            int x = point.x;
            int y = point.y;
            if (areaType[x] == AreaType.sea && areaType[y] == AreaType.sea) {
                adjacencyType[x][y] = AdjacencyType.seaToSea;
                adjacencyType[y][x] = AdjacencyType.seaToSea;
            }
            if (areaType[x] == AreaType.sea && areaType[y] == AreaType.land) {
                adjacencyType[x][y] = AdjacencyType.seaToLand;
                adjacencyType[y][x] = AdjacencyType.landToSea;
            }
            if (areaType[x] == AreaType.port && areaType[y] == AreaType.sea) {
                adjacencyType[x][y] = AdjacencyType.portToSea;
                adjacencyType[y][x] = AdjacencyType.seaToPort;
            }
            if (areaType[x] == AreaType.port && areaType[y] == AreaType.land) {
                adjacencyType[x][y] = AdjacencyType.portOfCastle;
                adjacencyType[y][x] = AdjacencyType.castleWithPort;
            }
            if (areaType[x] == AreaType.land && areaType[y] == AreaType.land) {
                adjacencyType[x][y] = AdjacencyType.landToLand;
                adjacencyType[y][x] = AdjacencyType.landToLand;
            }
            adjacentAreasToArea.get(x).add(y);
            adjacentAreasToArea.get(y).add(x);
        }
    }

    // Метод выводит текст, описывающий все области карты.
    public void print() {
        for (int i = 0; i < NUM_AREA; i++) {
            System.out.print(TextInfo.AREA_NUMBER + i + ": " + areaNameRus[i] + ". ");
            if (numCastle[i] == 1) {
                System.out.print(TextInfo.CASTLE + ". ");
            } else if (numCastle[i] == 2) {
                System.out.print(TextInfo.FORTRESS + ". ");
            }
            if (numCrown[i] == 1) {
                System.out.print(numCrown[i] + TextInfo.POWER_SIGN + ". ");
            } else if (numCrown[i] == 2) {
                System.out.print(numCrown[i] + TextInfo.POWER_SIGNS + ". ");
            }
            if (numBarrel[i] == 1) {
                System.out.print(numBarrel[i] + TextInfo.BARREL + ". ");
            } else if (numBarrel[i] == 2) {
                System.out.print(numBarrel[i] + TextInfo.BARRELS + ". ");
            }
            System.out.print(TextInfo.ADJACENT);
            boolean firstFlag = true;
            for (int j : adjacentAreasToArea.get(i)) {
                if (!firstFlag) {
                    System.out.print(", ");
                } else {
                    firstFlag = false;
                }
                System.out.print(areaNameRus[j]);
            }
            System.out.println();
        }
    }

    public HashSet<Integer> getAdjacentAreas(int id) {
        return adjacentAreasToArea.get(id);
    }

    public AdjacencyType getAdjacencyType(int area1, int area2) {
        return adjacencyType[area1][area2];
    }

    public String getAreaNameRus(int area) {
        return areaNameRus[area];
    }

    public String getAreaNameRusLocative(int area) {
        return areaNameRusLocative[area];
    }

    public String getAreaNameRusGenitive(int area) {
        return areaNameRusGenitive[area];
    }

    public String getAreaNameRusAccusative(int area) {
        return areaNameRusAccusative[area] != null ? areaNameRusAccusative[area] : getAreaNameRus(area);
    }

    public String getAreaNm(int area) {
        return areaNm[area];
    }

    public AreaType getAreaType(int area) {
        return areaType[area];
    }

    public int getNumCastle(int area) {
        return numCastle[area];
    }

    public int getNumCrown(int area) {
        return numCrown[area];
    }

    public int getNumBarrel(int area) {
        return numBarrel[area];
    }

    public void setAreaNameRus(String[] areaNameRus) {
        this.areaNameRus = areaNameRus;
    }

    public void setAreaNm(String[] areaNm) {
        this.areaNm = areaNm;
    }

    public void setAreaType(AreaType[] areaType) {
        this.areaType = areaType;
    }

    public void setNumCastle(int[] numCastle) {
        this.numCastle = numCastle;
    }

    public void setNumCrown(int[] numCrown) {
        this.numCrown = numCrown;
    }

    public void setNumBarrel(int[] numBarrel) {
        this.numBarrel = numBarrel;
    }

    /**
     * Добавляем новую область в карту
     * @param areaType        тип местности (земля, море, порт)
     * @param number          порядковый номер местности
     * @param nameRus         название
     * @param nameRusLocative название в предложном падеже
     * @param nm              краткое название (2-3 буквы)
     * @param numCastle       число ярусов замка (и, соотвтетсвенно, очков сбора) в области
     * @param numCrown        количество знаков короны
     * @param numBarrel       количество бочек
     */
    private void addNewArea(AreaType areaType, int number, String nameRus, String nameRusLocative,
                            String nameRusGenitive, String nm, int numCastle, int numCrown, int numBarrel) {
        this.areaType[number] = areaType;
        areaNameRus[number] = nameRus;
        areaNameRusGenitive[number] = nameRusGenitive;
        areaNameRusLocative[number] = nameRusLocative;
        areaNm[number] = nm;
        this.numCastle[number] = numCastle;
        this.numCrown[number] = numCrown;
        this.numBarrel[number] = numBarrel;
    }

    private void addAreaAccusative(int number, String nameRusAccusative) {
        areaNameRusAccusative[number] = nameRusAccusative;
    }

    /**
     * Метод возвращает номер области с замком, которой принадлежит данная обасть с портом
     * @param portArea портовая область
     * @return номер области с замком
     */
    public int getCastleWithPort(int portArea) {
        HashSet<Integer> adjacentAreas = getAdjacentAreas(portArea);
        for (int curArea: adjacentAreas) {
            if (getNumCastle(curArea) > 0) {
                return curArea;
            }
        }
        return -1;
    }

    /**
     * Метод возвращает номер морской области, граничащей с данным портом
     * @param portArea портовая область
     * @return номер соседней морской области
     */
    public int getSeaNearPort(int portArea) {
        HashSet<Integer> adjacentAreas = getAdjacentAreas(portArea);
        for (int curArea: adjacentAreas) {
            if (getAreaType(curArea) == AreaType.sea) {
                return curArea;
            }
        }
        return -1;
    }
}
