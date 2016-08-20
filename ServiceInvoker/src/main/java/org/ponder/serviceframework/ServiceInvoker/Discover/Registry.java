/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdAuthenticationException;
import mousio.etcd4j.responses.EtcdException;
import mousio.etcd4j.responses.EtcdKeysResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class Registry  {

    private static final Logger log = LoggerFactory.getLogger(Registry.class);
    protected static final String REGISTRY_PREFIX = "/ponder/ServiceFramework/Services";
    /**
     * SortedMap<String,...>:String protocol@servicename@version
     * SortedMap<...,Map<String,...>>: String hostname:port
     * SortedMap<...,Map<...,WrapService>>:WrapService Service inside? or
     * ServiceReference inside?
     */
    private final SortedMap<String, Map<String, WrapService>> SERVICE_PROCESSOR_MAP = new TreeMap<>(new VersionAwareComparator());

    

    public Registry() {
        
    }

    public void register(String protocol, String servicename, String version, WrapService service) throws InvalidServiceIDException {
        if (protocol.contains("/") || protocol.contains(":") || protocol.contains("@")) {
            throw new InvalidServiceIDException("Protocol不能包含字符'/',':','@'");
        }
        if (servicename.contains("/") || servicename.contains(":") || servicename.contains("@")) {
            throw new InvalidServiceIDException("ServiceName不能包含字符'/',':','@'");
        }

        String url = protocol + "@" + servicename + "@" + version;
        Map<String, WrapService> instances = SERVICE_PROCESSOR_MAP.get(url);
        if (instances == null) {
            instances = new HashMap<>();
            instances.put(service.getUri(), service);
            SERVICE_PROCESSOR_MAP.put(url, instances);
            System.out.println("Register:" + url + " Instance:" + service.getUri() + " Weight:" + service.getWeight());
        } else {
            instances.put(service.getUri(), service);
            System.out.println("Register:" + url + " Instance:" + service.getUri() + " Weight:" + service.getWeight());
        }
    }

    public void unregister(String protocol, String servicename, String version, String serviceuri) {
        String servicetype = protocol + "@" + servicename + "@" + version;
        Map<String, WrapService> instances = SERVICE_PROCESSOR_MAP.get(servicetype);
        if (instances != null) {
            instances.remove(serviceuri);
            System.out.println("Unregister:" + servicetype + " Instance:" + serviceuri);
            if (instances.isEmpty()) {
                SERVICE_PROCESSOR_MAP.remove(servicetype);
            }
        } else {
            SERVICE_PROCESSOR_MAP.remove(servicetype);
        }
    }

    public void unregister(String servicetype, String serviceuri) {
        Map<String, WrapService> instances = SERVICE_PROCESSOR_MAP.get(servicetype);
        if (instances != null) {
            instances.remove(serviceuri);
            if (instances.isEmpty()) {
                SERVICE_PROCESSOR_MAP.remove(servicetype);
            }
        } else {
            SERVICE_PROCESSOR_MAP.remove(servicetype);
        }
    }
    /**
     * 
     * @param servicetype <Protocol>@<ServiceName>@<Version>
     * @param uri <Host>:<Port>
     * @return 
     */
    protected WrapService pick(String servicetype,String uri){
        Map<String, WrapService> instances = SERVICE_PROCESSOR_MAP.get(servicetype);
        if (instances != null) {
            return instances.get(uri);
        } 
        return null;
    }
    
    /**
     *
     * @param protocol
     * @param servicename
     * @param version
     * @return 返回符合规定范围内的一组服务
     */

    protected SortedMap<String, Map<String, WrapService>> getService(String protocol, String servicename, String version) {
        String fromkey = protocol + "@" + servicename + "@" + version;
        String tokey;
        try {
            String mversion = version.substring(0, version.contains(".") ? version.indexOf(".") : version.length());
            Integer nextmainversion = Integer.parseInt(mversion) + 1;
            tokey = protocol + "@" + servicename + "@" + nextmainversion;
        } catch (NumberFormatException ex) {
            tokey = protocol + "@" + servicename + "@" + version;
        }

        return SERVICE_PROCESSOR_MAP.subMap(fromkey, tokey);
    }


    public SortedMap<String, Map<String, WrapService>> getNewestCompetiableService(String protocol, String servicename, String version) {
        String tokey;
        try {
            String mversion = version.substring(0, version.contains(".") ? version.indexOf(".") : version.length());
            Integer nextmainversion = Integer.parseInt(mversion) + 1;
            tokey = protocol + "@" + servicename + "@" + nextmainversion;
        } catch (NumberFormatException ex) {
            tokey = protocol + "@" + servicename + "@" + version;
        }
        return SERVICE_PROCESSOR_MAP.headMap(tokey);
    }

   

    public void show() {
        if(SERVICE_PROCESSOR_MAP.isEmpty()){
            System.out.println("|||SERVICE_PROCESSOR_MAP is empty.");
            return;
        }
        for (String key : SERVICE_PROCESSOR_MAP.keySet()) {
            System.out.println("|||"+key + ":");
            Map<String, WrapService> instances = SERVICE_PROCESSOR_MAP.get(key);
            instances.keySet().stream().forEach(instancekey -> {
                WrapService service = instances.get(instancekey);
                System.out.println("|||"+key + "    " + instancekey + " " + service.getWeight());
            });
        }
    }

    protected final Map<String, String> getleafnode(Map<String, String> leaves, EtcdKeysResponse.EtcdNode node) {
        if (node.dir) {
            if (node.nodes != null && !node.nodes.isEmpty()) {
                for (EtcdKeysResponse.EtcdNode item : node.nodes) {
                    leaves = getleafnode(leaves, item);
                }
            }
            return leaves;
        } else {
            if (leaves == null) {
                leaves = new HashMap<>();
            }
            leaves.put(node.key, node.value);
            return leaves;
        }
    }

    public static class VersionAwareComparator  implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            String str1 = (String) o1;
            String str2 = (String) o2;
            int result = str1.substring(0, str1.lastIndexOf("@") == -1 ? 0 : str1.lastIndexOf("@")).compareTo(str2.substring(0, str2.lastIndexOf("@") == -1 ? 0 : str2.lastIndexOf("@")));
            if (result != 0) {
                return result;
            }
            String version1 = str1.substring(str1.lastIndexOf("@") + 1);
            String version2 = str2.substring(str2.lastIndexOf("@") + 1);
            String mainver1 = version1.substring(0, version1.contains(".") ? version1.indexOf(".") : version1.length());
            String mainver2 = version2.substring(0, version2.contains(".") ? version2.indexOf(".") : version2.length());
            if (!mainver1.equals(mainver2)) {
                try {
                    Integer m1 = Integer.parseInt(mainver1);
                    Integer m2 = Integer.parseInt(mainver2);
                    result = m1.compareTo(m2);
                    return result;
                } catch (NumberFormatException ex) {
                    return mainver1.compareTo(mainver2);
                }
            } else {
                String remainder1 = version1.substring(version1.indexOf(".") + 1);
                String subver1 = remainder1.substring(0, remainder1.contains(".") ? remainder1.indexOf(".") : remainder1.length());
                String remainder2 = version2.substring(version2.indexOf(".") + 1);
                String subver2 = remainder2.substring(0, remainder2.contains(".") ? remainder2.indexOf(".") : remainder2.length());
                if (!subver1.equals(subver2)) {
                    try {
                        Integer s1 = Integer.parseInt(subver1);
                        Integer s2 = Integer.parseInt(subver2);
                        result = s1.compareTo(s2);
                        return result;
                    } catch (NumberFormatException ex) {
                        return subver1.compareTo(subver2);
                    }
                } else {
                    String lastpart1 = remainder1.substring(remainder1.indexOf(".") + 1);
                    String lastpart2 = remainder2.substring(remainder2.indexOf(".") + 1);
                    if (!lastpart1.equals(lastpart2)) {
                        try {
                            Integer l1 = Integer.parseInt(lastpart1);
                            Integer l2 = Integer.parseInt(lastpart2);
                            result = l1.compareTo(l2);
                            return result;

                        } catch (NumberFormatException ex) {
                            return lastpart1.compareTo(lastpart2);
                        }
                    } else {
                        return 0;
                    }
                }
            }

        }

    }

}
