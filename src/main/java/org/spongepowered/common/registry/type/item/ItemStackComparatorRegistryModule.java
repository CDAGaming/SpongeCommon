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
package org.spongepowered.common.registry.type.item;

import com.google.common.collect.Lists;
import org.spongepowered.api.data.Property;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.DamageableData;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackComparators;
import org.spongepowered.api.registry.RegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public final class ItemStackComparatorRegistryModule implements RegistryModule {

    @RegisterCatalog(ItemStackComparators.class)
    private final Map<String, Comparator<ItemStack>> comparators = new HashMap<>();

    @Override
    public void registerDefaults() {

        Comparator<ItemStack> TYPE = Comparator.comparing(i -> i.getType().getId());
        this.comparators.put("TYPE", TYPE);
        Comparator<ItemStack> SIZE = Comparator.comparing(ItemStack::getQuantity);
        this.comparators.put("SIZE", SIZE);
        Comparator<ItemStack> TYPE_SIZE = TYPE.thenComparing(SIZE);
        this.comparators.put("TYPE_SIZE", TYPE_SIZE);
        this.comparators.put("DEFAULT", TYPE_SIZE);
        Properties PROPERTIES = new Properties();
        this.comparators.put("PROPERTIES", PROPERTIES);
        ItemDataComparator ITEM_DATA = new ItemDataComparator();
        this.comparators.put("ITEM_DATA", ITEM_DATA);
        this.comparators.put("ITEM_DATA_IGNORE_DAMAGE", new ItemDataComparator(DamageableData.class));
        this.comparators.put("IGNORE_SIZE", TYPE.thenComparing(PROPERTIES).thenComparing(ITEM_DATA));
        this.comparators.put("ALL", TYPE.thenComparing(SIZE).thenComparing(PROPERTIES).thenComparing(ITEM_DATA));
    }

    static final class Properties implements Comparator<ItemStack> {

        @Override
        public int compare(@Nullable final ItemStack o1, @Nullable final ItemStack o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            List<Property<?, ?>> properties = Lists.newArrayList(o2.getApplicableProperties());
            for (Property<?, ?> property : o1.getApplicableProperties()) {
                if (properties.contains(property)) {
                    properties.remove(property);
                } else {
                    return -1;
                }
            }
            return properties.size();
        }
    }
    static final class ItemDataComparator implements Comparator<ItemStack> {

        private final Class<? extends DataManipulator<?, ?>>[] ignored;

        public ItemDataComparator(Class<? extends DataManipulator<?, ?>>... ignored) {
            this.ignored = ignored;
        }

        @Override
        public int compare(@Nullable final ItemStack o1, @Nullable final ItemStack o2) {
            if (o1 == null && o2 == null) {
                return 0;
            }
            if (o1 == null) {
                return 1;
            }
            if (o2 == null) {
                return -1;
            }
            List<DataManipulator<?, ?>> manipulators = Lists.newArrayList(o2.getContainers());
            for (final DataManipulator<?, ?> manipulator : o1.getContainers()) {
                if (manipulators.contains(manipulator)) {
                    manipulators.remove(manipulator);
                } else if (!isIgnored(manipulators, manipulator)) {
                    return -1;
                }
            }
            return manipulators.size();
        }

        private boolean isIgnored(List<DataManipulator<?, ?>> list, DataManipulator<?, ?> toCheck) {
            for (Class<? extends DataManipulator<?, ?>> ignore : this.ignored) {
                if (ignore.isAssignableFrom(toCheck.getClass())) {
                    list.removeIf(manip -> ignore.isAssignableFrom(manip.getClass()));
                    return true;
                }
            }
            return false;
        }
    }
}
