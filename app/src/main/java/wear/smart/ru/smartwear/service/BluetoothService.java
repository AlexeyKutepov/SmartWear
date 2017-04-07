package wear.smart.ru.smartwear.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.IBinder;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import wear.smart.ru.smartwear.common.Constants;

/**
 * Сервис для работы с bluetooth
 */
public class BluetoothService extends Service {

    private String address;

    private BluetoothAdapter bluetooth;
    private BluetoothSocket socket;
    private BluetoothDevice device;
    private Gson gson;
    private ConnectedThread connectedThread;

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        gson = new Gson();
        bluetooth = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.address = intent.getStringExtra(Constants.MAC);

        if (bluetooth != null && bluetooth.isEnabled()) {
            if (socket == null || !socket.isConnected()) {
                connect();
            }
        } else {
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (socket != null && socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Установить соеденение
     */
    private void connect() {
        device = bluetooth.getRemoteDevice(this.address);

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
        connectedThread = new ConnectedThread();
        connectedThread.start();
    }

    /**
     * Получение данных от устройства
     */
    private class ConnectedThread extends Thread {
        private final InputStream inStream;

        public ConnectedThread() {
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException ignored) {}
            inStream = tmpIn;
        }

        public void run() {
            byte[] buffer = new byte[1024];// буферный массив
            int bytes;

            while (socket.isConnected()) {
                try {
                    bytes = inStream.read(buffer);
                    if (bytes > 0) {
                        String message = new String(buffer);
//                        Для теста
//                        String message = "{\"insideTemp\":\"36.6\", \"outsideTemp\":\"-20\"}";

                        Map<Object, Object> resultMap;
                        Type type = new TypeToken<Map<Object, Object>>() { }.getType();
                        resultMap = gson.fromJson(message, type);

                        Intent intent = new Intent(Constants.SMART_WEAR_MESSAGE);
                        if (resultMap.containsKey(Constants.INSIDE_TEMP)) {
                            intent.putExtra(Constants.INSIDE_TEMP, (String) resultMap.get(Constants.INSIDE_TEMP));
                        }
                        if (resultMap.containsKey(Constants.OUTSIDE_TEMP)) {
                            intent.putExtra(Constants.OUTSIDE_TEMP, (String) resultMap.get(Constants.OUTSIDE_TEMP));
                        }
                        sendBroadcast(intent);
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}
