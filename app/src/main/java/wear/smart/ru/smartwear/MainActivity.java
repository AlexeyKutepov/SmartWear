package wear.smart.ru.smartwear;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import wear.smart.ru.smartwear.bluetooth.BluetoothClient;
import wear.smart.ru.smartwear.bluetooth.BluetoothServer;
import wear.smart.ru.smartwear.common.Constants;
import wear.smart.ru.smartwear.component.VerticalSeekBar;


public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter bluetooth;
    private BluetoothClient bluetoothClient;
    private BluetoothServer bluetoothServer;
    private String mDeviceAddress;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder bluetoothNotSupportedBuilder;
    private AlertDialog.Builder devicesNotFoundBuilder;
    private AlertDialog.Builder devicesListBuilder;
    private AlertDialog.Builder connectErrorBuilder;

    private SearchDeviceTask searchDeviceTask;

    private ArrayAdapter<String> arrayAdapter;
    private Switch switchMode;
    private VerticalSeekBar seekBarTemp;
    private TextView textViewOutTemp;
    private TextView textViewInTemp;
    private ImageView imageViewBattery;
    private ImageView imageViewBluetooth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDeviceAddress = null;

        /*
         * Диалоговые окна
         */
        progressDialog = new ProgressDialog(this);
        devicesNotFoundBuilder = new AlertDialog.Builder(this);
        devicesListBuilder = new AlertDialog.Builder(this);
        bluetoothNotSupportedBuilder = new AlertDialog.Builder(this);
        connectErrorBuilder = new AlertDialog.Builder(this);

        /*
         * Интерфейс
         */
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        seekBarTemp = (VerticalSeekBar) findViewById(R.id.seekBarTemp);
        seekBarTemp.setOnSeekBarChangeListener(onSeekBarChangeListener);
        switchMode = (Switch) findViewById(R.id.switchMode);
        switchMode.setOnCheckedChangeListener(onCheckedChangeListenerSwitchMode);
        textViewOutTemp = (TextView) findViewById(R.id.textViewOutTemp);
        textViewInTemp = (TextView) findViewById(R.id.textViewInTemp);
        imageViewBattery = (ImageView) findViewById(R.id.imageViewBattery);
        imageViewBluetooth = (ImageView) findViewById(R.id.imageViewBluetooth);

        /*
         * Задачи
         */
        searchDeviceTask = new SearchDeviceTask();

        /*
         * bluetooth
         */
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (bluetooth != null) {
            if (bluetooth.isEnabled()) {
                bluetoothClient = new BluetoothClient(bluetooth);
                registerReceiver(receiver, makeIntentFilter());
                searchDeviceTask.execute();
            } else {
                // Bluetooth выключен. Предложим пользователю включить его.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        } else {
            bluetoothNotSupportedBuilder.setMessage(R.string.bluetooth_is_not_supported_dialog_message)
                    .setTitle(R.string.bluetooth_is_not_supported_dialog_title)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAndRemoveTask();
                        }
                    });
            AlertDialog dialog = bluetoothNotSupportedBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    /**
     * Обработка событий
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // Пользователь включил bluetooth
            bluetoothClient = new BluetoothClient(bluetooth);
            registerReceiver(receiver, makeIntentFilter());
            searchDeviceTask.execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, makeIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    /**
     * Обработка входящих сообщений
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Когда найдено новое устройство
                    // Получаем объект BluetoothDevice из интента
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Чтоб небыло дубликатов
                    for (int i = 0; i < arrayAdapter.getCount(); i++) {
                        String item = arrayAdapter.getItem(i);
                        if (item != null) {
                            if (item.equals(device.getName() + "\n" + device.getAddress())) {
                                break;
                            }
                        }
                    }
                    //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                    arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    break;

            }
        }
    };


    /**
     * Фильтр событий
     *
     * @return {@link IntentFilter}
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        return intentFilter;
    }

    /**
     * Изменение температуры вручную
     */
    VerticalSeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new VerticalSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Integer temp = (int)(2.55 * progress);
            String data = "F0" + Integer.toHexString(temp) + Integer.toHexString(temp) + Integer.toHexString(temp) + Integer.toHexString(temp);
            bluetoothClient.sendData(hexStringToByteArray(data));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Пока эти события нам не нужны
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // Пока эти события нам не нужны
        }
    };

    /**
     * В зависимости от состояния переключателя руч/авто делаем доступными или недоступными элементы управления контроллером
     */
    CompoundButton.OnCheckedChangeListener onCheckedChangeListenerSwitchMode = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                seekBarTemp.setEnabled(false);
            } else {
                seekBarTemp.setEnabled(true);
            }
        }
    };

    /**
     * Ожидание завершения поиска устройств и формирование списка устройств
     */
    private class SearchDeviceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bluetooth.cancelDiscovery();
            arrayAdapter.clear();
            bluetooth.startDiscovery();
            progressDialog.setTitle(R.string.search_devices_dialog_title);
            progressDialog.setMessage(getResources().getString(R.string.search_devices_dialog_message));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (bluetooth.isDiscovering()) {
                continue;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            if (arrayAdapter.isEmpty()) {
                devicesNotFoundBuilder
                        .setMessage(R.string.devices_not_found_dialog_message)
                        .setTitle(R.string.devices_not_found_dialog_title)
                        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAndRemoveTask();
                            }
                        })
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                searchDeviceTask = new SearchDeviceTask();
                                searchDeviceTask.execute();
                            }
                        });
                AlertDialog dialog = devicesNotFoundBuilder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            } else {
                devicesListBuilder.setTitle(R.string.search_devices_result_dialog_title)
                        .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String item = arrayAdapter.getItem(which);
                                if (item != null) {
                                    bluetooth.cancelDiscovery();
                                    mDeviceAddress = item.split("\n")[1];
                                    if (!bluetoothClient.connect(mDeviceAddress)) {
                                        connectErrorBuilder
                                                .setTitle(R.string.connect_error_dialog_title)
                                                .setMessage(R.string.connect_error_dialog_message)
                                                .setPositiveButton(R.string.repeat_search, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        searchDeviceTask = new SearchDeviceTask();
                                                        searchDeviceTask.execute();
                                                    }
                                                })
                                                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finishAndRemoveTask();
                                                    }
                                                });
                                        AlertDialog errorDialog = connectErrorBuilder.create();
                                        errorDialog.setCanceledOnTouchOutside(false);
                                        errorDialog.show();
                                    } else {
                                        imageViewBluetooth.setImageResource(R.drawable.bluetooth_on);
                                    }
                                }
                            }
                        })
                        .setPositiveButton(R.string.repeat_search, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                searchDeviceTask = new SearchDeviceTask();
                                searchDeviceTask.execute();
                            }
                        })
                        .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAndRemoveTask();
                            }
                        });
                AlertDialog dialog = devicesListBuilder.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        }
    }

    /**
     * Конвертирование строки в массив байт
     * @param s
     * @return
     */
    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }


}
