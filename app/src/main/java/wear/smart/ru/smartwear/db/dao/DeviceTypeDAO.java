package wear.smart.ru.smartwear.db.dao;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.sql.SQLException;

import wear.smart.ru.smartwear.db.entity.DeviceType;

/**
 * DAO для работы с DeviceType
 */
public class DeviceTypeDAO extends BaseDaoImpl<DeviceType, Integer> {
    public DeviceTypeDAO(Class<DeviceType> dataClass) throws SQLException {
        super(dataClass);
    }

    public DeviceTypeDAO(ConnectionSource connectionSource, Class<DeviceType> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    public DeviceTypeDAO(ConnectionSource connectionSource, DatabaseTableConfig<DeviceType> tableConfig) throws SQLException {
        super(connectionSource, tableConfig);
    }
}
