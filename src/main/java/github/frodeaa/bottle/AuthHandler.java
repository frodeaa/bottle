package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.log.Logger;
import com.blade.Blade;
import com.blade.route.RouteHandler;
import com.blade.web.http.Request;
import com.blade.web.http.Response;
import github.frodeaa.blade.sql2o.Db;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonMap;

public class AuthHandler implements RouteHandler {

    private Logger LOGGER = Logger.getLogger(AuthHandler.class);

    @Override
    public void handle(Request request, Response response) {
        String authHeader = request.header("Authorization");
        if (authHeader != null) {
            String[] auth = authHeader.split(" ");
            if (auth.length == 2 && "Basic".equals(auth[0])) {
                String[] userPass = new String(Base64.getDecoder().decode(auth[1])).split(":");
                List<User> users;
                try {
                    UUID userId = UUID.fromString(userPass[0]);
                    users = User.byId(userId, Blade.me().plugin(Db.class));
                } catch (IllegalArgumentException e) {
                    response.status(400).json(JSONKit.toJSONString(singletonMap("message", e.getMessage())));
                    return;
                }
                if (users.size() == 1 && BCrypt.checkpw(
                        String.format("%s:%s", userPass[0], userPass[1]), users.get(0).getPassword())) {
                    LOGGER.info("authenticated " + users.get(0).toJson());
                    return;
                }
            }
            response.header("Access-Control-Allow-Origin", "*");
            response.status(403);
            response.json(JSONKit.toJSONString(singletonMap("status_code", 403)));
        } else {
            response.status(401);
            response.header("WWW-Authenticate", "Basic realm=Bottle API");
            response.json(JSONKit.toJSONString(singletonMap("status_code", 401)));
        }
    }

}
