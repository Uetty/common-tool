package com.uetty.common.tool.core.cloud.jdbc;

import com.aliyuncs.utils.StringUtils;
import com.uetty.common.tool.core.cache.CacheManager;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Logger;

@Slf4j
public class KmsMysqlDriver implements Driver {

    private static final String SCHEMA = "secrets-manager";
    private static final String DRIVER_PROPERTIES_KEY_USER = "user";
    private static final String DRIVER_PROPERTIES_KEY_PASSWORD = "password";

    private static final String RDS_CREDENTIAL_CACHE_KEY = "rds_credential";

    private RdsCredentialProvider rdsCredentialProvider;

    public KmsMysqlDriver() {
        ServiceLoader<RdsCredentialProvider> providerServiceLoader = ServiceLoader.load(RdsCredentialProvider.class);
        Iterator<RdsCredentialProvider> iterator = providerServiceLoader.iterator();
        if (iterator.hasNext()) {
            rdsCredentialProvider = iterator.next();
        }
        if (rdsCredentialProvider == null) {
            log.warn("KmsRdsProvider not found");
        }
    }

    protected KmsRdsCredential getRdsCredentialFromCache() {
        return CacheManager.get(CacheManager.CACHE_TYPE_MEMORY, RDS_CREDENTIAL_CACHE_KEY);
    }

    protected void putRdsCredentialCache(KmsRdsCredential credential) {
        CacheManager.put(CacheManager.CACHE_TYPE_MEMORY, RDS_CREDENTIAL_CACHE_KEY, credential, 2 * 3600_000L);
    }

    protected void clearRdsCredentialCache(KmsRdsCredential credentialCache) {
        CacheManager.remove(CacheManager.CACHE_TYPE_MEMORY, RDS_CREDENTIAL_CACHE_KEY, credentialCache);
    }

    protected KmsRdsCredential acquireRdsCredential(RdsCredentialProvider kmsRdsProvider) {
        return kmsRdsProvider.getRdsCredential(null);
    }

    protected KmsRdsCredential getKmsCredential() {
        KmsRdsCredential fromCache = getRdsCredentialFromCache();
        if (fromCache != null) {
            return fromCache;
        }
        KmsRdsCredential credential = acquireRdsCredential(this.rdsCredentialProvider);
        putRdsCredentialCache(credential);

        return credential;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }
        String unwrappedUrl = "";
        if (url.startsWith(SCHEMA)) {
            unwrappedUrl = unwrapUrl(url);
        }

        KmsRdsCredential rdsCredential = getKmsCredential();

        info.put(DRIVER_PROPERTIES_KEY_USER, rdsCredential.getUsername());
        info.put(DRIVER_PROPERTIES_KEY_PASSWORD, rdsCredential.getPassword());
        try {
            return getWrappedDriver().connect(unwrappedUrl, info);
        } catch (Exception e) {
            // 连接失败，立即清理缓存
            clearRdsCredentialCache(rdsCredential);
            throw e;
        }
    }

    protected String getRealDriverClass() {
        return null;
    }

    private Driver getWrappedDriver() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getName().equals(getRealDriverClass())) {
                return driver;
            }
        }
        try {
            Class<?> clazz = Class.forName("com.mysql.cj.jdbc.Driver");
            return (Driver) clazz.newInstance();
        } catch (Exception e) {
            try {
                Class<?> clazz = Class.forName("com.mysql.jdbc.Driver");
                return (Driver) clazz.newInstance();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url.startsWith(SCHEMA)) {
            return getWrappedDriver().acceptsURL(unwrapUrl(url));
        } else {
            return !url.startsWith("jdbc:");
        }
    }

    private String unwrapUrl(String url) {
        if (StringUtils.isEmpty(url) || !url.startsWith(SCHEMA)) {
            throw new IllegalArgumentException("JDBC URL is invalid");
        }
        return url.replaceFirst(SCHEMA, "jdbc");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return getWrappedDriver().getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return getWrappedDriver().getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return getWrappedDriver().getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return getWrappedDriver().jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return getWrappedDriver().getParentLogger();
    }
}
