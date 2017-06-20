package wear.smart.ru.smartwear.db.entity;

import com.j256.ormlite.field.DatabaseField;

/**
 * Список устройств
 */
public class Device {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String mac;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private DeviceType type;

    public Device() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }
}
