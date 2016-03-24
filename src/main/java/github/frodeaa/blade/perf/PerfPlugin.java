package github.frodeaa.blade.perf;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;
import com.blade.Blade;
import com.blade.plugin.Plugin;

public class PerfPlugin implements Plugin {

    public static final String START_TIME = "start_time";
    private Logger LOGGER = LoggerFactory.getLogger(PerfPlugin.class);


    @Override
    public void run() {

        Blade.me().before(".*", (request, response) -> {
            request.attribute(START_TIME, System.currentTimeMillis());

        });

        Blade.me().after(".*", (request, response) -> {
            long startTime = request.attribute(START_TIME);
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info(
                    String.format("Request : %s\t%s\t%s %sms",
                            request.method(), request.pathInfo(), response.status(), duration));
        });

    }

    @Override
    public void destroy() {

    }
}
