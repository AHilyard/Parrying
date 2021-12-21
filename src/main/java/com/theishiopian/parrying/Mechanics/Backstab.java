package com.theishiopian.parrying.Mechanics;

import com.theishiopian.parrying.Config.Config;
import com.theishiopian.parrying.Entity.DaggerEntity;
import com.theishiopian.parrying.Registration.ModEnchantments;
import com.theishiopian.parrying.Registration.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public abstract class Backstab
{
    public static void DoBackstab(LivingHurtEvent event, LivingEntity entity)
    {
        if(Config.backStabEnabled.get())
        {
            Entity e = event.getSource().getEntity();

           if(e instanceof LivingEntity)
           {
               LivingEntity attacker = (LivingEntity) e;

               if(entity.getMaxHealth() <= Config.backStabMaxHealth.get())
               {
                   Vec3 attackerDir = attacker.getViewVector(1);
                   Vec3 defenderDir = entity.getViewVector(1);

                   double angle = (new Vec3(attackerDir.x, 0, attackerDir.z)).dot(new Vec3(defenderDir.x, 0, defenderDir.z));

                   if(angle > Config.backStabAngle.get())
                   {
                        int tLevel = 0;
                        int vLevel = 0;

                       if(event.getSource() instanceof IndirectEntityDamageSource)
                       {
                            if((event.getSource()).getDirectEntity() instanceof DaggerEntity)
                            {
                                DaggerEntity d = (DaggerEntity) event.getSource().getDirectEntity();

                                ItemStack dagger = d.daggerItem;

                                tLevel = Config.treacheryEnabled.get() ? EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.TREACHERY.get(), dagger) : 0;
                                vLevel = Config.venomousEnabled.get() ? EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.VENOMOUS.get(), dagger) : 0;
                            }
                       }
                       else
                       {
                           tLevel = Config.treacheryEnabled.get() ? EnchantmentHelper.getEnchantmentLevel(ModEnchantments.TREACHERY.get(), attacker) : 0;
                           vLevel = Config.venomousEnabled.get() ? EnchantmentHelper.getEnchantmentLevel(ModEnchantments.VENOMOUS.get(), attacker) : 0;
                       }

                       event.setAmount((float) (event.getAmount() * (Config.backStabDamageMultiplier.get() + tLevel)));

                       if(vLevel > 0)
                       {
                            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, vLevel - 1));
                       }

                       if(attacker instanceof Player && tLevel > 0)
                       {
                           ((Player)attacker).magicCrit(entity);
                       }

                       Vec3 pos = entity.position();

                       ((ServerLevel) attacker.level).sendParticles(ModParticles.STAB_PARTICLE.get(), pos.x, pos.y+1.5f, pos.z, 1, 0D, 0D, 0D, 0.0D);
                       attacker.level.playSound(null, attacker.blockPosition(), SoundEvents.PLAYER_BIG_FALL, SoundSource.PLAYERS, 2, 1);
                   }
               }
           }
        }
    }
}