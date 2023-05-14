# 手写RPC框架*NettyRpc*
*NettyRpc*是以*netty*实现底层通信，目前提供*Zookeeper*作为注册中心，*JSON*序列化方法的RPC框架。持续更新中。
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
项目底层通信使用*TCP*协议，因此要着重解决*TCP*协议通信面临的问题。
#### 3.1.1 TCP协议
TCP是面向**字节流**的协议，消息的发送取决于发送窗口、拥塞窗口以及当前发送缓冲区的大小等条件。当用户消息通过 TCP 协议传输时，消息可能会被操作系统分组成多个的 TCP 报文，也就是一个完整的用户消息被拆分成多个 TCP 报文进行传输,也有可能多个消息在一个TCP报文中传输。看下面的例子。  
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/tcp-message.jpg)  
当需要发送消息A，消息B时，可能一条TCP报文中含有完整的A和一部分B，也可能只含有A的一部分，或者含有A和B以及其他消息。  
>同一个TCP报文中出现两个消息称之为**粘包**问题，只有消息的一部分称之为**半包**问题。**粘包**和**半包**问题是定义TCP为基础的通信协议时必须要解决的问题。  

解决**粘包**和**半包**问题常用的解决方案有三种，分别是：
* 固定的消息长度
* 特殊字符作为消息边界
* 自定义消息结构

##### 固定的消息长度
固定的消息长度是最简单的一种方式，用户消息都是固定长度的，比如规定一个消息的长度是 64 个字节，当接收方接满 64 个字节，就认为这个内容是一个完整且有效的消息。但是这种方式灵活性不高，实际中很少用。  
##### 特殊字符作为消息边界
我们可以在两个用户消息之间插入一个特殊的字符串，这样接收方在接收数据时，读到了这个特殊字符，就把认为已经读完一个完整的消息。  
HTTP 是一个非常好的例子。  
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/http-message.jpg)  
HTTP 通过设置回车符、换行符作为 HTTP 报文协议的边界。  
有一点要注意，这个作为边界点的特殊字符，如果刚好消息内容里有这个特殊字符，我们要对这个字符转义，避免被接收方当作消息的边界点而解析到无效的数据。
##### 自定义消息结构
可以自定义一个消息结构，由包头和数据组成，其中包头包是固定大小的，而且包头里有一个字段来说明紧随其后的数据有多大。当接收方接收到包头的大小（比如 4 个字节）后，
就解析包头的内容，于是就可以知道数据的长度，然后接下来就继续读取数据，直到读满数据的长度，就可以组装成一个完整到用户消息来处理了。本项目也采用这种处理方法。





