package wear.smart.ru.smartwear.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import wear.smart.ru.smartwear.common.Constants;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private final IBinder mBinder = new LocalBinder();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    private Semaphore bleSemaphore = new Semaphore(1);

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    /**
     * Инициализация
     *
     * @return результат инициализации
     */
    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Установка соединения
     *
     * @param address адрес
     * @return результат установки соединения с устройством
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Отключиться
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * Закрыть соединение
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Методы обратного вызова для bluetooth соединения
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(Constants.TEMP_SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristicInsideTemp = service.getCharacteristic(Constants.INSIDE_TEMP_UUID);
                    if (characteristicInsideTemp != null) {
                        setCharacteristicNotification(characteristicInsideTemp, true);
                    }
                    BluetoothGattCharacteristic characteristicOutsideTemp = service.getCharacteristic(Constants.OUTSIDE_TEMP_UUID);
                    if (characteristicOutsideTemp != null) {
                        setCharacteristicNotification(characteristicOutsideTemp, true);
                    }
                    BluetoothGattCharacteristic characteristicBattery = service.getCharacteristic(Constants.BATTERY_UUID);
                    if (characteristicBattery != null) {
                        setCharacteristicNotification(characteristicBattery, true);
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.w(TAG, "onDescriptorWrite: " + descriptor.getUuid());
            bleSemaphore.release();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.w(TAG, "onCharacteristicWrite: " + characteristic.getUuid());
            bleSemaphore.release();
        }
    };

    /**
     * Отправка события
     *
     * @param action событие
     */
    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    /**
     * Отправка события
     *
     * @param action         событие
     * @param characteristic характеристики
     */
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (Constants.INSIDE_TEMP_UUID.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                String message = new String(data).trim();
                Double result = new BigDecimal(message).setScale(1, RoundingMode.UP).doubleValue();
                intent.putExtra(Constants.INSIDE_TEMP, result.toString());
            }
        } else if (Constants.OUTSIDE_TEMP_UUID.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                String message = new String(data).trim();
                Double result = new BigDecimal(message).setScale(1, RoundingMode.UP).doubleValue();
                intent.putExtra(Constants.OUTSIDE_TEMP, result.toString());
            }
        } else if (Constants.BATTERY_UUID.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                String message = new String(data).trim();
                intent.putExtra(Constants.BATTERY, Double.valueOf(message));
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    /**
     * Задать значение характеристики
     * @param characteristicUUID уникальный UUID характеристики
     * @param value значение
     * @return результат выполнения операции
     */
    public boolean setCharacteristicValueByUUID(UUID characteristicUUID, String value) {
        if (mBluetoothGatt != null) {
                BluetoothGattService service = mBluetoothGatt.getService(Constants.TEMP_SERVICE_UUID);
                if (service != null) {
                    BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);
                    if (characteristic != null) {
                        new Thread(new CharacteristicValueSetter(characteristic, value)).start();
                        return true;
                    }
                }
        }
        return false;
    }

    private class CharacteristicValueSetter implements Runnable {

        private BluetoothGattCharacteristic characteristic;
        private String value;

        CharacteristicValueSetter(BluetoothGattCharacteristic characteristic, String value) {
            this.characteristic = characteristic;
            this.value = value;
        }

        @Override
        public void run() {
            Log.w(TAG, "Waiting... Characteristic: " + characteristic.getUuid());
            try {
                bleSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            characteristic.setValue(value);
            mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    /**
     * Сделать доступными/недоступными оповещения у характеристик
     * @param characteristic характеристика
     * @param enabled запретить/разрешить оповещение
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        if (enabled) {
            // TODO наверное нужно захардкодить UUID-ы дескрипторов
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                new Thread(new DescriptorNotificationSetter(descriptor)).start();
            }
        }
    }

    private class DescriptorNotificationSetter implements Runnable {

        private BluetoothGattDescriptor descriptor;

        DescriptorNotificationSetter(BluetoothGattDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public void run() {
            Log.w(TAG, "Waiting... Descriptor: " + descriptor.getUuid());
            try {
                bleSemaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
}
