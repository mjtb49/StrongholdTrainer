package io.github.mjtb49.strongholdtrainer.util;

import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class StrongholdSearcher {
    public static StructurePiece search(Map<StructurePiece, List<StructurePiece>> tree, StructurePiece start) {
        List<StructurePiece> pieces = tree.getOrDefault(start, new ArrayList<>());

        if (pieces.isEmpty()) {
            // Can't find any pieces
            return null;
        }


        for (StructurePiece piece : pieces) {
            if (piece instanceof StrongholdGenerator.PortalRoom) {
                // Found portal room, return
                return piece;
            } else {
                // Recurse
                StructurePiece next = search(tree, piece);
                if (next != null) {
                    // Found portal room in the tree
                    return piece;
                }
            }
        }

        // Didn't find any pieces
        return null;
    }
}
