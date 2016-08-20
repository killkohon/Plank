# Plank
A Service governance framework over OSGi. Providing the service registering,service discoverage,service invocation and so on.


Plank是一个基于OSGI和Thrift RPC的服务治理框架，用etcd作为包含：

1、服务注册

  将Thrift IDL对应的接口和实现，利用OSGI Service Registry的方式发布成OSGI服务（本地服务），服务包含若干服务属性；
  
  ServiceRegister模块会以白板模式（whiteboard）发现这些本地服务，并包装成Thrift远程服务暴露出去，然后，会在Etcd上注册服务信息，可支持多版本、多实例的服务注册。

2、服务发现
  
  在服务消费端的OSGI Framework上，ServiceInvoker模块可以从Etcd获取服务信息，区分版本，并在调用是于多个兼容实例中负载均衡。

3、服务调用

  服务调用方需引入Thrift IDL对应的接口，通过InvokerProxyFactory来创建相应调用代理（InvokerProxy)，通过调用代理来实现服务调用。

后续会逐渐添加服务统计、服务监控以及其它PRC的支持；

所有实现都将基于OSGi模块化规范。

欢迎和Killko Hon交流Plank的相关技术问题。
Email:killko@qq.com
QQ: 405366881

