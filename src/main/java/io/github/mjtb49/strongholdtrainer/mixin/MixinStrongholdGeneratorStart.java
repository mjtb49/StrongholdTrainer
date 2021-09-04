package io.github.mjtb49.strongholdtrainer.mixin;

import io.github.mjtb49.strongholdtrainer.api.StrongholdTreeAccessor;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePiece;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;

import java.util.*;

@Mixin(StrongholdGenerator.Start.class)
public class MixinStrongholdGeneratorStart implements StrongholdTreeAccessor {
    private Map<StructurePiece, List<StructurePiece>> tree = new HashMap<>();
    private ArrayList<StructurePiece> pieces = new ArrayList<>();
    Map<StructurePiece, StructurePiece> parents = new HashMap<>();

    public void addPiece(StructurePiece piece) {
        if (pieces.size() != 0) {
            tree.get(pieces.get(pieces.size() - 1)).add(piece);
            parents.put(piece, pieces.get(pieces.size() - 1));
        }
    }

    public void registerPiece(StructurePiece piece) {
        pieces.add(piece);
        tree.put(piece, new ArrayList<>());
    }

    public void correctOrder5Way() {
        if (pieces.size() == 0) {
            return;
        }
        StructurePiece piece = pieces.get(pieces.size() - 1);
        List<StructurePiece> list = tree.get(piece);
        if (list.size() == 5 && piece instanceof StrongholdGenerator.FiveWayCrossing) {
            Direction direction = piece.getFacing();
            if (direction != null) {
                switch (direction) {
                    case NORTH:
                        Collections.swap(list, 1, 2);
                        Collections.swap(list, 3, 4);
                        break;
                    case SOUTH:
                        Collections.swap(list, 1, 3);
                        Collections.swap(list, 2, 4);
                        break;
                    case WEST:
                        Collections.swap(list, 1, 4);
                        Collections.swap(list, 2, 3);
                        break;
                    default:
                }
            }
        } else {
            System.err.println("Attempted to correct other room as if 5 Way");
        }
    }

    public void correctOrderSquareAndCorridor() {
        if (pieces.size() == 0) {
            return;
        }
        StructurePiece piece = pieces.get(pieces.size() - 1);
        List<StructurePiece> list = tree.get(piece);
        if (list.size() == 3 && (piece instanceof StrongholdGenerator.SquareRoom || piece instanceof StrongholdGenerator.Corridor)) {
            Direction direction = piece.getFacing();
            if (direction != null) {
                switch (direction) {
                    case WEST:
                    case SOUTH:
                        Collections.swap(list, 1, 2);
                        break;
                    default:
                }
            }
        } else {
            System.err.println("Attempted to correct other room as if square or corridor");
        }
    }

    @Override
    public ArrayList<StructurePiece> getPieces() {
        return pieces;
    }

    @Override
    public Map<StructurePiece, List<StructurePiece>> getTree() {
        return tree;
    }


    @Override
    public Map<StructurePiece, StructurePiece> getParents() {
        return parents;
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
