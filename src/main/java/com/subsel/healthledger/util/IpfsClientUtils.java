package com.subsel.healthledger.util;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

import java.io.IOException;

public class IpfsClientUtils {

    private static final String ipfsAddress = "/ip4/127.0.0.1/tcp/5001";
    private static final IPFS ipfs = new IPFS(ipfsAddress);

    public static String getContentCid(byte[] content) throws IOException {
        NamedStreamable.ByteArrayWrapper file = new NamedStreamable.ByteArrayWrapper("wellBeingData.txt", content);
        MerkleNode addResult = ipfs.add(file).get(0);
        return addResult.hash.toString();
    }

    public static String getContentFromCid(String cid) throws IOException {
        Multihash filePointer = Multihash.fromBase58(cid);
        return new String(ipfs.cat(filePointer));
    }
}
