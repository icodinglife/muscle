package com.macho.muscle.core.utils;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KryoUtil {
    private static final ThreadLocal<Kryo> localKryo = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();

        kryo.setRegistrationRequired(false);

        kryo.register(Object[].class);

        return kryo;
    });

    public static Kryo getKryo() {
        return localKryo.get();
    }

    public static byte[] serialize(Object obj) {
        Kryo kryo = getKryo();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, obj);
        output.flush();
        byte[] data = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
            // todo
        }
        return data;
    }

    public static <T> T deserialize(byte[] data) {
        Kryo kryo = getKryo();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        Input input = new Input(bais, data.length);
        Object obj = kryo.readClassAndObject(input);
        return (T) obj;
    }

    public static void main(String[] args) {
        String a = "abcd";
        byte[] d = serialize(a);
        System.out.println(d);
    }
}
