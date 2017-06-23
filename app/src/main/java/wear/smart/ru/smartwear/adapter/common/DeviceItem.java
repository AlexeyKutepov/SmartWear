package wear.smart.ru.smartwear.adapter.common;


/**
 * Описание устройства
 */
public class DeviceItem {

    private String deviceName;
    private String deviceMac;
    private int deviceIcon;

    public DeviceItem() {
    }

    public DeviceItem(String deviceName, String deviceMac, int deviceIcon) {
        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
        this.deviceIcon = deviceIcon;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public int getDeviceIcon() {
        return deviceIcon;
    }

    public void setDeviceIcon(int deviceIcon) {
        this.deviceIcon = deviceIcon;
    }
}
