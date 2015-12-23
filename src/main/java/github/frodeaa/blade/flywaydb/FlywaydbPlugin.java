package github.frodeaa.blade.flywaydb;

import blade.kit.log.Logger;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.sql2o.DbUrl;
import org.flywaydb.core.Flyway;

import java.net.URISyntaxException;

public class FlywaydbPlugin implements Plugin {

    private static Logger LOGGER = Logger.getLogger(FlywaydbPlugin.class);

    @Override
    public void run() {
        Flyway flyway = new Flyway();
        try {
            flyway.setDataSource(new DbUrl(System.getProperty("DATABASE_URL")).getDataSource());
            int migrations = flyway.migrate();
            LOGGER.info(String.format("completed db migration, applied %s migrations", migrations));
        } catch (URISyntaxException e) {
            LOGGER.error("Failed to migrate");
        }
    }

    @Override
    public void destroy() {

    }
}
