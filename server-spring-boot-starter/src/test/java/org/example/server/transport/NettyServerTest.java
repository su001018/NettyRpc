package org.example.server.transport;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;


class NettyServerTest {
    public static void main(String[] args) throws InterruptedException {

        //1. 创建两个线程组: 只需要一个线程组用于我们的实际处理（网络通信的读写）
        EventLoopGroup workGroup = new NioEventLoopGroup();

        //2. 通过辅助类去构造client,然后进行配置响应的配置参数
        Bootstrap b = new Bootstrap();
        b.group(workGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .option(ChannelOption.SO_RCVBUF, 1024 * 32)
                .option(ChannelOption.SO_SNDBUF, 1024 * 32)
                //3. 初始化ChannelInitializer
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //3.1  添加客户端业务处理类
                        ch.pipeline().addLast(new ClientHandler());
                    }
                });
        //4. 服务器端绑定端口并启动服务; 使用channel级别的监听close端口 阻塞的方式
        ChannelFuture cf = b.connect("192.168.153.1", 8081).syncUninterruptibly();

        //5. 发送一条数据到服务器端
        cf.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty!".getBytes()));

        //6. 休眠一秒钟后再发送一条数据到服务端
        Thread.sleep(1000);
        cf.channel().writeAndFlush(Unpooled.copiedBuffer("hello netty again!".getBytes()));

        //7. 同步阻塞关闭监听并释放资源
        cf.channel().closeFuture().sync();
        workGroup.shutdownGracefully();

    }

}
class ClientHandler extends ChannelInboundHandlerAdapter {

    /**
     *  channelActive
     *  客户端通道激活
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("client channel active..");
    }

    /**
     *  channelRead
     *  真正的数据最终会走到这个方法进行处理
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        // 固定模式的 try .. finally
        // 在try代码片段处理逻辑, finally进行释放缓存资源, 也就是 Object msg (buffer)
        try {
            ByteBuf buf = (ByteBuf) msg;
            byte[] req = new byte[buf.readableBytes()];
            buf.readBytes(req);

            String body = new String(req, "utf-8");
            System.out.println("Client :" + body );
            String response = "收到服务器端的返回信息：" + body;
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     *  exceptionCaught
     *  异常捕获方法
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}