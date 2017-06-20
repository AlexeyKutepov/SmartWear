package wear.smart.ru.smartwear.db.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;

import wear.smart.ru.smartwear.db.entity.Device;

/**
 * DAO для работы с Device
 */
public class DeviceDAO extends BaseDaoImpl<Device, Integer> {
    public DeviceDAO(Class<Device> dataClass) throws SQLException {
        super(dataClass);
    }

    public DeviceDAO(ConnectionSource connectionSource, Class<Device> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public DeviceDAO(ConnectionSource connectionSource, DatabaseTableConfig<Device> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }
}
