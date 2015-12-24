package github.frodeaa.blade;

import org.sql2o.GenericDatasource;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

public class DbUrl {

    private final String dbStr;
    private final URI dbUrl;

    public DbUrl(String dbStr) throws URISyntaxException {
        this.dbStr = dbStr;
        if (this.dbStr == null || this.dbStr.trim().isEmpty()) {
            throw new IllegalArgumentException("empty dbStr is not allowed");
        }
        this.dbUrl = new URI(this.dbStr);
    }

    public DataSource getDataSource() {
        String url = String.format("jdbc:%s://%s:%s%s",
                this.dbUrl.getScheme(), this.dbUrl.getHost(), this.dbUrl.getPort(), this.dbUrl.getPath());
        String username = null;
        String password = null;

        if (this.dbUrl.getUserInfo() != null) {
            String[] auth = this.dbUrl.getUserInfo().split(":");
            if (auth.length > 0) {
                username = auth[0];
            }

            if (auth.length > 1) {
                password = auth[1];
            }
        }
        return new GenericDatasource(url, username, password);
    }

}
