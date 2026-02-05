package com.villavredestein.tools;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGen {
    public static void main(String[] args) {
        var enc = new BCryptPasswordEncoder();
        System.out.println("admin=" + enc.encode("Vredestein2006!"));
        System.out.println("cleaner=" + enc.encode("cleaner123"));
    }
}