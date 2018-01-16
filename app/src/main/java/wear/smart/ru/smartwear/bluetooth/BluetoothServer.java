package wear.smart.ru.smartwear.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

import wear.smart.ru.smartwear.common.Constants;

public class BluetoothServer extends Thread {
    private final BluetoothServerSocket mmServerSocket;

    public BluetoothServer(BluetoothAdapter bluetooth) {

        BluetoothServerSocket tmp = null;
        try {
            tmp = bluetooth.listenUsingRfcommWithServiceRecord(bluetooth.getName(), Constants.MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmServerSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket;
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                break;
            }
            if (socket != null) {
                //manageConnectedSocket(socket);
                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}