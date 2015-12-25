(function(){
    'use strict';
    var serverURI = 'http://localhost:9000',
        config = null,
        count = 0,
        links = document.getElementById("links"),
        addBtn = document.getElementById("addBtn"),
        msg = document.getElementById("message");

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
                if (req.responseText.length > 0) {
                    callback(null, JSON.parse(req.responseText));
                }else {
                    callback(null, null);
                }
            } else if (req.readyState == 4) {
                callback(req.response);
            }
        };
    };

    var newUser = function(callback) {
        console.log("create new user");
        var userData = {password: Math.random().toString(36)};
        doXhr("POST", "/users", false, userData, function(err, data) {
            if (!err) {
                data.password = userData.password;
                callback(null, config);
            } else {
                callback(err);
            }
        });
    };

    var newBottle = function(bottle, callback) {
        console.log("create new bottle", bottle);
        doXhr("POST", "/bottles", true, bottle, function(err, data) {
            if (!err) {
                callback(data);
            }
        });
    };

    var getBottles = function(callback) {
        console.log("get all bottles");
        doXhr("GET", "/bottles", true, null, callback);
    };

    var removeBottle = function(id, callback) {
        console.log("remove bottle", id);
        doXhr("DELETE", "/bottles/" + id, true, null, callback);
    };

    var message = function(messageStr) {
        msg.innerText = messageStr;
        setTimeout(function() {
            msg.innerText = "Total Links: "+count;
        }, 1000);
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
         var linkBtn = document.createElement("span"),
            title = bottle.title;

         if (title.length > 50) {
            title = title.substr(0, 50) + "...";
         }

         linkBtn.setAttribute("class", "removeBtn");
         linkBtn.setAttribute("id", bottle.id);
         return linkBtn.outerHTML+"<a target='_blank' href='"+bottle.url+"'>" + getIcon(bottle.url)  + title +"</a>";
    };

    var removeBottleLink = function(e) {
        if(e.target.parentNode.parentNode){
            removeBottle(e.target.getAttribute("id"), function(err, bottle) {
                console.log("removed bottle", bottle, "update bottle list");
                message("Removed Link");
                count--;
                chrome.browserAction.setBadgeText({"text": badgeText(count)});
                e.target.parentNode.parentNode.removeChild(e.target.parentNode);
            });
        }
    };

    var listBottles = function(err, bottles) {
        if (err) {
            count = 0;
            return;
        }
        bottles.sort(function(a, b){
            if(a.datetime_added < b.datetime_added) return -1;
            if(a.datetime_added > b.datetime_added) return 1;
            return 0;
        });
        count = bottles.length;
        for (var i = 0; i < count; i++) {
            var list = document.createElement("li");
            list.innerHTML= createLinkHtml(bottles[i]);
            links.appendChild(list);
            list.getElementsByClassName("removeBtn")[0].addEventListener("click", removeBottleLink, false);
        }
        chrome.browserAction.setBadgeText({"text": badgeText(count)});
        console.log("finish list bottles", count);
        message("Finished!");
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

    addBtn.addEventListener("click", function(){
        chrome.tabs.getSelected(null, function(tab){
            newBottle({"title": tab.title, "url": tab.url}, function(bottle) {
                var list = document.createElement("li");
                list.innerHTML = createLinkHtml(bottle);
                links.appendChild(list);
                list.getElementsByClassName("removeBtn")[0].addEventListener("click", removeBottleLink, false);
                message("Saved!");
                count++;
                chrome.browserAction.setBadgeText({"text": badgeText(count)});
            });
        });
    });
})();