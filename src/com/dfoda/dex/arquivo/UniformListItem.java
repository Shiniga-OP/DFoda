package com.dfoda.dex.arquivo;

import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;
import java.util.List;

public final class UniformListItem<T extends OffsettedItem> extends OffsettedItem {
    public static final int HEADER_SIZE = 4;
    public final ItemType itemType;
    public final List<T> items;

    public UniformListItem(ItemType itemType, List<T> items) {
        super(UniformListItem.getAlignment(items), UniformListItem.writeSize(items));
        if(itemType == null) throw new NullPointerException("itemType == null");
        this.items = items;
        this.itemType = itemType;
    }

    public static int getAlignment(List<? extends OffsettedItem> items) {
        try {
            return Math.max(4, items.get(0).getAlignment());
        } catch(IndexOutOfBoundsException ex) {
            throw new IllegalArgumentException("items.size() == 0");
        } catch(NullPointerException ex) {
            throw new NullPointerException("items == null");
        }
    }

    private static int writeSize(List<? extends OffsettedItem> items) {
        OffsettedItem first = items.get(0);
        return items.size() * first.writeSize() + UniformListItem.getAlignment(items);
    }

    @Override
    public ItemType itemType() {
        return this.itemType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(this.getClass().getName());
        sb.append(this.items);
        return sb.toString();
    }

    @Override
    public void addContents(DexFile file) {
        for (OffsettedItem i : this.items) {
            i.addContents(file);
        }
    }

    @Override
    public final String toHuman() {
        StringBuilder sb = new StringBuilder(100);
        boolean first = true;
        sb.append("{");
        for (OffsettedItem i : this.items) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(i.toHuman());
        }
        sb.append("}");
        return sb.toString();
    }

    public final List<T> getItems() {
        return this.items;
    }

    @Override
    protected void place0(Section addedTo, int offset) {
        offset += this.headerSize();
        boolean first = true;
        int theSize = -1;
        int theAlignment = -1;
        for (OffsettedItem i : this.items) {
            int size = i.writeSize();
            if (first) {
                theSize = size;
                theAlignment = i.getAlignment();
                first = false;
            } else {
                if (size != theSize) {
                    throw new UnsupportedOperationException("item size mismatch");
                }
                if (i.getAlignment() != theAlignment) {
                    throw new UnsupportedOperationException("item alignment mismatch");
                }
            }
            offset = i.place(addedTo, offset) + size;
        }
    }

    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        int size = this.items.size();
        if (out.annotates()) {
            out.annotate(0, this.offsetString() + " " + this.typeName());
            out.annotate(4, "  size: " + Hex.u4(size));
        }
        out.writeInt(size);
        for (OffsettedItem i : this.items) {
            i.writeTo(file, out);
        }
    }

    private int headerSize() {
        return this.getAlignment();
    }
}

