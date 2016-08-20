/*
 * 
 * 
 * 
 */
package org.ponder.serviceframework.ServiceInvoker.Discover;

import java.io.IOException;
import java.util.ArrayList;
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
import org.ponder.serviceframework.FrameworkContract.ServiceNotDefinedException;
import org.ponder.serviceframework.ServiceInvoker.Thrift.ThriftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author han
 */
public class ServiceInstanceProvider extends Registry implements AutoCloseable, Runnable {

    private static final Logger log = LoggerFactory.getLogger(ServiceInstanceProvider.class);
    private static ServiceInstanceProvider singleton = null;
    private Thread maintainer = null;
    private EtcdClient etcd;
    private long etcdindex = 0;

    /**
     * Key(String):服务类型，格式：Protocol@ServiceName Value（List):ServicePool集合
     */
    private final Map<String, List<ServicePool>> SERVICE_SUBSET = new HashMap<>();

    private ServiceInstanceProvider(EtcdClient etcd) {
        this.etcd = etcd;
        EtcdKeysResponse resp;
        try {
            resp = etcd.get(REGISTRY_PREFIX).recursive().send().get();
            if (resp != null) {
                EtcdKeysResponse.EtcdNode thenode = resp.node;
                Map<String, String> leaves = getleafnode(null, thenode);
                if (leaves != null && !leaves.isEmpty()) {
                    leaves.keySet().stream().forEach((key) -> {
                        System.out.println(key + " = " + leaves.get(key));
                        int weight;
                        String servicename;
                        String protocol;
                        String version;
                        String host;
                        int port;
                        try {
                            weight = Integer.parseInt(leaves.get(key));
                        } catch (NumberFormatException ex) {
                            weight = 1;
                        }
                        try {
                            servicename = key.replace(REGISTRY_PREFIX + "/", "");
                            host = servicename.substring(servicename.indexOf("/"));
                            servicename = servicename.substring(0, servicename.indexOf("/"));
                            protocol = servicename.substring(0, servicename.indexOf("@"));
                            servicename = servicename.substring(servicename.indexOf("@") + 1);
                            version = servicename.substring(servicename.indexOf("@") + 1);
                            servicename = servicename.substring(0, servicename.indexOf("@"));
                            String partofport = host.substring(host.indexOf(":") + 1);
                            host = host.substring(1, host.indexOf(":"));
                            port = Integer.parseInt(partofport);
                            WrapService service = new WrapService(protocol, servicename, version, host, port, weight);
                            register(protocol, servicename, version, service);
                        } catch (NumberFormatException | IndexOutOfBoundsException | InvalidServiceIDException ex) {

                        }
                    });
                }
            }
        } catch (EtcdException | EtcdAuthenticationException | TimeoutException | IOException ex) {
            log.warn("",ex);
        }
        maintainer = new Thread(this);
    }

    public static ServiceInstanceProvider getProvider(EtcdClient etcd) {
        if (singleton == null) {
            singleton = new ServiceInstanceProvider(etcd);
        }
        if (singleton.maintainer != null) {
            if (!singleton.maintainer.isAlive()) {
                singleton.maintainer.start();
            }
        }
        return singleton;
    }

    public ServicePool getServiceInstances(String proto, String servicename, String version) throws ServiceNotDefinedException, NoSuchMethodException {
        if (proto.equalsIgnoreCase("Thrift")) {
            SortedMap<String, Map<String, WrapService>> allinstances = getService("Thrift", servicename, version);
            ThriftClient pool = new ThriftClient("Thrift", servicename, version);
            SortedMap<Integer, WrapService> weightedMap = new TreeMap<>();
            int totalweight = 0;
            for (Map<String, WrapService> sub : allinstances.values()) {
                for (WrapService service : sub.values()) {
                    if (service.getWeight() > 0) {
                        totalweight += service.getWeight();
                        weightedMap.put(totalweight, service);
                        pool.add(service);
                    }
                }
            }
            List<ServicePool> poolist = SERVICE_SUBSET.get("Thrift@" + servicename);
            if (poolist == null) {
                poolist = new ArrayList<>();
            }
            poolist.add(pool);
            SERVICE_SUBSET.put("Thrift@" + servicename, poolist);
            pool.show();
            return pool;
        } else {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        if (maintainer != null) {
            if (!maintainer.isAlive()) {
                maintainer.interrupt();
            }
        }
    }

    @Override
    public void run() {
        EtcdKeysResponse resp;
        while (true) {
            try {
                if (etcdindex == 0) {
                    resp = etcd.get(REGISTRY_PREFIX).recursive().waitForChange().send().get();
                    etcdindex = resp.etcdIndex + 1;
                } else {
                    resp = etcd.get(REGISTRY_PREFIX).recursive().waitForChange(etcdindex).send().get();
                    etcdindex++;
                }
//                if (resp.node != null) {
//                    System.out.println("resp.action.name()=" + resp.action.name());
//                    System.out.println("resp.index=" + resp.etcdIndex);
//
//                    System.out.println("resp.node.key=" + resp.node.key);
//                    System.out.println("resp.node.value=" + resp.node.value);
//                    for (EtcdKeysResponse.EtcdNode node : resp.node.nodes) {
//                        System.out.println("resp.node.nodes[" + node.key + "]=" + node.value);
//                    }
//                }
//                if (resp.prevNode != null) {
//                    System.out.println("resp.prevNode.key=" + resp.prevNode.key);
//                    System.out.println("resp.prevNode.value=" + resp.prevNode.value);
//                    for (EtcdKeysResponse.EtcdNode node : resp.prevNode.nodes) {
//                        System.out.println("resp.prevNode.nodes[" + node.key + "]=" + node.value);
//                    }
//                }
                if (resp.node == null) {
                    continue;
                }
                if (resp.node.dir) {
                    continue;
                }
                String servicetype = resp.node.key.substring(REGISTRY_PREFIX.length() + 1, resp.node.key.lastIndexOf("/"));
                String serviceuri = resp.node.key.substring(resp.node.key.lastIndexOf("/") + 1);
                switch (resp.action) {
                    case expire:
                    case delete: {
                        System.out.println(resp.action.name() + " " + servicetype + "  " + serviceuri);
                        WrapService svr = pick(servicetype, serviceuri);
                        if (svr != null) {
                            unregister(servicetype, serviceuri);
                            List<ServicePool> poollist = SERVICE_SUBSET.get(svr.getProtocol() + "@" + svr.getServicename());
                            if (poollist != null && !poollist.isEmpty()) {
                                for (ServicePool pool : poollist) {
                                    pool.remove(svr);
                                    pool.show();
                                }
                            }
                        }
                        show();
                        break;
                    }
                    case update: {
                        if (resp.node.value == null) {
                            break;
                        }
                        try {
                            int weight = Integer.parseInt(resp.node.value);
                            String servicename = resp.node.key.replace(REGISTRY_PREFIX + "/", "");
                            String host = servicename.substring(servicename.indexOf("/"));
                            servicename = servicename.substring(0, servicename.indexOf("/"));
                            String protocol = servicename.substring(0, servicename.indexOf("@"));
                            servicename = servicename.substring(servicename.indexOf("@") + 1);
                            String version = servicename.substring(servicename.indexOf("@") + 1);
                            servicename = servicename.substring(0, servicename.indexOf("@"));
                            String partofport = host.substring(host.indexOf(":") + 1);
                            host = host.substring(1, host.indexOf(":"));
                            int port = Integer.parseInt(partofport);
                            WrapService service = new WrapService(protocol, servicename, version, host, port, weight);
                            register(protocol, servicename, version, service);
                            List<ServicePool> poollist = SERVICE_SUBSET.get(service.getProtocol() + "@" + service.getServicename());
                            if (poollist != null && !poollist.isEmpty()) {
                                for (ServicePool pool : poollist) {
                                    pool.add(service);
                                    pool.show();
                                }
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException | InvalidServiceIDException ex) {

                        }
                        System.out.println(resp.action.name() + " " + servicetype + "  " + serviceuri);
                        show();
                        break;
                    }
                    case set:
                    case create: {
                        if (resp.node.value == null) {
                            break;
                        }
                        try {
                            int weight = Integer.parseInt(resp.node.value);
                            String servicename = resp.node.key.replace(REGISTRY_PREFIX + "/", "");
                            String host = servicename.substring(servicename.indexOf("/"));
                            servicename = servicename.substring(0, servicename.indexOf("/"));
                            String protocol = servicename.substring(0, servicename.indexOf("@"));
                            servicename = servicename.substring(servicename.indexOf("@") + 1);
                            String version = servicename.substring(servicename.indexOf("@") + 1);
                            servicename = servicename.substring(0, servicename.indexOf("@"));
                            String partofport = host.substring(host.indexOf(":") + 1);
                            host = host.substring(1, host.indexOf(":"));
                            int port = Integer.parseInt(partofport);
                            WrapService service = new WrapService(protocol, servicename, version, host, port, weight);
                            register(protocol, servicename, version, service);
                            List<ServicePool> poollist = SERVICE_SUBSET.get(service.getProtocol() + "@" + service.getServicename());
                            if (poollist != null && !poollist.isEmpty()) {
                                for (ServicePool pool : poollist) {
                                    pool.add(service);
                                    pool.show();
                                }
                            }
                        } catch (NumberFormatException | IndexOutOfBoundsException | InvalidServiceIDException ex) {

                        }
                        System.out.println(resp.action.name() + " " + servicetype + "  " + serviceuri);
                        show();
                        break;
                    }
                    default: {
                        System.out.println(resp.action.name() + " " + servicetype + "  " + serviceuri);
                        break;
                    }
                }

            } catch (IOException | EtcdException | EtcdAuthenticationException | TimeoutException ex) {

            }
        }
    }
}
