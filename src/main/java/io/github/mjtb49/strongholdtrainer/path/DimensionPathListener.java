package io.github.mjtb49.strongholdtrainer.path;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Random;

public class DimensionPathListener extends AbstractPathListener{

    private final Random random = new Random();

    @Override
    public void update(StrongholdPath.PathEvent event) {
        if(event == StrongholdPath.PathEvent.PATH_COMPLETE){
            PlayerEntity playerEntity = strongholdPath.getPlayerEntity();
            if(random.nextInt(1000) == 1){
                if(random.nextBoolean()){
                    playerEntity.sendMessage(new LiteralText("[Parkour Mode]").formatted(Formatting.BOLD, Formatting.DARK_BLUE).styled(
                            style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in minecraft:the_end run newStronghold"))
                    ), false);
                } else {
                    playerEntity.sendMessage(new LiteralText("[Lava Mode]").formatted(Formatting.BOLD, Formatting.RED).styled(
                            style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/execute in minecraft:the_nether run newStronghold"))
                    ), false);
                }
            }
        }
    }
}
