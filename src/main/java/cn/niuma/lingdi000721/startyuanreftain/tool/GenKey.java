package cn.niuma.lingdi000721.startyuanreftain.tool;

import java.security.SecureRandom;
import java.util.Base64;

public class GenKey {
    public static void main(String[] args) {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String base64Key = Base64.getEncoder().encodeToString(bytes);
        System.out.println(base64Key);
    }
}
