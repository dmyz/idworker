package com.imadcn.framework.idworker.toolkit.json;

public interface Json {
    
    String toJsonString(Object object);
    
    <T> T parseObject(String json, Class<T> clazz);
}
