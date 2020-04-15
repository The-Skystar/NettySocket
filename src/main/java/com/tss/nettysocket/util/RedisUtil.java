package com.tss.nettysocket.util;

import com.tss.nettysocket.bean.Config;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.SafeEncoder;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author ：xiangjun.yang
 * @date ：Created in 2019/3/18 12:10
 * @description：redis工具类
 */
public class RedisUtil {
    private static RedisUtil redisUtil = new RedisUtil();

    private static JedisPool jedisPool;

    private RedisUtil(){
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(Config.getMaxIdle());
        jedisPoolConfig.setMaxWaitMillis(Config.getMaxWaitMillis());
        jedisPool = new JedisPool(jedisPoolConfig, Config.getHost(), Config.getPort(), Config.getTimeout(), Config.getPassword(), Config.getDatabase());
    }

    public static RedisUtil getInstance(){
        return redisUtil;
    }

    //对象序列化为字符串
    public static String objectSerialiable(Object obj){
        String serStr = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            serStr = byteArrayOutputStream.toString("ISO-8859-1");
            serStr = java.net.URLEncoder.encode(serStr, "UTF-8");

            objectOutputStream.close();
            byteArrayOutputStream.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return serStr;
    }

    //字符串反序列化为对象
    public static Object objectDeserialization(String serStr){
        Object newObj = null;
        try {
            String redStr = java.net.URLDecoder.decode(serStr, "UTF-8");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            newObj = objectInputStream.readObject();
            objectInputStream.close();
            byteArrayInputStream.close();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return newObj;
    }

    /**
     * 缓存生存时间
     */
    private final static int expire = 60000;

    /**
     * 获取连接时最大等待时间
     *
     * @return long 返回最大等待时间
     */
    public long getMaxBorrowWaitTimeMillis() {
        return jedisPool.getMaxBorrowWaitTimeMillis();
    }

    /**
     * 获取连接时平均等待时间
     *
     * @return long 返回平均等待时间
     */
    public long getMeanBorrowWaitTimeMillis() {
        return jedisPool.getMeanBorrowWaitTimeMillis();
    }

    /**
     * 获取jedis实例个数
     *
     * @return int 返回的个数
     */
    public int getNumActive() {
        return jedisPool.getNumActive();
    }

    /**
     * 获取空闲的jedis实例个数
     *
     * @return int 返回的个数
     */
    public int getNumIdle() {
        return jedisPool.getNumIdle();
    }

    /**
     * 获取等待的连接池个数
     *
     * @return int 返回的个数
     */
    public int getNumWaiters() {
        return jedisPool.getNumWaiters();
    }

    /**
     * 设置过期时间
     *
     * @param key
     *            要设置的过期时间的redis的键
     * @param seconds
     *            要设置的过期时间，以秒为单位
     * @throws Exception
     */
    public void expire(String key, int seconds) throws Exception {
        if (seconds <= 0) {
            return;
        }
        Jedis jedis = getJedis();
        try {
            jedis.expire(key, seconds);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
    }

    /**
     * 设置默认过期时间
     *
     * @param key
     *            要设置默认过期时间的redis的键
     * @throws Exception
     */
    public void expire(String key) throws Exception {
        expire(key, expire);
    }

// *******************************************Keys*******************************************//
    /**
     * 清空所有key
     *
     * @throws Exception
     */
    public String flushAll() throws Exception {
        Jedis jedis = getJedis();
        String stata = null;
        try {
            stata = jedis.flushAll();
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }

        return stata;
    }

    /**
     * 更改key
     *
     * @param oldkey
     *            待更新的键值
     * @param newkey
     *            新的键值
     * @return 状态码
     * @throws Exception
     */
    public String rename(String oldkey, String newkey) throws Exception {
        return rename(SafeEncoder.encode(oldkey), SafeEncoder.encode(newkey));
    }

    /**
     * 更改key,仅当新key不存在时才执行
     *
     * @param oldkey
     *            待更新的键值
     * @param newkey
     *            新的键值
     * @return 状态码
     * @throws Exception
     */
    public long renamenx(String oldkey, String newkey) throws Exception {
        Jedis jedis = getJedis();
        Long status = null;
        try {
            status = jedis.renamenx(oldkey, newkey);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return status;
    }

    /**
     * 更改key
     *
     * @param oldkey
     *            待更新的键值
     * @param newkey
     *            新的键值
     * @return 状态码
     * @throws Exception
     */
    public String rename(byte[] oldkey, byte[] newkey) throws Exception {
        Jedis jedis = getJedis();
        String status = null;
        try {
            status = jedis.rename(oldkey, newkey);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return status;
    }

    /**
     * 设置key的过期时间，以秒为单位
     *
     * @param key
     *            要设置过期时间对应的键
     * @param seconds
     *            时间,已秒为单位
     * @return 影响的记录数
     * @throws Exception
     */
    public long expired(String key, int seconds) throws Exception {
        Jedis jedis = getJedis();
        Long count = null;
        try {
            count = jedis.expire(key, seconds);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 设置key的过期时间,它是距历元（即格林威治标准时间 1970 年 1 月 1 日的 00:00:00，格里高利历）的偏移量。
     *
     * @param key
     *            要设置过期时间对应的键
     * @param timestamp
     *            时间,以秒为单位
     * @return 影响的记录数
     * @throws Exception
     */
    public long expireAt(String key, long timestamp) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.expireAt(key, timestamp);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 查询key的过期时间
     *
     * @param key
     *            要查询过期时间对应的键
     * @return 以秒为单位的时间表示
     * @throws Exception
     */
    public long ttl(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.ttl(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 取消对key过期时间的设置
     *
     * @param key
     *            要取消过期时间对应的键
     * @return 影响的记录数
     * @throws Exception
     */
    public long persist(String key) throws Exception {
        Jedis jedis = getJedis();
        long count = jedis.persist(key);
        try {
            count = jedis.ttl(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 删除keys对应的记录,可以是多个key
     *
     * @param keys
     * @return 删除的记录数
     * @throws Exception
     */
    public long del(String... keys) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.del(keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 删除keys对应的记录,可以是多个key
     *
     * @param keys
     *            要删除的记录所对应的键，可以是多个
     * @return 删除的记录数
     * @throws Exception
     */
    public long del(byte[]... keys) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.del(keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 对List,Set,SortSet进行排序,如果集合数据较大应避免使用这个方法
     *
     * @param key
     * @return List<String> 集合的全部记录
     * @throws Exception
     **/
    public List<String> sort(String key) throws Exception {
        Jedis jedis = getJedis();
        List<String> list = null;
        try {
            list = jedis.sort(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    /**
     * 对List,Set,SortSet进行排序或limit
     *
     * @param key
     *            要排序的数据类型所对应的键
     * @param parame
     *            定义排序类型或limit的起止位置.
     * @return List<String> 全部或部分记录
     * @throws Exception
     **/
    public List<String> sort(String key, SortingParams parame) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        List<String> list = null;
        try {
            list = jedis.sort(key, parame);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    /**
     * 返回指定key存储的类型
     *
     * @param key
     * @return String string|list|set|zset|hash
     * @throws Exception
     **/
    public String type(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        String type = null;
        try {
            type = jedis.type(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return type;
    }

    /**
     * 查找所有匹配给定的模式的键
     *
     * @param pattern
     *            key的表达式,*表示多个，？表示一个
     * @throws Exception
     */
    public Set<String> keys(String pattern) throws Exception {
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.keys(pattern);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 删除redis中整个库
     *
     * @throws Exception
     */
    public String flush() throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        String success = jedis.flushDB();
        // 回收jedis连接
        returnResource(jedis);
        return success;

    }

    /**
     * 向redis中插入简单String键值对
     *
     * @param key
     *            : String 要插入redis的键
     * @param value
     *            : String 要插入redis的值
     * @return call : String 返回OK 或 ERR
     * @throws Exception
     */
    public String setString(String key, String value) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        // jedis的set
        String call = jedis.set(key, value);
        // 回收jedis连接
        returnResource(jedis);
        // 返回操作成功或失败信息
        return call;

    }

    /**
     * 使用redis的get方法
     *
     * @param key
     *            : String 要取出redis的键
     * @return value : String 要取出redis的值
     * @throws Exception
     */
    public String getString(String key) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        // jedis的get
        String value = jedis.get(key);
        // 回收jedis连接
        returnResource(jedis);
        // 返回value
        return value;

    }

    /**
     * 将整型键值递增
     *
     * @param key
     *            : String 要自增的键值
     * @return result : long 返回自增后的值
     * @throws Exception
     *
     */
    public long incr(String key) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        long result = 0;
        if (key != null) {
            result = jedis.incr(key);
        }
        // 回收jedis连接
        returnResource(jedis);
        return result;

    }

    /**
     * 将整型键值由给定的值递增
     *
     * @param key
     *            : String 要自增的键值 value : long 给定的增加的值
     * @return result : long 返回自增后的值
     * @throws Exception
     *
     */
    public long incrby(String key, long value) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        long result = 0;
        if (key != null) {
            result = jedis.incrBy(key, value);
        }
        // 回收jedis连接
        returnResource(jedis);
        return result;

    }

    /**
     * 将整型键值递减
     *
     * @param key
     *            : String 要自减的键值
     * @return result : long 返回自减后的值
     * @throws Exception
     *
     */
    public long decr(String key) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        long result = 0;
        if (key != null) {
            result = jedis.decr(key);
        }
        // 回收jedis连接
        returnResource(jedis);
        return result;

    }

    /**
     * 将整型键值由给定的值递减
     *
     * @param key
     *            : String 要自减的键值 value : long 给定的减少的值
     * @return result : long 返回自减后的值
     * @throws Exception
     *
     */
    public long decrby(String key, long value) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        long result = 0;
        if (key != null) {
            result = jedis.decrBy(key, value);
        }
        // 回收jedis连接
        returnResource(jedis);
        return result;

    }

    /**
     * 判断redis中存在某个键值对，包括任意数据结构
     *
     * @param key
     *            : String 寻找的键值对的键
     * @return exist : boolean 返回是否存在键值是参数key的键值对
     * @throws Exception
     */
    public boolean exists(String key) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        boolean exist = jedis.exists(key);
        // 回收jedis连接
        returnResource(jedis);
        return exist;

    }

    /**
     * 删除redis中的某几个存储键值对
     *
     * @param keys
     *            : String[] 要删除的那些键值对的键
     * @throws Exception
     * @return delNum : long 成功删除的键值对数量
     */
    public long delete(String... keys) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        long delNum = jedis.del(keys);
        // 回收jedis连接
        returnResource(jedis);
        return delNum;

    }
    /**
     * 更改key
     *
     * @param oldkey
     * @param newkey
     * @return 状态码
     * @throws Exception
     */
    // public String rename(String oldkey, String newkey) throws Exception {
    // Jedis jedis = getJedis();
    // String status = null;
    // status = jedis.rename(oldkey, newkey);
    // returnResource(jedis);
    // return status;
    // }

    /**
     * 使用redis的map存储结构
     *
     * @param key
     *            : String 对应map的名称键值
     * @param map
     *            : Map 对应要插入redis数据库的map
     * @return call : String 返回OK 或 ERR
     * @throws Exception
     */
    public String setMap(String key, HashMap<String, String> hashMap) throws Exception {

        if (null != hashMap) {// hashMap是非空参数

            // 获得jedis连接
            Jedis jedis = getJedis();
            // 插入hashmap
            String call = jedis.hmset(key, hashMap);
            // 回收jedis连接
            returnResource(jedis);
            // 返回操作成功或失败信息
            return call;

        } else {// hashMap是空参数

            return "插入失败，hashMap是空参数";

        }

    }

    /**
     * 使用pipelining管道向redis批量插入map数据结构
     *
     * @param map
     *            : Map<String, Map> 对应要插入redis数据库的map的键值对Map，
     *            其中key代表插入redis的map数据结构的键，value代表插入redis的map数据结构
     * @throws Exception
     */
    public void pipeLinedHmset(Map<String, Map> map) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        // 获取pipeline实例
        Pipeline pipe = jedis.pipelined();
        // 遍历map
        if (null != map) {
            Iterator<Entry<String, Map>> iter = map.entrySet().iterator();
            while (iter.hasNext()) {
                Entry<String, Map> entry = iter.next();
                // 向redis中插入map数据结构的脚本
                pipe.hmset(entry.getKey(), entry.getValue());
            }
        }
        // 一次性提交所有脚本
        pipe.sync();
        // 回收jedis连接
        returnResource(jedis);

    }

    /**
     * 使用pipelining批量删除redis中一系列map的一个键值对
     *
     * @param map
     *            : Map<String, String> map的key映射redis中操作的map的键<key,field>，
     *            map的value映射redis操作的map中要删除的键
     * @throws Exception
     */
    public void pipeLinedHDel(Map<String, String> map) throws Exception {
        // 获取jedis连接
        Jedis jedis = getJedis();
        // 获取pipeline实例
        Pipeline pipe = jedis.pipelined();
        // 遍历map
        Iterator<Entry<String, String>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            pipe.hdel(entry.getKey(), entry.getValue());
        }
        // 一次性提交所有脚本
        pipe.sync();
        // 回收jedis连接
        returnResource(jedis);
    }

    /**
     * 删除map中的一些键值对
     *
     * @param :
     *            选择要操作的数据库
     * @param key
     *            : String 要操作的map在redis中的键
     * @param fields
     *            : String[] 要删除的map中的那些键值对的键
     * @return delNum : long map中被删除的键值对的数量
     * @throws Exception
     */
    public long redisHdel(String key, String... fields) throws Exception {
        // 获取jedis连接
        Jedis jedis = getJedis();
        long delNum = jedis.hdel(key, fields);
        // 回收jedis连接
        returnResource(jedis);
        return delNum;
    }

    /**
     * 判断redis中的map中存在某个键值对
     *
     * @param key
     *            : String 要操作的map在redis中的键
     * @param field
     *            : String 要查找的map中的键值对的键
     * @param exist
     *            : boolean 返回map中是否存在要查找的键值对
     * @throws Exception
     */
    public boolean redisHExists(String key, String field) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        boolean exist = jedis.hexists(key, field);
        // 回收jedis连接
        returnResource(jedis);
        return exist;

    }

    /**
     * 获取redis中存储的某个map
     *
     * @param key
     *            : String 对应map的名称键值
     * @return list : List 对应要取出的map的value列表
     * @throws Exception
     */
    public Map getMap(String key) throws Exception {

        Map<String, String> map = new HashMap<String, String>();
        // 获取jedis连接
        Jedis jedis = getJedis();
        // 获取redis中的键为key的hmap
        map = jedis.hgetAll(key);
        // 回收jedis连接
        returnResource(jedis);
        // 返回map
        return map;

    }

    /*
     * 使用redis自带的list数据结构
     *
     */

    /**
     * 逐个向list头部（左侧）插入列表元素， redis中如果不存在键是key的list，redis就会自动创建该list
     *
     * @param key
     *            : String 要创建或要插入的list的键
     * @param list
     *            : ArrayList<String> 要插入redis的list
     * @throws Exception
     */
    public void redisLpush(String key, ArrayList<String> list) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                jedis.lpush(key, list.get(i).toString());
            }
        }
        // 回收jedis连接
        returnResource(jedis);
    }

    /**
     * 逐个向list尾部（右侧）插入列表元素， redis中如果不存在键是key的list，redis就会自动创建该list
     *
     * @param key
     *            : String 要创建或要插入的list的键
     * @param list
     *            : ArrayList<String> 要插入redis的list
     * @throws Exception
     */
    public void redisRpush(String key, ArrayList<String> list) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        // 获取pipeline实例
        Pipeline pipe = jedis.pipelined();
        // 遍历list
        if (null != list) {
            for (int i = 0; i < list.size(); i++) {
                pipe.rpush(key, list.get(i).toString());
            }
        }
        // 一次性提交所有脚本
        pipe.sync();
        // 回收jedis连接
        returnResource(jedis);

    }

    /**
     * 删除list头部（左侧）元素
     *
     * @param :
     *            选择要操作的数据库
     * @param key
     *            : String 要操作的list的键
     * @return elemDel : String 删除的list中的元素
     * @throws Exception
     */
    public String redisLpop(String key) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        String elemDel = jedis.lpop(key);
        // 回收jedis连接
        returnResource(jedis);
        return elemDel;

    }

    /**
     * 删除list尾部（右侧）元素
     *
     * @param :
     *            选择要操作的数据库
     * @param key
     *            : String 要操作的list的键
     * @return elemDel : String 删除的list中的元素
     * @throws Exception
     */
    public String redisRpop(String key) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        String elemDel = jedis.rpop(key);
        // 回收jedis连接
        returnResource(jedis);
        return elemDel;

    }

    /**
     * 从redis中拿出某个list
     *
     * @param key
     *            : String 要获取的list的键
     * @param start
     *            : long 取list起始行位置
     * @param end
     *            : long 取list终止行位置，默认-1表示取所有行
     * @return list : List 返回从redis中读取的list数据结构对象
     * @throws Exception
     */
    public List getList(String key, long start, long end) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        List list = jedis.lrange(key, start, end);
        // 回收jedis连接
        returnResource(jedis);
        return list;

    }

    /*
     * pipelining管道
     *
     * 使用管道类似于事物，先收集要执行的脚本，一次性执行， 可以减少很多创建连接的动作，节省时间，
     * 但也有局限，当一次性读写的数量巨大时，尽量分批进行，防止stackoverflow
     *
     *
     *
     */

// *******************************************String*******************************************//
    /**
     * 根据key获取记录
     *
     * @param key
     *            指定的key
     * @return 取得的值
     * @throws Exception
     */
    public String get(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        String value = null;
        try {
            value = jedis.get(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 根据key获取记录
     *
     * @param key
     *            指定的key
     * @return 取得的值
     * @throws Exception
     */
    public byte[] get(byte[] key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        byte[] value = null;
        try {
            value = jedis.get(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 添加有过期时间的记录
     *
     * @param key
     *            要添加的key
     * @param seconds
     *            过期时间，以秒为单位
     * @param value
     *            要添加的value
     * @return String 操作状态
     * @throws Exception
     */
    public String setEx(String key, int seconds, String value) throws Exception {
        Jedis jedis = getJedis();
        String str = null;
        try {
            str = jedis.setex(key, seconds, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 添加有过期时间的记录
     *
     * @param key
     *            要添加的key
     * @param seconds
     *            过期时间，以秒为单位
     * @param value
     *            要添加的value
     * @return String 操作状态
     * @throws Exception
     */
    public String setEx(byte[] key, int seconds, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        String str = null;
        try {
            str = jedis.setex(key, seconds, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 添加一条记录，仅当给定的key不存在时才插入
     *
     * @param key
     *            要添加的key
     * @param value
     *            要添加的value
     * @return long 状态码，1插入成功且key不存在，0未插入，key存在
     * @throws Exception
     */
    public long setnx(String key, String value) throws Exception {
        Jedis jedis = getJedis();
        long str = 0;
        try {
            str = jedis.setnx(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 添加记录,如果记录已存在将覆盖原有的value
     *
     * @param key
     *            要添加的key
     * @param value
     *            要添加的value
     * @return 状态码
     * @throws Exception
     */
    public String set(String key, String value) throws Exception {
        return set(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    /**
     * 添加记录,如果记录已存在将覆盖原有的value
     *
     * @param key
     *            要添加的key
     * @param value
     *            要添加的value
     * @return 状态码
     * @throws Exception
     */
    public String set(String key, byte[] value) throws Exception {
        return set(SafeEncoder.encode(key), value);
    }

    /**
     * 添加记录,如果记录已存在将覆盖原有的value
     *
     * @param key
     *            要添加的key
     * @param value
     *            要添加的value
     * @return 状态码
     * @throws Exception
     */
    public String set(byte[] key, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        String status = null;
        try {
            status = jedis.set(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return status;
    }

    /**
     * 从指定位置开始插入数据，插入的数据会覆盖指定位置以后的数据<br/>
     * 例:String str1="123456789";<br/>
     * 对str1操作后setRange(key,4,0000)，str1="123400009";
     *
     * @param key
     *            要添加的key
     * @param offset
     *            开始添加的位置
     * @param value
     *            要添加的value
     * @return long value的长度
     * @throws Exception
     */
    public long setRange(String key, long offset, String value) throws Exception {
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.setrange(key, offset, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 在指定的key中追加value
     *
     * @param key
     *            指定的key
     * @param value
     *            要添加的value
     * @return long 追加后value的长度
     * @throws Exception
     **/
    public long append(String key, String value) throws Exception {
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.append(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 将key对应的value减去指定的值，只有value可以转为数字时该方法才可用
     *
     * @param key
     *            指定的key
     * @param number
     *            要减去的值
     * @return long 减指定值后的值
     * @throws Exception
     */
    public long decrBy(String key, long number) throws Exception {
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.decrBy(key, number);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * <b>可以作为获取唯一id的方法</b><br/>
     * 将key对应的value加上指定的值，只有value可以转为数字时该方法才可用
     *
     * @param key
     *            指定的key
     * @param number
     *            要减去的值
     * @return long 相加后的值
     * @throws Exception
     */
    public long incrBy(String key, long number) throws Exception {
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.incrBy(key, number);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 对指定key对应的value进行截取
     *
     * @param key
     *            指定的key
     * @param startOffset
     *            开始位置(包含)
     * @param endOffset
     *            结束位置(包含)
     * @return String 截取的值
     * @throws Exception
     */
    public String getrange(String key, long startOffset, long endOffset) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        String value = null;
        try {
            value = jedis.getrange(key, startOffset, endOffset);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 获取并设置指定key对应的value<br/>
     * 如果key存在返回之前的value,否则返回null
     *
     * @param key
     *            指定的key
     * @param value
     *            指定的value
     * @return String 原始value或null
     * @throws Exception
     */
    public String getSet(String key, String value) throws Exception {
        Jedis jedis = getJedis();
        String str = null;
        try {
            str = jedis.getSet(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 批量获取记录,如果指定的key不存在返回List的对应位置将是null
     *
     * @param keys
     *            指定的key集合
     * @return List<String> 值得集合
     * @throws Exception
     */
    public List<String> mget(String... keys) throws Exception {
        Jedis jedis = getJedis();
        List<String> str = null;
        try {
            str = jedis.mget(keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 批量存储记录
     *
     * @param keysvalues
     *            例:keysvalues="key1","value1","key2","value2";
     * @return String 状态码
     * @throws Exception
     */
    public String mset(String... keysvalues) throws Exception {
        Jedis jedis = getJedis();
        String str = null;
        try {
            str = jedis.mset(keysvalues);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 使用pipelining管道批量执行redis写入简单String键值对
     *
     * @param map
     *            : Map 要批量插入redis的map,map的键就是要插入redis中的key，
     *            map的值就是要插入redis的简单String值
     * @throws Exception
     */
    public void pipeLinedSetString(Map map) throws Exception {

        // 获取jedis链接
        Jedis jedis = getJedis();
        // 获取pipeline实例
        Pipeline pipe = jedis.pipelined();
        // 遍历map
        try {
            if (null != map) {
                Iterator<Entry> iter = map.entrySet().iterator();
                while (iter.hasNext()) {
                    // 用迭代器遍历map
                    Entry entry = iter.next();
                    // 向redis插入String键值对脚本
                    pipe.set((String) entry.getKey(), (String) entry.getValue());
                }
            }

            // 一次性提交所有脚本
            pipe.sync();
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            // 回收jedis连接
            returnResource(jedis);
        }
    }

    /**
     * 使用pipelining管道从redis批量读取简单String键值对
     *
     * @param list
     *            : List 传入要读取键值对的键列表
     * @return map : Map 返回从redis读出的键值对
     * @throws Exception
     */
    public Map pipeLinedGetString(List list) throws Exception {
        // 获取jedis连接
        Jedis jedis = getJedis();
        // 获取pipeline实例
        Pipeline pipe = jedis.pipelined();
        // 返回的map
        Map map = new HashMap();
        try {
            // 遍历list
            if (null != list) {
                for (int i = 0; i < list.size(); i++) {
                    Response<String> response = pipe.get((String) list.get(i));
                    pipe.sync();
                    map.put((String) list.get(i), response.get());
                }
            }
            // 一次性提交所有脚本
            // pipe.sync();//写在这里报错
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            // 回收jedis连接
            returnResource(jedis);
        }
        return map;
    }

    /**
     * 获取key对应的值的长度
     *
     * @param key
     *            指定的key
     * @return value值得长度
     * @throws Exception
     */
    public long strlen(String key) throws Exception {
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.strlen(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

// *******************************************Lists*******************************************//
    /**
     * 使用pipelining管道批量执行redis删除键值对，包括任意redis存储结构
     *
     * @param list
     *            : List 传入要删除的key列表
     * @throws Exception
     */
    public void pipeLinedDel(List list) throws Exception {

        // 获取jedis连接
        Jedis jedis = getJedis();
        // 获取pipeline实例
        Pipeline pipe = jedis.pipelined();
        try {
            // 遍历list
            if (null != list) {
                for (int i = 0; i < list.size(); i++) {
                    // 从redis删除键值对脚本
                    pipe.del((String) list.get(i));
                }
            }
            // 一次性提交所有脚本
            pipe.sync();
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            // 回收jedis连接
            returnResource(jedis);
        }
    }

    /**
     * List长度
     *
     * @param key
     *            指定的key
     * @return 长度
     * @throws Exception
     */
    public long llen(String key) throws Exception {
        return llen(SafeEncoder.encode(key));
    }

    /**
     * List长度
     *
     * @param key
     *            指定的key
     * @return 长度
     * @throws Exception
     */
    public long llen(byte[] key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.llen(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 覆盖操作,将覆盖List中指定位置的值
     *
     * @param key
     *            指定的key
     * @param index
     *            位置
     * @param value
     *            值
     * @return 状态码
     * @throws Exception
     */
    public String lset(byte[] key, int index, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        String status = null;
        try {
            status = jedis.lset(key, index, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return status;
    }

    /**
     * 覆盖操作,将覆盖List中指定位置的值
     *
     * @param key
     * @param index
     *            位置
     * @param value
     *            值
     * @return 状态码
     * @throws Exception
     */
    public String lset(String key, int index, String value) throws Exception {
        return lset(SafeEncoder.encode(key), index, SafeEncoder.encode(value));
    }

    /**
     * 在value的相对位置插入记录
     *
     * @param key
     *            指定的key
     * @param where
     *            前面插入或后面插入
     * @param pivot
     *            相对位置的内容
     * @param value
     *            插入的内容
     * @return 记录总数
     * @throws Exception
     */
    public long linsert(String key, LIST_POSITION where, String pivot, String value) throws Exception {
        return linsert(SafeEncoder.encode(key), where, SafeEncoder.encode(pivot), SafeEncoder.encode(value));
    }

    /**
     * 在指定位置插入记录
     *
     * @param key
     * @param where
     *            前面插入或后面插入
     * @param pivot
     *            相对位置的内容
     * @param value
     *            插入的内容
     * @return 记录总数
     * @throws Exception
     */
    public long linsert(byte[] key, LIST_POSITION where, byte[] pivot, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.linsert(key, where, pivot, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 获取List中指定位置的值
     *
     * @param key
     *            指定的key
     * @param index
     *            位置
     * @return 指定key所在的位置值
     * @throws Exception
     **/
    public String lindex(String key, int index) throws Exception {
        return SafeEncoder.encode(lindex(SafeEncoder.encode(key), index));
    }

    /**
     * 获取List中指定位置的值
     *
     * @param key
     * @param index
     *            位置
     * @return 值
     * @throws Exception
     **/
    public byte[] lindex(byte[] key, int index) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        byte[] value = null;
        try {
            value = jedis.lindex(key, index);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 将List中的第一条记录移出List
     *
     * @param key
     * @return 移出的记录
     * @throws Exception
     */
    public String lpop(String key) throws Exception {
        return SafeEncoder.encode(lpop(SafeEncoder.encode(key)));
    }

    /**
     * 将List中的第一条记录移出List
     *
     * @param key
     * @return 移出的记录
     * @throws Exception
     */
    public byte[] lpop(byte[] key) throws Exception {
        Jedis jedis = getJedis();
        byte[] value = null;
        try {
            value = jedis.lpop(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 将List中最后第一条记录移出List
     *
     * @param key
     * @return 移出的记录
     * @throws Exception
     */
    public String rpop(String key) throws Exception {
        Jedis jedis = getJedis();
        String value = null;
        try {
            value = jedis.rpop(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 向List尾部追加记录
     *
     * @param key
     *            指定的key
     * @param value
     *            添加的value
     * @return 记录总数
     * @throws Exception
     */
    public long lpush(String key, String value) throws Exception {
        return lpush(SafeEncoder.encode(key), SafeEncoder.encode(value));
    }

    /**
     * 向List头部追加记录
     *
     * @param key
     *            指定的key
     * @param value
     *            添加的value
     * @return 记录总数
     * @throws Exception
     */
    public long rpush(String key, String value) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.rpush(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 向List头部追加记录
     *
     * @param key
     *            指定的key
     * @param value
     *            添加的value
     * @return 记录总数
     * @throws Exception
     */
    public long rpush(byte[] key, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.rpush(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 向List中追加记录
     *
     * @param key
     *            指定的key
     * @param value
     *            添加的value
     * @return 记录总数
     * @throws Exception
     */
    public long lpush(byte[] key, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.lpush(key, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 获取指定范围的记录，可以做为分页使用
     *
     * @param key
     *            指定的key
     * @param start
     *            开始位置
     * @param end
     *            结束位置，如果为负数，则尾部开始计算
     * @return List 返回查询的记录
     * @throws Exception
     */
    public List<String> lrange(String key, long start, long end) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        List<String> list = null;
        try {
            list = jedis.lrange(key, start, end);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    /**
     * 获取指定范围的记录，可以做为分页使用
     *
     * @param key
     *            指定的key
     * @param start
     *            开始位置
     * @param end
     *            结束位置，如果为负数，则尾部开始计算
     * @return List 返回查询的记录
     * @throws Exception
     */
    public List<byte[]> lrange(byte[] key, int start, int end) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        List<byte[]> list = null;
        try {
            list = jedis.lrange(key, start, end);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    /**
     * 删除List中c条记录，被删除的记录值为value
     *
     * @param key
     * @param c
     *            要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
     * @param value
     *            要匹配的值
     * @return 删除后的List中的记录数
     * @throws Exception
     */
    public long lrem(byte[] key, int c, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        long count = 0;
        try {
            count = jedis.lrem(key, c, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return count;
    }

    /**
     * 删除List中c条记录，被删除的记录值为value
     *
     * @param key
     * @param c
     *            要删除的数量，如果为负数则从List的尾部检查并删除符合的记录
     * @param value
     *            要匹配的值
     * @return 删除后的List中的记录数
     * @throws Exception
     */
    public long lrem(String key, int c, String value) throws Exception {
        return lrem(SafeEncoder.encode(key), c, SafeEncoder.encode(value));
    }

    /**
     * 算是删除吧，只保留start与end之间的记录
     *
     * @param key
     * @param start
     *            记录的开始位置(0表示第一条记录)
     * @param end
     *            记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
     * @return 执行状态码
     * @throws Exception
     */
    public String ltrim(byte[] key, int start, int end) throws Exception {
        Jedis jedis = getJedis();
        String str = null;
        try {
            str = jedis.ltrim(key, start, end);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return str;
    }

    /**
     * 算是删除吧，只保留start与end之间的记录
     *
     * @param key
     *            指定的key
     * @param start
     *            记录的开始位置(0表示第一条记录)
     * @param end
     *            记录的结束位置（如果为-1则表示最后一个，-2，-3以此类推）
     * @return 执行状态码
     * @throws Exception
     */
    public String ltrim(String key, int start, int end) throws Exception {
        return ltrim(SafeEncoder.encode(key), start, end);
    }

// *******************************************Sets*******************************************//

    /**
     * 向Set添加一条记录，如果member已存在返回0,否则返回1
     *
     * @param key
     *            要添加的键
     * @param member
     *            要添加的member
     * @return 操作码, 0或1
     * @throws Exception
     */
    public long sadd(String key, String member) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.sadd(key, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 获取给定key中元素个数
     *
     * @param key
     *            指定的键
     * @return 元素个数
     * @throws Exception
     */
    public long scard(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        long len = jedis.scard(key);
        try {
            len = jedis.scard(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 返回从第一组和所有的给定集合之间的差异的成员
     *
     * @param keys
     *            给定的键集合
     * @return 差异的成员集合
     * @throws Exception
     */
    public Set<String> sdiff(String... keys) throws Exception {
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.sdiff(keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 这个命令等于sdiff,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
     *
     * @param newkey
     *            新结果集的key
     * @param keys
     *            比较的集合
     * @return 新集合中的记录数
     * @throws Exception
     **/
    public long sdiffstore(String newkey, String... keys) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.sdiffstore(newkey, keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 返回给定集合交集的成员,如果其中一个集合为不存在或为空，则返回空Set
     *
     * @param keys
     *            求交集的key集合
     * @return 交集成员的集合
     * @throws Exception
     **/
    public Set<String> sinter(String... keys) throws Exception {
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.sinter(keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 这个命令等于sinter,但返回的不是结果集,而是将结果集存储在新的集合中，如果目标已存在，则覆盖。
     *
     * @param newkey
     *            新结果集的key
     * @param keys
     *            比较的集合
     * @return 新集合中的记录数
     * @throws Exception
     **/
    public long sinterstore(String newkey, String... keys) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.sinterstore(newkey, keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 确定一个给定的值是否存在
     *
     * @param key
     *            指定的键
     * @param member
     *            要判断的值
     * @return 存在返回1，不存在返回0
     * @throws Exception
     **/
    public boolean sismember(String key, String member) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        boolean s = false;
        try {
            s = jedis.sismember(key, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 返回集合中的所有成员
     *
     * @param key
     *            指定的key
     * @return 成员集合
     * @throws Exception
     */
    public Set<String> smembers(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.smembers(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 返回集合中的所有成员
     *
     * @param key
     *            指定的key
     * @return 成员集合
     * @throws Exception
     */
    public Set<byte[]> smembers(byte[] key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Set<byte[]> set = null;
        try {
            set = jedis.smembers(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 将成员从源集合移出放入目标集合 <br/>
     * 如果源集合不存在或不包哈指定成员，不进行任何操作，返回0<br/>
     * 否则该成员从源集合上删除，并添加到目标集合，如果目标集合中成员已存在，则只在源集合进行删除
     *
     * @param srckey
     *            源集合
     * @param dstkey
     *            目标集合
     * @param member
     *            源集合中的成员
     * @return 状态码，1成功，0失败
     * @throws Exception
     */
    public long smove(String srckey, String dstkey, String member) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.smove(srckey, dstkey, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     *            指定的键
     * @return 被删除的成员
     * @throws Exception
     */
    public String spop(String key) throws Exception {
        Jedis jedis = getJedis();
        String s = null;
        try {
            s = jedis.spop(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 从集合中删除指定成员
     *
     * @param key
     * @param member
     *            要删除的成员
     * @return 状态码，成功返回1，成员不存在返回0
     * @throws Exception
     */
    public long srem(String key, String member) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.srem(key, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 合并多个集合并返回合并后的结果，合并后的结果集合并不保存<br/>
     *
     * @param keys
     *            要合并的键的集合
     * @return 合并后的结果集合
     * @throws Exception
     */
    public Set<String> sunion(String... keys) throws Exception {
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.sunion(keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 合并多个集合并将合并后的结果集保存在指定的新集合中，如果新集合已经存在则覆盖
     *
     * @param newkey
     *            新集合的key
     * @param keys
     *            要合并的集合
     * @throws Exception
     **/
    public long sunionstore(String newkey, String... keys) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.sunionstore(newkey, keys);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

// *******************************************SortSet*******************************************//

    /**
     * 向集合中增加一条记录,如果这个值已存在，这个值对应的权重将被置为新的权重
     *
     * @param key
     *            要添加的键
     * @param score
     *            权重
     * @param member
     *            要加入的值，
     * @return 状态码 1成功，0已存在member的值
     * @throws Exception
     */
    public long zadd(String key, double score, String member) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.zadd(key, score, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 获取集合中元素的数量
     *
     * @param key
     *            指定的键
     * @return 如果返回0则集合不存在
     * @throws Exception
     */
    public long zcard(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.zcard(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 获取指定权重区间内集合的数量
     *
     * @param key
     *            指定的键
     * @param min
     *            最小排序位置
     * @param max
     *            最大排序位置
     * @throws Exception
     */
    public long zcount(String key, double min, double max) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.zcount(key, min, max);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 获得set的长度
     *
     * @param key
     *            指定的键
     * @return set的长度
     * @throws Exception
     */
    public long zlength(String key) throws Exception {
        long len = 0;
        Set<String> set = zrange(key, 0, -1);
        len = set.size();
        return len;
    }

    /**
     * 权重增加给定值，如果给定的member已存在
     *
     * @param key
     *            指定的键
     * @param score
     *            要增的权重
     * @param member
     *            要插入的值
     * @return 增后的权重
     * @throws Exception
     */
    public double zincrby(String key, double score, String member) throws Exception {
        Jedis jedis = getJedis();
        double s = 0;
        try {
            s = jedis.zincrby(key, score, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 返回指定位置的集合元素,0为第一个元素，-1为最后一个元素
     *
     * @param key
     *            指定的键
     * @param start
     *            开始位置(包含)
     * @param end
     *            结束位置(包含)
     * @return Set<String>
     * @throws Exception
     */
    public Set<String> zrange(String key, int start, int end) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.zrange(key, start, end);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 返回指定权重区间的元素集合
     *
     * @param key
     *            指定的键
     * @param min
     *            上限权重
     * @param max
     *            下限权重
     * @return Set<String>
     * @throws Exception
     */
    public Set<String> zrangeByScore(String key, double min, double max) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.zrangeByScore(key, min, max);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 获取指定值在集合中的位置，集合排序从低到高
     *
     * @param key
     *            指定的键
     * @param member
     * @return long 位置
     * @throws Exception
     */
    public long zrank(String key, String member) throws Exception {
        Jedis jedis = getJedis();
        long index = 0;
        try {
            index = jedis.zrank(key, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return index;
    }

    /**
     * 获取指定值在集合中的位置，集合排序从高到低
     *
     * @param key
     *            指定的键
     * @param member
     * @return long 位置
     * @throws Exception
     */
    public long zrevrank(String key, String member) throws Exception {
        Jedis jedis = getJedis();
        long index = 0;
        try {
            index = jedis.zrevrank(key, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return index;
    }

    /**
     * 从集合中删除成员
     *
     * @param key
     *            指定的键
     * @param member
     * @return 返回1成功
     * @throws Exception
     */
    public long zrem(String key, String member) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.zrem(key, member);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 删除
     *
     * @param key
     *            要删除的对应的键
     * @return
     * @throws Exception
     */
    public long zrem(String key) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.del(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 删除给定位置区间的元素
     *
     * @param key
     *            指定的键
     * @param start
     *            开始区间，从0开始(包含)
     * @param end
     *            结束区间,-1为最后一个元素(包含)
     * @return 删除的数量
     * @throws Exception
     */
    public long zremrangeByRank(String key, int start, int end) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.zremrangeByRank(key, start, end);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 删除给定权重区间的元素
     *
     * @param key
     * @param min
     *            下限权重(包含)
     * @param max
     *            上限权重(包含)
     * @return 删除的数量
     * @throws Exception
     */
    public long zremrangeByScore(String key, double min, double max) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.zremrangeByScore(key, min, max);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 获取给定区间的元素，原始按照权重由高到低排序
     *
     * @param key
     *            指定的键
     * @param start
     *            开始位置
     * @param end
     *            结束位置
     * @return Set<String>
     * @throws Exception
     */
    public Set<String> zrevrange(String key, int start, int end) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.zrevrange(key, start, end);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 获取给定值在集合中的权重
     *
     * @param key
     *            指定的键
     * @param memebr
     *            指定的memebr
     * @return double 权重
     * @throws Exception
     */
    public double zscore(String key, String memebr) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Double score = null;
        try {
            score = jedis.zscore(key, memebr);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        if (score != null)
            return score;
        return 0;
    }

// *******************************************Hash*******************************************//

    /**
     * 从hash中删除指定的存储
     *
     * @param key
     *            指定的键
     * @param fieid
     *            存储的名字
     * @return 状态码，1成功，0失败
     * @throws Exception
     */
    public long hdel(String key, String fieid) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.hdel(key, fieid);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    public long hdel(String key) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.del(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 测试hash中指定的存储是否存在
     *
     * @param key
     * @param fieid
     *            存储的名字
     * @return 1存在，0不存在
     * @throws Exception
     */
    public boolean hexists(String key, String fieid) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        boolean s = false;
        try {
            s = jedis.hexists(key, fieid);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 返回hash中指定存储位置的值
     *
     * @param key
     * @param fieid
     *            存储的名字
     * @return 存储对应的值
     * @throws Exception
     */
    public String hget(String key, String fieid) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        String s = jedis.hget(key, fieid);
        try {
            s = jedis.hget(key, fieid);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 从hash中删除指定的存储
     *
     * @param key
     *            指定的键
     * @param fieid
     *            存储的名字
     * @return 状态码，1成功，0失败
     * @throws Exception
     */
    public byte[] hget(byte[] key, byte[] fieid) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        byte[] s = null;
        try {
            s = jedis.hget(key, fieid);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 以Map的形式返回hash中的存储和值
     *
     * @param key
     * @return Map<Strinig,String>
     * @throws Exception
     */
    public Map<String, String> hgetAll(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Map<String, String> map = null;
        try {
            map = jedis.hgetAll(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return map;
    }

    /**
     * 添加一个对应关系
     *
     * @param key
     *            要添加的key
     * @param fieid
     *            要添加的field
     * @param value
     *            要添加的value
     * @return 状态码 1成功，0失败，fieid已存在将更新，也返回0
     * @throws Exception
     **/
    public long hset(String key, String fieid, String value) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.hset(key, fieid, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 添加一个对应关系
     *
     * @param key
     *            要添加的key
     * @param fieid
     *            要添加的field
     * @param value
     *            要添加的value
     * @return 状态码 1成功，0失败，fieid已存在将更新，也返回0
     * @throws Exception
     **/
    public long hset(String key, String fieid, byte[] value) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.hset(key.getBytes(), fieid.getBytes(), value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 添加对应关系，只有在fieid不存在时才执行
     *
     * @param key
     *            要添加的key
     * @param fieid
     *            要添加的field
     * @param value
     *            要添加的value
     * @return 状态码 1成功，0失败fieid已存
     * @throws Exception
     **/
    public long hsetnx(String key, String fieid, String value) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.hsetnx(key, fieid, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 获取hash中value的集合
     *
     * @param key
     *            指定的key
     * @return List<String>
     * @throws Exception
     */
    public List<String> hvals(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        List<String> list = null;
        try {
            list = jedis.hvals(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    /**
     * 在指定的存储位置加上指定的数字，存储位置的值必须可转为数字类型
     *
     * @param key
     *            指定的key
     * @param fieid
     *            存储位置
     * @param value
     *            要增加的值,可以是负数
     * @return 增加指定数字后，存储位置的值
     * @throws Exception
     */
    public long hincrby(String key, String fieid, long value) throws Exception {
        Jedis jedis = getJedis();
        long s = 0;
        try {
            s = jedis.hincrBy(key, fieid, value);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 返回指定hash中的所有存储名字,类似Map中的keySet方法
     *
     * @param key
     *            指定的key
     * @return Set<String> 存储名称的集合
     * @throws Exception
     */
    public Set<String> hkeys(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        Set<String> set = null;
        try {
            set = jedis.hkeys(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return set;
    }

    /**
     * 获取hash中存储的个数，类似Map中size方法
     *
     * @param key
     *            指定的key
     * @return long 存储的个数
     * @throws Exception
     */
    public long hlen(String key) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        long len = 0;
        try {
            len = jedis.hlen(key);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return len;
    }

    /**
     * 根据多个key，获取对应的value，返回List,如果指定的key不存在,List对应位置为null
     *
     * @param key
     *            指定的key
     * @param fieids
     *            存储位置
     * @return List<String>
     * @throws Exception
     */
    public List<String> hmget(String key, String... fieids) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        List<String> list = null;
        try {
            list = jedis.hmget(key, fieids);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    public List<byte[]> hmget(byte[] key, byte[]... fieids) throws Exception {
        // ShardedJedis jedis = getShardedJedis();
        Jedis jedis = getJedis();
        List<byte[]> list = null;
        try {
            list = jedis.hmget(key, fieids);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return list;
    }

    /**
     * 添加对应关系，如果对应关系已存在，则覆盖
     *
     * @param key
     *            要添加的key
     * @param map
     *            对应关系
     * @return 状态，成功返回OK
     * @throws Exception
     */
    public String hmset(String key, Map<String, String> map) throws Exception {
        Jedis jedis = getJedis();
        String s = null;
        try {
            s = jedis.hmset(key, map);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 添加对应关系，如果对应关系已存在，则覆盖
     *
     * @param key
     * @param map
     *            对应关系
     * @return 状态，成功返回OK
     * @throws Exception
     */
    public String hmset(byte[] key, Map<byte[], byte[]> map) throws Exception {
        Jedis jedis = getJedis();
        String s = null;
        try {
            s = jedis.hmset(key, map);
        } catch (JedisConnectionException e) {
            throw e;
        } finally {
            returnResource(jedis);
        }
        return s;
    }

    /**
     * 获取jedis实例
     */
    public Jedis getJedis() throws Exception {
        try {
            if (jedisPool != null) {
                return jedisPool.getResource();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 回收jedis实例
     */
    public void returnResource(final Jedis jedis) {
        // 方法参数被声明为final，表示它是只读的
        if (jedis != null) {
            // jedisPool.returnResource(jedis);
            // jedis.close()取代jedisPool.returnResource(jedis)方法将3.0版本开始
            jedis.close();

        }

    }
}
