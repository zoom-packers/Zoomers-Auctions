package com.epherical.auctionworld.data;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class AuctionFilterManager {

    private Node<Item> tree = new Node<>(true);


    public AuctionFilterManager() {
    }

    public void updateFilter(Registry<Item> items) {
        tree = new Node<>(true);
        items.getTagNames().forEach(itemTagKey -> {
            ResourceLocation location = itemTagKey.location();
            String namespace = location.getNamespace();
            if (!namespace.equals("auctionworld")) {
                return;
            }
            String path = location.getPath();
            String[] split = path.split("/");
            if (split.length == 0) {
                tree.addChild(path, itemTagKey, false);
                // if there is no split, we will just filter based on the tag
            } else {
                for (int i = 0; i < split.length; i++) {
                    tree = tree.addChild(split[i], itemTagKey, i < (split.length - 1));
                }
                tree = tree.getRoot();
            }
        });
        tree = tree.getRoot();
    }

    public Node<Item> getTree() {
        return tree;
    }

    public static class Node<T> {
        private final boolean root;

        @Nullable
        private Node<T> parent;
        private Map<String, Node<T>> children;
        private boolean expansion;

        @Nullable
        private TagKey<T> value;


        public Node(boolean root) {
            this.root = root;
            children = new HashMap<>();
        }

        public Node() {
            this(false);
        }


        public Node<T> addChild(String key, TagKey<T> tag, boolean expansion) {
            // The root can not contain a tagKey
            Node<T> childNode = getChild(key);
            // Assumption from root, we are adding a child with this key "blocks" for the first time.
            // now let's check if it's an expansion, if it's not, just set the childNode's value
            if (childNode == null || childNode == this) {
                childNode = new Node<>();
                childNode.parent = this;
                childNode.expansion = expansion;
                if (expansion) {
                    children.put(key, childNode);
                } else {
                    childNode.value = tag;
                    children.put(key, childNode);
                }
            }
            return childNode;
        }

        /**
         * @return The last child available
         */
        public Node<T> getChild(String key) {
            Node<T> node = children.get(key);
            if (node != null) {
                return node.getChild(key);
            }
            return this;
        }

        public Node<T> getRoot() {
            if (this.parent != null) {
                return this.parent.getRoot();
            } else {
                return this;
            }
        }


        public String toString(String indent) {
            // this barely works
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<String, Node<T>> entry : children.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                if (!entry.getValue().expansion) {
                    builder.append(entry.getKey()).append("\n");
                } else {
                    builder.append(entry.getKey()).append("\n").append(indent).append(entry.getValue().toString(indent.repeat(2)));
                }
            }
            return builder.toString();
        }

        public void beginRenderText(GuiGraphics graphics, Font font, Integer x, Integer y, int level) {
            //graphics.drawString(font, "Categories", x, y, 0xFFFFFF);
            renderText(graphics, font, x, y, level);
        }
        public int renderText(GuiGraphics graphics, Font font, Integer x, Integer y, int level) {
            for (Map.Entry<String, Node<T>> entry : children.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
                if (!entry.getValue().expansion) {
                    graphics.drawString(font, entry.getKey(), x + ((level - 1) * 8), y, 0xFFFFFF, false);
                    y += 9;
                } else {
                    graphics.drawString(font, entry.getKey(), x, y, 0xFFFFFF, false);
                    y += 9;
                    y = entry.getValue().renderText(graphics, font, x, y, level + 1);
                }
            }
            return y;
        }
    }
}
