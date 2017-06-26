package com.simple.okhttp;

/**
 * Created by chengxin on 2017/6/23.
 */

public class Weather {
    private Weatherinfo weatherinfo;

    static class Weatherinfo {
        private String city;
        private String cityid;
        private String temp1;
        private String temp2;
        private String weather;
        private String img1;
        private String img2;
        private String ptime;
    }

    @Override
    public String toString() {
        return "city:" + weatherinfo.city + " cityid:" + weatherinfo.cityid + " temp1:" + weatherinfo.temp1;
    }
}
