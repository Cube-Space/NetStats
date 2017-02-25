package net.cubespace.NetStats.Webserver;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.cubespace.NetStats.Config.Main;
import net.cubespace.lib.CubespacePlugin;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class HTTPServer implements Runnable {
    private final Main config;
    private final CubespacePlugin plugin;

    public HTTPServer(CubespacePlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig("main");
    }

    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(config.getWebserver_Threads());

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HTTPServerInitializer(plugin));

            b.bind(config.getWebserver_IP(), config.getWebserver_Port()).sync().channel().closeFuture().sync();
            plugin.getLogger().info("Bound to " + config.getWebserver_IP() + ":" + config.getWebserver_Port());
        } catch (InterruptedException e) {
            plugin.getLogger().severe("Could not bind to that IP: " + e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
