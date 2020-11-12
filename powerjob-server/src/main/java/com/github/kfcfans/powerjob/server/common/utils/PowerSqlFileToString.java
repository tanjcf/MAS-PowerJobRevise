package com.github.kfcfans.powerjob.server.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.TreeSet;
/**
 * 重构toString 数组输出格式。
 *
 * @author jctan
 * @since 2020/9/29
 */
@Slf4j
public class PowerSqlFileToString extends AbstractCollection {
    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public int size() {
        return 0;
    }

    public String toString(Iterator<String> it) {
        if (! it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();

        for (;;) {
            String e = it.next();
            sb.append(e);
            if (! it.hasNext())
                return sb.toString();
            sb.append(',');
        }

    }
    public String toString(Object it) {
        if (it.toString().length()<1)
            return "";
      String sb;
        sb=it.toString().substring(1,it.toString().length()-1);
        sb=sb.replaceAll(" ","");
        return sb;

    }
}
