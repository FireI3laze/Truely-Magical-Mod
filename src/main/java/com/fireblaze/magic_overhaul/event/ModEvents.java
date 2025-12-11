package com.fireblaze.magic_overhaul.event;

import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "magic_overhaul")
public class ModEvents {
    /*
    // Static map für aktive Partikelstrahlen
    public static final Map<ParticleBeam, Integer> activeBeams = new HashMap<>();

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Level level = event.getLevel();
        Player player = event.getEntity();

        BlockPos pos = event.getPos();
        ItemStack stack = player.getMainHandItem();

        if (stack.getItem() instanceof WandItem) {
            // Nur ausführen, wenn Spieler gerade die Animation "anfängt"
            if (!player.swinging) return;
            player.swinging = false; // verhindern, dass es beim nächsten Tick erneut feuert
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof ArcaneEnchantingTableBlockEntity tableBE) {
                ItemStackHandler inv = (ItemStackHandler) tableBE.getItemHandler();
                ItemStack target = inv.getStackInSlot(0); // Item, das verzaubert werden soll
                ItemStack essence = inv.getStackInSlot(1); // Charged Magic Essence

                if (target.isEmpty() || essence.isEmpty()) {
                    player.displayClientMessage(Component.literal("No item or Magic Essence in table!"), true);
                    event.setCanceled(true);
                    return;
                }

                Map<Enchantment, Integer> selected = new HashMap<>(tableBE.getSelected());
                if (selected.isEmpty()) {
                    player.displayClientMessage(Component.literal("No enchantments selected!"), true);
                    event.setCanceled(true);
                    return;
                }

                boolean anyChange = selected.entrySet().stream().anyMatch(e -> {
                    int oldLevel = target.getEnchantmentLevel(e.getKey());
                    int newLevel = e.getValue();
                    return newLevel > oldLevel;
                });

                if (!anyChange) {
                    player.displayClientMessage(Component.literal("All selected enchantments are already applied!"), true);
                    event.setCanceled(true);
                    return;
                }

                if (essence.getCount() < selected.size()) {
                    player.displayClientMessage(Component.literal("Not enough Magic Essence!"), true);
                    event.setCanceled(true);
                    return;
                }

                if (level instanceof ServerLevel serverLevel) {
                    // --- Prüfen, ob aktuell schon eine Animation läuft ---
                    if (!activeBeams.isEmpty()) {
                        player.displayClientMessage(Component.literal("Enchantment in progress!"), true);
                        event.setCanceled(true);
                        return;
                    }

                    // --- Partikelanimation von allen gelinkten Monolithen starten ---
                    tableBE.setEnchantingInProgress(true);

                    startParticleBeamAnimation(serverLevel, tableBE, () -> {
                        int usedEssence = 0;
                        for (var entry : selected.entrySet()) {
                            Enchantment ench = entry.getKey();
                            int newLevel = entry.getValue();
                            int oldLevel = target.getEnchantmentLevel(ench);

                            // WICHTIG: Bad Omen kann Curse anwenden, muss aber nicht die selected Map leeren
                            if (newLevel > oldLevel) {
                                usedEssence += applyEnchantmentWithBadOmenChance(serverLevel, player, target, ench, newLevel, tableBE.getBlockPos());
                            }
                        }

                        // Essenz reduzieren
                        essence.shrink(usedEssence);
                        tableBE.setChanged();

                        // Partikel-Explosion
                        spawnEnchantExplosion(serverLevel, tableBE.getBlockPos());

                        // Item droppen
                        ItemStack enchantedItem = target.copy();
                        tableBE.setEnchantingInProgress(false);

                        inv.setStackInSlot(0, ItemStack.EMPTY);
                        tableBE.markForRenderUpdate();

                        double dropX = tableBE.getBlockPos().getX() + 0.5;
                        double dropY = tableBE.getBlockPos().getY() + 1.0;
                        double dropZ = tableBE.getBlockPos().getZ() + 0.5;

                        ItemEntity entity = new ItemEntity(serverLevel, dropX, dropY, dropZ, enchantedItem);
                        entity.setDeltaMovement(0, 0, 0);
                        entity.setPickUpDelay(40);
                        entity.setNoGravity(true);
                        serverLevel.addFreshEntity(entity);
                    });
                }
                event.setCanceled(true); // verhindert Schaden / Mining
            }
        }
    }

    private record ParticleBeam(ServerLevel level, BlockPos start, Vec3 end, Runnable onComplete) {
        public static boolean completed = false; // Ob dieser Strahl fertig ist
    }

    // Aufruf: Starte Partikelstrahl-Animation
    private static void startParticleBeamAnimation(ServerLevel level, ArcaneEnchantingTableBlockEntity tableBE, Runnable onComplete) {
        BlockPos tablePos = tableBE.getBlockPos();
        Map<BlockPos, Enchantment> usedMonoliths = new HashMap<>();
        Map<Enchantment, Integer> selected = tableBE.getSelected();

        // Bestimme die Monolithen für die ausgewählten Enchantments
        for (Enchantment ench : selected.keySet()) {
            BlockPos monolith = tableBE.getLinkedMonolithManager().getMonolithForEnchantment(ench);
            if (monolith != null) {
                usedMonoliths.put(monolith, ench);
            }
        }

        int delayTicks = 0;

        int i = 0;
        int size = usedMonoliths.size();

        for (BlockPos monolithPos : usedMonoliths.keySet()) {

            // ---- SOUND: einmal pro Monolith beim Beam-Start ----
            level.playSound(
                    null,
                    monolithPos,
                    ModSounds.BEAM_LOOP.get(),
                    SoundSource.BLOCKS,
                    5.0f,
                    0.9f
            );

            // Zielpunkt am Tisch
            Vec3 particleTargetPos = new Vec3(
                    tablePos.getX() + 0.5,
                    tablePos.getY() + 1.625,
                    tablePos.getZ() + 0.5
            );

            // Nur der letzte Beam bekommt onComplete, alle anderen null
            boolean isLast = (i == size - 1);
            Runnable complete = isLast ? onComplete : null;

            // ParticleBeam anlegen
            ParticleBeam beam = new ParticleBeam(level, monolithPos, particleTargetPos, complete);

            // Startverzögerung, falls du wieder welche willst
            activeBeams.put(beam, -i * delayTicks);
            i++;
        }
    }

    private static void spawnEnchantExplosion(ServerLevel level, BlockPos pos) {
        level.playSound(
                null,
                pos,
                ModSounds.MAGIC_EXPLOSION.get(),
                SoundSource.BLOCKS,
                5.0f,
                1.0f
        );

        int particleCount = 300;  // Anzahl der Partikel
        double spread = 1;      // Streuung der Partikel
        ParticleOptions[] particleTypes = new ParticleOptions[]{
                ParticleTypes.ENCHANT,
                ParticleTypes.END_ROD,
                ParticleTypes.WITCH
        };

        for (int i = 0; i < particleCount; i++) {
            // Zufällige Position um den Tisch herum
            double offsetX = (level.random.nextDouble() - 0.5) * spread;
            double offsetY = (level.random.nextDouble() - 0.5) * spread;
            double offsetZ = (level.random.nextDouble() - 0.5) * spread;

            // Zufällige Geschwindigkeit
            double velocityX = (level.random.nextDouble() - 0.5) * 0.5;
            double velocityY = level.random.nextDouble() * 0.5;
            double velocityZ = (level.random.nextDouble() - 0.5) * 0.5;

            // Zufälliger Partikeltyp
            ParticleOptions type = particleTypes[level.random.nextInt(particleTypes.length)];

            level.sendParticles(
                    type,
                    pos.getX() + 0.5 + offsetX,
                    pos.getY() + 1.0 + offsetY,
                    pos.getZ() + 0.5 + offsetZ,
                    1,          // count pro Aufruf
                    velocityX,  // dx
                    velocityY,  // dy
                    velocityZ,  // dz
                    0.5         // extra Geschwindigkeit/Größe
            );
        }
    }

    // Globaler Server-Tick-Event
    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END) return;

        var iterator = activeBeams.entrySet().iterator();
        boolean anyRunning = false; // prüfen, ob noch ein Strahl läuft

        while (iterator.hasNext()) {
            var entry = iterator.next();
            ParticleBeam beam = entry.getKey();
            int tick = entry.getValue();
            int durationTicks = 75; // Beam-Laufzeit
            int delayAfterBeam = 10;    // 1 Sekunde Verzögerung

            if (tick < 0) { // Noch in Startverzögerung
                activeBeams.put(beam, tick + 1);
                anyRunning = true;
                continue;
            }

            if (tick < durationTicks) { // Beam läuft
                anyRunning = true;

                // Partikellogik
                double startX = beam.start.getX() + 0.5;
                double startY = beam.start.getY() + 1.0;
                double startZ = beam.start.getZ() + 0.5;
                double endX = beam.end.x;
                double endY = beam.end.y;
                double endZ = beam.end.z;

                // Map von Partikeltyp -> Anzahl pro Tick
                Map<ParticleOptions, Integer> particleMap = new HashMap<>();
                particleMap.put(ParticleTypes.END_ROD, 100);
                particleMap.put(ParticleTypes.ENCHANT, 50);
                particleMap.put(ParticleTypes.ELECTRIC_SPARK, 50);

                for (var entry2 : particleMap.entrySet()) {
                    ParticleOptions type = entry2.getKey();
                    int count = entry2.getValue();

                    for (int i = 0; i < count; i++) {
                        double factor = i / (double) count;
                        double x = startX + (endX - startX) * factor;
                        double y = startY + (endY - startY) * factor;
                        double z = startZ + (endZ - startZ) * factor;

                        double offsetX = (beam.level.random.nextDouble() - 0.5) * 0.02;
                        double offsetY = (beam.level.random.nextDouble() - 0.5) * 0.02;
                        double offsetZ = (beam.level.random.nextDouble() - 0.5) * 0.02;

                        beam.level.sendParticles(type, x + offsetX, y + offsetY, z + offsetZ, 1, 0, 0, 0, 0);
                    }
                }


                activeBeams.put(beam, tick + 1);
                continue;
            }

            if (tick < durationTicks + delayAfterBeam) {
                // Verzögerung läuft
                activeBeams.put(beam, tick + 1);
                anyRunning = true;
                continue;
            }

            // Verzögerung vorbei, onComplete ausführen
            if (beam.onComplete != null) {
                beam.onComplete.run();
            }
            iterator.remove();
        }

        // --- Enchant anwenden nur, wenn kein Strahl mehr aktiv ---
        if (!anyRunning && !activeBeams.isEmpty()) {
            // Es gibt nur eine onComplete-Runnable pro Animation, die wir einmal ausführen
            ParticleBeam anyBeam = activeBeams.keySet().iterator().next();
            if (anyBeam.onComplete != null) {
                anyBeam.onComplete.run();
            }
            activeBeams.clear(); // alle Strahlen entfernt, Item ist enchanted
        }
    }

    // Neue Methode in ModEvents
    private static int applyEnchantmentWithBadOmenChance(ServerLevel level, Player player, ItemStack target, Enchantment ench, int newLevel, BlockPos tablePos) {
        Map<Enchantment, Integer> current = EnchantmentHelper.getEnchantments(target);

        // Prüfe, ob ench einen bestehenden Enchant ersetzt
        boolean replacing = current.containsKey(ench);

        // Entferne inkompatible Enchants (außer ench selbst)
        current.entrySet().removeIf(e -> e.getKey() != ench && !ench.isCompatibleWith(e.getKey()));

        boolean applyCurse = false;
        var badOmen = player.getEffect(MobEffects.BAD_OMEN);
        if (badOmen != null) {
            int amplifier = badOmen.getAmplifier() + 1;
            int chance = amplifier * 5;
            if (level.random.nextInt(100) < chance) {
                applyCurse = true;
            }
        }

        if (applyCurse) {
            Enchantment randomCurse = level.random.nextBoolean()
                    ? Enchantments.BINDING_CURSE
                    : Enchantments.VANISHING_CURSE;

            current.put(randomCurse, 1);

            // Curse-Sound abspielen
            level.playSound(null, tablePos, ModSounds.BAD_OMEN.get(), SoundSource.BLOCKS, 5.0f, 1.0f);

            if (!level.isClientSide) {
                player.displayClientMessage(Component.literal("Bad Omen! A curse has been applied!"), true);
            }

            // Curse-Beam-Partikel
            spawnCurseBeamParticles(level, tablePos);
        } else {
            current.put(ench, newLevel);

            // Partikel, wenn ein bestehender Enchant ersetzt wird
            if (replacing) {
                spawnReplacingBeamParticles(level, tablePos);
            }
        }

        EnchantmentHelper.setEnchantments(current, target);
        return 1;
    }

    // Neue Methode: Curse-Particle-Strahl
    private static void spawnCurseBeamParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                100, 0.02, 0.02, 0.02, 0.01);
        level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                100, 0.01, 0.01, 0.01, 0.01);
    }

    private static void spawnReplacingBeamParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                80, 0.02, 0.02, 0.02, 0.02);
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                50, 0.01, 0.01, 0.01, 0.01);
    }
    */
}
