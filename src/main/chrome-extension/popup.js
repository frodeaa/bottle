(function(){
    'use strict';
    var serverURI = 'http://localhost:9000',
        config = null,
        count = 0,
        links = document.getElementById("links");

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

    var newUser = function(callback) {
        console.log("create new user");
        var userData = {password: Math.random().toString(36)};
        doXhr("POST", "/users", null, userData, function(err, data) {
            if (!err) {
                data.password = userData.password;
                callback(null, config);
            } else {
                callback(err);
            }
        });
    };

    var badgeText = function(c){
        if(c > 999){
            return c.toString()+"+";
        }
        return c.toString();
    };

    var getIcon = function(url) {
        var domain = url.replace('http://','').replace('https://','').split(/[/?#]/)[0];
        var imgUrl = "http://www.google.com/s2/favicons?domain=" + domain;
        var img = document.createElement("img");
        img.setAttribute('src', imgUrl);
        return img.outerHTML;
    };

    var createLinkHtml = function(bottle) {
         var linkBtn = document.createElement("span");
         linkBtn.setAttribute("class", "removeBtn");
         linkBtn.setAttribute("id", bottle.id);
         return linkBtn.outerHTML+"<a target='_blank' href='"+bottle.url+"'>" + getIcon(bottle.url) + " " + bottle.title +"</a>";
    };

    var listBottles = function(err, bottles) {
        if (err) {
            count = 0;
            return;
        }
        count = bottles.length;
        for (var i = 0; i < count; i++) {
            var list = document.createElement("li");
            list.innerHTML= createLinkHtml(bottles[i]);
            links.appendChild(list);
        }
        chrome.browserAction.setBadgeText({"text": badgeText(count)});
        console.log("finish list bottles", count);
    };

    var getBottles = function(callback) {
        callback(null, []);
    };



    chrome.storage.sync.get("bottle-cfg", function(cfg) {
        config = cfg;
        if ('bottle-cfg' in config) {
            config = config['bottle-cfg'];
        }
        if (!("password" in config) || !('id' in config)) {
            console.log("no config found");
            newUser(function(err, data) {
                if (!err) {
                    config = data;
                    chrome.storage.sync.set( {'bottle-cfg': config});
                    listBottles(null, []);
                } else {
                    console.log("failed to create new user", err);
                }
            });
        } else {
            console.log("use existing config");
            getBottles(listBottles);
        }
    });
})();