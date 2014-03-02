package net.cubespace.NetStats.Webserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import net.cubespace.NetStats.Config.Main;
import net.cubespace.NetStats.Webserver.Handler.GetGoogleAPIKey;
import net.cubespace.NetStats.Webserver.Handler.GetPlayerHandler;
import net.cubespace.NetStats.Webserver.Handler.StaticFileHandler;
import net.cubespace.lib.CubespacePlugin;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class HTTPServerInitializer extends ChannelInitializer<SocketChannel> {
    private CubespacePlugin plugin;
    private Main config;

    public HTTPServerInitializer(CubespacePlugin plugin) {
        super();

        this.plugin = plugin;
        this.config = plugin.getConfigManager().getConfig("main");
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("decoder", new HttpRequestDecoder());
        pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());

        HTTPServerHandler httpServerHandler = new HTTPServerHandler();

        GetPlayerHandler getPlayerHandler = new GetPlayerHandler(plugin);
        httpServerHandler.addHandler("/api/getPlayers", getPlayerHandler);

        GetGoogleAPIKey getGoogleAPIKey = new GetGoogleAPIKey(plugin);
        httpServerHandler.addHandler("/api/getGoogleAPIKey", getGoogleAPIKey);

        //Add the Statichandler (must be the last one)
        StaticFileHandler staticFileHandler = new StaticFileHandler();
        staticFileHandler.setWebDir(config.getWebserver_WebDir());
        staticFileHandler.addIndex("index.html");
        httpServerHandler.addHandler(".*", staticFileHandler);

        pipeline.addLast("handler", httpServerHandler);
    }
}