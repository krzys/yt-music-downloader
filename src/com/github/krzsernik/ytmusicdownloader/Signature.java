package com.github.krzsernik.ytmusicdownloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signature {
    private static String baseUrl;
    private static List<Integer> signatureSteps = new ArrayList<>();

    public static void setBaseUrl(String baseUrl) {
        Signature.baseUrl = baseUrl;

        try {
            Request req = new Request(baseUrl, "GET");
            req.send();

            String javascript = req.getContent();
            Signature.parseJavaScript(javascript);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> signatureSetterPatterns = List.of(
            ".\\.s&&.\\.set\\(.\\.sp,encodeURIComponent\\(([a-zA-Z0-9]+)\\(decodeURIComponent\\(.\\.s\\)\\)\\)\\)",
            ".&&.\\.set\\(.,encodeURIComponent\\(([a-zA-Z0-9]+)\\(decodeURIComponent\\(.\\)\\)\\)\\)");
    private static void parseJavaScript(String code) {
        boolean found = false;
        String encodingFunction = null;

        outer: for(String pattern : signatureSetterPatterns) {
            Pattern patt = Pattern.compile(pattern);
            Matcher mat = patt.matcher(code);

            while(mat.find()) {
                encodingFunction = mat.group(1);
                found = true;

                break outer;
            }
        }

        if(!found) {
            System.err.println("Pattern havent found");
            System.err.println(baseUrl);

            return;
        }

        String function = code.split(encodingFunction + "=function\\(a\\)\\{")[1];
        function = function.split(";return a\\.join\\(\"\"\\)\\};")[0];

        List<String> signatureSteps = new ArrayList<String>(List.of(function.split(";")));
        signatureSteps.remove(signatureSteps.get(0)); // remove `a.split("")`

        String functionsObjectName = signatureSteps.get(0).split("\\.")[0];
        String functionsObject = code.split(functionsObjectName + "=\\{")[1];
        functionsObject = functionsObject.split("\\}\\};")[0];

        Map<String, Integer> funcMap = new HashMap<>();
        for(String encoder : functionsObject.split("\\},\\s*")) {
            String name = encoder.split(":")[0];
            System.out.println(name);

            int methodIndex = -1;
            if(encoder.contains("a.splice(0,b)")) {
                methodIndex = 0;
            } else if(encoder.contains("var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c")) {
                methodIndex = 1;
            } else if(encoder.contains("a.reverse()")) {
                methodIndex = 2;
            }

            funcMap.put(name, methodIndex);
        }

        for(String step : signatureSteps) {
            String name = step.split("[\\.\\(]")[1];
            int arg = Integer.parseInt(step.split("[,\\)]")[1]);

            System.out.println(name);
            System.out.println(funcMap.get(name));

            Signature.signatureSteps.add((funcMap.get(name) << 7) + arg);
        }
    }

    // function(a,b){a.splice(0,b)}
    public static String Splice(String s, int b) {
        return s.substring(b);
    }
    // function(a,b){var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c}
    public static String Replace(String s, int b) {
        char[] a = s.toCharArray();
        char c = a[0];
        a[0] = a[b % a.length];
        a[b % a.length] = c;

        return String.copyValueOf(a);
    }
    // function(a){a.reverse()}
    public static String Reverse(String s, @SuppressWarnings("unused") int b) {
        StringBuilder a = new StringBuilder(s);
        return a.reverse().toString();
    }

    public static String createSignature(String s) {
        for(int step : signatureSteps) {
            switch(step << 7) {
                case 0:
                    s = Splice(s, step & 127);
                    break;
                case 1:
                    s = Replace(s, step & 127);
                    break;
                case 2:
                    s = Reverse(s, step & 127);
                    break;
            }
        }

        return s;
    }
}
