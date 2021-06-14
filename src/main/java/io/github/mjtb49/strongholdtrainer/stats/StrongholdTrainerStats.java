package io.github.mjtb49.strongholdtrainer.stats;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class StrongholdTrainerStats {

    static final String MODID = "st";

    public static final Identifier NUM_STRONGHOLDS = new Identifier(MODID, "num_strongholds");
    public static final Identifier NUM_REVIEWED_ROOMS = new Identifier(MODID, "num_reviewed_rooms");
    public static final Identifier NUM_BEST_ROOMS = new Identifier(MODID, "num_best_rooms");
    public static final Identifier NUM_INACCURACIES = new Identifier(MODID, "num_inaccuracies");
    public static final Identifier NUM_MISTAKES = new Identifier(MODID, "num_mistakes");
    public static final Identifier TOTAL_TIME = new Identifier(MODID, "total_time");
    public static final Identifier AVG_TIME = new Identifier(MODID, "avg_time");

    public static void register() {
        RoomStats.register();

        Registry.register(Registry.CUSTOM_STAT, "num_strongholds", NUM_STRONGHOLDS);
        Registry.register(Registry.CUSTOM_STAT, "num_reviewed_rooms", NUM_REVIEWED_ROOMS);
        Registry.register(Registry.CUSTOM_STAT, "num_best_rooms", NUM_BEST_ROOMS);
        Registry.register(Registry.CUSTOM_STAT, "num_inaccuracies", NUM_INACCURACIES);
        Registry.register(Registry.CUSTOM_STAT, "num_mistakes", NUM_MISTAKES);
        Registry.register(Registry.CUSTOM_STAT, "total_time", TOTAL_TIME);
        Registry.register(Registry.CUSTOM_STAT, "avg_time", AVG_TIME);

        Stats.CUSTOM.getOrCreateStat(NUM_STRONGHOLDS, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(NUM_REVIEWED_ROOMS, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(NUM_BEST_ROOMS, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(NUM_INACCURACIES, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(NUM_MISTAKES, StatFormatter.DEFAULT);
        Stats.CUSTOM.getOrCreateStat(TOTAL_TIME, StatFormatter.TIME);
        Stats.CUSTOM.getOrCreateStat(AVG_TIME, StatFormatter.TIME);
    }

    public static void updateStrongholdTimeStats(ServerPlayerEntity playerEntity, int timeInTicks) {
        playerEntity.incrementStat(NUM_STRONGHOLDS);
        playerEntity.increaseStat(TOTAL_TIME, timeInTicks);
        int count = playerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(NUM_STRONGHOLDS));
        int time = playerEntity.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(TOTAL_TIME));
        int avg = time / count;
        playerEntity.resetStat(Stats.CUSTOM.getOrCreateStat(AVG_TIME));
        playerEntity.increaseStat(AVG_TIME, avg);
    }
}
