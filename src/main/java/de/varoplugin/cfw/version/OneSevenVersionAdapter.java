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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Properties;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Sign;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import com.cryptomorin.xseries.XPotion;

import net.md_5.bungee.api.chat.ClickEvent;

@SuppressWarnings("deprecation")
class OneSevenVersionAdapter implements VersionAdapter {

    protected Class<?> minecraftServerClass;
    protected Method minecraftServerMethod, propertyManagerMethod;
    protected Field propertiesField;
    protected Class<?> enumClientCommandClass, packetPlayInClientCommandClass;
    protected Class<?> nmsPlayerClass;
    protected Method entityHandleMethod;
    protected Method sendPacketMethod;
    protected Field connectionField, networkManagerField, pingField, localeField, xpCooldownField;

    OneSevenVersionAdapter() {
        try {
            this.init();
        } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void init() throws IllegalArgumentException, NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        this.initServerProperties();
        this.initEntity();
        this.initPlayer();
        this.initRespawn();
        this.initNetworkManager();
        this.initLocale();
        this.initXp();
    }

    protected void initServerProperties() throws ClassNotFoundException, NoSuchMethodException, SecurityException, NoSuchFieldException {
        this.minecraftServerClass = Class.forName(VersionUtils.getNmsClass() + ".MinecraftServer");
        this.minecraftServerMethod = this.minecraftServerClass.getMethod("getServer");
        this.propertyManagerMethod = this.minecraftServerClass.getDeclaredMethod("getPropertyManager");
        this.propertiesField = this.propertyManagerMethod.getReturnType().getDeclaredField("properties");
        this.propertiesField.setAccessible(true);
    }

    protected void initEntity() throws NoSuchMethodException, SecurityException, ClassNotFoundException {
        this.entityHandleMethod = Class.forName(VersionUtils.getCraftBukkitPackage() + ".entity.CraftEntity").getMethod("getHandle");
    }

    protected void initPlayer() throws NoSuchMethodException, SecurityException, NoSuchFieldException, ClassNotFoundException {
        this.nmsPlayerClass = Class.forName(VersionUtils.getNmsClass() + ".EntityPlayer");
        this.pingField = this.nmsPlayerClass.getField("ping");
        this.connectionField = this.nmsPlayerClass.getField("playerConnection");
        this.sendPacketMethod = this.connectionField.getType().getMethod("sendPacket", Class.forName(VersionUtils.getNmsClass() + ".Packet"));
    }

    protected void initRespawn() throws ClassNotFoundException {
        this.enumClientCommandClass = Class.forName(VersionUtils.getNmsClass() + ".EnumClientCommand");
        this.packetPlayInClientCommandClass = Class.forName(VersionUtils.getNmsClass() + ".PacketPlayInClientCommand");
    }

    protected void initNetworkManager() throws IllegalArgumentException {
        for (Field field : this.connectionField.getType().getFields())
            if (field.getName().equals("networkManager")) {
                this.networkManagerField = field;
                return;
            }
        throw new Error("[CFW] Failed to initalize 1.7+ networkManager! Are you using a modified version of Spigot/Bukkit?");
    }

    protected void initLocale() throws NoSuchFieldException, SecurityException, IllegalArgumentException {
        this.localeField = this.nmsPlayerClass.getDeclaredField("locale");
        this.localeField.setAccessible(true);
    }

    protected void initXp() {
        this.initXp(VersionUtils.getNmsClass() + ".EntityHuman", VersionUtils.getNmsClass() + ".FoodMetaData");
    }

    protected void initXp(String entityHumanName, String foodMetaName) {
        // this is EXTREMELY unsafe
        try {
            int fieldNum = 0;
            for (Field field : Class.forName(entityHumanName).getDeclaredFields())
                if (fieldNum == 0 && field.getType() == Class.forName(foodMetaName))
                    fieldNum = 1;
                // This is for 1.19+, but I don't want to create a new version adapter class for one single line of code (Yes, I am lazy)
                else if (fieldNum == 1 && field.getType().getName().equals("net.minecraft.world.entity.monster.warden.WardenSpawnTracker"))
                    ;
                else if (fieldNum == 1 && field.getType() == int.class)
                    fieldNum = 2;
                else if (fieldNum == 2 && field.getType() == float.class)
                    fieldNum = 3;
                else if (fieldNum == 3 && field.getType() == float.class)
                    fieldNum = 4;
                else if (fieldNum == 4 && field.getType() == int.class) {
                    this.xpCooldownField = field;
                    return;
                } else
                    fieldNum = 0;

            throw new Error("Unable to find xp cooldown field");
        } catch (SecurityException | ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    protected Object getHandle(Entity entity) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return this.entityHandleMethod.invoke(entity);
    }

    @Override
    public Collection<? extends Player> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    @Override
    public Object getConnection(Player player) {
        try {
            return this.connectionField.get(this.getHandle(player));
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    @Override
    public int getPing(Player player) {
        try {
            return this.pingField.getInt(this.getHandle(player));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void respawnPlayer(Player player) {
        try {
            Object respawnEnum = this.enumClientCommandClass.getEnumConstants()[0];
            Constructor<?>[] constructors = this.packetPlayInClientCommandClass.getConstructors();
            for (Constructor<?> constructor : constructors) {
                Class<?>[] args = constructor.getParameterTypes();
                if (args.length == 1 && args[0] == respawnEnum.getClass()) {
                    Object packet = this.packetPlayInClientCommandClass.getConstructor(args).newInstance(respawnEnum);
                    this.sendPacket(player, packet);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendActionbar(Player player, String message, int duration, Plugin instance) {
        // 1.8+
    }

    @Override
    public void sendActionbar(Player player, String message) {
        // 1.8+
    }

    @Override
    public void sendClickableMessage(Player player, String message, ClickEvent.Action action, String value) {
        player.sendMessage(message);
    }

    @Override
    public void sendLinkedMessage(Player player, String message, String link) {
        this.sendClickableMessage(player, message, ClickEvent.Action.OPEN_URL, link);
    }

    public void sendPacket(Player player, Object packet) {
        try {
            this.sendPacketMethod.invoke(this.getConnection(player), packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendTablist(Player player, String header, String footer) {
        // 1.8+
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle) {
        // 1.8+
    }

    @Override
    public void sendBlockChange(Player player, Location location, Material material) {
        player.sendBlockChange(location, material, (byte) 1);
    }

    @Override
    public void setAttributeSpeed(Player player, double value) {
        // 1.9+
    }

    @Override
    public void setNameTagVisibility(Team team, boolean shown) {
        // 1.8+
    }

    @Override
    public void setArmorStandAttributes(Entity armorStand, boolean visible, boolean customNameVisible, boolean gravity, String customName) {
        // 1.8+
    }

    @Override
    public void removeAi(LivingEntity entity) {
        entity.addPotionEffect(new PotionEffect(XPotion.SLOWNESS.getPotionEffectType(), Integer.MAX_VALUE, 255));
    }

    @Override
    public void setXpCooldown(Player player, int cooldown) {
        try {
            this.xpCooldownField.set(this.getHandle(player), cooldown);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteItemAnnotations(ItemStack item) {
        // 1.8+ (?)
    }

    @Override
    public ItemStack getItemInUse(Player player) {
        return player.getItemInHand();
    }

    @Override
    public BlockFace getSignAttachedFace(Block block) {
        // Can be null on newer versions (paper 1.18.1 ???) but I'm not sure why
        BlockFace face = ((Sign) block.getState().getData()).getAttachedFace();
        return face == null ? BlockFace.SOUTH : face;
    }

    @Override
    public Object getNetworkManager(Player player) {
        try {
            return this.networkManagerField.get(this.getConnection(player));
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    @Override
    public String getLocale(Player player) {
        try {
            return ((String) this.localeField.get(this.getHandle(player))).toLowerCase();
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new Error(e);
        }
    }

    @Override
    public void forceClearWorlds() {
        try {
            for (World world : Bukkit.getWorlds())
                if (!Bukkit.unloadWorld(world, false)) {
                    Object handle = world.getClass().getMethod("getHandle").invoke(world);
                    Field dimensionField = handle.getClass().getField("dimension");
                    dimensionField.setAccessible(true);
                    dimensionField.set(handle, 2);
                    if (!Bukkit.unloadWorld(world, false))
                        throw new Error("Unable to unload world " + world.getName());
                }
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | InvocationTargetException | NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    @Override
    public Properties getServerProperties() {
        try {
            Object mcServer = this.minecraftServerMethod.invoke(null);
            Object propertyManager = this.propertyManagerMethod.invoke(mcServer);
            return (Properties) this.propertiesField.get(propertyManager);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    @Override
    public void setServerProperty(String key, Object value) {
        this.getServerProperties().setProperty(key, String.valueOf(value));
    }

    @Override
    public Entity spawnNametagEntity(Player target, String nametag) {
        Location location = target.getLocation();
        World world = target.getWorld();

        // Spawn an invisible baby sheep
        Sheep sheep = (Sheep) world.spawn(location, Sheep.class);
        sheep.setBaby();
        sheep.setInvisible(true);
        sheep.setCustomName(nametag);
        sheep.setCustomNameVisible(true);

        // Make the sheep ride the player
        target.addPassenger(sheep);

        return sheep;
    }
}
