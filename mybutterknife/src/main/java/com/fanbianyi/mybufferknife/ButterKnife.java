package com.fanbianyi.mybufferknife;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ButterKnife {
    public static void bind(Activity activity) {
        try {
            //生成的类
            Class<?> generatedClz = Class.forName(activity.getClass().getCanonicalName() + "_Binding");
            //使用这个注解的类
            Class<?> sourceClz = Class.forName(activity.getClass().getCanonicalName());
            //参数必须是自身，用父类Activity也不行，会提示找不到
            Method m = generatedClz.getMethod("initView", sourceClz);
            m.invoke(null, activity);
        } catch (ClassNotFoundException e) {
            Log.d("hjw_test", "ClassNotFoundException = " + e);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            Log.d("hjw_test", "NoSuchMethodException = " + e);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.d("hjw_test", "IllegalAccessException = " + e);
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.d("hjw_test", "InvocationTargetException = " + e);
            e.printStackTrace();
        }
    }
}
