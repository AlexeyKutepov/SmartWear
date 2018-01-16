package wear.smart.ru.smartwear.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

import wear.smart.ru.smartwear.common.Constants;

/**
 * Сервис для работы с bluetooth
 */
public class BluetoothClient {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;

    public BluetoothClient(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }

    public boolean connect(String address) {
        device = bluetoothAdapter.getRemoteDevice(address);

        try {
            socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.connect();
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException e2) {
                e.printStackTrace();
            }
        }
        return socket.isConnected();
    }

    public void sendData(byte[] data) {
        if (socket.isConnected()) {
            try {
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BluetoothSocket getSocket() {
        return socket;
    }
}
