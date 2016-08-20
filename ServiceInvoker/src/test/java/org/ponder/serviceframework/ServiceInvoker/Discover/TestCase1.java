/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ponder.serviceframework.ServiceInvoker.Discover;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author han
 */
public class TestCase1 {

    @Test
    public void test1(){
        String s1="Thrift@org.ponder.service@1.0.1";
        String s11="Thrift@org.ponder.service@1.0.1";
        String s2="Thrift@org.ponder.service@1.1.1";
        String s3="Thrift@org.ponder.service@2.1.1";
        String s4="Thrift@org.ponder.service@12.1.1";
        String s5="Thrift@org.ponder.aservice:12.1.1";
        String s6="Thrift@org.ponder.service@2.3.1";
        String s7="Thrift@org.ponder.service@2.23.1";
        String s8="Thrift@org.ponder.service@2.3.4";
        String s9="Thrift@org.ponder.service@2.3.4d";
        String s10="Thrift@org.ponder.service@a2.3.4";
        String s12="Thrift@org.ponder.service@2.a3.4";
        String s13="Thrift@org.ponder.service@a2.3.5";
        String s14="Thrift@org.ponder.service@a2.3.5";
        String s15="Thrift@org.ponder.service@2.3";
        String s16="Thrift@org.ponder.service@2.3.";
        String s17="Thrift@org.ponder.service@2.";
        String s18="Thrift@org.ponder.service@2";
        String s19="Thrift@org.ponder.service@3";
        
        String s20="org.ponder.service@2.1.1";
        String s21="org.ponder.service@2.1.3";
        
        String s22="org.ponder.service@2.1.3";
        String s23="org.ponder.service2.1.3";
        String s24="org.ponder.service3.1.3";
        
        String s25="org.ponder.service@12";
        String s26="org.ponder.service@3";
        
         String s27="org.ponder.service@3a";
        Registry.VersionAwareComparator comp=new Registry.VersionAwareComparator();
        
        Assert.assertTrue(comp.compare(s1, s2)<0);
        Assert.assertTrue(comp.compare(s1, s11)==0);
        Assert.assertTrue(comp.compare(s3, s2)>0);
        Assert.assertTrue(comp.compare(s4, s3)>0);
        Assert.assertTrue(comp.compare(s4, s5)>0);
        Assert.assertTrue(comp.compare(s5, s4)<0);
        Assert.assertTrue(comp.compare(s3, s6)<0);
        Assert.assertTrue(comp.compare(s7, s6)>0);
        Assert.assertTrue(comp.compare(s6, s7)<0);
        Assert.assertTrue(comp.compare(s8, s6)>0);
        Assert.assertTrue(comp.compare(s8, s9)<0);
        Assert.assertTrue(comp.compare(s8, s10)<0);
        Assert.assertTrue(comp.compare(s12, s8)>0);
        Assert.assertTrue(comp.compare(s13, s10)>0);
        Assert.assertTrue(comp.compare(s13, s14)==0);
        Assert.assertTrue(comp.compare(s8, s15)>0);
         Assert.assertTrue(comp.compare(s8, s16)>0);
         Assert.assertTrue(comp.compare(s8, s17)>0);
         Assert.assertTrue(comp.compare(s8, s18)>0);
         Assert.assertTrue(comp.compare(s8, s19)<0);
         Assert.assertTrue(comp.compare(s21, s20)>0);
         Assert.assertTrue(comp.compare(s22, s23)>0);
         Assert.assertTrue(comp.compare(s23, s24)<0);
         Assert.assertTrue(comp.compare(s23, s22)<0);
         Assert.assertTrue(comp.compare(s24, s23)>0);
         Assert.assertTrue(comp.compare(s25, s26)>0);
         Assert.assertTrue(comp.compare(s25, s27)<0);
    }
}
