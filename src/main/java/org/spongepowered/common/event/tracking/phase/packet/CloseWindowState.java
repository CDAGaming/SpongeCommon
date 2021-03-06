/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.event.tracking.phase.packet;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.network.Packet;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.entity.EntityUtil;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;

import java.util.List;
import java.util.stream.Collectors;

final class CloseWindowState extends BasicPacketState {

    @Override
    public void populateContext(EntityPlayerMP playerMP, Packet<?> packet, BasicPacketContext context) {
        context.openContainer(playerMP.openContainer);
    }

    @Override
    public void unwind(BasicPacketContext context) {
        final EntityPlayerMP player = context.getSource(EntityPlayerMP.class).get();
        final Container container = context.getOpenContainer();
        ItemStackSnapshot lastCursor = context.getCursor();
        ItemStackSnapshot newCursor = ItemStackUtil.snapshotOf(player.inventory.getItemStack());
        if (lastCursor != null) {
            Sponge.getCauseStackManager().pushCause(player);
            InteractInventoryEvent.Close event =
                    SpongeCommonEventFactory.callInteractInventoryCloseEvent(container, player, lastCursor, newCursor, true);
            if (event.isCancelled()) {
                Sponge.getCauseStackManager().popCause();
                return;
            }
            Sponge.getCauseStackManager().popCause();
        }

        try (CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            Sponge.getCauseStackManager().pushCause(player);
            Sponge.getCauseStackManager().addContext(EventContextKeys.SPAWN_TYPE, SpawnTypes.PLACEMENT);// Non-merged
            // items
            context.getCapturedItemsSupplier().acceptAndClearIfNotEmpty(items -> {
                final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Custom drop =
                        SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }
            });
            // Pre-merged items
            context.getCapturedItemStackSupplier().acceptAndClearIfNotEmpty(stacks -> {
                final List<EntityItem> items = stacks.stream()
                    .map(drop -> drop.create(player.getServerWorld()))
                    .collect(Collectors.toList());
                final List<Entity> entities = items
                    .stream()
                    .map(EntityUtil::fromNative)
                    .collect(Collectors.toList());
                if (!entities.isEmpty()) {
                    DropItemEvent.Custom drop =
                        SpongeEventFactory.createDropItemEventCustom(Sponge.getCauseStackManager().getCurrentCause(), entities);
                    SpongeImpl.postEvent(drop);
                    if (!drop.isCancelled()) {
                        for (Entity droppedItem : drop.getEntities()) {
                            droppedItem.setCreator(player.getUniqueID());
                            ((IMixinWorldServer) player.getServerWorld()).forceSpawnEntity(droppedItem);
                        }
                    }
                }
            });
        }
        context.getCapturedBlockSupplier()
            .acceptAndClearIfNotEmpty(blocks -> TrackingUtil.processBlockCaptures(blocks, this, context));

    }

    @Override
    public boolean doesCaptureEntityDrops() {
        return true;
    }
}
