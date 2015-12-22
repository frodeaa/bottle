package github.frodeaa.bottle;

import blade.kit.json.Json;
import com.blade.Blade;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;
import org.sql2o.Connection;

import static java.util.Collections.singletonMap;
import static blade.kit.json.Json.parse;


public class App {

    public static void main(String[] args) {
        Blade blade = Blade.me();

        blade.get("/", (req, resp) -> {
            resp.html("<h1>Hello Blade!</h1>");
        });

        blade.get("/health", (req, resp) -> {
            Integer status = Integer.valueOf(500);
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                status = con.createQuery("select 200").executeAndFetch(Integer.class).get(0);
            }
            resp.json(parse(singletonMap("status", status)).toString());
        });

        ((Plugin) blade.plugin(Sql2oPlugin.class)).run();
        blade.start();
    }
}
