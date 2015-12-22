package github.frodeaa.bottle;

import com.blade.Blade;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;
import org.sql2o.Connection;


public class App {

    public static void main(String[] args) {
        Blade blade = Blade.me();

        blade.get("/", (req, resp) -> {
            resp.html("<h1>Hello Blade!</h1>");
        });

        blade.get("/health", (req, resp) -> {
            Integer status = Integer.valueOf(-1);
            Db db = blade.plugin(Db.class);
            try (Connection con = db.open()) {
                status = con.createQuery("select 0").executeAndFetch(Integer.class).get(0);
            }
            resp.json(status.toString());
        });

        ((Plugin) blade.plugin(Sql2oPlugin.class)).run();
        blade.start();
    }
}
