package com.github.coerx.qarchiver.core.pack;

import java.util.Map;

interface SimplePackUnpack {
    byte[] pack(Map<String, byte[]> nameAndDateMap);

    Map<String, byte[]> unpack(byte[] parcel);
}
