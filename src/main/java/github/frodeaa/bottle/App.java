package github.frodeaa.bottle;

import com.blade.Blade;

public class App {

    public static void main(String[] args) {
        Blade blade = Blade.me();
        blade.get("/", (req, resp) -> {
            resp.html("<h1>Hello Blade!</h1>");
        });
        blade.start();
    }
}
