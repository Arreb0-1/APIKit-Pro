/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package burp.application.apitypes.rest;

import org.junit.jupiter.api.Test;

class WadlParserTest {
    WadlParserTest() {
    }

    @Test
    public void test() {
        int i2 = 0;
        while (i2 < 10) {
            final int finalI = i2++;
            new Thread(new Runnable() {

                @Override
                public void run() {
                    System.out.println(finalI);
                }
            }).start();
        }
        System.out.println("Hello WADL");
    }
}

