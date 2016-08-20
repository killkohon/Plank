/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public abstract class ServicePool implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(ServicePool.class);
    protected SortedMap<Integer, WrapService> services;
    protected int totalweight;
    protected String protocol;
    protected String servicename;
    protected String version;

    public String getProtocol() {
        return protocol;
    }

    public String getServicename() {
        return servicename;
    }

    public String getVersion() {
        return version;
    }

    private ServicePool() {

    }

    public ServicePool(String proto, String sname, String ver) {
        this.protocol = proto;
        this.servicename = sname;
        this.version = ver;
        this.services = new TreeMap<>();
        this.totalweight = 0;
    }

    public void show() {
        Iterator<Map.Entry<Integer, WrapService>> entries = services.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, WrapService> entry = entries.next();
            Integer key = entry.getKey();
            WrapService service = entry.getValue();
            System.out.println("key=" + key + " value=" + service.getWeight());
        }
    }

    public void add(WrapService instance) {
        if (instance == null) {
            return;
        }
        if (this.protocol == null || !this.protocol.equalsIgnoreCase(instance.getProtocol())) {
            return;
        }
        if (this.servicename == null || !this.servicename.equalsIgnoreCase(instance.getServicename())) {
            return;
        }
        if (this.version == null || this.version.compareTo(instance.getVersion()) > 0) {
            //不接受低版本的服务实例
            return;
        }
        String mversion = instance.getVersion().substring(0, instance.getVersion().contains(".") ? instance.getVersion().indexOf(".") : instance.getVersion().length());
        Integer nextmainversion = Integer.parseInt(mversion) + 1;
        if (this.version.compareTo("" + nextmainversion) >= 0) {
            //不接受主版本号高于本实例池的服务实例
            return;
        }

        if (instance.getWeight() > 0) {
            int deltaweight = 0;
            Set<Integer> keyset = services.keySet();
            Iterator<Integer> iterator = keyset.iterator();
            List<Integer> keys = new ArrayList<>();
            while (iterator.hasNext()) {
                Integer key = iterator.next();
                keys.add(key);
            }
            boolean found = false;
            for (Integer key : keys) {
                WrapService service = services.get(key);
                if (service == null) {
                    continue;
                }
                synchronized (this) {
                    if (instance.match(service)) {
                        found = true;
                        if (instance.getWeight() != service.getWeight()) {
                            deltaweight += instance.getWeight();
                            deltaweight -= service.getWeight();
                            services.remove(key);
                            services.put(key + deltaweight, service);
                            totalweight += instance.getWeight();
                            totalweight -= service.getWeight();
                        }
                    } else if (deltaweight != 0) {
                        services.remove(key);
                        services.put(key + deltaweight, service);
                    }
                }
            }
            //没有匹配的服务实例，则添加
            if (!found) {
                synchronized (this) {
                    totalweight += instance.getWeight();
                    services.put(totalweight, instance);
                }
            }
        }
    }

    public void remove(WrapService instance) {
        if (instance == null) {
            return;
        }
        int weight = 0;
        int lastkey = 0;
        Set<Integer> keyset = services.keySet();
        Iterator<Integer> iterator = keyset.iterator();
        List<Integer> keys = new ArrayList<>();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            keys.add(key);
        }
        for (Integer key : keys) {
            WrapService service = services.get(key);
            if (service == null) {
                continue;
            }
            synchronized (this) {
                if (service.getPort() != instance.getPort()) {
                    lastkey = key;
                    services.remove(key);
                    services.put(key - weight, service);
                    continue;
                }
                if (!service.getProtocol().equalsIgnoreCase(instance.getProtocol())) {
                    lastkey = key;
                    services.remove(key);
                    services.put(key - weight, service);
                    continue;
                }
                if (!service.getHost().equalsIgnoreCase(instance.getHost())) {
                    lastkey = key;
                    services.remove(key);
                    services.put(key - weight, service);
                    continue;
                }

                if (!service.getServicename().equals(instance.getServicename())) {
                    lastkey = key;
                    services.remove(key);
                    services.put(key - weight, service);
                    continue;
                }
                if (!service.getVersion().equals(instance.getVersion())) {
                    lastkey = key;
                    services.remove(key);
                    services.put(key - weight, service);
                    continue;
                }
                weight += (key - lastkey);
                services.remove(key);
                totalweight -= (key - lastkey);
                lastkey = key;
            }
        }
    }

    protected WrapService pickone() {
        if (this.services.isEmpty()) {
            return null;
        }

        WrapService instance = null;
        synchronized (this) {
            double rand = Math.ceil(Math.random() * this.totalweight);
            instance = this.services.get(services.tailMap((int) rand).firstKey());
        }
        return instance;
    }

    public abstract Class<?> getClazz();
}
