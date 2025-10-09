package com.hbm.util;

import com.google.common.collect.Sets;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Handles anything regarding hashes, base64 encoding, etc. Shady looking stuff, that is
 * <p>
 * mlbv: I made the UUIDs to be UUID instead of String in order to make .equals work properly. Hope this doesn't break anything.
 * @author hbm
 */
public class ShadyUtil {

    // This is a list of UUIDs used for various things, primarily for accessories.
    // For a comprehensive list, check RenderAccessoryUtility.java
    public static final UUID HbMinecraft        = UUID.fromString("192af5d7-ed0f-48d8-bd89-9d41af8524f8");
    public static final UUID LPkukin            = UUID.fromString("937c9804-e11f-4ad2-a5b1-42e62ac73077");
    public static final UUID Dafnik             = UUID.fromString("3af1c262-61c0-4b12-a4cb-424cc3a9c8c0");
    public static final UUID a20                = UUID.fromString("4729b498-a81c-42fd-8acd-20d6d9f759e0");
    public static final UUID LordVertice        = UUID.fromString("a41df45e-13d8-4677-9398-090d3882b74f");
    public static final UUID CodeRed_           = UUID.fromString("912ec334-e920-4dd7-8338-4d9b2d42e0a1");
    public static final UUID dxmaster769        = UUID.fromString("62c168b2-d11d-4dbf-9168-c6cea3dcb20e");
    public static final UUID Dr_Nostalgia       = UUID.fromString("e82684a7-30f1-44d2-ab37-41b342be1bbd");
    public static final UUID Samino2            = UUID.fromString("87c3960a-4332-46a0-a929-ef2a488d1cda");
    public static final UUID Hoboy03new         = UUID.fromString("d7f29d9c-5103-4f6f-88e1-2632ff95973f");
    public static final UUID Dragon59MC         = UUID.fromString("dc23a304-0f84-4e2d-b47d-84c8d3bfbcdb");
    public static final UUID Steelcourage       = UUID.fromString("ac49720b-4a9a-4459-a26f-bee92160287a");
    public static final UUID ZippySqrl          = UUID.fromString("03c20435-a229-489a-a1a1-671b803f7017");
    public static final UUID Schrabby           = UUID.fromString("3a4a1944-5154-4e67-b80a-b6561e8630b7");
    public static final UUID SweatySwiggs       = UUID.fromString("5544aa30-b305-4362-b2c1-67349bb499d5");
    public static final UUID Drillgon           = UUID.fromString("41ebd03f-7a12-42f3-b037-0caa4d6f235b");
    public static final UUID Doctor17           = UUID.fromString("e4ab1199-1c22-4f82-a516-c3238bc2d0d1");
    public static final UUID Doctor17PH         = UUID.fromString("4d0477d7-58da-41a9-a945-e93df8601c5a");
    public static final UUID ShimmeringBlaze    = UUID.fromString("061bc566-ec74-4307-9614-ac3a70d2ef38");
    public static final UUID FifeMiner          = UUID.fromString("37e5eb63-b9a2-4735-9007-1c77d703daa3");
    public static final UUID lag_add            = UUID.fromString("259785a0-20e9-4c63-9286-ac2f93ff528f");
    public static final UUID Pu_238             = UUID.fromString("c95fdfd3-bea7-4255-a44b-d21bc3df95e3");
    public static final UUID Tankish            = UUID.fromString("609268ad-5b34-49c2-abba-a9d83229af03");
    public static final UUID FrizzleFrazzle     = UUID.fromString("fc4cc2ee-12e8-4097-b26a-1c6cb1b96531");
    public static final UUID the_NCR            = UUID.fromString("28ae585f-4431-4491-9ce8-3def6126e3c6");
    public static final UUID Barnaby99_x        = UUID.fromString("b04cf173-cff0-4acd-aa19-3d835224b43d");
    public static final UUID Ma118              = UUID.fromString("1121cb7a-8773-491f-8e2b-221290c93d81");
    public static final UUID Adam29Adam29       = UUID.fromString("bbae7bfa-0eba-40ac-a0dd-f3b715e73e61");
    public static final UUID Alcater            = UUID.fromString("0b399a4a-8545-45a1-be3d-ece70d7d48e9");
    public static final UUID ege444             = UUID.fromString("42ee978c-442a-4cd8-95b6-29e469b6df10");
    public static final UUID LePeeperSauvage    = UUID.fromString("433c2bb7-018c-4d51-acfe-27f907432b5e");

    public static final Set<String> hashes = new HashSet<>();
    static {
        hashes.add("41de5c372b0589bbdb80571e87efa95ea9e34b0d74c6005b8eab495b7afd9994");
        hashes.add("31da6223a100ed348ceb3254ceab67c9cc102cb2a04ac24de0df3ef3479b1036");
    }

    public static final int c = 0x3d;
    public static final String initializer = "Ur bp7mN-@UFZKXBx9N[/>M'k\\7\\9m3b";
    public static final String signature   = "dYPq\\YzrNpfn[ZDxdk7PS2jhTY72cZT7SoH|\\WL3dIznfC";
    public static final String mask        = "E#?V,%l!nb4 ik_wJ@(&k4o>Wq";
    public static final String checksum    = "dpXt\\Xnr\\Yzm";
    public static final String testCase    = "dYPq\\YzrNm3FUH;P[ZTq";
    public static final String testValue   = "WGm?";
    public static final String smTest1     = "hgwS";
    public static final String smTest2     = "8Sfw";
    public static final String smTest3     = "j11D";
    public static final String smTest4     = "s783";

    public static final Set<UUID> contributors = Sets.newHashSet(
            UUID.fromString("06ab7c03-55ce-43f8-9d3c-2850e3c652de"), // mustang_rudolf
            UUID.fromString("5bf069bc-5b46-4179-aafe-35c0a07dee8b"), // JMF781
            UUID.fromString("ccd9aa1c-26b9-4dde-8f37-b96f8d99de22")  // kakseao
    );

    // simple cryptographic utils
    public static String encode(String msg) { return Base64.getEncoder().encodeToString(msg.getBytes()); }
    public static String decode(String msg) { return new String(Base64.getDecoder().decode(msg)); }

    public static String offset(String msg, int o) {
        byte[] bytes = msg.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] += o;
        }
        return new String(bytes);
    }

    /** Encryptor for the h-cat answer strings */
    public static String smoosh(String s1, String s2, String s3, String s4) {
        Random rand = new Random();
        String s = "";

        byte[] b1 = s1.getBytes();
        byte[] b2 = s2.getBytes();
        byte[] b3 = s3.getBytes();
        byte[] b4 = s4.getBytes();

        if (b1.length == 0 || b2.length == 0 || b3.length == 0 || b4.length == 0) return "";

        s += s1;
        rand.setSeed(b1[0]);
        s += rand.nextInt(0xffffff);
        s += s2;
        rand.setSeed(rand.nextInt(0xffffff) + b2[0]);
        rand.setSeed(b2[0]);
        s += rand.nextInt(0xffffff);
        s += s3;
        rand.setSeed(rand.nextInt(0xffffff) + b3[0]);
        rand.setSeed(b3[0]);
        s += rand.nextInt(0xffffff);
        s += s4;
        rand.setSeed(rand.nextInt(0xffffff) + b4[0]);
        rand.setSeed(b4[0]);
        s += rand.nextInt(0xffffff);
        return getHash(s);
    }

    /** Simple SHA256 call */
    public static String getHash(String inp) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha256.digest(inp.getBytes());
            String str = "";
            for (int b : bytes) str = str + Integer.toString((b & 0xFF) + 256, 16).substring(1);
            return str;
        } catch (NoSuchAlgorithmException e) { }
        return "";
    }

    // mlbv: I doubt if it would work on 1.12.2
//    public static void test() {
//        if(!GeneralConfig.enableDebugMode) return; //only run in debug mode
//
//        //unit test for smooshing
//        MainRegistry.logger.debug(smoosh(smTest1, smTest2, smTest3, smTest4));
//
//        try {
//            Class test = Class.forName(decode(offset(signature, -2)));
//            Field field = ReflectionHelper.findField(test, decode(offset(checksum, -2)));
//            if(field != null) {
//                System.out.println("TEST SECTION START");
//                //Class toLoad = Class.forName(decode(offset(testCase, -2)));
//                //Field toRead = ReflectionHelper.findField(toLoad, decode(offset(testValue, -2)));
//                //ModEventHandler.reference = toRead;
//                System.out.println("TEST SECTION END");
//            }
//        } catch(Throwable e) { }
//    }
}
