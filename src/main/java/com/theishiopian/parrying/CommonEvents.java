package com.theishiopian.parrying;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class CommonEvents
{
    public static void OnAttackedEvent(LivingAttackEvent event)
    {
        DamageSource source = event.getSource();

        if(event.getEntity() instanceof ServerPlayerEntity)
        {
            PlayerEntity player = (PlayerEntity) event.getEntity();
            ItemStack held = player.getMainHandItem();
            Vector3d playerDir = player.getViewVector(1);

            if(source instanceof EntityDamageSource && !(source instanceof IndirectEntityDamageSource))
            {
                if(player.getMainHandItem().getAttributeModifiers(EquipmentSlotType.MAINHAND).containsKey(Attributes.ATTACK_DAMAGE))
                {
                    Entity attacker = source.getEntity();

                    Vector3d attackerDir = attacker.position().subtract(player.position());

                    Vector3d attackerDirNorm = attackerDir.normalize();

                    double angle = playerDir.dot(attackerDirNorm);

                    if(angle > 0.95 && player.swinging)
                    {
                        //log.info("blocked");
                        //Random random = new Random();
                        //player.level.playSound(null, player.blockPosition(), ModSoundEvents.BLOCK_HIT.get(), SoundCategory.PLAYERS, 1, random.nextFloat() * 2f);
                        //attacker.playSound(ParryingMod.BLOCK_HIT_EVENT, 1, random.nextFloat() * 2f);
                        player.knockback(0.33f, attackerDir.x, attackerDir.z);
                        player.hurtMarked = true;//this makes knockback work
                        float damage = event.getAmount();
                        player.causeFoodExhaustion(0.5f);
                        held.getItem().damageItem(player.getMainHandItem(), (int) damage, null, null);

                        Random random = new Random();
                        double pX = (attacker.getX() + player.getX()) / 2 + (random.nextDouble()-0.5) * 0.2 + (attackerDirNorm.x * 0.2);
                        double pY = ((attacker.getY() + player.getY()) / 2) + 1.45 + (random.nextDouble()-0.5) * 0.2+ (attackerDirNorm.y * 0.2);
                        double pZ = (attacker.getZ() + player.getZ()) / 2 + (random.nextDouble()-0.5) * 0.2+ (attackerDirNorm.z * 0.2);
                        player.level.playSound(null, player.blockPosition(), ModSoundEvents.BLOCK_HIT.get(), SoundCategory.PLAYERS, 1, random.nextFloat() * 2f);
                        ((ServerWorld) player.level).sendParticles(ModParticles.PARRY_PARTICLE.get(), pX, pY, pZ, 1, 0D, 0D, 0D, 0.0D);
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    public static void ArrowParryEvent(ProjectileImpactEvent.Arrow event)
    {
        final AbstractArrowEntity projectile = event.getArrow();

        if(!projectile.level.isClientSide)
        {
            if (!(event.getRayTraceResult() instanceof EntityRayTraceResult))return;
            Entity entity = ((EntityRayTraceResult)event.getRayTraceResult()).getEntity();
            if(event.getEntity() != null && entity instanceof PlayerEntity)
            {
                PlayerEntity player = (PlayerEntity) entity;
                Vector3d playerDir = player.getViewVector(1);
                Vector3d arrowDir = projectile.position().subtract(player.position());
                Vector3d playerDirLevel = new Vector3d(playerDir.x, 0, playerDir.z);
                Vector3d arrowDirLevel = new Vector3d(arrowDir.x, 0, arrowDir.z);
                double angle = playerDirLevel.dot(arrowDirLevel);
                ItemStack held = player.getMainHandItem();
                int level = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DEFLECTING.get(), held);
                if(level > 0 && player.swinging && angle > 0.5)
                {
                    float power = (float)(projectile.getDeltaMovement().normalize().length() / 5f) * level;
                    projectile.setDeltaMovement(playerDir.x * power, playerDir.y * power, playerDir.z * power);
                    projectile.yRot = (float)(MathHelper.atan2(playerDir.x, playerDir.z) * (double)(180F / (float)Math.PI));
                    projectile.xRot = (float)(MathHelper.atan2(playerDir.y, (double)1) * (double)(180F / (float)Math.PI));
                    projectile.yRotO = projectile.yRot;
                    projectile.xRotO = projectile.xRot;
                    projectile.hasImpulse = true;
                    Random random = new Random();
                    player.level.playSound(null, player.blockPosition(), ModSoundEvents.BLOCK_HIT.get(), SoundCategory.PLAYERS, 1, random.nextFloat() * 2f);
                    Vector3d particlePos = projectile.position();
                    ((ServerWorld) player.level).sendParticles(ModParticles.PARRY_PARTICLE.get(), particlePos.x, particlePos.y, particlePos.z, 1, 0D, 0D, 0D, 0.0D);

                    event.setCanceled(true);
                }
            }
        }
    }

}