package adblocker

import android.content.Context
import com.github.salomonbrys.kodein.instance
import core.*
import gs.environment.inject
import org.blokada.R
import tunnel.Blockade
import kotlin.math.max

val DASH_ID_HOSTS_COUNT = "tunnel_hosts"

class TunnelDashCountDropped(
        val ctx: Context,
        val t: Tunnel = ctx.inject().instance()
) : Dash("tunnel_drop",
        R.drawable.ic_block,
        ctx.getString(R.string.tunnel_dropped_count_desc),
        onLongClick = { t.tunnelDropCount %= 0; true }
) {

    private val listener: Any
    init {
        text = getBlockedString(0)
        listener = t.tunnelDropCount.doOnUiWhenSet().then {
            text = getBlockedString(t.tunnelDropCount())
        } // TODO: think item lifetime vs listener leak
    }

    private fun getBlockedString(blocked: Int): String {
        return ctx.resources.getString(R.string.tunnel_dropped_count2, Format.counter(blocked))
    }
}

class TunnelDashHostsCount(
        val ctx: Context,
        val s: Filters = ctx.inject().instance(),
        val d: gs.property.Device = ctx.inject().instance(),
        val t: tunnel.Main = ctx.inject().instance()
) : Dash(DASH_ID_HOSTS_COUNT,
        R.drawable.ic_counter
) {

    init {
        text = ctx.resources.getString(R.string.tunnel_hosts_count2, 0.toString())

        ctx.ktx().on(tunnel.Events.RULESET_BUILT, { event ->
            val (wildcard, blacklist, whitelist) = event
            android.util.Log.d("tunnelDashes.kt", "event is " + event + " wildcard is "+ wildcard + " deny is " + blacklist + " allow is " + whitelist)
            text = "%s\n%s\n%s\n%s".format(
                    //ctx.resources.getString(R.string.tunnel_hosts_count2,
                    //        Format.counter(max(wildcard, 0)));
            // context.resources.getString(R.string.tunnel_config_memory_capacity2, // works just need to add more space for it to show up
            //         Format.counter(capacity + deny, round = true)),
            ctx.resources.getString(R.string.tunnel_hosts_count2,
                    Format.counter(max(blacklist, 0))),
                    ctx.resources.getString(R.string.tunnel_hosts_count3,
                            Format.counter(max(wildcard, 0))),
                    ctx.resources.getString(R.string.tunnel_hosts_count4,
                            Format.counter(max(whitelist, 0))),
                    ctx.resources.getString(R.string.tunnel_hosts_count5,
                            Format.counter(max(blacklist+wildcard+whitelist, 0))))
        })

        ctx.ktx().on(tunnel.Events.RULESET_BUILDING, {
            text = ctx.resources.getString(R.string.tunnel_hosts_updating)
        })

        ctx.ktx().on(tunnel.Events.FILTERS_CHANGING, {
            text = ctx.resources.getString(R.string.tunnel_hosts_downloading)
        })
    }
}

