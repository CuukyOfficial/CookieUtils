/*
 * MIT License
 * 
 * Copyright (c) 2020-2022 CuukyOfficial
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package de.varoplugin.cfw.version;

import java.util.Collection;
import java.util.Properties;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.chat.ClickEvent;

public interface VersionAdapter {

    Collection<? extends Player> getOnlinePlayers();

    Object getConnection(Player player);

    int getPing(Player player);

    void respawnPlayer(Player player);

    void sendActionbar(Player player, String message, int duration, Plugin instance);

    void sendActionbar(Player player, String message);

    void sendClickableMessage(Player player, String message, ClickEvent.Action action, String value);

    void sendLinkedMessage(Player player, String message, String link);

    void sendTablist(Player player, String header, String footer);

    void sendTitle(Player player, String title, String subtitle);

    void sendBlockChange(Player player, Location location, Material material);

    void setAttributeSpeed(Player player, double value);

    void setNameTagVisibility(Team team, boolean shown);

    void setArmorStandAttributes(Entity armorStand, boolean visible, boolean customNameVisible, boolean gravity, String customName);

    void removeAi(LivingEntity entity);

    void setXpCooldown(Player player, int cooldown);

    void deleteItemAnnotations(ItemStack item);

    ItemStack getItemInUse(Player player);

    BlockFace getSignAttachedFace(Block block);

    Object getNetworkManager(Player player);

    String getLocale(Player player);

    @Deprecated
    void forceClearWorlds();

    Properties getServerProperties();

    void setServerProperty(String key, Object value);

    Entity spawnNametagEntity(Player target, String nametag);
}
