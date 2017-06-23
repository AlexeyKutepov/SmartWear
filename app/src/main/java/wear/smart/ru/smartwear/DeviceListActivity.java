package wear.smart.ru.smartwear;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import wear.smart.ru.smartwear.adapter.CheckDeviceListAdapter;
import wear.smart.ru.smartwear.adapter.common.CheckDeviceItem;
import wear.smart.ru.smartwear.common.Constants;

public class DeviceListActivity extends AppCompatActivity {

    Context context = this;

    /**
     * Bluetooth
     */
    private BluetoothAdapter bluetooth;

    /**
     * Задачи
     */
    private SearchDeviceTask searchDeviceTask;

    /**
     * Диалоговые окна
     */
    private ProgressDialog progressDialog;
    private AlertDialog.Builder bluetoothNotSupportedBuilder;
    private AlertDialog.Builder devicesNotFoundBuilder;
    private AlertDialog.Builder devicesListBuilder;

    /**
     * Адаптеры
     */
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<CheckDeviceItem> arrayList = new ArrayList<>();
    private CheckDeviceListAdapter checkDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.device_list_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_launcher);

        /*
         * Адаптеры
         */
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        /*
         * Диалоговые окна
         */
        progressDialog = new ProgressDialog(this);
        bluetoothNotSupportedBuilder = new AlertDialog.Builder(this);
        devicesListBuilder = new AlertDialog.Builder(this);
        devicesNotFoundBuilder = new AlertDialog.Builder(this);

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
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_list_menu, menu);
        return true;
    }

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


                    CheckDeviceItem checkDeviceItem = new CheckDeviceItem(device.getName(), device.getAddress(), R.drawable.bluetooth_on, true);
                    arrayList.add(checkDeviceItem);
                    break;
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
            arrayList.clear();
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
                checkDeviceListAdapter = new CheckDeviceListAdapter(context, arrayList);
                devicesListBuilder.setTitle(R.string.search_devices_result_dialog_title)
                          .setAdapter(checkDeviceListAdapter, null)

//                        .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                String item = arrayAdapter.getItem(which);
//                            }
//                        })
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

}
