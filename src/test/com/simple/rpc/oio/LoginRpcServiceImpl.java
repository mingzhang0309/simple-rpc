package com.simple.rpc.oio;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public class LoginRpcServiceImpl implements LoginRpcService {
    private Map<String, String> cache = new HashMap<String, String>();

    @Override
    public boolean login(String username, String password) {
        System.out.println("login:user:" + username + " pass:" + password + " attach haha:");
        String pass = cache.get(username);
        return pass != null && pass.equals(password);
    }

    public LoginRpcServiceImpl() {
        cache.put("linda", "123456");
        cache.put("test", "123456");
        cache.put("admin", "123456");
    }
}
