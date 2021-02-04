package com.personal.project.explora.utils;

import java.util.Collection;

public class ObjectUtil {

    public static boolean isEmpty(Collection obj) {
        return obj == null || obj.isEmpty();
    }

    public static boolean isEmpty(String obj) {
        return obj == null || obj.isEmpty();
    }

}
