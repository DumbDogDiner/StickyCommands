/*
 * Copyright (c) 2021 DumbDogDiner <dumbdogdiner.com>. All rights reserved.
 * Licensed under the MIT license, see LICENSE for more information.
 */
package com.dumbdogdiner.stickycommands.player

import com.dumbdogdiner.stickyapi.bukkit.util.ServerUtil
import com.dumbdogdiner.stickycommands.StickyCommands
import com.dumbdogdiner.stickycommands.api.player.PlayerState
import com.dumbdogdiner.stickycommands.api.player.SpeedType
import me.xtomyserrax.StaffFacilities.SFAPI
import org.bukkit.entity.Player

class StickyPlayerState(
    private val player: Player
) : PlayerState {

    private var _afk: Boolean = false
    private var _afkTime: Int = 0

    override fun getPlayer(): Player {
        return this.player
    }

    override fun isAfk(): Boolean {
        return this._afk
    }

    override fun getAfkTime(): Int {
        return this._afkTime
    }

    override fun incrementAfkTime() {
        this._afkTime++
    }

    override fun resetAfkTime() {
        this._afkTime = 0
    }

    override fun isHidden(): Boolean {
        if (StickyCommands.staffFacilitiesEnabled) {
            val player = getPlayer()
        return SFAPI.isPlayerFakeleaved(player) ||
                    SFAPI.isPlayerStaffVanished(player) ||
                    SFAPI.isPlayerVanished(player) ||
                    isVanished
        }
        return false }

    /**
     * Checks if a given player is in a vanished state.
     *
     * @return Whether the user is vanished.
     */
    override fun isVanished(): Boolean {
        for (meta in getPlayer().getMetadata("vanished")) {
            if (meta.asBoolean()) {
                return true
            }
        }
        return false
    }

    override fun setAfk(isAfk: Boolean) {
        setAfk(isAfk, false)
    }

    override fun setAfk(isAfk: Boolean, broadcast: Boolean) {
        this._afk = isAfk
        // reset the time if we're unsetting their afk status
        if (!isAfk) {
            this._afkTime = 0
            System.out.println("ghe")
        }

        if (broadcast) {
            if (!isHidden) {
                val node = if (isAfk) "afk.afk" else "afk.not-afk"
                // TODO: Make method for getting all variables related to a user, such as location, username, uuid, etc
                val vars = HashMap<String, String>()
                vars.put("player", player.name)
                vars.put("player_uuid", player.uniqueId.toString())

                ServerUtil.broadcastMessage(StickyCommands.localeProvider!!.translate(node, vars))
            }
        }
    }

    override fun hasFlyModeEnabled(): Boolean {
        return this.player.allowFlight
    }

    override fun getSpeed(type: SpeedType): Float {
        return when (type) {
            SpeedType.WALK -> this.player.walkSpeed
            SpeedType.FLY -> this.player.flySpeed
        }
    }

    override fun setSpeed(type: SpeedType, speed: Float) {
        // we can't reassign vars, so we have to do this.
        var _speed = speed

        // sanity checks to make sure bukkit doesn't complain
        // and that the speed is correct for walking
        if (_speed <= 0f) _speed = 0.1f else if (speed > 1f) _speed = 1f

        if (type === SpeedType.WALK) _speed = if (_speed + 0.1f > 1f) _speed else _speed + 0.1f

        // TODO: Database implementation
        when (type) {
            SpeedType.FLY -> {
                this.getPlayer().flySpeed = _speed
            }
            SpeedType.WALK -> {
                this.getPlayer().walkSpeed = _speed
            }
        }
    }

    override fun setFlyModeEnabled(flyEnabled: Boolean) {
        this.player.allowFlight = flyEnabled
    }
}
