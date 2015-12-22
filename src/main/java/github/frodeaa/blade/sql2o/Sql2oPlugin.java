package github.frodeaa.blade.sql2o;

import blade.kit.log.Logger;
import com.blade.plugin.Plugin;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Sql2oPlugin implements Plugin, Db {

    private static Logger LOGGER = Logger.getLogger(Sql2oPlugin.class);
    private Sql2o sql2o = null;

    @Override
    public void run() {
        try {
            Class.forName("org.postgresql.Driver");
            sql2o = new Sql2o("jdbc:postgresql://localhost:5432/bottle", "frode", "");
            LOGGER.info("sql2o initialized");
        } catch (Throwable e) {
            LOGGER.error("Failed to load sql2o", e);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("sql2o destroy!");
    }

    public Connection open() {
        return sql2o.open();
    }
}
