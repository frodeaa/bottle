package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.json.JsonArray;
import blade.kit.json.JsonObject;
import blade.kit.json.JsonValue;
import github.frodeaa.blade.sql2o.Db;
import org.sql2o.Connection;

import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class Bottle {
    private Long id;
    private String title;
    private String url;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.add("id", getId());
        o.add("title", getTitle());
        o.add("url", getUrl());
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
        bottle.setTitle(values.get("title").asString());
        bottle.setUrl(values.get("url").asString());
        return bottle;

    }

    public void insertWith(Db db) {
        try (Connection con = db.open()) {
            setId(con.createQuery("insert into bottles(title, url) values(:title, :url) returning id")
                    .bind(this).executeAndFetch(Bottle.class).get(0).getId());
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
