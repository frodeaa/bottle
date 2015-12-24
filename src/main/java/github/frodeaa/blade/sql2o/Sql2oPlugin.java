package github.frodeaa.blade.sql2o;

import blade.kit.log.Logger;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.DbUrl;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

public class Sql2oPlugin implements Plugin, Db {

    private static Logger LOGGER = Logger.getLogger(Sql2oPlugin.class);
    private DbUrl dbUrl;
    private Sql2o sql2o = null;

    @Override
    public void run() {
        try {
            dbUrl = new DbUrl(System.getProperty("DATABASE_URL"));
            sql2o = new Sql2o(dbUrl.getDataSource());
            LOGGER.info("sql2o initialized");
        } catch (Exception e) {
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

    public int healthCheck(int status) {
        try (Connection con = open()) {
            return con.createQuery("select :status")
                    .addParameter("status", status).executeAndFetch(Integer.class).get(0);
        }
    }
}
