package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.MixinStrongholdGeneratorStartAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;

import java.util.*;

@Mixin(StrongholdGenerator.Start.class)
public class MixinStrongholdGeneratorStart implements MixinStrongholdGeneratorStartAccessor {
    private Map<StructurePiece, List<StructurePiece>> tree = new HashMap<>();
    private ArrayList<StructurePiece> pieces = new ArrayList<>();

    public void addPiece(StructurePiece piece) {
        tree.get(pieces.get(pieces.size() - 1)).add(piece);
    }

    public void registerPiece(StructurePiece piece) {
        pieces.add(piece);
        tree.put(piece, new ArrayList<StructurePiece>());
    }

    @Override
    public ArrayList<StructurePiece> getPieces() {
        return pieces;
    }

    @Override
    public Map<StructurePiece, List<StructurePiece>> getTree() {
        return tree;
    }

    /// @author XeroOl
    public void printContents() {
        List<StructurePiece> pieces = new ArrayList<>(this.pieces);
        Collections.reverse(pieces);
        Map<StructurePiece, Integer> index = new HashMap<>();
        for (int i = 0; i < pieces.size(); i++) {
            StructurePiece p = pieces.get(i);
            index.put(p, i);
            String result = p.getClass().getSimpleName();
            List<StructurePiece> ch = tree.get(p);
            for (StructurePiece piece : ch) {
                if (piece == null) {
                    result += " -1";
                } else {
                    result += " " + index.get(piece);
                }
            }
            System.out.println(result);
        }
    }
}
