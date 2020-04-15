package com.tss.nettysocket.bean;

/**
 * @author ：xiangjun.yang
 * @description：全局参数变量配置类
 */
public class Config {
    private static String host = "127.0.0.1";
    private static int port = 6379;
    private static int timeout = 1000;
    private static int maxIdle = 8;
    private static long maxWaitMillis = -1;
    private static String password = "";
    private static int database = 0;
    private static boolean keepAlive = true;
    private static int readIdleTimeSeconds = 60;
    private static int writeIdleTimeSeconds = 60;
    private static int allIdleTimeSeconds = 0;
    private static boolean persistence = false;
    private static int maxWaitReplySeconds = 60;

    public static String getHost() {
        return host;
    }

    public static int getPort() {
        return port;
    }

    public static int getTimeout() {
        return timeout;
    }

    public static int getMaxIdle() {
        return maxIdle;
    }

    public static long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public static String getPassword() {
        return password;
    }

    public static int getDatabase() {
        return database;
    }

    public static void setHost(String host) {
        Config.host = host;
    }

    public static void setPort(int port) {
        Config.port = port;
    }

    public static void setTimeout(int timeout) {
        Config.timeout = timeout;
    }

    public static void setMaxIdle(int maxIdle) {
        Config.maxIdle = maxIdle;
    }

    public static void setMaxWaitMillis(long maxWaitMillis) {
        Config.maxWaitMillis = maxWaitMillis;
    }

    public static void setPassword(String password) {
        Config.password = password;
    }

    public static void setDatabase(int database) {
        Config.database = database;
    }

    public static boolean isKeepAlive() {
        return keepAlive;
    }

    public static void setKeepAlive(boolean keepAlive) {
        Config.keepAlive = keepAlive;
    }

    public static int getReadIdleTimeSeconds() {
        return readIdleTimeSeconds;
    }

    public static void setReadIdleTimeSeconds(int readIdleTimeSeconds) {
        Config.readIdleTimeSeconds = readIdleTimeSeconds;
    }

    public static int getWriteIdleTimeSeconds() {
        return writeIdleTimeSeconds;
    }

    public static void setWriteIdleTimeSeconds(int writeIdleTimeSeconds) {
        Config.writeIdleTimeSeconds = writeIdleTimeSeconds;
    }

    public static int getAllIdleTimeSeconds() {
        return allIdleTimeSeconds;
    }

    public static void setAllIdleTimeSeconds(int allIdleTimeSeconds) {
        Config.allIdleTimeSeconds = allIdleTimeSeconds;
    }

    public static boolean isPersistence() {
        return persistence;
    }

    public static void setPersistence(boolean persistence) {
        Config.persistence = persistence;
    }

    public static int getMaxWaitReplySeconds() {
        return maxWaitReplySeconds;
    }

    public static void setMaxWaitReplySeconds(int maxWaitReplySeconds) {
        Config.maxWaitReplySeconds = maxWaitReplySeconds;
    }
}
