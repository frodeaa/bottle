package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import com.blade.Blade;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.flywaydb.FlywaydbPlugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;
import org.sql2o.Connection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singletonMap;


public class App {

    private static String env(String key) {
        String value = System.getenv(key);
        return value == null ? "" : value;
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null ? defaultValue : value;
    }

    private static String loadDbUrl() throws URISyntaxException {
        String dbUrl = env("DATABASE_URL");
        if (dbUrl.isEmpty()) {
            if (!env("POSTGRES_PORT").isEmpty()) {
                URI dbUri = new URI(env("POSTGRES_PORT"));
                String dbAuth = String.format("%s:%s", env("POSTGRES_USER"), env("POSTGRES_PASSWORD"));
                dbUrl = String.format("postgresql://%s@%s:%s/%s",
                        dbAuth, dbUri.getHost(), dbUri.getPort(), env("POSTGRES_DATABASE", "bottle"));
            } else {
                dbUrl = "postgresql://bottle:bottle@localhost:5432/bottle";
            }
        }
        System.out.println(dbUrl);
        return dbUrl;
    }

    public static void main(String[] args) throws URISyntaxException {

        System.setProperty("DATABASE_URL", loadDbUrl());

        Blade blade = Blade.me();

        blade.post("/bottles", (req, resp) -> {
            Bottle.from(req.body().asString()).insertWith(blade.plugin(Db.class));
            resp.status(201);
        });

        blade.get("/bottles/:id", (req, resp) -> {
            Collection<Bottle> bottles = Collections.emptyList();
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                bottles = con.createQuery("select * from bottles where id = :id and datetime_removed is null")
                        .addParameter("id", req.paramAsInt("id")).executeAndFetch(Bottle.class);
            }
            if (bottles.isEmpty()) {
                resp.notFound();
            } else {
                resp.json(Bottle.asJson(bottles));
            }
        });

        blade.delete("/bottles/:id", (req, resp) -> {
            Collection<Bottle> bottles = Collections.emptyList();
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                bottles = con.createQuery("update bottles set datetime_removed = now() " +
                        "where id = :id and datetime_removed is null returning *")
                        .addParameter("id", req.paramAsInt("id")).executeAndFetch(Bottle.class);
            }
            if (bottles.isEmpty()) {
                resp.notFound();
            } else {
                resp.status(204);
            }
        });

        blade.get("/bottles", (req, resp) -> {
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                resp.json(Bottle.asJson(con.createQuery("select * from bottles " +
                        "where datetime_removed is null").executeAndFetch(Bottle.class)));
            }
        });


        blade.get("/health", (req, resp) -> {
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                resp.json(JSONKit.toJSONString(singletonMap("status",
                        con.createQuery("select 200").executeAndFetch(Integer.class).get(0))));
            }
        });

        ((Plugin) blade.plugin(FlywaydbPlugin.class)).run();
        ((Plugin) blade.plugin(Sql2oPlugin.class)).run();
        blade.start();
    }
}
