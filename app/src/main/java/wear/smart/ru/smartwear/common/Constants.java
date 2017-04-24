package wear.smart.ru.smartwear.common;

import java.util.UUID;

/**
 * Константы
 */

public class Constants {
    private static final Constants ourInstance = new Constants();

    public static Constants getInstance() {
        return ourInstance;
    }

    private Constants() {
    }

    public final static int REQUEST_ENABLE_BT = 1;

    public static final UUID MY_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public static final UUID TEMP_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"); // Сервис
    public static final UUID INSIDE_TEMP_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e"); // Температура одежды
    public static final UUID BATTERY_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"); // Заряд батареи
    public static final UUID OUTSIDE_TEMP_UUID = UUID.fromString("6e400004-b5a3-f393-e0a9-e50e24dcca9e"); // Температура на улице
    public static final UUID MODE_UUID = UUID.fromString("6e400005-b5a3-f393-e0a9-e50e24dcca9e"); // Авто - 0/Руч - 1
    public static final UUID INPUT_TEMP_UUID = UUID.fromString("6e400006-b5a3-f393-e0a9-e50e24dcca9e"); // Температура, задаваемая на телефоне
    public static final UUID MODE_HAT_UUID = UUID.fromString("6e400007-b5a3-f393-e0a9-e50e24dcca9e"); // Шапка
    public static final UUID MODE_JACKET_UUID = UUID.fromString("6e400008-b5a3-f393-e0a9-e50e24dcca9e"); // Куртка
    public static final UUID MODE_MITTENS_UUID = UUID.fromString("6e400009-b5a3-f393-e0a9-e50e24dcca9e"); // Варежки
    public static final UUID MODE_BOOTS_UUID = UUID.fromString("6e400010-b5a3-f393-e0a9-e50e24dcca9e"); // Ботинки

    /**
     * Сообщение от умной одежды
     */
    public static final String INSIDE_TEMP = "insideTemp";
    public static final String OUTSIDE_TEMP = "outsideTemp";
    public static final String BATTERY = "battery";

}
