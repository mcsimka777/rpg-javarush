package com.game.controller;

import java.util.*;

public interface Restrictions {
    Set<String> REQUIRED_FIELDS = new HashSet<String>()
    {{
        add("name");
        add("title");
        add("race");
        add("profession");
        add("experience");
        add("birthday");
    }};
    int NAME_MAX_LENGTH = 12;
    int TITLE_MAX_LENGTH = 30;
    int EXP_MIN_VALUE = 0;
    int EXP_MAX_VALUE = 10000000;
    Long MIN_DATE = new GregorianCalendar(2000,Calendar.JANUARY,1).getTimeInMillis();
    Long MAX_DATE = new GregorianCalendar(3001,Calendar.JANUARY,1).getTimeInMillis();
}
