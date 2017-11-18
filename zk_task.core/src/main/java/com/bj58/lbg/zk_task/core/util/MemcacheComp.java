package com.bj58.lbg.zk_task.core.util;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.bj58.spat.memcached.MemcachedClient;


public class MemcacheComp {

	private String memPath = "D:\\58ganji\\workspace_chq\\com.bj58.lbg.zookeeper.core\\config\\memcache.xml";
	private static MemcacheComp memcacheComp = new MemcacheComp();
    private static final Logger log = Logger.getLogger(MemcacheComp.class);
    private static final int DEFAULT_TIMEOUT = 3600 * 24 * 7; //默认7天
  
    
    protected static MemcachedClient memcache;
    
    private MemcacheComp() {
		init();
	}
	
	public static MemcacheComp getMemcacheComp() {
		return memcacheComp;
	}
    
    public void init() {
        
        try {
            memcache = MemcachedClient.getInstrance(memPath);
        } catch (Exception e) {
        	e.printStackTrace();
            log.error("Memcache 初始化失败", e);;
        }
    }
    
    /**
     * 放入缓存中，默认缓存时间是 7天
     * @param key
     * @param obj
     */
    public void set(String key, Object obj) throws Exception {
        
        this.set(key, obj, DEFAULT_TIMEOUT);
    }
    
    /**
     * 放入缓存中
     * @param key
     * @param obj
     * @param seconds
     */
    public void set(String key, Object obj, int seconds) throws Exception {
        
        memcache.set(key, obj, seconds);
    }
    
    /**
     * 放入缓存中
     * @param key
     * @param obj
     * @param seconds
     */
    public static void set2(String key, Object obj, int seconds) throws Exception {
        
        memcache.set(key, obj, seconds);
    }
    
    public boolean syncAdd(String key, Object obj, int seconds) throws Exception {
        
        return memcache.syncAdd(key, obj, seconds, 1000);
    }
    
    public static boolean syncAdd2(String key, Object obj, int seconds) throws Exception {
        
        return memcache.syncAdd(key, obj, seconds, 1000);
    }
    
    /**
     * 锁定特殊资源一段时间，默认锁定 5 秒
     * @param type 锁定资源类型
     * @param id 资源 Key
     * @throws Exception
     */
    public void lock(String id) throws Exception {
        
        while(!memcache.syncAdd(id, "Lock", 5, 1000));
    }
    
    public static void lock2(String id) {
        
        try {
            while(!memcache.syncAdd(id, "Lock", 5, 1000));
        } catch (Exception e) {
            log.warn("锁失败", e);
        }
    }
    
    public static void unLock2(String id) {
        
        try {
            memcache.delete(id);
        } catch (Exception e) {
            log.warn("解锁失败", e);
        }
    }
    
    /**
     * 解锁特定资源
     * @param type 锁定资源类型
     * @param id 资源 Key
     * @throws Exception
     */
    public void unLock(String id) throws Exception {
        
        memcache.delete(id);
    }
    
    /**
     * 移除单个缓存
     * @param key
     */
    public void delete(String key) throws Exception {
        
        memcache.delete(key);
    }

    /**
     * 根据key从缓存中取得数据
     */
    public <T> T get(String key) throws Exception {
        
        return memcache.get(key, 1000);
    }
    
    public static <T> T get2(String key) throws Exception {
        
        return memcache.get(key, 1000);
    }
    
    /**
     * 批量获取
     * @param keys
     * @return
     * @throws Exception
     */
    public <T> Map<String, T> get(String... keys) throws Exception {
        
        return memcache.getMap(2000, keys);
    }
    
    public <T> List<T> gets(String... keys) throws Exception {
        
        return memcache.gets(keys);
    }
}
