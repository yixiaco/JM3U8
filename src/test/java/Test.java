import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.hexm.util.StringUtil;

import java.util.Arrays;

/**
 * @author hexm
 * @date 2020/6/11 15:32
 */
public class Test {

    public static void main(String[] args) throws Exception {
        String str = "MjVlM2Q4ZTUtMDUxOS00ZjM5LTg4NDAtYmVjYmJmMzc0YWEzV2NxTDAwdktnT25xKzhsSHg0RWwvMFFvUjBBLzVZVlZBQUFBQUFBQUFBRHJMc2FUeSswbXkrcUowSTVUeXRwOG0xMHJ0SkJxWlFpaWVPSk5QUHc5WkxNOTM0MSszeXBN";
        System.out.println(str);
        System.out.println(Base64.decodeStr(str));
        System.out.println(Base64.decodeStr("126c23c2-f8fb-4454-8984-10774fcfe845SWcnwwD1gYi0bk"));
        System.out.println(StringUtil.isBase64(str));
        System.out.println();

        String response = "74ba61bdaae24214e81f783e83e1437ac7bd83469019a93819da598b333e0b33834267a8c1bf80bb55e99c5c1e72e528dcd2aeb27e2631054dda1d8a545c6bfeo3ff80455579a41a962eaca57ccc5a993o30fd4e913b80ad9583bd7923d4da48ca";
        String[] rb = response.split("o");
        byte[] key = HexUtil.decodeHex(rb[1]);
        byte[] iv = HexUtil.decodeHex(rb[2]);
        //设置key
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, iv);
        String url = aes.decryptStr(Base64.encode(HexUtil.decodeHex(rb[0])));
        String[] s = url.split("-");
        byte[] kb = new byte[s.length];
        for (int i = 0; i < s.length; i++) {
            kb[i] = (byte) Integer.parseInt(s[i]);
        }
//        String url = AESpkcs7paddingUtil.decrypt(Base64.encode(HexUtil.decodeHex(rb[0])), key, iv);
        System.out.println(Arrays.toString(kb));
    }
}
