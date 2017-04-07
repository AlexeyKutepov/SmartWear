package wear.smart.ru.smartwear;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.Arrays;

import wear.smart.ru.smartwear.common.Constants;
import wear.smart.ru.smartwear.component.VerticalSeekBar;
import wear.smart.ru.smartwear.service.BluetoothService;

public class MainActivity extends Activity {

    private BluetoothAdapter bluetooth;
    private Intent bluetoothService;

    private ProgressDialog progressDialog;
    private AlertDialog.Builder bluetoothNotSupportedBuilder;
    private AlertDialog.Builder devicesNotFoundBuilder;
    private AlertDialog.Builder devicesListBuilder;

    private SearchDeviceTask searchDeviceTask;

    private ArrayAdapter<String> arrayAdapter;
    private Switch switchMode;
    private VerticalSeekBar seekBarTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        /*
         * Задачи
         */
        searchDeviceTask = new SearchDeviceTask();

        /*
         * bluetooth
         */
        bluetoothService = new Intent(this, BluetoothService.class);
        bluetooth = BluetoothAdapter.getDefaultAdapter();

        if (bluetooth != null) {
            if (bluetooth.isEnabled()) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);
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
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(receiver, filter);
            searchDeviceTask.execute();
        }
    }

    /**
     * Поиск bluetooth-устройств
     */
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Когда найдено новое устройство
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Получаем объект BluetoothDevice из интента
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Добавляем имя и адрес в array adapter, чтобы показвать в ListView
                arrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
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
                                stopService(bluetoothService);
                                String item = arrayAdapter.getItem(which);
                                if (item != null) {
                                    String address = item.split("\n")[1];
                                    bluetooth.cancelDiscovery();
                                    bluetoothService.putExtra(Constants.MAC, address);
                                    startService(bluetoothService);
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
