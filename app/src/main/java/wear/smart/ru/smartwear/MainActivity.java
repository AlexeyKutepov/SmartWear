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
import android.widget.Switch;
import android.widget.TextView;

import wear.smart.ru.smartwear.common.Constants;
import wear.smart.ru.smartwear.component.VerticalSeekBar;
import wear.smart.ru.smartwear.service.BluetoothLeService;

public class MainActivity extends Activity {

    private final static String TAG = MainActivity.class.getSimpleName();

    private BluetoothAdapter bluetooth;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder bluetoothNotSupportedBuilder;
    private AlertDialog.Builder devicesNotFoundBuilder;
    private AlertDialog.Builder devicesListBuilder;

    private SearchDeviceTask searchDeviceTask;

    private ArrayAdapter<String> arrayAdapter;
    private Switch switchMode;
    private VerticalSeekBar seekBarTemp;
    private TextView textViewOutTemp;
    private TextView textViewInTemp;


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

        /*
         * Интерфейс
         */
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        seekBarTemp = (VerticalSeekBar) findViewById(R.id.seekBarTemp);
        switchMode = (Switch) findViewById(R.id.switchMode);
        switchMode.setOnCheckedChangeListener(onCheckedChangeListenerSwitchMode);
        textViewOutTemp = (TextView) findViewById(R.id.textViewOutTemp);
        textViewInTemp = (TextView) findViewById(R.id.textViewInTemp);


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
                Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

                registerReceiver(receiver, makeIntentFilter());
                searchDeviceTask.execute();
            } else {
                // Bluetooth выключен. Предложим пользователю включить его.
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            }
        } else {
            bluetoothNotSupportedBuilder.setMessage(R.string.bluetooth_is_not_supported_dialog_message)
                    .setTitle(R.string.bluetooth_is_not_supported_dialog_title);
            AlertDialog dialog = bluetoothNotSupportedBuilder.create();
            dialog.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, makeIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
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
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    /**
     * Обработка событий
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) { // Пользователь включил bluetooth
            Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
            bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

            registerReceiver(receiver, makeIntentFilter());
            searchDeviceTask.execute();
        }
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
                    //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                    arrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    break;
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    // Соединение установлено
                    mConnected = true;
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    // Соединение разорвано
                    mConnected = false;
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    // Обнаружен сервис
                    Log.d(TAG, "ACTION_GATT_SERVICES_DISCOVERED");
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    // Получены данные
                    // Пришло сообщение от устройства
                    if (intent.hasExtra(Constants.INSIDE_TEMP)) {
                        textViewInTemp.setText(intent.getStringExtra(Constants.INSIDE_TEMP));
                    }
                    if (intent.hasExtra(Constants.OUTSIDE_TEMP)) {
                        textViewOutTemp.setText(intent.getStringExtra(Constants.OUTSIDE_TEMP));
                    }
                    break;
            }
        }
    };

    /**
     * Управление соединениями
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    /**
     * Фильтр событий
     * @return
     */
    private static IntentFilter makeIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

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
                devicesNotFoundBuilder.setMessage(R.string.devices_not_found_dialog_message)
                        .setTitle(R.string.devices_not_found_dialog_title)
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                dialog.show();
            } else {
                devicesListBuilder.setTitle(R.string.search_devices_result_dialog_title)
                        .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String item = arrayAdapter.getItem(which);
                                if (item != null) {
                                    bluetooth.cancelDiscovery();
                                    mDeviceAddress = item.split("\n")[1];
                                    if (mBluetoothLeService != null) {
                                        final boolean result = mBluetoothLeService.connect(mDeviceAddress);
                                        Log.d(TAG, "Connect request result=" + result);
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
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                AlertDialog dialog = devicesListBuilder.create();
                dialog.show();
            }
        }
    }

}
