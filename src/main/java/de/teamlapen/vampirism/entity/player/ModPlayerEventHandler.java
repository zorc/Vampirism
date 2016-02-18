package de.teamlapen.vampirism.entity.player;

import de.teamlapen.vampirism.entity.player.hunter.HunterPlayer;
import de.teamlapen.vampirism.entity.player.vampire.SkillHandler;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Event handler for player related events
 */
public class ModPlayerEventHandler {


    @SubscribeEvent
    public void onEntityConstructing(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            /*
            Register ExtendedProperties.
            Could be done via factions, but that might be a little bit overkill for 2-5 factions and might cause trouble with addon mods.
             */
            if (VampirePlayer.get((EntityPlayer) event.entity) == null) {
                VampirePlayer.register((EntityPlayer) event.entity);
            }
            if (HunterPlayer.get((EntityPlayer) event.entity) == null) {
                HunterPlayer.register((EntityPlayer) event.entity);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingUpdateLast(LivingEvent.LivingUpdateEvent event) {
        if (event.entity instanceof EntityPlayer) {
            VampirePlayer.get((EntityPlayer) event.entity).onUpdateBloodStats();
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (VampirePlayer.get(event.entityPlayer).getSkillHandler().isSkillActive(SkillHandler.batSkill)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlockPlaced(BlockEvent.PlaceEvent event) {
        try {
            if (VampirePlayer.get(event.player).getSkillHandler().isSkillActive(SkillHandler.batSkill)) {
                event.setCanceled(true);
            }
        } catch (Exception e) {
            // Added try catch to prevent any exception in case some other mod uses auto placers or so
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (VampirePlayer.get(event.entityPlayer).getSkillHandler().isSkillActive(SkillHandler.batSkill)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemUse(PlayerUseItemEvent.Start event) {
        if (VampirePlayer.get(event.entityPlayer).getSkillHandler().isSkillActive(SkillHandler.batSkill)) {
            event.setCanceled(true);
        }
    }


}