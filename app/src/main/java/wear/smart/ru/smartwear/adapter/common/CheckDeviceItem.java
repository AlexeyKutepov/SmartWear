package wear.smart.ru.smartwear.adapter.common;

/**
 * Описание устройства
 */
public class CheckDeviceItem {
    private String deviceName;
    private String deviceMac;
    private int deviceIcon;
    private boolean deviceCheckBox;

    public CheckDeviceItem() {
    }

    public CheckDeviceItem(String deviceName, String deviceMac, int deviceIcon, boolean deviceCheckBox) {
        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
        this.deviceIcon = deviceIcon;
        this.deviceCheckBox = deviceCheckBox;
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

    public boolean isDeviceCheckBox() {
        return deviceCheckBox;
    }

    public void setDeviceCheckBox(boolean deviceCheckBox) {
        this.deviceCheckBox = deviceCheckBox;
    }
}
