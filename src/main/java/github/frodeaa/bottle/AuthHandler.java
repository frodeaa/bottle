package github.frodeaa.bottle;

import blade.kit.json.JSONKit;
import blade.kit.log.Logger;
import com.blade.Blade;
import com.blade.route.RouteHandler;
import com.blade.web.http.Request;
import com.blade.web.http.Response;
import github.frodeaa.blade.sql2o.Db;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static java.util.Collections.singletonMap;

public class AuthHandler implements RouteHandler {

    private Logger LOGGER = Logger.getLogger(AuthHandler.class);

    private String ip(Request requeset) {
        String forwardedFor = requeset.header("X-Forwarded-For");
        if (forwardedFor != null) {
            return forwardedFor.split(" ")[0];
        }
        return requeset.raw().getRemoteAddr();
    }

    @Override
    public void handle(Request request, Response response) {
        String authHeader = request.header("Authorization");

        String ip = ip(request);
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
                if (users.size() == 1 && users.get(0).checkPassword(userPass[1])) {
                    LOGGER.info("authenticated " + users.get(0).getExternal_id() + ", " + ip);
                    request.attribute("user", users.get(0));
                    return;
                }
            }
            LOGGER.info("authentication failed " + ip);
            response.header("Access-Control-Allow-Origin", "*");
            response.status(403).json(JSONKit.toJSONString(singletonMap("status_code", 403)));
        } else {
            response.header("WWW-Authenticate", "Basic realm=Bottle API");
            response.status(401).json(JSONKit.toJSONString(singletonMap("status_code", 401)));
        }
    }

}
