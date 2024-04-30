package com.app.pingpong.global.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    public static boolean isRegexNickname(String target) {
        String regex = "^(?=.*[a-z0-9가-힣])[a-z0-9가-힣]{1,10}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }

    public static boolean isRegexTeamName(String target) {
        String regex = "^[\\wㄱ-ㅎㅏ-ㅣ가-힣~!^@#$%^&*()_\\-+=]{1,10}$";
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(target);
        return matcher.find();
    }
}
