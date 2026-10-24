package com.rotpaddon.mrpresident.room;


import net.minecraft.block.BarrelBlock;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

/**
 * Turns the empty shell of the base-mod room into a hotel room: bed, sofa, TV, fridge with drinks.
 * Interior of the base structure is x 3..12, y 5..10, z 3..12 relative to the room corner.
 * Only empty (air) spots are used, so nothing a player already built is ever overwritten.
 */
public final class RoomFurnisher {
    private static final int FLOOR = 5;

    private RoomFurnisher() {}

    public static void furnish(ServerWorld world, BlockPos corner) {
        rug(world, corner);
        sofa(world, corner);
        tv(world, corner);
        bed(world, corner);
        fridge(world, corner);
        lights(world, corner);
    }

    private static void rug(ServerWorld w, BlockPos c) {
        for (int x = 5; x <= 9; x++) {
            for (int z = 7; z <= 10; z++) {
                place(w, c, x, FLOOR, z, Blocks.RED_CARPET.defaultBlockState());
            }
        }
        set(w, c, 7, FLOOR, 9, Blocks.SPRUCE_SLAB.defaultBlockState()); // coffee table
    }

    private static void sofa(ServerWorld w, BlockPos c) {
        BlockState seat = Blocks.CRIMSON_STAIRS.defaultBlockState().setValue(StairsBlock.FACING, Direction.SOUTH);
        for (int x = 6; x <= 8; x++) {
            place(w, c, x, FLOOR, 12, seat);
        }
        place(w, c, 5, FLOOR, 12, Blocks.RED_WOOL.defaultBlockState());
        place(w, c, 9, FLOOR, 12, Blocks.RED_WOOL.defaultBlockState());
    }

    private static void tv(ServerWorld w, BlockPos c) {
        for (int x = 6; x <= 9; x++) {
            place(w, c, x, FLOOR, 3, Blocks.SPRUCE_PLANKS.defaultBlockState());
            place(w, c, x, FLOOR + 1, 3, Blocks.SPRUCE_PLANKS.defaultBlockState());
            for (int y = 7; y <= 9; y++) {
                place(w, c, x, y, 3, Blocks.BLACK_CONCRETE.defaultBlockState());
            }
        }
        set(w, c, 7, 8, 3, Blocks.SEA_LANTERN.defaultBlockState());
        set(w, c, 8, 8, 3, Blocks.SEA_LANTERN.defaultBlockState());
    }

    private static void bed(ServerWorld w, BlockPos c) {
        BlockState bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.WEST);
        place(w, c, 4, FLOOR, 6, bed.setValue(BedBlock.PART, BedPart.FOOT));
        place(w, c, 3, FLOOR, 6, bed.setValue(BedBlock.PART, BedPart.HEAD));
    }

    private static void fridge(ServerWorld w, BlockPos c) {
        place(w, c, 12, FLOOR, 11, Blocks.IRON_BLOCK.defaultBlockState());
        BlockPos barrelPos = c.offset(12, FLOOR + 1, 11);
        if (place(w, c, 12, FLOOR + 1, 11, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.WEST))) {
            TileEntity te = w.getBlockEntity(barrelPos);
            if (te instanceof IInventory) {
                IInventory inv = (IInventory) te;
                inv.setItem(0, new ItemStack(Items.HONEY_BOTTLE, 8));
                inv.setItem(1, new ItemStack(Items.MILK_BUCKET));
                inv.setItem(2, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION));
                inv.setItem(3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HEALING));
                inv.setItem(4, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS));
            }
        }
    }

    private static void lights(ServerWorld w, BlockPos c) {
        BlockState lantern = Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
        place(w, c, 5, 10, 5, lantern);
        place(w, c, 10, 10, 10, lantern);
        place(w, c, 5, 10, 10, lantern);
        place(w, c, 10, 10, 5, lantern);
    }

    private static boolean place(ServerWorld w, BlockPos c, int x, int y, int z, BlockState state) {
        BlockPos pos = c.offset(x, y, z);
        return w.getBlockState(pos).isAir() && w.setBlock(pos, state, 2);
    }

    private static void set(ServerWorld w, BlockPos c, int x, int y, int z, BlockState state) {
        w.setBlock(c.offset(x, y, z), state, 2);
    }
}
