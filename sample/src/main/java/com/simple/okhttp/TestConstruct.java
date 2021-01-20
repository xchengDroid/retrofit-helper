package com.simple.okhttp;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class TestConstruct {

    private TestConstruct() {
    }

    public static TestConstruct get() {
        return new TestConstruct();
    }

    Constructor<?> test;

    public void testAccess() {
        try {
            Log.e("print", Modifier.isPublic(589825) + "toBinaryString:" + Integer.toBinaryString(589825));
            Constructor<?> handler = TestConstruct.class.getDeclaredConstructor();
            Log.e("print", "handler:" + handler + " --hash:" + handler.hashCode());
            Log.e("print", "TestConstruct:" + handler.getModifiers());
            Log.e("print", "getModifiers:" + Modifier.toString(handler.getModifiers()));
            Log.e("print", "isAccessible:" + handler.isAccessible());
            //handler.setAccessible(true);
            Object o2 = handler.newInstance();
            Log.e("print", "newInstance:" + o2);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
//        catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
    }

    private static class Inner {
        private Inner() {
        }
    }
}
