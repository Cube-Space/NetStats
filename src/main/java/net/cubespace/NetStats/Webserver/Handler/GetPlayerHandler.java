package net.cubespace.NetStats.Webserver.Handler;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import net.cubespace.NetStats.Database.Player;
import net.cubespace.NetStats.Webserver.Gson.Players;
import net.cubespace.NetStats.Webserver.HandlerUtil;
import net.cubespace.lib.CubespacePlugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.IF_MODIFIED_SINCE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;

/**
 * @author geNAZt (fabian.fassbender42@googlemail.com)
 */
public class GetPlayerHandler implements IHandler {
    private static Gson gson = new Gson();
    private static ScheduledTask refreshPlayerTask;
    private static Long lastModified;
    private static String json;

    public GetPlayerHandler(final CubespacePlugin plugin) {
        if (refreshPlayerTask == null) {
            refreshPlayerTask = plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Player> playerList = plugin.getDatabase().getDAO(Player.class).queryBuilder().
                                where().
                                    eq("online", true).
                                query();


                        List<net.cubespace.NetStats.Webserver.Gson.Player> sendList = new ArrayList<>();
                        for(Player players : playerList) {
                            net.cubespace.NetStats.Webserver.Gson.Player player = new net.cubespace.NetStats.Webserver.Gson.Player();
                            player.setName(players.getName());
                            player.setPing(players.getPing());
                            player.setLat(players.getLat());
                            player.setLon(players.getLon());
                            player.setOnline(players.getOnline());

                            sendList.add(player);
                        }

                        Players players = new Players();
                        players.setPlayers(sendList);

                        json = gson.toJson(players);
                        lastModified = System.currentTimeMillis();
                    } catch (SQLException e) {
                        plugin.getPluginLogger().warn("Could not update Player JSON", e);
                    }
                }
            }, 20, 1000, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        // Cache Validation
        String ifModifiedSince = request.headers().get(IF_MODIFIED_SINCE);
        if (ifModifiedSince != null && !ifModifiedSince.isEmpty()) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat(HandlerUtil.HTTP_DATE_FORMAT, Locale.US);
            Date ifModifiedSinceDate = dateFormatter.parse(ifModifiedSince);

            // Only compare up to the second because the datetime format we send to the client
            // does not have milliseconds
            long ifModifiedSinceDateSeconds = ifModifiedSinceDate.getTime() / 1000;
            long fileLastModifiedSeconds =  lastModified / 1000;
            if (ifModifiedSinceDateSeconds == fileLastModifiedSeconds) {
                HandlerUtil.sendNotModified(ctx);
                return;
            }
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(json.getBytes()));
        HandlerUtil.setDateAndCacheHeaders(response, lastModified);
        response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
        response.headers().set(CONNECTION, HttpHeaders.Values.CLOSE);

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
