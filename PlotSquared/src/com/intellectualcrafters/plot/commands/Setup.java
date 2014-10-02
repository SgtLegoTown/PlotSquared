package com.intellectualcrafters.plot.commands;

import static com.intellectualcrafters.plot.PlotWorld.AUTO_MERGE_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.DEFAULT_FLAGS_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.MAIN_BLOCK_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.PLOT_BIOME_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.PLOT_HEIGHT_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.PLOT_WIDTH_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.ROAD_BLOCK_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.ROAD_HEIGHT_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.ROAD_STRIPES_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.ROAD_STRIPES_ENABLED_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.ROAD_WIDTH_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.SCHEMATIC_FILE_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.SCHEMATIC_ON_CLAIM_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.TOP_BLOCK_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.WALL_BLOCK_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.WALL_FILLING_DEFAULT;
import static com.intellectualcrafters.plot.PlotWorld.WALL_HEIGHT_DEFAULT;

import com.intellectualcrafters.plot.C;
import com.intellectualcrafters.plot.PlayerFunctions;
import com.intellectualcrafters.plot.PlotMain;
import com.intellectualcrafters.plot.PlotWorld;
import com.intellectualcrafters.plot.WorldGenerator;
import com.intellectualcrafters.plot.listeners.PlayerEvents;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import sun.java2d.pipe.hw.ExtendedBufferCapabilities.VSyncType;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Citymonstret on 2014-09-26.
 */
public class Setup extends SubCommand implements Listener {

    public static Map<String, SetupObject> setupMap = new HashMap<>();

    private static class SetupStep {
        private String constant;
        private Object default_value;
        private String description;
        private Object value = 0;
        private String type;
        private boolean require_previous;
        public SetupStep(String constant, Object default_value, String description, String type, boolean require_previous) {
            this.constant = constant;
            this.default_value = default_value;
            this.description = description;
            this.type = type;
            this.require_previous = require_previous;
        }
        public boolean getRequire() {
            return this.require_previous;
        }
        
        public String getType() {
            return this.type;
        }

        public boolean setValue(String string) {
            if (!validValue(string)) {
                return false;
            }
            switch (this.type) {
                case "integer":
                    value = Integer.parseInt(string);
                    break;
                case "boolean":
                    value = Boolean.parseBoolean(string);
                    break;
                case "double":
                    value = Double.parseDouble(string);
                    break;
                case "float":
                    value = Float.parseFloat(string);
                    break;
                case "biome":
                    value = Biome.valueOf(string.toUpperCase());
                    break;
                case "block":
                    value = string;
                    break;
                case "blocklist":
                    value = string.split(",");
                    break;
                case "string":
                    value = string;
                    break;
            }
            return true;
        }
        public boolean validValue(String string) {
            try {
                if (this.type.equals("integer")) {
                    Integer.parseInt(string);
                    return true;
                }
                if (this.type.equals("boolean")) {
                    Boolean.parseBoolean(string);
                    return true;
                }
                if (this.type.equals("double")) {
                    Double.parseDouble(string);
                    return true;
                }
                if (this.type.equals("float")) {
                    Float.parseFloat(string);
                    return true;
                }
                if (this.type.equals("biome")) {
                    Biome.valueOf(string.toUpperCase());
                    return true;
                }
                if (this.type.equals("block")) {
                    if (string.contains(":")) {
                        String[] split = string.split(":");
                        Short.parseShort(split[0]);
                        Short.parseShort(split[1]);
                    }
                    else {
                        Short.parseShort(string);
                    }
                    return true;
                }
                if (this.type.equals("blocklist")) {
                    for (String block:string.split(",")) {
                        if (block.contains(":")) {
                            String[] split = block.split(":");
                            Short.parseShort(split[0]);
                            Short.parseShort(split[1]);
                        }
                        else {
                            Short.parseShort(block);
                        }
                    }
                    return true;
                }
                if (this.type.equals("string")) {
                    return true;
                }
            }
            catch (Exception e) {}
            return false;
        }

        public Object getValue() {
            return this.value;
        }

        public String getConstant() {
            return this.constant;
        }

        public Object getDefaultValue() {
            return this.default_value;
        }

        public String getDescription() {
            return this.description;
        }
    }

    private class SetupObject {
        String world;
        int current = 0;
        
        SetupStep[] step = new SetupStep[] { 
                new SetupStep("road.height", PlotWorld.ROAD_HEIGHT_DEFAULT, "Height of road", "integer", false),
                new SetupStep("plot.height", PlotWorld.PLOT_HEIGHT_DEFAULT, "Height of plot", "integer", false),
                new SetupStep("wall.height", PlotWorld.WALL_HEIGHT_DEFAULT, "Height of wall", "integer", false),
                new SetupStep("plot.size", PlotWorld.PLOT_WIDTH_DEFAULT, "Size of plot", "integer", false),
                new SetupStep("road.width", PlotWorld.ROAD_WIDTH_DEFAULT, "Width of road", "integer", false),
                new SetupStep("plot.biome", PlotWorld.PLOT_BIOME_DEFAULT, "Plot biome", "biome", false),
                new SetupStep("plot.filling", PlotWorld.MAIN_BLOCK_DEFAULT, "Plot filling", "blocklist", false),
                new SetupStep("plot.floor", PlotWorld.TOP_BLOCK_DEFAULT, "Plot floor", "blocklist", false),
                new SetupStep("wall.block", PlotWorld.WALL_BLOCK_DEFAULT, "Wall block", "block", false),
                new SetupStep("wall.filling", PlotWorld.WALL_FILLING_DEFAULT, "Wall filling", "block", false),
                new SetupStep("road.enable_stripes", PlotWorld.ROAD_STRIPES_ENABLED_DEFAULT, "Enable road stripes", "boolean", false),
                new SetupStep("road.stripes", PlotWorld.ROAD_STRIPES_DEFAULT, "Road stripes block", "block", true),
                new SetupStep("road.block", PlotWorld.ROAD_BLOCK_DEFAULT, "Road block", "block", false),
        };
        public SetupObject(String world) {
            this.world = world;
        }
        
        public SetupStep getNextStep() {
            return this.step[current++];
        }

        public int getCurrent() {
            return this.current;
        }
        
        public void setCurrent(String string) {
            this.step[current].setValue(string);
        }

        public int getMax() {
            return this.step.length;
        }
    }

    public Setup() {
        super("setup", "plots.admin", "Setup a PlotWorld", "/plot setup {world}", "setup", CommandCategory.ACTIONS);
    }
    
    @Override
    public boolean execute(Player plr, String... args) {
        if(setupMap.containsKey(plr.getName())) {
            SetupObject object = setupMap.get(plr.getName());
            if(object.getCurrent() == object.getMax()) {
                sendMessage(plr, C.SETUP_FINISHED, object.world);
                
                SetupStep[] steps = object.step;
                String world = object.world;
                for (SetupStep step:steps) {
                    PlotMain.config.set("worlds."+world+"."+step.constant, step.value);
                }
                try {
                    PlotMain.config.save(PlotMain.configFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                World newWorld = WorldCreator.name(world).generator(new WorldGenerator(world)).createWorld();
                plr.teleport(newWorld.getSpawnLocation());
                
                setupMap.remove(plr.getName());
                
                return true;
            }
            SetupStep step = object.step[object.current];
            if(args.length < 1) {
                sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
                return true;
            } else {
                if (args[0].equalsIgnoreCase("cancel")) {
                    setupMap.remove(plr.getName());
                    PlayerFunctions.sendMessage(plr, "&cCancelled setup.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("back")) {
                    if (object.current>0) {
                        object.current--;
                        step = object.step[object.current];
                        sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
                        return true;
                    }
                    else {
                        sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
                        return true;
                    }
                }
                boolean valid = step.validValue(args[0]);
                if(valid) {
                    sendMessage(plr, C.SETUP_VALID_ARG, step.getConstant(), args[0]);
                    step.setValue(args[0]);
                    object.current++;
                    step = object.step[object.current];
                    sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
                } else {
                    sendMessage(plr, C.SETUP_INVALID_ARG, args[0], step.getConstant());
                    sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
                }
            }
        } else {
            if (args.length < 1) {
                sendMessage(plr, C.SETUP_MISSING_WORLD);
                return true;
            }
            String world = args[0];
            if (StringUtils.isNumeric(args[0])) {
                sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                return true;
            }
            if (PlotMain.getWorldSettings(world)!=null) {
                sendMessage(plr, C.SETUP_WORLD_TAKEN, world);
                return true;
            }
            setupMap.put(plr.getName(), new SetupObject(world));
            sendMessage(plr, C.SETUP_INIT);
            SetupObject object = setupMap.get(plr.getName());
            SetupStep step = object.step[object.current];
            sendMessage(plr, C.SETUP_STEP, object.current + 1 + "", step.getDescription(), step.getType(), step.getDefaultValue() + "");
            return true;
        }
        return true;
    }

}
