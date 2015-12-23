package github.frodeaa.blade.sql2o;

import com.blade.plugin.Plugin;
import org.sql2o.Connection;

public interface Db extends Plugin {

    public Connection open();

}
