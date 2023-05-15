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
整体的项目依赖关系如图所示：  
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/nettyrpc-design.jpg)  
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
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/http-message.png)  
HTTP 通过设置回车符、换行符作为 HTTP 报文协议的边界。  
有一点要注意，这个作为边界点的特殊字符，如果刚好消息内容里有这个特殊字符，我们要对这个字符转义，避免被接收方当作消息的边界点而解析到无效的数据。
##### 自定义消息结构
可以自定义一个消息结构，由包头和数据组成，其中包头包是固定大小的，而且包头里有一个字段来说明紧随其后的数据有多大。当接收方接收到包头的大小（比如 4 个字节）后，
就解析包头的内容，于是就可以知道数据的长度，然后接下来就继续读取数据，直到读满数据的长度，就可以组装成一个完整到用户消息来处理了。本项目也采用这种处理方法。
#### 3.1.2 自定义消息
消息对象`Message`由两部分组成，分别是消息头`messageHeader`和消息体`messageBody`。
```java
public class Message<T> implements Serializable {
    //消息头
    MessageHeader messageHeader;
    //消息内容
    T messageBody;
}
```
##### MessageHeader
设计的消息格式如下图所示
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/message-header.png)  
消息头部固定为18字节，魔数指定为0x52，版本假定为1.
```java
public class Constants {
    //消息头总长度
    public static final int HEADER_LEN=18;
    //标识魔数
    public static final short MAGIC=0x52;
    //版本号
    public static final byte VERSION=1;

}
```
因此`MessageHeader`的成员如下：
```java
public class MessageHeader implements Serializable {
    //产生消息id
    private static AtomicLong num = new AtomicLong(0);
    private short magic;
    private byte version;
    private byte serialization;
    private byte type;
    private byte status;
    private long id;
    private int length;
}
```
##### MessageBody
`MessageBody`表示消息的内容，有两种类型，分别是客户端请求`MessageRequest`和服务端应答`MessageResponse`。
###### MessageRequest
包含了客户端请求的服务名称，调用的方法名，参数类型和参数列表。
```java
public class MessageRequest implements Serializable {
    //服务名称
    private String ServiceName;
    //调用方法名
    private String method;
    //方法参数类型
    Class<?>[] parameterTypes;
    //方法参数
    Object[] parameters;
}
```
###### MessageResponse
若请求成功，则`data`字段包含了请求结果，否则`message`字段包含了错误信息。
```java
public class MessageResponse implements Serializable {
    //回应数据
    private Object data;
    //错误信息
    private String message;

}
```
#### 3.1.3 序列化
网络传输的数据均为字节形式，因此想要传输对象，必须将其转化为字节形式，同样收到字节数组也要能将其转化为对象。
把对象转化为可传输的字节序列过程称为**序列化**，把字节序列还原为对象的过程称为**反序列化**。  
定义接口`Serialization`，序列化的不同实现方式都要继承该接口，提供序列化和反序列化方法。
```java
public interface Serialization {
    byte[] serialize(Object obj) throws IOException;
    <T>T deserialize(byte[] data,Class<T>clz) throws IOException;
}
```
##### JSON
利用`jackson`提供的`ObjectMapper`实现序列化和反序列化。
```java
public class JsonSerialization implements Serialization{
    private static final ObjectMapper MAPPER;
    @Override
    public byte[] serialize(Object obj) throws IOException {
        return obj instanceof String ? ((String) obj).getBytes() : MAPPER.writeValueAsString(obj).getBytes(StandardCharsets.UTF_8);
    }
    @Override
    public <T> T deserialize(byte[] data, Class<T> clz) throws IOException {
        return MAPPER.readValue(new String(data), clz);
    }
}
```

#### 3.1.4 编码器和解码器
由于网络传输的数据都是字节，因此我们需要在消息发送时将其编码为字节数组，
接收消息时，将其从字节数组回复到消息对象，即编码器和解码器。
##### ByteBuf
`ByteBuf`是`netty`的`Server`与`Client`之间通信的数据传输载体(`Netty`的数据容器)，它提供了一个`byte`数组(`byte[]`)的抽象视图，既解决了`JDK API`的局限性，又为网络应用程序的开发者提供了更好的`API`。  
* `ByteBuf`维护了两个不同的索引，一个用于读取，一个用于写入。`readerIndex`和`writerIndex`的初始值都是0，当从`ByteBuf`中读取数据时，它的`readerIndex`将会被递增(它不会超过`writerIndex`)，当向`ByteBuf`写入数据时，它的`writerIndex`会递增。
* 名称以`readXXX`或者`writeXXX`开头的`ByteBuf`方法，会推进对应的索引，而以`setXXX`或`getXXX`开头的操作不会。
* 在读取之后，0～`readerIndex`的就被视为`discard`的，调用`discardReadBytes`方法，可以释放这部分空间，它的作用类似`ByteBuffer`的`compact()`方法。
* `readerIndex`和`writerIndex`之间的数据是可读取的，等价于`ByteBuffer`的`position`和`limit`之间的数据。`writerIndex`和`capacity`之间的空间是可写的，等价于`ByteBuffer`的`limit`和`capacity`之间的可用空间。
* `markXXX`会记录`readerIndex`或`writerIndex`的原始位置，调用`resetXXX`方法后会将`readerIndex`或`writerIndex`复原到记录的旧值。
##### 编码器`MessageEncoder`
`MessageEecoder`继承`netty`中的`MessageToByteEncoder<T>`类，通过实现方法`encode()`将`T`类型对象编码为字节数组并填入`ByteBuf`。其中的泛型`T`赋值为`Message<T>`，即对`Message`对象编码。  

先读取消息头`MessageHeader`的内容填入`ByteBuf`，之后将消息体`MessageBody`序列化之后的`byte[]`数组填入`ByteBuf`。需要注意的是，序列化`MessageBody`之后得到`byte[]`数组的长度，才能确定`MessageHeader`中的`length`字段。
```java
public class MessageEncoder<T> extends MessageToByteEncoder<Message<T>> {
    protected void encode(ChannelHandlerContext channelHandlerContext, Message<T> tMessage, ByteBuf byteBuf) throws Exception {
        System.out.println("==========encoder===========");
        //获取消息头
        MessageHeader header = tMessage.getMessageHeader();
        //编码
        byteBuf.writeShort(header.getMagic());
        byteBuf.writeByte(header.getVersion());
        byteBuf.writeByte(header.getSerialization());
        byteBuf.writeByte(header.getType());
        byteBuf.writeByte(header.getStatus());
        byteBuf.writeLong(header.getId());
        //序列化对象
        Serialization serialization = SerializationFactory.getSerialization(SerializationType.findType(header.getSerialization()));
        byte[] data = serialization.serialize(tMessage.getMessageBody());
        byteBuf.writeInt(data.length);
        byteBuf.writeBytes(data);
    }
}
```
##### 解码器`MessageDecoder`
`MessageEecoder`继承`netty`中的`ByteToMessageDecoder`类，通过实现方法`decode()`将`ByteBuf`中的字节内容解码为消息对象并添加到`List<Object> list`列表。 

解码步骤如下：
1. 若`ByteBuf`中可读取内容长度小于固定的消息头长度，此时不能完整获取消息信息，直接返回，留到下次读取（`readerIndex`未改变，`ByteBuf`内容不会丢失）。
2. 记录`readerIndex`值，若读取消息头后得到的消息内容长度值大于当前`ByteBuf`中可读取内容长度，则恢复`readerIndex`，并返回，留到下一次读取。
3. 依次读取`MessageHeader`内容，根据`MessageHeader`中`length`字段读取`MessageBody`序列化之后的`byte[]`数组，根据`type`和`serialization`字段将`byte[]`数组反序列化为`MessageRequest`或`MessageResponse`。
4. 构造`Message`对象，将其添加到`List<Object> list`列表。
```java
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        //协议长度小于头部长度，放弃读取
        if(byteBuf.readableBytes()< Constants.HEADER_LEN){
            System.out.println("协议长度小于头部长度，放弃读取");
            return;
        }
        //标记读指针位置，出错回退
        byteBuf.markReaderIndex();
        if(byteBuf.readShort()!=Constants.MAGIC){
            throw new Exception("协议出错，头部魔数不匹配");
        }
        //读取消息头内容
        byte version=byteBuf.readByte();
        byte serialization=byteBuf.readByte();
        byte type=byteBuf.readByte();
        byte status=byteBuf.readByte();
        long id=byteBuf.readLong();
        int length=byteBuf.readInt();
        //半包现象，消息内容不完整，放弃读取
        if(byteBuf.readableBytes()<length){
            byteBuf.resetReaderIndex();
            System.out.println("半包现象，消息内容不完整，放弃读取");
            return;
        }
        //读取消息体
        byte[] data=new byte[length];
        byteBuf.readBytes(data);
        //构造消息头
        MessageHeader header=new MessageHeader(serialization,type,id,length);
        switch (MessageType.findType(type)){
            case REQUEST -> {
                Message<MessageRequest>message=new Message<>();
                message.setMessageHeader(header);
                MessageRequest request= SerializationFactory.getSerialization(SerializationType.findType(serialization))
                        .deserialize(data,MessageRequest.class);
                message.setMessageBody(request);
                list.add(message);
                break;
            }
            case RESPONSE -> {
                Message<MessageResponse>message=new Message<>();
                message.setMessageHeader(header);
                MessageResponse response=SerializationFactory.getSerialization(SerializationType.findType(serialization))
                        .deserialize(data,MessageResponse.class);
                message.setMessageBody(response);
                list.add(message);
                break;
            }
        }
    }
}
```
### 3.2 服务注册与发现
#### 3.2.1 Zookeeper
ZooKeeper的数据结构，跟Unix文件系统非常类似，可以看做是一颗树，每个节点叫做ZNode。每一个节点可以通过路径来标识，结构图如下：
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/zookeeper-design.png)  
ZNode分为两种类型：  
短暂/临时(Ephemeral)：当客户端和服务端断开连接后，所创建的ZNode(节点)会自动删除  
持久(Persistent)：当客户端和服务端断开连接后，所创建的ZNode(节点)不会删除
##### Curator
Apache Curator是一个比较完善的zookeeper客户端框架，通过封装的一套高级API，简化了ZooKeeper的操作，因此在实际应用中都是使用Apache Curator来操作zookeeper的。
##### Curator Service Discovery
生产者可以将自己的服务注册到Zookeeper中，创建一个临时节点（当连接断开之后，节点将被删除），存放服务信息（url，ip，port等信息），把这些临时节点都存放在以serviceName命名的节点下面，这样我们要获取某个服务的地址，只需要到Zookeeper中找到这个path，然后就可以读取到里面存放的服务信息。  
Curator Service Discovery实现了上述过程，它对此抽象出了ServiceInstance、ServiceProvider、ServiceDiscovery三个接口，通过它我们可以很轻易的实现Service Discovery。
###### ServiceInstance
Curator中使用ServiceInstance作为一个服务实例，ServiceInstances具有名称，ID，地址，端口和/或ssl端口以及可选的payload(用户定义）。ServiceInstances以下列方式序列化并存储在ZooKeeper中：  
![picture](https://gitee.com/su_ya_kang/NettyRpc/raw/master/picture/zookeeper-service-instance.png)  
###### ServiceProvider
ServiceProvider是主要的抽象类。它封装了发现服务为特定的命名服务和提供者策略。提供者策略方案是从一组给定的服务实例选择一个实例。有三个捆绑策略:轮询调度、随机和粘性(总是选择相同的一个)。
ServiceProviders是使用ServiceProviderBuilder分配的。消费者可以从从ServiceDiscovery获取ServiceProviderBuilder（参见下文）。ServiceProviderBuilder允许您设置服务名称和其他几个可选值。
必须通过调用start()来启动ServiceProvider 。完成后，您应该调用close()。ServiceProvider中有以下两个重要方法：
```
/** 获取一个服务实例 */
public ServiceInstance<T> getInstance() throws Exception;
/** 获取所有的服务实例 */
public Collection<ServiceInstance<T>> getAllInstances() throws Exception;
```
###### ServiceDiscovery
为了创建ServiceProvider,你必须有一个ServiceDiscovery。它是由一个ServiceDiscoveryBuilder创建。开始必须调用start()方法。当使用完成应该调用close()方法。
#### 3.2.2 服务详细信息ServiceDetails
为了在ZNode节点上保存服务的详细信息，需要自定义数据结构`ServiceDetails`，其中保存了服务地址、端口等信息。
```java
public class ServiceDetails {
    //服务名称
    String serviceName;
    //版本号
    String version;
    //服务地址
    String address;
    //服务端口号
    Integer port;
}
```
#### 3.2.3 负载均衡
当客户端请求服务时，若有多个服务端提供服务，客户端需要从中选择一个进行通信。为了避免某些服务端被频繁选中导致压力过大甚至崩溃，
因此RPC框架需要提供负载均衡算法避免上述情况，这里简单的只提供随机选择和轮询选择，待扩展。
#### 3.2.4 服务注册ServiceRegister
定义服务注册接口`ServiceRegister`，其具有函数`register()`实现服务注册、`unRegister()`实现服务取消注册，`close()`在关闭时释放资源。
生产者者在启动时，调用`register()`方法注册服务，服务端关闭时调用`unRegister()`取消其提供的服务。
```java
public interface ServiceRegister {
    void register(ServiceDetails serviceDetails) throws Exception;
    void unRegister(ServiceDetails serviceDetails) throws Exception;
    void close() throws Exception;
}
```
##### ZookeeperServiceRegister
`ZookeeperServiceRegister`实现接口`ServiceRegister`，初始化时，开启客户端和服务发现`serviceDiscovery`，并将其存储在关闭列表`closeableList`。
```java
public class ZookeeperServiceRegister implements ServiceRegister{
    public ZookeeperServiceRegister(String address) throws Exception {
        client= CuratorFrameworkFactory.newClient(address, new ExponentialBackoffRetry(1000,3));
        client.start();
        closeableList.add(client);
        serviceDiscovery= ServiceDiscoveryBuilder.builder(ServiceDetails.class).client(client).basePath(ROOT_PATH).serializer(new JsonInstanceSerializer<>(ServiceDetails.class)).build();
        serviceDiscovery.start();
        closeableList.add(serviceDiscovery);
    }
    //...
}
```
`register()`构造一个带有`ServiceDetails`信息的`ServiceInstance`，调用`serviceDiscovery`的`registerService`方法将服务加入。
```java
public class ZookeeperServiceRegister implements ServiceRegister {
    //...
    @Override
    public void register(ServiceDetails serviceDetails) throws Exception {
        ServiceInstance<ServiceDetails> serviceInstance = ServiceInstance.<ServiceDetails>builder().address(serviceDetails.getAddress()).port(serviceDetails.getPort()).name(serviceDetails.getServiceName()).payload(serviceDetails).build();
        serviceDiscovery.registerService(serviceInstance);
    }
    //...
}
```
`unRegister()`构造一个带有`ServiceDetails`信息的`ServiceInstance`，调用`serviceDiscovery`的`unregisterService`方法将服务取消注册。
```java
public class ZookeeperServiceRegister implements ServiceRegister {
    //...
    @Override
    public void unRegister(ServiceDetails serviceDetails) throws Exception {
        ServiceInstance<ServiceDetails>serviceInstance=ServiceInstance.<ServiceDetails>builder().address(serviceDetails.getAddress()).port(serviceDetails.getPort()).name(serviceDetails.getServiceName()).payload(serviceDetails).build();
        serviceDiscovery.unregisterService(serviceInstance);
    }
    //...
}
```
`close()`方法则调用`closeableList`中每个元素的`close()`方法。
```java
public class ZookeeperServiceRegister implements ServiceRegister{
    //...
    @Override
    public void close() throws Exception {
        synchronized (this){
            for(Closeable closeable:closeableList){
                CloseableUtils.closeQuietly(closeable);
            }
            closeableList.clear();
        }
    }
}
```
#### 3.2.5 服务发现ServiceDetailsDiscovery
定义服务发现接口`ServiceDetailsDiscovery`，其具有函数`discovery()`实现服务发现、`close()`在关闭时释放资源。
消费者在调用服务方法时，会调用`discovery()`方法根据服务名称找到服务详细信息`ServiceDetails`，之后使用`ServiceDetails`中的服务端地址端口号发起请求。
```java
public interface ServiceDetailsDiscovery {
    ServiceDetails discovery(String ServiceName) throws Exception;
    void close() throws Exception;
}
```
##### ZookeeperServiceDetailsDiscovery
构造函数和`close()`函数基本同`ZookeeperServiceRegister`。`discovery()`客户端的负载均衡策略构造相应的`ServiceProvider`，
`ServiceProvider`提供的`getInstance()`方法返回要求的`ServiceInstance`。
```java
public class ZookeeperServiceDetailsDiscovery implements ServiceDetailsDiscovery{
    @Override
    public ServiceDetails discovery(String serviceName) throws Exception {
        ProviderStrategy<ServiceDetails> strategy=null;
        switch (LoadBalanceType.findType(loadBalance)){
            case RANDOM -> {
                strategy=new RandomStrategy<ServiceDetails>();
                break;
            }
            case ROUND_ROBIN -> {
                strategy=new RoundRobinStrategy<ServiceDetails>();
                break;
            }
        }
        ServiceProvider<ServiceDetails>provider=serviceDiscovery.serviceProviderBuilder().serviceName(serviceName)
                .providerStrategy(strategy).build();
        provider.start();
        closeableList.add(provider);
        ServiceDetails serviceDetails= provider.getInstance().getPayload();
        return serviceDetails;
    }
}
```






##参考链接
1. [如何手撸一个较为完整的RPC框架](https://juejin.cn/post/6992867064952127524)
2. [如何理解是 TCP 面向字节流协议？](https://xiaolincoding.com/network/3_tcp/tcp_stream.html)
3. [Netty笔记(九)之ByteBuf使用详解](https://blog.csdn.net/usagoole/article/details/88024517)
4. [RPC框架调用过程详解](https://blog.csdn.net/heyeqingquan/article/details/78006587)
5. [利用Zookeeper(Curator)实现 - 服务注册与发现](https://blog.csdn.net/u010277958/article/details/92397384)





 





