package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import com.blade.Blade;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.flywaydb.FlywaydbPlugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;
import org.sql2o.Connection;

import java.util.Collection;
import java.util.Collections;

import static java.util.Collections.singletonMap;


public class App {

    public static void main(String[] args) {

        System.setProperty("DATABASE_URL",
                System.getProperty("DATABASE_URL", "postgresql://frode:@localhost:5432/bottle"));

        Blade blade = Blade.me();

        blade.post("/bottles", (req, resp) -> {
            Bottle.from(req.body().asString()).insertWith(blade.plugin(Db.class));
            resp.status(201);
        });

        blade.get("/bottles/:id", (req, resp) -> {
            Collection<Bottle> bottles = Collections.emptyList();
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                bottles = con.createQuery("select * from bottles where id = :id")
                        .addParameter("id", req.paramAsInt("id")).executeAndFetch(Bottle.class);
            }
            if (bottles.isEmpty()) {
                resp.notFound();
            } else {
                resp.json(Bottle.asJson(bottles));
            }
        });

        blade.get("/bottles", (req, resp) -> {
            try (Connection con = ((Db) blade.plugin(Db.class)).open()) {
                resp.json(Bottle.asJson(con.createQuery("select * from bottles").executeAndFetch(Bottle.class)));
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
