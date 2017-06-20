package wear.smart.ru.smartwear.db.entity;

import com.j256.ormlite.field.DatabaseField;

/**
 * Типы устройств
 */
public class DeviceType {

    @DatabaseField(id = true)
    private int id;

    @DatabaseField
    private int code;

    @DatabaseField
    private String name;

    public DeviceType() {
    }

    public DeviceType(int id, int code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
