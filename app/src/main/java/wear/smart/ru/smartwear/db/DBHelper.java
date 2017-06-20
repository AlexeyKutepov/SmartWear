package wear.smart.ru.smartwear.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

import wear.smart.ru.smartwear.R;
import wear.smart.ru.smartwear.common.Constants;
import wear.smart.ru.smartwear.db.dao.DeviceDAO;
import wear.smart.ru.smartwear.db.dao.DeviceTypeDAO;
import wear.smart.ru.smartwear.db.entity.Device;
import wear.smart.ru.smartwear.db.entity.DeviceType;

/**
 * Класс для работы с базой данных
 */
public class DBHelper extends OrmLiteSqliteOpenHelper {

    private static final String TAG = DBHelper.class.getName();
    private static final String DATABASE_NAME = "smartClothes.db";
    private static final int DATABASE_VERSION = 1;

    private DeviceDAO deviceDAO = null;
    private DeviceTypeDAO deviceTypeDAO = null;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            Log.i(TAG, "onCreate");
            TableUtils.createTable(connectionSource, Device.class);
            TableUtils.createTable(connectionSource, DeviceType.class);

            /*
             * Заполнение классификаторов
             */
            populateDeviceType();
        } catch (SQLException e) {
            Log.e(TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            Log.i(TAG, "onUpgrade");
            TableUtils.dropTable(connectionSource, Device.class, true);
            TableUtils.dropTable(connectionSource, DeviceType.class, true);

            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Can't drop databases", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        super.close();
        deviceDAO = null;
        deviceTypeDAO = null;
    }

    /**
     * Получить DAO для работы с Device
     * @return {@link DeviceDAO}
     * @throws SQLException ошибка при работе с базой данных
     */
    public DeviceDAO getDeviceDAO() throws SQLException {
        if (deviceDAO == null) {
            deviceDAO = new DeviceDAO(connectionSource, Device.class);
        }
        return deviceDAO;
    }

    /**
     * Получить DAO для работы с DeviceType
     * @return {@link DeviceTypeDAO}
     * @throws SQLException ошибка при работе с базой данных
     */
    public DeviceTypeDAO getDeviceTypeDAO() throws SQLException {
        if (deviceTypeDAO == null) {
            deviceTypeDAO = new DeviceTypeDAO(connectionSource, DeviceType.class);
        }
        return deviceTypeDAO;
    }

    /**
     * Заполнение классификатора DeviceType
     * @throws SQLException ошибка при работе с базой данных
     */
    private void populateDeviceType() throws SQLException {
        deviceTypeDAO = getDeviceTypeDAO();

        deviceTypeDAO.createOrUpdate(new DeviceType(0, 0, Constants.SMART_COAT));
        deviceTypeDAO.createOrUpdate(new DeviceType(1, 1, Constants.SMART_SHOES));
        deviceTypeDAO.createOrUpdate(new DeviceType(2, 2, Constants.SMART_HAT));
        deviceTypeDAO.createOrUpdate(new DeviceType(3, 3, Constants.SMART_MITTENS));
    }

}
