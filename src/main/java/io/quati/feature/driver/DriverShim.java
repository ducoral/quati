package io.quati.feature.driver;

import java.sql.*;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

public class DriverShim implements Driver {

    private final Driver delegate;
    private final ClassLoader driverClassLoader;

    public DriverShim(Driver d) {
        this.delegate = Objects.requireNonNull(d, "delegate");
        this.driverClassLoader = delegate.getClass().getClassLoader();
    }

    private <T> T withContextClassLoader(Callable<T> action) throws SQLException {
        var original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(driverClassLoader);
            return action.call();
        } catch (Exception e) {
            throw new SQLException("error in delegated driver call (%s)".formatted(delegate.getClass().getName()), e);
        } finally {
             Thread.currentThread().setContextClassLoader(original);
        }
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return withContextClassLoader(() -> delegate.connect(url, info));
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return withContextClassLoader(() -> delegate.acceptsURL(url));
    }

    @Override
    public int getMajorVersion() {
        return delegate.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return delegate.getMinorVersion();
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return delegate.getPropertyInfo(url, info);
    }

    @Override
    public boolean jdbcCompliant() {
        return delegate.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }
}