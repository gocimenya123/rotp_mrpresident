package com.rotpaddon.mrpresident.event;

import java.util.UUID;

import com.github.standobyte.jojo.capability.world.MrPresidentWorldDataProvider;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData;
import com.github.standobyte.jojo.mrpresident.dimension.MrPresidentWorldData.ChunkSectionPos;
import com.github.standobyte.jojo.entity.stand.StandEntity;
import com.github.standobyte.jojo.power.impl.stand.IStandPower;
import com.github.standobyte.jojo.util.mc.damage.ModdedDamageSourceWrapper;
import com.rotpaddon.mrpresident.AddonMain;
import com.rotpaddon.mrpresident.effect.TurtleFormEffect;
import com.rotpaddon.mrpresident.init.InitStatusEffects;
import com.rotpaddon.mrpresident.room.RoomKeys;
import com.rotpaddon.mrpresident.room.RoomService;

import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = AddonMain.MOD_ID)
public class MrPresidentEvents {
    // the vanilla "base" weapon modifier ids: with these the tooltip shows the total (9 damage, 1.8 speed)
    private static final UUID BASE_ATTACK_DAMAGE_ID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");
    private static final UUID BASE_ATTACK_SPEED_ID = UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3");
    public static final double KEY_DAMAGE = 9.0;
    public static final double KEY_ATTACK_SPEED = 1.8;

    /** The Turtle Key pulled out with the ability is a weapon: 9 attack damage, 1.8 attack speed. */
    @SubscribeEvent
    public static void onKeyAttributes(ItemAttributeModifierEvent event) {
        if (event.getSlotType() != EquipmentSlotType.MAINHAND || !RoomKeys.isPlayerKey(event.getItemStack())) return;
        // the player's own base values are 1 damage and 4 attack speed
        event.addModifier(Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, "Weapon modifier", KEY_DAMAGE - 1.0, AttributeModifier.Operation.ADDITION));
        event.addModifier(Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, "Weapon modifier", KEY_ATTACK_SPEED - 4.0, AttributeModifier.Operation.ADDITION));
    }

    /** The owner holding his own key in the main hand can hit Stands directly. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyHitsStand(LivingAttackEvent event) {
        LivingEntity target = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (target.level.isClientSide() || !(target instanceof StandEntity)
                || ((StandEntity) target).canTakeDamageFrom(source)
                || !(source.getDirectEntity() instanceof LivingEntity)) {
            return;
        }
        LivingEntity attacker = (LivingEntity) source.getDirectEntity();
        if (!RoomKeys.fits(attacker.getMainHandItem(), attacker.getUUID())) return;
        event.setCanceled(true);
        target.hurt(new ModdedDamageSourceWrapper(source).setCanHurtStands(), event.getAmount());
    }

    /** Turtle Form: the shell soaks up most of the damage. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().hasEffect(InitStatusEffects.TURTLE_FORM.get())) {
            event.setAmount(event.getAmount() * (1.0F - TurtleFormEffect.DAMAGE_REDUCTION));
        }
    }

    /** Turtle Form: a turtle sized hitbox. */
    @SubscribeEvent
    public static void onEntitySize(EntityEvent.Size event) {
        // the event also fires from the Entity constructor, when the effect map doesn't exist yet
        if (event.getEntity() instanceof PlayerEntity && event.getEntity().isAddedToWorld()
                && ((PlayerEntity) event.getEntity()).hasEffect(InitStatusEffects.TURTLE_FORM.get())) {
            event.setNewSize(EntitySize.scalable(1.0F, 0.5F));
            event.setNewEyeHeight(0.35F);
        }
    }

    /**
     * The base mod's exit gem sends people to the room owner's entity, which breaks when the owner is inside the
     * room dimension himself. For rooms of this addon's users the exit goes through the owner's saved exit point.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onExitGemUse(PlayerInteractEvent.RightClickBlock event) {
        PlayerEntity player = event.getPlayer();
        if (player.level.isClientSide() || player.isShiftKeyDown() || event.getHand() != Hand.MAIN_HAND
                || !RoomService.isRoomDimension(player)
                || event.getWorld().getBlockState(event.getPos()).getBlock() != ModBlocks.MR_PRESIDENT_EXIT.get()) {
            return;
        }
        ServerWorld roomWorld = (ServerWorld) player.level;
        MrPresidentWorldData rooms = roomWorld.getCapability(MrPresidentWorldDataProvider.CAPABILITY).orElse(null);
        if (rooms == null) return;
        UUID ownerId = rooms.getTurtleId(new ChunkSectionPos(event.getPos()));
        if (ownerId == null || !RoomService.isAddonRoom(roomWorld, ownerId)) return; // turtle rooms: base logic

        RoomService.teleportOut(player, ownerId, roomWorld.getServer());
        event.setCanceled(true);
        event.setCancellationResult(ActionResultType.CONSUME);
    }

    /** Per player: remember the exit position, and drop falling entities into the room in turtle form. */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.side != LogicalSide.SERVER
                || !(event.player instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) event.player;
        boolean hasStand = IStandPower.getStandPowerOptional(player).map(RoomService::hasMrPresident).orElse(false);
        if (!hasStand || RoomService.isRoomDimension(player)) return;

        if (player.tickCount % 5 == 0) {
            RoomService.trackOuter(player);
        }
        if (player.hasEffect(InitStatusEffects.TURTLE_FORM.get())) {
            RoomService.captureFalling(player);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            RoomService.tickKidnapTimers(ServerLifecycleHooks.getCurrentServer());
        }
    }

    /** An owner leaving the server throws his guests out; the owner himself stays where he is. */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
            boolean hasStand = IStandPower.getStandPowerOptional(player).map(RoomService::hasMrPresident).orElse(false);
            if (hasStand) {
                RoomService.expelAll(player.server, player.getUUID(), false);
            }
        }
    }
}
