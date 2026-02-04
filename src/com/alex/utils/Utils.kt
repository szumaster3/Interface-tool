package com.alex.utils

import com.alex.io.OutputStream
import com.alex.store.Store
import java.math.BigInteger
import java.util.*

object Utils {
    @JvmStatic
    fun cryptRSA(data: ByteArray?, exponent: BigInteger?, modulus: BigInteger?): ByteArray {
        return BigInteger(data).modPow(exponent, modulus).toByteArray()
    }

    fun getArchivePacketData(indexId: Int, archiveId: Int, archive: ByteArray): ByteArray {
        val stream = OutputStream(archive.size + 4)
        stream.writeByte(indexId)
        stream.writeShort(archiveId)
        stream.writeByte(0)
        stream.writeInt(archive.size)
        var offset = 8

        for (var6 in archive.indices) {
            if (offset == 512) {
                stream.writeByte(-1)
                offset = 1
            }

            stream.writeByte(archive[var6].toInt())
            ++offset
        }

        val var61 = ByteArray(stream.offset)
        stream.offset = 0
        stream.getBytes(var61, 0, var61.size)
        return var61
    }

    @JvmStatic
    fun getNameHash(name: String): Int {
        return name.lowercase(Locale.getDefault()).hashCode()
    }

    fun getInterfaceDefinitionsSize(store: Store): Int {
        return store.indexes[3].lastArchiveId + 1
    }

    fun getInterfaceDefinitionsComponentsSize(store: Store, interfaceId: Int): Int {
        return store.indexes[3].getLastFileId(interfaceId) + 1
    }

    fun getAnimationDefinitionsSize(store: Store): Int {
        val lastArchiveId = store.indexes[20].lastArchiveId
        return lastArchiveId * 128 + store.indexes[20].getValidFilesCount(lastArchiveId)
    }

    fun getItemDefinitionsSize(store: Store): Int {
        val lastArchiveId = store.indexes[19].lastArchiveId
        return lastArchiveId * 256 + store.indexes[19].getValidFilesCount(lastArchiveId)
    }

    fun getNPCDefinitionsSize(store: Store): Int {
        val lastArchiveId = store.indexes[18].lastArchiveId
        return lastArchiveId * 128 + store.indexes[18].getValidFilesCount(lastArchiveId)
    }

    fun getObjectDefinitionsSize(store: Store): Int {
        val lastArchiveId = store.indexes[16].lastArchiveId
        return lastArchiveId * 256 + store.indexes[16].getValidFilesCount(lastArchiveId)
    }

    fun getGraphicDefinitionsSize(store: Store): Int {
        val lastArchiveId = store.indexes[21].lastArchiveId
        return lastArchiveId * 256 + store.indexes[21].getValidFilesCount(lastArchiveId)
    }
}
