(function(){
    'use strict';
    var serverURI = 'http://localhost:9000',
        config = null;

    var doXhr = function(method, path, auth, data, callback) {
        var req = new window.XMLHttpRequest();
        req.open(method, serverURI + path, true);
        req.setRequestHeader('Content-Type', 'application/json');
        if (auth) {
            req.setRequestHeader("Authorization", "Basic " + window.btoa(config.id + ":" + config.password));
        }
        if (data) {
            req.send(JSON.stringify(data));
        } else {
            req.send();
        }
        req.onreadystatechange = function() {
            if (req.readyState == 4 && req.status >= 200 && req.status < 300) {
                callback(null, JSON.parse(req.responseText));
            } else if (req.readyState == 4) {
                callback(req.response);
            }
        };
    };

    chrome.storage.sync.get("bottle-cfg", function(cfg) {
        config = cfg;
        if ('bottle-cfg' in config) {
            config = config['bottle-cfg'];
        }
        if (!("password" in config) || !('id' in config)) {
            console.log("no config found, login to get new");
        } else {
            console.log("found counfig, continue");
        }
    });

    doXhr("GET", "/bottle", "", null, null);


})();