package com.github.coerx.qarchiver.core.pack;

import com.github.coerx.qarchiver.core.common.utils.CommonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 单个文件大小和最后打包的文件大小都不要超过2GB
 * <pre>
 * file header format:
 * filename     200 bytes
 * file size    4 bytes
 * </pre>
 */
class SimpleTar implements SimplePackUnpack {
    private static final int FILE_NAME_FIELD_OFFSET = 0;
    private static final int FILE_NAME_FIELD_SIZE = 200;
    private static final int FILE_LENGTH_FIELD_OFFSET = 200;
    private static final int FILE_LENGTH_FIELD_SIZE = 8;
    private static final int HEADER_SIZE = 208;

    /**
     * @param nameAndDateMap map的key为filename，包括路径名和文件名，如果是目录，name结尾为/
     * @return
     */
    @Override
    public byte[] pack(Map<String, byte[]> nameAndDateMap) {
        byte[] allData = nameAndDateMap.entrySet().stream().map(entry -> {
            String fileName = entry.getKey();
            boolean isDir = fileName.endsWith("\\");
            //建议用UTF-8编码,其它编码类型的字符串中间可能会有值为0的字节
            byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
            if (fileNameBytes.length > FILE_NAME_FIELD_SIZE) {
                throw new RuntimeException("文件名过长或文件路径过深");
            }

            byte[] fileData = null;
            if (isDir) {
                fileData = new byte[HEADER_SIZE];
            } else {
                fileData = new byte[HEADER_SIZE + entry.getValue().length];
            }

            System.arraycopy(fileNameBytes, 0, fileData, 0, fileName.getBytes().length);
            byte[] lengthToBytes = CommonUtil.longToBytes(entry.getValue().length);
            System.arraycopy(lengthToBytes, 0, fileData, FILE_NAME_FIELD_SIZE, lengthToBytes.length);

            if (isDir) {
                return fileData;
            }
            System.arraycopy(entry.getValue(), 0, fileData, HEADER_SIZE, entry.getValue().length);
            return fileData;
        }).reduce(new byte[0], (accu, next) -> {
            if (accu.length + next.length < 0) throw new RuntimeException("打包文件或者单个太大了");
            byte[] tmp = new byte[accu.length + next.length];
            System.arraycopy(accu, 0, tmp, 0, accu.length);
            System.arraycopy(next, 0, tmp, accu.length, next.length);
            return tmp;
        });
        return allData;
    }

    @Override
    public Map<String, byte[]> unpack(byte[] pack) {
        Map<String, byte[]> fileMap = new HashMap<>();
        int offset = 0;
        while (offset < pack.length) {
            if (offset + HEADER_SIZE > pack.length) {
                throw new RuntimeException("文件数据不全");
            }
            byte[] header = Arrays.copyOfRange(pack, offset, offset + HEADER_SIZE);
            offset += HEADER_SIZE;
            byte[] fileNameBytes = Arrays.copyOfRange(header, 0, FILE_NAME_FIELD_SIZE);
            int end;
            for (end = 0; end < fileNameBytes.length && fileNameBytes[end] != 0; end++) ;
            String fileName = new String(fileNameBytes, 0, end, StandardCharsets.UTF_8);

            long dataSize = CommonUtil.bytesToLong(Arrays.copyOfRange(header, FILE_LENGTH_FIELD_OFFSET, FILE_LENGTH_FIELD_OFFSET + FILE_LENGTH_FIELD_SIZE));
            if (offset + dataSize > pack.length) {
                throw new RuntimeException("文件数据不全");
            }
            byte[] fileData = Arrays.copyOfRange(pack, offset, offset
                    + (int) dataSize);
            offset += dataSize;
            fileMap.put(fileName, fileData);
        }
        return fileMap;
    }
}
