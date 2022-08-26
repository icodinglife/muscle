package com.macho.muscle.core.utils;

import com.esotericsoftware.kryo.kryo5.Kryo;

public class KryoUtils {
    private static final ThreadLocal<Kryo> localKryo = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            return super.initialValue();
        }
    };

    public static Kryo getKryo() {
        return new Kryo();
    }
}
