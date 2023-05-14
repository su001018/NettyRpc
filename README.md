# 手写RPC框架*NettyRpc*

## 1.RPC调用基本原理
RPC(*Remote Procedure Call Protocol*)——远程过程调用协议，
它是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。
调用方可以像使用本地方法那样使用服务提供提供的服务。  
其基本设计框架如下图所示。  
![picture](https://gitee.com/su_ya_kang/NettyRpc/blob/master/picture/rpc-design.jpg)  
完整的RPC主要由三部分组成，分别为客户端（消费者），服务端（生产者）和注册中心。
注册中心起到的作用是实现客户端调用方法时的解耦与负载均衡。服务端启动时，
向注册中心注册自己提供的方法，附带提供服务的地址和端口。当客户端需要调用服务时，
会向注册中心查询指定名称的服务，用注册中心存储的服务端地址和端口再调用服务端提供的服务。  
RPC调用的详细过程如下图所示。  
![picture](https://gitee.com/su_ya_kang/NettyRpc/blob/master/picture/rpc-procedure.jpg)


## 2.项目整体框架

