package io.github.mjtb49.strongholdtrainer.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.PositionTracker;
import net.minecraft.text.LiteralText;

import java.io.*;

public class InventoryHelper {

    public static void saveInventoryToFile(File file, PlayerEntity playerEntity) throws IOException {
        try {
            ListTag tags = new ListTag();
            playerEntity.inventory.serialize(tags);
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
            tags.write(dataOutputStream);
            dataOutputStream.flush();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            playerEntity.sendMessage(new LiteralText(e.getMessage()), false);
        }
    }

    public static void loadInventoryFromFile(File file, PlayerEntity entity) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        try {
            entity.inventory.deserialize(ListTag.READER.read(dataInputStream, 256, PositionTracker.DEFAULT));
            dataInputStream.close();
        } catch (IOException e){
            e.printStackTrace();
            entity.sendMessage(new LiteralText(e.getMessage()), false);
        }
    }
}
