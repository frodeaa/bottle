package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.json.ParseException;
import blade.kit.logging.LoggerFactory;
import com.blade.Blade;
import github.frodeaa.blade.flywaydb.FlywaydbPlugin;
import github.frodeaa.blade.perf.PerfPlugin;
import github.frodeaa.blade.sql2o.Db;
import github.frodeaa.blade.sql2o.Sql2oPlugin;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;


public class App {

    private static Db db(Blade blade) {
        return blade.ioc().getBean(Sql2oPlugin.class);
    }

    public static void main(String[] args) throws URISyntaxException {

        System.setProperty("DATABASE_URL", new EnvConfig().databaseUrl());

        Blade blade = Blade.me();

        blade.before("/bottles.*", new AuthHandler());

        blade.post("/users", (request, response) -> {
            try {
                User user = User.fromRequest(request.body().asString());
                user.insertWith(db(blade));
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
                        request.body().asString()).insertWith(db(blade)).toJson().toString());
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.get("/bottles/:id", (request, response) -> {
            try {
                UUID externalId = UUID.fromString(request.param("id"));
                User user = request.attribute("user");
                Collection<Bottle> bottles = Bottle.byId(user, externalId, db(blade));
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
                User user = request.attribute("user");
                Collection<Bottle> removedBottles = Bottle.deleteAll(user, db(blade));
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
                User user = request.attribute("user");
                Collection<Bottle> removedBottles = Bottle.deleteById(user,
                        externalId, db(blade));

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
                User user = request.attribute("user");
                response.json(Bottle.asJson(Bottle.list(user, db(blade))));
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.get("/health", (request, response) -> {
            try {
                int status = (db(blade)).healthCheck(200);
                response.status(status).json(JSONKit.toJSONString(singletonMap("status", status)));
            } catch (ParseException | IllegalArgumentException e) {
                response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
            } catch (Exception e) {
                response.status(500);
            }
        });

        blade.get("/", (request, response) -> response.render("index"));

        asList(FlywaydbPlugin.class, Sql2oPlugin.class, PerfPlugin.class).forEach(p -> blade.plugin(p));

        try {
            blade.createServer(9000).start("/");
            asList(FlywaydbPlugin.class, Sql2oPlugin.class, PerfPlugin.class).forEach(p -> blade.ioc().getBean(p).run());
        } catch (Exception e) {
            LoggerFactory.getLogger(App.class).error("failed to start", e);
        }
    }
}
