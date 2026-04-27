package com.huangjun.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ToJsonUtils<T> {

    private static final ThreadLocal<ObjectMapper> mapper = ThreadLocal.withInitial(ObjectMapper::new);

    public static ObjectMapper getMapper() {
        return mapper.get();
    }

    public static String toJson(Object object) {
        try{
            String string = getMapper().writeValueAsString(object);
            return string.isEmpty()?"":string;
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return getMapper().readValue(json, clazz);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
