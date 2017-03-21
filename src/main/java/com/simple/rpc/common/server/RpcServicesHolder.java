package com.simple.rpc.common.server;

import com.simple.rpc.common.RpcServiceBean;
import java.util.List;

/**
 * Created by stephen.zhang on 17/3/21.
 */
public interface RpcServicesHolder {
    public List<RpcServiceBean> getRpcServices();
}
