package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import com.blade.Blade;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.flywaydb.FlywaydbPlugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;

import java.net.URISyntaxException;
import java.util.Collection;

import static java.util.Collections.singletonMap;


public class App {

    public static void main(String[] args) throws URISyntaxException {

        System.setProperty("DATABASE_URL", new EnvConfig().databaseUrl());

        Blade blade = Blade.me();

        blade.post("/bottles", (req, resp) -> {
            Bottle.from(req.body().asString()).insertWith(blade.plugin(Db.class));
            resp.status(201);
        });

        blade.get("/bottles/:id", (req, resp) -> {
            Collection<Bottle> bottles = Bottle.byId(req.paramAsLong("id"), blade.plugin(Db.class));
            if (bottles.isEmpty()) {
                resp.notFound();
            } else {
                resp.json(Bottle.asJson(bottles));
            }
        });

        blade.delete("/bottles/:id", (req, resp) -> {
            if (Bottle.deleteById(req.paramAsLong("id"), blade.plugin(Db.class))) {
                resp.status(204);
            } else {
                resp.notFound();
            }
        });

        blade.get("/bottles", (req, resp) -> {
            resp.json(Bottle.asJson(Bottle.list(blade.plugin(Db.class))));
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
