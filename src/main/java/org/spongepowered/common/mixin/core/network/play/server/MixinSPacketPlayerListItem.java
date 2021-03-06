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
package org.spongepowered.common.mixin.core.network.play.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.network.play.server.IMixinSPacketPlayerListItem;

import java.util.List;

import javax.annotation.Nullable;

@Mixin(SPacketPlayerListItem.class)
public class MixinSPacketPlayerListItem implements IMixinSPacketPlayerListItem {

    @Shadow @Final public List<SPacketPlayerListItem.AddPlayerData> players;

    @Override
    public void addEntry(GameProfile profile, int latency, GameType gameMode, @Nullable ITextComponent displayName) {
        this.players.add(((SPacketPlayerListItem) (Object) this).new AddPlayerData(profile, latency, gameMode, displayName));
    }

}
