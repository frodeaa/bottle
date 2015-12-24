package github.frodeaa.bottle;

import java.net.URI;
import java.net.URISyntaxException;

public class EnvConfig {

    private final String defaultDatabaseUrl = "postgresql://bottle:bottle@localhost:5432/bottle";


    public String databaseUrl() throws URISyntaxException {
        String dbUrl = env("DATABASE_URL");
        if (dbUrl.isEmpty()) {
            dbUrl = databaseUrlFromEnv();
        }
        return dbUrl.isEmpty() ? defaultDatabaseUrl : dbUrl;
    }

    private String databaseUrlFromEnv() throws URISyntaxException {
        String url = env("POSTGRES_PORT");
        if (!url.isEmpty()) {
            URI dbUri = new URI(url);
            String dbAuth = String.format("%s:%s", env("POSTGRES_USER"), env("POSTGRES_PASSWORD"));
            url = String.format("postgresql://%s@%s:%s/%s",
                    dbAuth, dbUri.getHost(), dbUri.getPort(), env("POSTGRES_DATABASE", "bottle"));
        }
        return url;
    }

    private String env(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value;
    }

    private String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null ? defaultValue : value;
    }
}
