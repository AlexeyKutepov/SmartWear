package wear.smart.ru.smartwear.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import wear.smart.ru.smartwear.R;
import wear.smart.ru.smartwear.adapter.common.DeviceItem;

/**
 * Адаптер для списка устройств
 */
public class DeviceAdapter extends BaseAdapter {

    private Context ctx;
    private LayoutInflater lInflater;
    private ArrayList<DeviceItem> deviceItemList;

    public DeviceAdapter(Context context, ArrayList<DeviceItem> deviceItemList) {
        this.ctx = context;
        this.deviceItemList = deviceItemList;
        this.lInflater = (LayoutInflater) ctx
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return deviceItemList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.device_item, parent, false);
        }

        DeviceItem d = ((DeviceItem) getItem(position));

        ((TextView) view.findViewById(R.id.deviceName)).setText(d.getDeviceName());
        ((TextView) view.findViewById(R.id.deviceMac)).setText(d.getDeviceMac());
        ((CheckBox) view.findViewById(R.id.deviceCheckBox)).setChecked(d.isDeviceCheckBox());
        ((ImageView) view.findViewById(R.id.deviceIcon)).setImageResource(d.getDeviceIcon());

        return view;
    }
}
