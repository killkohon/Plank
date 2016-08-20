/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceRegister;

import java.util.Comparator;
import java.util.SortedMap;
import java.util.TreeMap;
import org.ponder.serviceframework.FrameworkContract.InvalidServiceIDException;

/**
 *
 * @author han
 */
public class Registry  {

    private final SortedMap<String, Object> SERVICE_PROCESSOR_MAP = new TreeMap<String, Object>(new VersionawareComparator());

    Registry() {

    }


    public void register(String protocol, String servicename, String version, Object service) throws InvalidServiceIDException {
        if (protocol.contains("/") || protocol.contains(":")) {
            throw new InvalidServiceIDException("Protocol不能包含字符'/',':'");
        }
        if (servicename.contains("/") || servicename.contains(":")) {
            throw new InvalidServiceIDException("ServiceName不能包含字符'/',':'");
        }

        String url = protocol + "://" + servicename + "/" + version;
        SERVICE_PROCESSOR_MAP.put(url, service);
    }

    public void unregister(String protocol, String servicename, String version) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public void unregister(String url) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param servicename
     * @param version
     * @return 返回符合规定范围内的一组服务
     */
    public SortedMap<String, Object> getService(String protocol, String servicename, String version) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }


    public SortedMap<String, Object> getNewestCompetiableService(String protocol, String servicename, String version) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static class VersionawareComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            String str1 = (String) o1;
            String str2 = (String) o2;
            int result = str1.substring(0, str1.lastIndexOf("/") == -1 ? 0 : str1.lastIndexOf("/")).compareTo(str2.substring(0, str2.lastIndexOf("/") == -1 ? 0 : str2.lastIndexOf("/")));
            if (result != 0) {
                return result;
            }
            String version1 = str1.substring(str1.lastIndexOf("/") + 1);
            String version2 = str2.substring(str2.lastIndexOf("/") + 1);
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
