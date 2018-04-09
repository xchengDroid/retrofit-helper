package com.xcheng.retrofit;

import java.util.HashMap;
import java.util.Map;

/**
 * 创建时间：2018/4/9
 * 编写人： chengxin
 * 功能描述：优化retrofit Map参数类型
 */
public class ParamMap<V> extends HashMap<String, V> {

    public ParamMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ParamMap(int initialCapacity) {
        super(initialCapacity);
    }

    public ParamMap() {
        super();
    }

    public ParamMap(Map<? extends String, ? extends V> m) {
        super(m);
    }

    public ParamMap<V> param(String key, V value) {
        super.put(key, value);
        return this;
    }

    public ParamMap<V> params(Map<? extends String, ? extends V> m) {
        super.putAll(m);
        return this;
    }
}
