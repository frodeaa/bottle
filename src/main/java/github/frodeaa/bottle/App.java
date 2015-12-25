package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.json.ParseException;
import com.blade.Blade;
import com.blade.plugin.Plugin;
import github.frodeaa.blade.flywaydb.FlywaydbPlugin;
import github.frodeaa.blade.perf.PerfPlugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

import static java.util.Collections.singletonMap;


public class App {

    public static void main(String[] args) throws URISyntaxException {

        System.setProperty("DATABASE_URL", new EnvConfig().databaseUrl());

        Blade blade = Blade.me();

        blade.before("/bottles.*", new AuthHandler());

        blade.post("/users", (request, response) -> {
            try {
                User user = User.fromRequest(request.body().asString());
                user.insertWith(blade.plugin(Db.class));
                response.status(201).json(user.toJson().toString());
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.post("/bottles", (request, response) -> {
            try {
                response.status(201).json(Bottle.from(request.attribute("user"),
                        request.body().asString()).insertWith(blade.plugin(Db.class)).toJson().toString());
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.get("/bottles/:id", (request, response) -> {
            try {
                UUID externalId = UUID.fromString(request.param("id"));
                Collection<Bottle> bottles = Bottle.byId(request.attribute("user"), externalId, blade.plugin(Db.class));
                if (bottles.isEmpty()) {
                    response.notFound();
                } else {
                    response.json(Bottle.asJson(bottles));
                }

            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.delete("/bottles", (request, response) -> {
            try {
                Collection<Bottle> removedBottles = Bottle.deleteAll(request.attribute("user"), blade.plugin(Db.class));
                if (removedBottles.isEmpty()) {
                    response.status(204);
                } else {
                    response.status(200).json(Bottle.asJson(removedBottles).toString());
                }
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.delete("/bottles/:id", (request, response) -> {
            UUID externalId;
            try {
                externalId = UUID.fromString(request.param("id"));
            } catch (IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
                return;
            }

            try {
                Collection<Bottle> removedBottles = Bottle.deleteById(request.attribute("user"),
                        externalId, blade.plugin(Db.class));

                if (removedBottles.isEmpty()) {
                    response.notFound();
                } else {
                    response.status(200).json(Bottle.asJson(removedBottles).toString());
                }

            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.get("/bottles", (request, response) -> {
            try {
                response.json(Bottle.asJson(Bottle.list(request.attribute("user"), blade.plugin(Db.class))));
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.get("/health", (request, response) -> {
            try {
                int status = ((Db) blade.plugin(Db.class)).healthCheck(200);
                response.status(status).json(JSONKit.toJSONString(singletonMap("status", status)));
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        ((Plugin) blade.plugin(FlywaydbPlugin.class)).run();
        ((Plugin) blade.plugin(Sql2oPlugin.class)).run();
        ((Plugin) blade.plugin(PerfPlugin.class)).run();

        blade.start();
    }
}
