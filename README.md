# 手写RPC框架*NettyRpc*

## 1. RPC调用基本原理
RPC(*Remote Procedure Call Protocol*)——远程过程调用协议，
它是一种通过网络从远程计算机程序上请求服务，而不需要了解底层网络技术的协议。
调用方可以像使用本地方法那样使用服务提供提供的服务。 

### 1.1 RPC基本框架
其基本设计框架如下图所示。  
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/rpc-design.jpg)  
完整的RPC主要由三部分组成，分别为客户端（消费者），服务端（生产者）和注册中心。
注册中心起到的作用是实现客户端调用方法时的解耦与负载均衡。服务端启动时，
向注册中心注册自己提供的方法，附带提供服务的地址和端口。当客户端需要调用服务时，
会向注册中心查询指定名称的服务，用注册中心存储的服务端地址和端口再调用服务端提供的服务。

### 1.2 RPC详细调用过程
RPC调用的详细过程如下图所示。  
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/rpc-procedure.jpg)  
* 对消费者来说，在RPC调用过程中，使用第1步、第2步、第3步、第4步是透明的，其他的都是使用RPC框架去封装这些事情。
* 当应用开始调用PRC的方式时，就会去容器中去取Bean对象，所以我们应该首先注册Bean对象到容器中，我们通过Java的动态代理，
将代理过程封装到代理对象中，代理对象实现接口，创建实例到容器中。
* 相应的，在调用远程对象的对象方法时，就会调用动态代理中的方法，这就是代理层的作用。  
* 代理对象在获取到请求方法、接口和参数时，就会用序列化层，将这些信息封装成一个请求报文，再让通信层向服务端传送报文的内容，
然后就到了生产者这块。  
* 相应的服务必须有个监听器，来监听来自其他服务的请求，这里选择使用netty的通信，监听暴漏服务的端口。
* 然后，通过请求中的类、方法、参数，反射调用对应的Bean，拿到结果之后，通过序列化层，封装好结果报文，服务端的通信层将报文反馈给调用方。
* 调用方解析到返回值，动态代理类返回结果，调用结束。

## 2.项目框架
项目整体结构如下：  
```
├─api
├─client-spring-boot-starter
├─common
├─consumer
├─provider
└─server-spring-boot-starter
```
`api`、`consumer`和`provider`模块为`NettyRpc`项目的直接子模块。  
`api`模块为服务接口模块，提供未实现的服务接口名称。  
`consumer`为消费者模块，是RPC服务的调用者，依赖`api`和`client-spring-boot-starter`模块，
`client-spring-boot-starter`模块为其提供全部的调用支持。  
`provider`为生产者模块，是RPC服务的实现者，依赖`api`和`server-spring-boot-starter`模块，
`server-spring-boot-starter`模块为其提供全部的调用支持。  
`common`模块是RPC协议中通用部分，提供包括通信协议、服务注册与发现、序列化、负载均衡等实现。  
`client-spring-boot-starter`是消费者核心功能的实现模块，是一个SpringBoot starter项目，依赖`common`模块。  
`server-spring-boot-starter`是生产者核心功能的实现模块，同样是一个SpringBoot starter项目，依赖`common`模块。 
> 什么是`starter`？  
> > 在使用`Spring`开发时，若我们相位项目引入某个模块时，往往需要收到去`maven`仓库查找需要的`jar`包，并在`xml`文件中进行繁琐的配置，
> > 并且需要留意`jar`包版本的兼容问题，这些重复繁琐的步骤很大程度上影响了我们的开发效率。`SpringBoot`为了解决上述问题，引入了`starter`机制。  
> > 利用`starter`实现自动化配置只需要两个条件——`maven`依赖、配置文件，`starter`实现自动化配置需要在`resources/META-INF/spring.factories`文件中填写配置类信息  
> > 
> >  ```org.springframework.boot.autoconfigure.EnableAutoConfiguration=org.example.server.config.ServerConfiguration```  
> > 
> > 引入`maven`实质上就是导入`jar`包，`SpringBoot`启动的时候会找到`starter` `jar`包中的`resources/META-INF/spring.factories`文件，根据`spring.factories`文件中的配置，找到需要自动配置的类。  

> 为什么要使用`starter`？ 
> > 使用`starter`可以将最核心的功能剥离出来，使系统耦合程度减小。利用`starter`的自动装配机制，需要使用的模块直接在`pom`文件中引入`starter`即可，不需要进行繁琐的配置。  

## 3.通用模块`common`设计
### 3.1 通信协议设计





