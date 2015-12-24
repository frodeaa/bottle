package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.json.JsonArray;
import blade.kit.json.JsonObject;
import blade.kit.json.JsonValue;
import github.frodeaa.blade.sql2o.Db;
import org.sql2o.Connection;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Bottle {
    private Long id;
    private String title;
    private String url;
    private Timestamp datetime_added;
    private Timestamp datetime_removed;

    public Long getId() {
        return id;
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

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.add("id", getId());
        o.add("title", getTitle());
        o.add("url", getUrl());
        o.add("datetime_added", getDatetime_added().toLocalDateTime().toString());
        return o;

    }

    public static Bottle from(String json) {
        Map<String, JsonValue> values = JSONKit.toMap(json);
        for (String required : Arrays.asList("title", "url")) {
            if (!values.containsKey(required) || values.get(required) == null || values.get(required).asString().equals("")) {
                throw new IllegalArgumentException("missing required value for " + required);
            }
        }
        Bottle bottle = new Bottle();
        bottle.title = values.get("title").asString();
        bottle.url = values.get("url").asString();
        return bottle;

    }

    public void insertWith(Db db) {
        try (Connection con = db.open()) {
            this.id = (con.createQuery("insert into bottles(title, url) values(:title, :url) returning id")
                    .bind(this).executeAndFetch(Bottle.class).get(0).getId());
        }
    }

    public static boolean deleteById(Long id, Db db) {
        try (Connection con = db.open()) {
            return !con.createQuery("update bottles set datetime_removed = now() " +
                    "where id = :id and datetime_removed is null returning *")
                    .addParameter("id", id).executeAndFetch(Bottle.class).isEmpty();
        }
    }

    public static List<Bottle> byId(Long id, Db db) {
        try (Connection con = db.open()) {
            return con.createQuery("select * from bottles where id = :id and datetime_removed is null")
                    .addParameter("id", id).executeAndFetch(Bottle.class);
        }

    }

    public static List<Bottle> list(Db db) {
        try (Connection con = db.open()) {
            return con.createQuery("select * from bottles " +
                    "where datetime_removed is null").executeAndFetch(Bottle.class);
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
