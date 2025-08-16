package com.dfoda.otimizadores.ca.codigo;

import java.util.ArrayList;
import com.dex.util.ErroCtx;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.tipo.TypeBearer;

public class LocalsArraySet extends LocalsArray {
    private final OneLocalsArray primary;
    private final ArrayList<LocalsArray> secondaries;

    public LocalsArraySet(int maxLocals) {
        super(maxLocals != 0);
        this.primary = new OneLocalsArray(maxLocals);
        this.secondaries = new ArrayList();
    }

    public LocalsArraySet(OneLocalsArray primary, ArrayList<LocalsArray> secondaries) {
        super(primary.getMaxLocals() > 0);
        this.primary = primary;
        this.secondaries = secondaries;
    }

    private LocalsArraySet(LocalsArraySet toCopy) {
        super(toCopy.getMaxLocals() > 0);
        this.primary = toCopy.primary.copy();
        this.secondaries = new ArrayList(toCopy.secondaries.size());
        int sz = toCopy.secondaries.size();
        for (int i = 0; i < sz; ++i) {
            LocalsArray la = toCopy.secondaries.get(i);
            if (la == null) {
                this.secondaries.add(null);
                continue;
            }
            this.secondaries.add(la.copy());
        }
    }

    @Override
    public void setImmutable() {
        this.primary.setImmutable();
        for (LocalsArray la : this.secondaries) {
            if (la == null) continue;
            la.setImmutable();
        }
        super.setImmutable();
    }

    @Override
    public LocalsArray copy() {
        return new LocalsArraySet(this);
    }

    @Override
    public void annotate(ErroCtx e) {
        e.addContext("(locals array set; primary)");
        this.primary.annotate(e);
        int sz = this.secondaries.size();
        for(int label = 0; label < sz; ++label) {
            LocalsArray la = this.secondaries.get(label);
            if(la == null) continue;
            e.addContext("(locals array set: primary for caller " + Hex.u2(label) + ')');
            la.getPrimary().annotate(e);
        }
    }

    @Override
    public String toHuman() {
        StringBuilder sb = new StringBuilder();
        sb.append("(locals array set; primary)\n");
        sb.append(this.getPrimary().toHuman());
        sb.append('\n');
        int sz = this.secondaries.size();
        for (int label = 0; label < sz; ++label) {
            LocalsArray la = this.secondaries.get(label);
            if (la == null) continue;
            sb.append("(locals array set: primary for caller " + Hex.u2(label) + ")\n");
            sb.append(la.getPrimary().toHuman());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    public void makeInitialized(Type type) {
        int len = this.primary.getMaxLocals();
        if (len == 0) {
            return;
        }
        this.throwIfImmutable();
        this.primary.makeInitialized(type);
        for (LocalsArray la : this.secondaries) {
            if (la == null) continue;
            la.makeInitialized(type);
        }
    }

    @Override
    public int getMaxLocals() {
        return this.primary.getMaxLocals();
    }

    @Override
    public void set(int idx, TypeBearer type) {
        this.throwIfImmutable();
        this.primary.set(idx, type);
        for (LocalsArray la : this.secondaries) {
            if (la == null) continue;
            la.set(idx, type);
        }
    }

    @Override
    public void set(RegisterSpec spec) {
        this.set(spec.getReg(), spec);
    }

    @Override
    public void invalidate(int idx) {
        this.throwIfImmutable();
        this.primary.invalidate(idx);
        for (LocalsArray la : this.secondaries) {
            if (la == null) continue;
            la.invalidate(idx);
        }
    }

    @Override
    public TypeBearer getOrNull(int idx) {
        return this.primary.getOrNull(idx);
    }

    @Override
    public TypeBearer get(int idx) {
        return this.primary.get(idx);
    }

    @Override
    public TypeBearer getCategory1(int idx) {
        return this.primary.getCategory1(idx);
    }

    @Override
    public TypeBearer getCategory2(int idx) {
        return this.primary.getCategory2(idx);
    }

    private LocalsArraySet mergeWithSet(LocalsArraySet other) {
        boolean secondariesChanged = false;
        OneLocalsArray newPrimary = this.primary.merge(other.getPrimary());
        int sz1 = this.secondaries.size();
        int sz2 = other.secondaries.size();
        int sz = Math.max(sz1, sz2);
        ArrayList<LocalsArray> newSecondaries = new ArrayList<LocalsArray>(sz);
        for (int i = 0; i < sz; ++i) {
            LocalsArray la1 = i < sz1 ? this.secondaries.get(i) : null;
            LocalsArray la2 = i < sz2 ? other.secondaries.get(i) : null;
            LocalsArray resultla = null;
            if (la1 == la2) {
                resultla = la1;
            } else if (la1 == null) {
                resultla = la2;
            } else if (la2 == null) {
                resultla = la1;
            } else {
                try {
                    resultla = la1.merge(la2);
                }
                catch (SimException ex) {
                    ex.addContext("Merging locals set for caller block " + Hex.u2(i));
                }
            }
            secondariesChanged = secondariesChanged || la1 != resultla;
            newSecondaries.add(resultla);
        }
        if(this.primary == newPrimary && !secondariesChanged) return this;
        return new LocalsArraySet(newPrimary, newSecondaries);
    }

    private LocalsArraySet mergeWithOne(OneLocalsArray other) {
        boolean secondariesChanged = false;
        OneLocalsArray newPrimary = this.primary.merge(other.getPrimary());
        ArrayList<LocalsArray> newSecondaries = new ArrayList<LocalsArray>(this.secondaries.size());
        int sz = this.secondaries.size();
        for(int i = 0; i < sz; ++i) {
            LocalsArray la = this.secondaries.get(i);
            LocalsArray res = null;
            if(la != null) {
                try {
                    res = la.merge(other);
                } catch(SimException e) {
                    e.addContext("Merging one locals against caller block " + Hex.u2(i));
                }
            }
            secondariesChanged = secondariesChanged || la != res;
            newSecondaries.add(res);
        }
        if(this.primary == newPrimary && !secondariesChanged) return this;
        return new LocalsArraySet(newPrimary, newSecondaries);
    }

    @Override
    public LocalsArraySet merge(LocalsArray outro) {
        LocalsArraySet res;
        try {
            res = outro instanceof LocalsArraySet ? this.mergeWithSet((LocalsArraySet)outro) : this.mergeWithOne((OneLocalsArray)outro);
        } catch(SimException e) {
            e.addContext("underlay locals:");
            this.annotate(e);
            e.addContext("overlay locals:");
            outro.annotate(e);
            throw e;
        }
        res.setImmutable();
        return res;
    }

    private LocalsArray getSecondaryForLabel(int label) {
        if(label >= this.secondaries.size()) return null;
        return this.secondaries.get(label);
    }

    @Override
    public LocalsArraySet mergeWithSubroutineCaller(LocalsArray other, int predLabel) {
        LocalsArray mine = this.getSecondaryForLabel(predLabel);
        OneLocalsArray newPrimary = this.primary.merge(other.getPrimary());
        LocalsArray newSecondary = mine == other ? mine : (mine == null ? other : mine.merge(other));
        if (newSecondary == mine && newPrimary == this.primary) {
            return this;
        }
        newPrimary = null;
        int szSecondaries = this.secondaries.size();
        int sz = Math.max(predLabel + 1, szSecondaries);
        ArrayList<LocalsArray> newSecondaries = new ArrayList<LocalsArray>(sz);
        for (int i = 0; i < sz; ++i) {
            LocalsArray la = null;
            if (i == predLabel) {
                la = newSecondary;
            } else if (i < szSecondaries) {
                la = this.secondaries.get(i);
            }
            if(la != null) newPrimary = newPrimary == null ? la.getPrimary() : newPrimary.merge(la.getPrimary());
            newSecondaries.add(la);
        }
        LocalsArraySet res = new LocalsArraySet(newPrimary, newSecondaries);
        res.setImmutable();
        return res;
    }

    public LocalsArray subArrayForLabel(int subLabel) {
        LocalsArray result = this.getSecondaryForLabel(subLabel);
        return result;
    }

    @Override
    protected OneLocalsArray getPrimary() {
        return this.primary;
    }
}

