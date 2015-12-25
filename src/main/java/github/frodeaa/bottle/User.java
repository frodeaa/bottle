package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.json.JsonObject;
import blade.kit.json.JsonValue;
import github.frodeaa.blade.sql2o.Db;
import org.mindrot.jbcrypt.BCrypt;
import org.sql2o.Connection;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class User {

    private Long id;
    private UUID external_id;
    private Timestamp datetime_added;
    private Timestamp datetime_disabled;
    private String password;

    public UUID getExternal_id() {
        return external_id;
    }

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public Timestamp getDatetime_added() {
        return datetime_added;
    }

    public Timestamp getDatetime_disabled() {
        return datetime_disabled;
    }

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.add("id", getExternal_id().toString());
        o.add("datetime_added", getDatetime_added().toLocalDateTime().toString());
        return o;
    }

    private void updateWith(User user) {
        this.id = user.id;
        this.external_id = user.external_id;
        this.datetime_added = user.datetime_added;
        this.datetime_disabled = user.datetime_disabled;
        this.password = user.password;
    }

    public void insertWith(Db db) {
        try (Connection con = db.open()) {
            updateWith(con.createQuery("insert into users(password, external_id) " +
                    "values(:password, :external_id) returning *")
                    .bind(this).executeAndFetch(User.class).get(0));
        }
    }

    public static User fromRequest(String json) {
        Map<String, JsonValue> values = JSONKit.toMap(json);
        for (String required : Arrays.asList("password")) {
            if (!values.containsKey(required) || values.get(required) == null || values.get(required).asString().equals("")) {
                throw new IllegalArgumentException("missing required value for " + required);
            }
        }
        User user = new User();
        user.external_id = UUID.randomUUID();
        user.password = BCrypt.hashpw(String.format("%s:%s", user.external_id, values.get("password").asString()),
                BCrypt.gensalt(12));
        return user;
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(
                String.format("%s:%s", getExternal_id(), password), getPassword());
    }

    public static List<User> byId(UUID externalId, Db db) {
        try (Connection con = db.open()) {
            return con.createQuery("select * from users " +
                    "where external_id = :external_id and datetime_disabled is null")
                    .addParameter("external_id", externalId).executeAndFetch(User.class);
        }
    }

}
