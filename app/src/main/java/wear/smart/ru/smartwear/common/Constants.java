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
    public static final UUID INSIDE_TEMP_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    /**
     * Сообщение от умной одежды
     */
    public static final String INSIDE_TEMP = "insideTemp";
    public static final String OUTSIDE_TEMP = "outsideTemp";

}
