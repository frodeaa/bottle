package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.json.JsonArray;
import blade.kit.json.JsonObject;
import blade.kit.json.JsonValue;
import github.frodeaa.blade.sql2o.Db;
import org.sql2o.Connection;

import java.sql.Timestamp;
import java.util.*;

public class Bottle {
    private Long id;
    private Long user_id;
    private UUID external_id;
    private String title;
    private String url;
    private Timestamp datetime_added;
    private Timestamp datetime_removed;

    public Long getId() {
        return id;
    }

    public UUID getExternal_id() {
        return external_id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Timestamp getDatetime_added() {
        return datetime_added;
    }

    public Timestamp getDatetime_removed() {
        return datetime_removed;
    }

    public Long getUser_id() {
        return user_id;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.add("id", getExternal_id().toString());
        o.add("title", getTitle());
        o.add("url", getUrl());
        o.add("datetime_added", getDatetime_added().toLocalDateTime().toString());
        return o;
    }

    public static Bottle from(User user, String json) {
        Map<String, JsonValue> values = JSONKit.toMap(json);
        for (String required : Arrays.asList("title", "url")) {
            if (!values.containsKey(required) || values.get(required) == null || values.get(required).asString().equals("")) {
                throw new IllegalArgumentException("missing required value for " + required);
            }
        }
        Bottle bottle = new Bottle();
        bottle.title = values.get("title").asString();
        bottle.url = values.get("url").asString();
        bottle.user_id = user.getId();
        return bottle;

    }

    private void updateWith(Bottle bottle) {
        this.id = bottle.id;
        this.external_id = bottle.external_id;
        this.title = bottle.title;
        this.url = bottle.url;
        this.datetime_added = bottle.datetime_added;
        this.datetime_removed = bottle.datetime_removed;
        this.user_id = bottle.user_id;
    }

    public Bottle insertWith(Db db) {
        try (Connection con = db.open()) {
            updateWith(con.createQuery("insert into bottles(title, url, user_id) values(:title, :url, :user_id) returning *")
                    .bind(this).executeAndFetch(Bottle.class).get(0));
        }
        return this;
    }

    public static boolean deleteById(User user, UUID externalId, Db db) {
        try (Connection con = db.open()) {
            return !con.createQuery("update bottles set datetime_removed = now() " +
                    "where external_id = :external_id and datetime_removed is null and user_id = :user_id returning *")
                    .addParameter("external_id", externalId)
                    .addParameter("user_id", user.getId()).executeAndFetch(Bottle.class).isEmpty();
        }
    }

    public static List<Bottle> byId(User user, UUID externalId, Db db) {
        try (Connection con = db.open()) {
            return con.createQuery("select * from bottles " +
                    "where external_id = :external_id and datetime_removed is null and user_id = :user_id")
                    .addParameter("external_id", externalId)
                    .addParameter("user_id", user.getId()).executeAndFetch(Bottle.class);
        }

    }

    public static List<Bottle> list(User user, Db db) {
        try (Connection con = db.open()) {
            return con.createQuery("select * from bottles " +
                    "where datetime_removed is null and user_id = :user_id")
                    .addParameter("user_id", user.getId()).executeAndFetch(Bottle.class);
        }
    }

    public static String asJson(Collection<Bottle> bottles) {
        JsonArray array = new JsonArray();
        for (Bottle bottle : bottles) {
            array.add(bottle.toJson());
        }
        return array.toString();
    }
}
